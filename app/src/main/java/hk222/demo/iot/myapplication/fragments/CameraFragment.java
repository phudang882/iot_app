package hk222.demo.iot.myapplication.fragments;

/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.appcompat.widget.AppCompatImageButton;
import com.google.common.util.concurrent.ListenableFuture;
import hk222.demo.iot.myapplication.*;
import hk222.demo.iot.myapplication.databinding.CameraBinding;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tensorflow.lite.support.label.Category;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hk222.demo.iot.myapplication.databinding.FragmentCameraBinding;
import org.tensorflow.lite.task.vision.classifier.Classifications;

/** Fragment for displaying and controlling the device camera and other UI */
public class CameraFragment extends Fragment
        implements ImageClassifierHelper.ClassifierListener {
    private static final String TAG = "Image Classifier";
    private double time;
    private FragmentCameraBinding fragmentCameraBinding;
    private ImageClassifierHelper imageClassifierHelper;
    private Bitmap bitmapBuffer;
    private ImageAnalysis imageAnalyzer;
    private ProcessCameraProvider cameraProvider;
    private TextView tvLabel;

    private int lensFacing;
    private final Object task = new Object();

    /**
     * Blocking camera operations are performed using this executor
     */
    private ExecutorService cameraExecutor;

    @SuppressLint("CutPasteId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentCameraBinding = FragmentCameraBinding
                .inflate(inflater, container, false);
        tvLabel = fragmentCameraBinding.labelLayout.tvLabel;
        AppCompatImageButton button = fragmentCameraBinding.labelLayout.switchButtion;
        button.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                               lensFacing = CameraSelector.LENS_FACING_BACK;
                                           } else {
                                               lensFacing = CameraSelector.LENS_FACING_FRONT;
                                           }
                                           fragmentCameraBinding.viewFinder.post(CameraFragment.this::setUpCamera);

                                       }
                                   }
        );
//        timerRunnable = new Runnable() {
//            @Override
//            public void run() {
//                CameraFragment.this.sendDataMQTT(BaseActivity.mqttHelper,MQTTHelper.username +"/feeds/AI",tvLabel.getText().toString());
//                timerHandler.postDelayed(this,1000);
//            }
//        };
        return fragmentCameraBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!PermissionsFragment.hasPermission(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                    .navigate(
                            CameraFragmentDirections.actionCameraToPermissions()
                    );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Shut down our background executor
        cameraExecutor.shutdown();
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        imageClassifierHelper = ImageClassifierHelper.create(requireContext()
                , this);

        // setup result adapter

        // Set up the camera and its use cases
        fragmentCameraBinding.viewFinder.post(this::setUpCamera);

        // Attach listeners to UI control widgets
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imageAnalyzer.setTargetRotation(
                fragmentCameraBinding.viewFinder.getDisplay().getRotation()
        );
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                // Build and bind the camera use cases
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // Declare and bind preview, capture and analysis use cases
    private void bindCameraUseCases() {
        // CameraSelector - makes assumption that we're only using the back
        // camera
        CameraSelector.Builder cameraSelectorBuilder = new CameraSelector.Builder();
        CameraSelector cameraSelector = cameraSelectorBuilder
                .requireLensFacing(lensFacing).build();

        // Preview. Only using the 4:3 ratio because this is the closest to
        // our model
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(
                        fragmentCameraBinding.viewFinder
                                .getDisplay().getRotation()
                )
                .build();

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        // The analyzer can then be assigned to the instance
        imageAnalyzer.setAnalyzer(cameraExecutor, image -> {
            if (bitmapBuffer == null) {
                bitmapBuffer = Bitmap.createBitmap(
                        image.getWidth(),
                        image.getHeight(),
                        Bitmap.Config.ARGB_8888);
            }
            classifyImage(image);
        });

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll();

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(
                    fragmentCameraBinding.viewFinder.getSurfaceProvider()
            );
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }
    }

    private void classifyImage(@NonNull ImageProxy image) {
        // Copy out RGB bits to the shared bitmap buffer
        bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());

        int imageRotation = image.getImageInfo().getRotationDegrees();
        image.close();
        synchronized (task) {
            // Pass Bitmap and rotation to the image classifier helper for
            // processing and classification
            imageClassifierHelper.classify(bitmapBuffer, imageRotation);
        }
    }

    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResults(List<Classifications> results, long inferenceTime) {
        List<Category> categories = results.get(0).getCategories();
        if (categories.size() == 0){
            return;
        }
        List<Category> sortedCategories = new ArrayList<>(categories);
        sortedCategories.sort(Comparator.comparingInt(Category::getIndex));
        Category category = sortedCategories.get(0);
        if (tvLabel == null){
            Log.e(TAG,"label null");
            return;
        }
        tvLabel.setText(category.getLabel());

    }
    public void sendDataMQTT(MQTTHelper mqttHelper, String topic, String value) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(StandardCharsets.UTF_8);
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
            String TAG = "Home";
            Log.d(TAG,"No publish");
        }
    }
}

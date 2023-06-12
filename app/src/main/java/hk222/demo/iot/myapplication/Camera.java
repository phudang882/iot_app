package hk222.demo.iot.myapplication;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.Nullable;
import hk222.demo.iot.myapplication.databinding.CameraBinding;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

/** Entrypoint for app */
public class Camera extends BaseActivity {

    private Runnable timerRunnable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraBinding cameraBinding = CameraBinding.inflate(getLayoutInflater());
        mqttHelper = new MQTTHelper(this);


    }

    @Override
    protected void onDestroy() {
        try {
            mqttHelper.mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            Log.d("except", "onDestroy: ");
        }
        super.onDestroy();
    }

    @Override
    int getContentViewId() {
        return R.layout.camera;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.action_camera;
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }
    public void startMQTT() {
    }
}
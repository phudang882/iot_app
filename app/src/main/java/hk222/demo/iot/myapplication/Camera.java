package hk222.demo.iot.myapplication;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import hk222.demo.iot.myapplication.databinding.CameraBinding;

/** Entrypoint for app */
public class Camera extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraBinding cameraBinding = CameraBinding.inflate(getLayoutInflater());
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

}
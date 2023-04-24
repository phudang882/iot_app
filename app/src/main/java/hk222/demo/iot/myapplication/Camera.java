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


    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraBinding cameraBinding = CameraBinding.inflate(getLayoutInflater());
        startMQTT();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.tvLabel);
                Log.d("test", "runAI " + (textView == null));
                if (textView != null){
                    sendDataMQTT(MQTTHelper.username + "/feeds/AI",textView.getText().toString());
                }
                timerHandler.postDelayed(this,5000);
            }
        };
        timerHandler.postDelayed(timerRunnable,0);
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
    public void sendDataMQTT(String topic, String value) {
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
    public void startMQTT() {
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception  {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
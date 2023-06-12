package hk222.demo.iot.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Home extends BaseActivity {

    MQTTHelper mqttHelper;
    TextView txtSensor_1;
    TextView txtSensor_2;
    TextView txtSensor_3;
    ProgressBar tempProgressBar;
    ProgressBar humiProgressBar;

    ProgressBar gasProgressBar;
    LabeledSwitch toggle_switch_fan;
    LabeledSwitch toggle_switch_led;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Home", "onCreateView: ");

        super.onCreate(savedInstanceState);
        tempProgressBar = findViewById(R.id.tempProgressBar_1);
        humiProgressBar = findViewById(R.id.humidProgressBar_1);
        gasProgressBar = findViewById(R.id.gasProgressBar_1);
        txtSensor_1 = findViewById(R.id.tempText_1);
        txtSensor_2 = findViewById(R.id.humidText_1);
        txtSensor_3 = findViewById(R.id.gasText_1);
        toggle_switch_fan = findViewById(R.id.toggle_switch_fan);
        toggle_switch_led = findViewById(R.id.toggle_switch_led);
        toggle_switch_fan.setOnToggledListener((toggleableView, isOn) -> {
            if (isOn) {
                sendDataMQTT(BaseActivity.username +"/feeds/"+ BaseActivity.defaultTopic.get(4), "ON");
            } else {
                sendDataMQTT(BaseActivity.username +"/feeds/" + BaseActivity.defaultTopic.get(4), "OFF");
            }
        });

        toggle_switch_led.setOnToggledListener((toggleableView, isOn) -> {
            if (isOn) {
                sendDataMQTT(BaseActivity.username +"/feeds/"+BaseActivity.defaultTopic.get(3), "ON");
            } else {
                sendDataMQTT(BaseActivity.username +"/feeds/"+BaseActivity.defaultTopic.get(3), "OFF");
            }
        });

        startMQTT();
    }

    @Override
    int getContentViewId() {
        return R.layout.home_menu;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.action_home;
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
        Log.d("start",BaseActivity.defaultTopic.get(3));
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception  {
                Log.d("Received: ",topic + "***" + message.toString());
                int round = 0;
                String messageS =  message.toString();
                try {
                    round = Math.round(Float.parseFloat(messageS));
                } catch (Exception e){Log.d("N","String");}

                if (Objects.equals(topic, BaseActivity.defaultTopic.get(0))) {
                    txtSensor_1.setText(messageS);
                    tempProgressBar.setProgress(round, true);
                }else if (topic.contentEquals(BaseActivity.defaultTopic.get(1))) {
                    txtSensor_2.setText(messageS);
                    humiProgressBar.setProgress(round, true);
                } else if (topic.contentEquals(BaseActivity.defaultTopic.get(2))){
                    txtSensor_3.setText(messageS);
                    gasProgressBar.setProgress(round,true);
                } else if (topic.contentEquals(BaseActivity.defaultTopic.get(3))) {
                    toggle_switch_led.setOn(messageS.contentEquals("ON"));
                } else if (topic.contentEquals(BaseActivity.defaultTopic.get(4))) {
                    toggle_switch_fan.setOn(messageS.contentEquals("ON"));
                } else {
                    throw new Exception("No topic name feed");
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}

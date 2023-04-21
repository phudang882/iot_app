package hk222.demo.iot.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.nio.charset.StandardCharsets;

public class Home extends BaseActivity {

    MQTTHelper mqttHelper;
    TextView txtSensor_1;
    TextView txtSensor_2;
    ProgressBar tempProgressBar;
    ProgressBar humiProgressBar;
    LabeledSwitch toggle_switch_fan;
    LabeledSwitch toggle_switch_led;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("Home", "onCreateView: ");

        super.onCreate(savedInstanceState);
        tempProgressBar = findViewById(R.id.tempProgressBar_1);
        humiProgressBar = findViewById(R.id.humidProgressBar_1);
        txtSensor_1 = findViewById(R.id.tempText_1);
        txtSensor_2 = findViewById(R.id.humidText_1);
        toggle_switch_fan = findViewById(R.id.toggle_switch_fan);
        toggle_switch_led = findViewById(R.id.toggle_switch_led);
        toggle_switch_fan.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    sendDataMQTT("xMysT/feeds/button2", "ON");
                } else {
                    sendDataMQTT("xMysT/feeds/button2", "OFF");
                }
            }
        });

        toggle_switch_led.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn) {
                    sendDataMQTT("xMysT/feeds/button1", "ON");
                } else {
                    sendDataMQTT("xMysT/feeds/button1", "OFF");
                }
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
                if (topic.contains("sensor")) {
                    txtSensor_1.setText(message.toString());
//                    Integer temp = message.toString();
                    tempProgressBar.setProgress(Math.round(Integer.parseInt(message.toString())), true);
                } else if (topic.contains("button1")) {
                    toggle_switch_led.setOn(message.toString().equals("ON"));
                } else if (topic.contains("button2")) {
                    toggle_switch_fan.setOn(message.toString().equals("ON"));
                } else if (topic.contains("receive")) {
                    txtSensor_2.setText(message.toString());
                    humiProgressBar.setProgress(Math.round(Float.parseFloat(message.toString())), true);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}

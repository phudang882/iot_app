package hk222.demo.iot.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtSensor_1;
    TextView txtSensor_2;
    LabeledSwitch toggle_switch;
    LabeledSwitch toggle_switch_led;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSensor_1 = findViewById(R.id.txtSensor_1);
        txtSensor_2 = findViewById(R.id.txtSensor_2);
        toggle_switch = findViewById(R.id.toggle_switch);
        toggle_switch_led = findViewById(R.id.toggle_switch_led);

        toggle_switch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn == true) {
                    sendDataMQTT("xMysT/feeds/button2", "ON");
                } else {
                    sendDataMQTT("xMysT/feeds/button2", "OFF");
                }
            }
        });

        toggle_switch_led.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if (isOn == true) {
                    sendDataMQTT("xMysT/feeds/button1", "ON");
                } else {
                    sendDataMQTT("xMysT/feeds/button1", "OFF");
                }
            }
        });

        startMQTT();
    }

    public void sendDataMQTT(String topic, String value) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){

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
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("Received: ",topic + "***" + message.toString());
                if (topic.contains("sensor")) {
                    if (Double.parseDouble(message.toString()) > 20) {
                        txtSensor_1.setText("Danger");
                    } else {
                        txtSensor_1.setText(message.toString());
                    }
                } else if (topic.contains("button2")) {
                    if (message.toString().equals("ON")) {
                        toggle_switch.setOn(true);
                    } else {
                        toggle_switch.setOn(false);
                    }
                } else if (topic.contains("button1")) {
                    if (message.toString().equals("ON")) {
                        toggle_switch_led.setOn(true);
                    } else {
                        toggle_switch_led.setOn(false);
                    }
                } else if (topic.contains("receive")) {
                    txtSensor_2.setText(message.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
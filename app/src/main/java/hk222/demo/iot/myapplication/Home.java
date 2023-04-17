package hk222.demo.iot.myapplication;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class Home extends Fragment {

    MQTTHelper mqttHelper;
    TextView txtSensor_1;
    TextView txtSensor_2;
    ProgressBar tempProgressBar;
    ProgressBar humiProgressBar;
    LabeledSwitch toggle_switch_fan;
    LabeledSwitch toggle_switch_led;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        view.setContentView(R.layout.activity_main);

    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home, container, false);
        tempProgressBar = view.findViewById(R.id.tempProgressBar_1);
        humiProgressBar = view.findViewById(R.id.humidProgressBar_1);
        txtSensor_1 = view.findViewById(R.id.tempText_1);
        txtSensor_2 = view.findViewById(R.id.humidText_1);
        toggle_switch_fan = view.findViewById(R.id.toggle_switch_fan);
        toggle_switch_led = view.findViewById(R.id.toggle_switch_led);

        toggle_switch_fan.setOnToggledListener(new OnToggledListener() {
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
        return view;
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
        mqttHelper = new MQTTHelper(getContext());
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
                    txtSensor_1.setText(message.toString());
//                    Integer temp = message.toString();
                    tempProgressBar.setProgress(Math.round(Integer.parseInt(message.toString())), true);
                } else if (topic.contains("button1")) {
                    if (message.toString().equals("ON")) {
                        toggle_switch_led.setOn(true);
                    } else {
                        toggle_switch_led.setOn(false);
                    }
                } else if (topic.contains("button2")) {
                    if (message.toString().equals("ON")) {
                        toggle_switch_fan.setOn(true);
                    } else {
                        toggle_switch_fan.setOn(false);
                    }
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

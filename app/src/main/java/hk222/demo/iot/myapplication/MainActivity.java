package hk222.demo.iot.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    MQTTHelper mqttHelper;
    TextView txtSensor_1;
    TextView txtSensor_2;
    ProgressBar tempProgressBar;
    ProgressBar humiProgressBar;
    LabeledSwitch toggle_switch_fan;
    LabeledSwitch toggle_switch_led;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempProgressBar = findViewById(R.id.tempProgressBar_1);
        humiProgressBar = findViewById(R.id.humidProgressBar_1);
        txtSensor_1 = findViewById(R.id.tempText_1);
        txtSensor_2 = findViewById(R.id.humidText_1);
        toggle_switch_fan = findViewById(R.id.toggle_switch_fan);
        toggle_switch_led = findViewById(R.id.toggle_switch_led);

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




//public class MainActivity extends AppCompatActivity {
//
//    MQTTHelper mqttHelper;
//    TextView txtSensor_1;
//    TextView txtSensor_2;
//    ProgressBar tempProgressBar;
//    ProgressBar humiProgressBar;
//    LabeledSwitch toggle_switch_fan;
//    LabeledSwitch toggle_switch_led;
//    BottomNavigationView main_bottom_navigation;
//    ViewPager main_view_pager;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        main_bottom_navigation = findViewById(R.id.main_bottom_navigation);
//        main_view_pager = findViewById(R.id.main_view_pager);
//
////        tempProgressBar = findViewById(R.id.tempProgressBar_1);
////        humiProgressBar = findViewById(R.id.humidProgressBar_1);
////        txtSensor_1 = findViewById(R.id.tempText_1);
////        txtSensor_2 = findViewById(R.id.humidText_1);
////        toggle_switch_fan = findViewById(R.id.toggle_switch_fan);
////        toggle_switch_led = findViewById(R.id.toggle_switch_led);
////
////        toggle_switch_fan.setOnToggledListener(new OnToggledListener() {
////            @Override
////            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
////                if (isOn == true) {
////                    sendDataMQTT("xMysT/feeds/button2", "ON");
////                } else {
////                    sendDataMQTT("xMysT/feeds/button2", "OFF");
////                }
////            }
////        });
////
////        toggle_switch_led.setOnToggledListener(new OnToggledListener() {
////            @Override
////            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
////                if (isOn == true) {
////                    sendDataMQTT("xMysT/feeds/button1", "ON");
////                } else {
////                    sendDataMQTT("xMysT/feeds/button1", "OFF");
////                }
////            }
////        });
////
////        startMQTT();
//    }
//    private void setUpBottomNavigation() {
//        main_bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.action_home:
//                        main_view_pager.setCurrentItem(0);
//                        break;
//                    case R.id.action_chart:
//                        break;
//                }
//                return true;
//            }
//        }
//        );
//    }
//
//    private void setUpViewPager() {
//        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        main_view_pager.setAdapter(mainViewPagerAdapter);
//        main_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//                                                    @Override
//                                                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onPageSelected(int position) {
//                                                        switch (position) {
//                                                            case 0:
//                                                                main_bottom_navigation.getMenu().findItem(R.id.action_home).setChecked(true);
//                                                                break;
//                                                            case 1:
//                                                                main_bottom_navigation.getMenu().findItem(R.id.action_chart).setChecked(true);
//                                                                break;
//                                                        }
//                                                    }
//                                                }
//        );
//    }
//
//    public void sendDataMQTT(String topic, String value) {
//        MqttMessage msg = new MqttMessage();
//        msg.setId(1234);
//        msg.setQos(0);
//        msg.setRetained(false);
//
//        byte[] b = value.getBytes(Charset.forName("UTF-8"));
//        msg.setPayload(b);
//
//        try {
//            mqttHelper.mqttAndroidClient.publish(topic, msg);
//        }catch (MqttException e){
//
//        }
//    }
//
//    public void startMQTT() {
//        mqttHelper = new MQTTHelper(this);
//        mqttHelper.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean reconnect, String serverURI) {
//
//            }
//
//            @Override
//            public void connectionLost(Throwable cause) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d("Received: ",topic + "***" + message.toString());
//                if (topic.contains("sensor")) {
//                    txtSensor_1.setText(message.toString());
////                    Integer temp = message.toString();
//                    tempProgressBar.setProgress(Math.round(Integer.parseInt(message.toString())), true);
//                } else if (topic.contains("button1")) {
//                    if (message.toString().equals("ON")) {
//                        toggle_switch_led.setOn(true);
//                    } else {
//                        toggle_switch_led.setOn(false);
//                    }
//                } else if (topic.contains("button2")) {
//                    if (message.toString().equals("ON")) {
//                        toggle_switch_fan.setOn(true);
//                    } else {
//                        toggle_switch_fan.setOn(false);
//                    }
//                } else if (topic.contains("receive")) {
//                    txtSensor_2.setText(message.toString());
//                    humiProgressBar.setProgress(Math.round(Float.parseFloat(message.toString())), true);
//                }
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//
//            }
//        });
//    }
//
//}
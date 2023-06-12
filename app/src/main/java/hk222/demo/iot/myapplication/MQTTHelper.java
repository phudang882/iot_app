package hk222.demo.iot.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
//import android.os.Build;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class MQTTHelper {
    private static final String TAG = "MQTTHelper";

    private static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static class Data {
        public Date date;
        public String message;
        public Data(String date, String message) throws ParseException {
            this.message = message;
        }
    }
    public MqttAndroidClient mqttAndroidClient;

    public MQTTHelper(Context context){
        Random random = new Random();
        int a = 100000000 + random.nextInt(899999999);
        mqttAndroidClient = new MqttAndroidClient(context, BaseActivity.serverUri,String.valueOf(a));
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                Log.w("Receive: ", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }
//    public DataPoint getData(String feed){
//
//    }
    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(BaseActivity.username);
        mqttConnectOptions.setPassword(BaseActivity.password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "onSuccess: ");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + BaseActivity.serverUri + exception.toString());
                    Log.e("Mqtt", BaseActivity.username + " " +BaseActivity.password);
                }
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        for (String arrayTopic : BaseActivity.defaultTopic) {
            try {
                mqttAndroidClient.subscribe(BaseActivity.username+ "/feeds/"+ arrayTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("TEST", "Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("TEST", "Subscribed fail!");
                    }
                });

            } catch (MqttException ex) {
                System.err.println("Exceptionst subscribing");
                ex.printStackTrace();
            }
        }
    }

}

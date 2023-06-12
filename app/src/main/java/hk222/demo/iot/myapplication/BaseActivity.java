package hk222.demo.iot.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {


    public static ArrayList<String> defaultTopic =
            new ArrayList<>(Arrays.asList("sensor", "sensor1", "sensor2", "buttonA", "buttonB","AI"));
    public static String clientId = "12345789";
    //    final String username = "dlhcmut";
    public static String username = "phudang882";
    public static String password = "aio_UaqF95J8PogiLwGBKqTqmoqbocDG";
    public static String serverUri = "tcp://io.adafruit.com:1883";
    public MQTTHelper mqttHelper = null;
    protected BottomNavigationView navigationView;
    protected static SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        if (mqttHelper == null){
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            username = sharedPreferences.getString("0","");
            password = sharedPreferences.getString("1","");
            for(int i=2;i<7;++i){
                defaultTopic.set(i-2,sharedPreferences.getString(String.valueOf(i),""));
            }
        }
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("Item", "onNavigationItemSelected: ");
        Intent intent;
        final int i = item.getItemId();
        if (i== R.id.action_home){
                intent = new Intent(this, Home.class);
        }
        else if (i == R.id.action_camera){
                intent = new Intent(this, Camera.class);
        }
        else if (i == R.id.action_setting){
                intent = new Intent(this,Setting.class);
        }
        else {
            intent = new Intent(this,Home.class);
        }
        startActivity(intent);

        //finish()
        return true;
    }

    private void updateNavigationBarState(){
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        MenuItem item = navigationView.getMenu().findItem(itemId);
        item.setChecked(true);
    }



    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();


}

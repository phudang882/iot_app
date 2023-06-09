package hk222.demo.iot.myapplication;


import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class Setting extends BaseActivity {
    private final String TAG = "setting";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ArrayList<String> settings = new ArrayList<>();

        if (!sharedPreferences.contains("0")) {
            settings.add(BaseActivity.username);
            settings.add(BaseActivity.password);
            settings.addAll(BaseActivity.defaultTopic);
            int len = settings.size();
            for (int i = 0; i < len; i++) {
                editor.putString(String.valueOf(i),settings.get(i));
                Log.d(TAG,settings.get(i));
            }
            editor.apply();
            Log.d(TAG,sharedPreferences.getString("0",""));
        }
        settings.clear();
        ConstraintLayout constraintLayout = findViewById(R.id.inputText);
        int len = sharedPreferences.getAll().size();
        for(int i= 0;i< len;++i){
            settings.add(sharedPreferences.getString(String.valueOf(i),""));
        }
        Log.d(TAG, String.valueOf(len));
        for (String setting : settings) {
            Log.d(TAG, "shared "+setting);
        }
        Log.d(TAG, String.valueOf(settings.size()));
        View v;
        for (int i = 0,j=0; i < constraintLayout.getChildCount(); i++) {
            v = constraintLayout.getChildAt(i);
            if (v instanceof EditText){
//                ((EditText) v).setText(settings.get(j));
//                j++;
                Log.d(TAG, String.valueOf(v.getId()));
            }
        }
        settings.clear();
        AppCompatButton appCompatButton = findViewById(R.id.save_button);
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Onclick","Ok");
//                appCompatButton.setBackgroundResource(R.drawable.shape);
                appCompatButton.setText(R.string.saving);
                appCompatButton.setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
                ArrayList<String> arrayList = new ArrayList<>();
                int a = constraintLayout.getChildCount();
                for (int i = 0; i< a; i++) {
                    View temp = constraintLayout.getChildAt(i);
                    if (temp instanceof EditText){
                        arrayList.add(((EditText) temp).getText().toString());
                    }
                }
                for (int i = 0; i < a; i++) {
                    editor.putString(String.valueOf(i),arrayList.get(i));
                }
                editor.apply();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appCompatButton.setText(R.string.save_button_text);
                        appCompatButton.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
                    }
                }, 1000);
        }});
    }
    @Override
    int getContentViewId() {
        return R.layout.setting_menu;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.action_setting;
    }

}
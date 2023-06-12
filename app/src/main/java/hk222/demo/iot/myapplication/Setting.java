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
            for (int i = 0; i < 7; i++) {
                editor.putString(String.valueOf(i),settings.get(i));
            }
            editor.apply();
            Log.d(TAG,sharedPreferences.getString("0",""));
        }
        settings.clear();
        for (int i = 0; i < 7; i++) {
            settings.add(sharedPreferences.getString(String.valueOf(i),""));
        }
        ConstraintLayout constraintLayout = findViewById(R.id.inputText);
        View v;
        for (int i = 0; i < constraintLayout.getChildCount(); i++) {
            v = constraintLayout.getChildAt(i);
            if (v instanceof EditText){
                ((EditText) v).setText(settings.get(i/2));
            }
        }
        AppCompatButton appCompatButton = findViewById(R.id.save_button);
        settings.clear();
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Onclick","Ok");
//                appCompatButton.setBackgroundResource(R.drawable.shape);
                appCompatButton.setText(R.string.saving);
                appCompatButton.setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
                ArrayList<String> arrayList = new ArrayList<>();
                for (int i = 0; i < constraintLayout.getChildCount(); i++) {
                    View temp = constraintLayout.getChildAt(i);
                    if (temp instanceof EditText){
                        arrayList.add(((EditText) temp).getText().toString());
                    }
                }
                for (int i = 0; i < 7; i++) {
                    editor.putString(String.valueOf(i),arrayList.get(i));
                }
                editor.apply();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        appCompatButton.setText(R.string.save_button_text);
                        appCompatButton.setTypeface(Typeface.DEFAULT);
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
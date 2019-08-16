package com.sveder.cardboardpassthrough;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by Toshiba on 29/12/2015.
 */
public class SplashScreen extends Activity {
    private SharedPreferences mPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    if(mPreferences.getString("AuthToken",null) == null){
                        Intent intent = new Intent(SplashScreen.this,LoginActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                        startActivity(intent);
                    }

                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause(){
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}

package com.sveder.cardboardpassthrough;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class HomeActivity extends Activity {

    private static final String TASKS_URL = "http://192.168.1.78:3000/api/v1/messages";
    private SharedPreferences mPreferences;
    private String lMessage = "";


    private SpeechRecognizer speechRecognizer;
    private Handler handler;
    private Timer timer;
    private boolean activado = false;
    listener voiceRecognizer;
    int status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        //handler = new Handler();
        //speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        //speechRecognizer.setRecognitionListener(new listener());

        voiceRecognizer = new listener();
        handler = new Handler(Looper.getMainLooper());

        callMessageTask();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) && activado == false){
            activado = true;
            status = 1;
            CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
            toast.show("Activando reconocimiento de voz");
            handler.post(voiceRecognizer);
        }else if((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) && activado == true){
            status = 0;
            activado = false;
            CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
            toast.show("Desactivando reconocimiento de voz");

        }else{
            CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
            toast.show("Primero tiene que activar el reconocimiento");
        }
        return true;
    }
    class listener implements RecognitionListener, Runnable {
        @Override
        public void run()
        {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            speechRecognizer.setRecognitionListener((RecognitionListener) this);

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //... all the intent stuff ...
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
            speechRecognizer.startListening(intent);
        }

        public void onReadyForSpeech(Bundle params)
        {
        }
        public void onBeginningOfSpeech()
        {
            CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
            toast.show("Reconociendo...");
        }
        public void onRmsChanged(float rmsdB)
        {
        }
        public void onBufferReceived(byte[] buffer)
        {
        }
        public void onEndOfSpeech()
        {
        }
        public void onError(int error)
        {
            if ((error == SpeechRecognizer.ERROR_NO_MATCH) || (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
            {
                if(status == 0){
                    handler.removeCallbacks(this);
                }else{
                    handler.postDelayed(this,8000);
                }
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
                toast.show("No logramos reconocer algo");
            }
            else
            {
                if(status == 0){
                    handler.removeCallbacks(this);
                }else{
                    handler.postDelayed(this,8000);
                }
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
                toast.show("Algo fue mal :(");
            }
        }
        public void onResults(Bundle results)
        {
            if(status == 0){
                handler.removeCallbacks(this);
            }else{
                handler.postDelayed(this,5000);
            }
            String str = new String();
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if(data != null){
                str += data.get(0);
                CustomToast toast = new CustomToast(getApplicationContext(), Toast.LENGTH_SHORT);
                toast.show(str);
            }
        }
        public void onPartialResults(Bundle partialResults)
        {
        }
        public void onEvent(int eventType, Bundle params)
        {
        }
    }
    private void callMessageTask(){
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            MessageTask getMessageTask = new MessageTask(HomeActivity.this);
                            getMessageTask.setMessageLoading(null);
                            getMessageTask.execute(TASKS_URL);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }
    private class MessageTask extends UrlJsonAsyncTask {
        public MessageTask(Context context) {
            super(context);
        }
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();
            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    // add the user email and password to
                    // the params
                    userObj.put("authentication_token", mPreferences.getString("AuthToken", null));
                    holder.put("user", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                    json.put("info", "Error");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }
        @Override
        protected void onPreExecute(){

        }
        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONObject jsonTasks = json.getJSONObject("data").getJSONObject("message");
                if (!lMessage.equals(jsonTasks.getString("message"))){
                    CustomToast toast = new CustomToast(context, Toast.LENGTH_LONG);
                    toast.show(jsonTasks.getString("message"));
                }
                lMessage = jsonTasks.getString("message");
            } catch (Exception e) {
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}

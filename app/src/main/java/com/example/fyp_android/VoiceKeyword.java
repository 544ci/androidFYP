package com.example.fyp_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class VoiceKeyword extends AppCompatActivity implements RecognitionListener {
    TextView title;
    TextView keyword;
    String keywordText;
    TextView loadingText;
    Button confirmButton;
    Button cancelButton;
    SpinKitView animation;
    private Model model;
    private SpeechRecognizer recognizer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    static {
        System.loadLibrary("kaldi_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_keyword);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        keywordText="";
        title = findViewById(R.id.textView10);
        keyword = findViewById(R.id.textView9);
        loadingText = findViewById(R.id.textView4);
        confirmButton= findViewById(R.id.confirm);
        cancelButton = findViewById(R.id.cancel);
        animation = findViewById(R.id.spin_kit);
        loading();
        new VoiceKeyword.SetupTask(this).execute();
    }

    private void loading() {
        title.setVisibility(View.INVISIBLE);
        keyword.setVisibility(View.INVISIBLE);
        confirmButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
    }
    private void doneLoading(){
        title.setVisibility(View.VISIBLE);
        keyword.setVisibility(View.VISIBLE);
        confirmButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        loadingText.setVisibility(View.INVISIBLE);
        animation.setVisibility(View.INVISIBLE);

        try {
            recognizer = new SpeechRecognizer(model);
            recognizer.addListener(this);
            recognizer.startListening();
        } catch (IOException e) {
            Log.e("com.example.voice",e.getMessage());
        }

    }
    @Override
    public void onStop() {
        super.onStop();
        recognizer.stop();
        recognizer.shutdown();


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.stop();
        recognizer.shutdown();
    }
    @Override
    public void onPartialResult(String s) {

    }

    @Override
    public void onResult(String s) {
        JSONObject json;

        try {
            json = new JSONObject(s);
            String parsedText = json.getString("text");
            Log.d("com.example.word",parsedText);
            if(!parsedText.equals("")){
                keyword.setText(parsedText);
                keywordText = parsedText;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    public void confirm(View view) {
        editor.putString("VoiceKeyword",keywordText);
        editor.commit();
        Log.d("!!here",sharedPreferences.getString("VoiceKeyword","no keyword"));
        recognizer.stop();
        recognizer.shutdown();
        this.finish();
    }

    public void cancel(View view) {
        recognizer.stop();
        recognizer.shutdown();
        this.finish();
    }


    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<VoiceKeyword> activityReference;

        SetupTask(VoiceKeyword activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                Log.d("!!!!", assetDir.toString());
                activityReference.get().model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                Log.e("com.example.voiee",e.getMessage());
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result == null) {
                activityReference.get().doneLoading();
            }
        }
    }
}

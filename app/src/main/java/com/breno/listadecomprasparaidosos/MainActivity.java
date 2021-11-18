package com.breno.listadecomprasparaidosos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final Integer              recordAudioRequestCode = 1;
    private static final Integer              networkRequestCode     = 2;
    private              boolean              flagOtherOptions       = true;
            static       boolean              enableAudioDescription = false;
    private static       SpeechRecognizer     speechRecognizer;
            static       ArrayList<String>    speechAsText;
    private              LottieAnimationView  micAnimation;
    private              FloatingActionButton fabPlus, fabHelp, fabAbout;
            static       TextToSpeech         ttsMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            checkPermission();

        checkInternetConnection();

        initTTSMain();

        fabPlus          = findViewById(R.id.fabPlus);
        fabHelp          = findViewById(R.id.fabHelp);
        fabAbout         = findViewById(R.id.fabAbout);
        micAnimation     = findViewById(R.id.micanimation);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        micAnimation.setScale(2);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                try {
                    ttsMain.stop();
                } catch (Exception e){
                    //TTS NOT SPEAKING - OK
                }
                micAnimation.playAnimation();
                Toast.makeText(MainActivity.this, "OUVINDO...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                micAnimation.pauseAnimation();
                micAnimation.cancelAnimation();
                Toast.makeText(MainActivity.this, "PAROU DE OUVIR", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                setSpeechAsText(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
                processSpeech();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        micAnimation.setOnClickListener(v -> speechRecognizer.startListening(speechRecognizerIntent));

        fabPlus.setOnClickListener(v -> {
            if (flagOtherOptions) {
                fabAbout.show();
                fabHelp.show();
                fabPlus.animate().rotation(45);
                fabAbout.animate().translationY(-150);
                fabHelp.animate().translationY(-300);
                fabPlus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.closeColor)));

                flagOtherOptions = false;

            }else {
                fabAbout.animate().translationY(-300);
                fabHelp.animate().translationY(-450);
                fabAbout.hide();
                fabHelp.hide();
                fabPlus.animate().rotation(0);
                fabPlus.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.projectColor)));

                flagOtherOptions = true;

            }
        });

        fabAbout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getResources().getString(R.string.aboutTitle));
            builder.setMessage(getResources().getString(R.string.aboutMessage));
            builder.setNegativeButton(getResources().getString(R.string.close), (dialog, which) -> dialog.dismiss());

            builder.show();
        });

        fabHelp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getResources().getString(R.string.helpTitle));
            builder.setMessage(getResources().getString(R.string.helpMsg));

            builder.setNeutralButton(getResources().getString(R.string.activateTutorial), (dialog, which) -> {
                dialog.dismiss();
                setEnableAudioDescription(true);
                audioDescriptionTutorialMain();
            });
            builder.setNegativeButton(getResources().getString(R.string.close), (dialog, which) -> dialog.dismiss());

            builder.show();
        });
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, recordAudioRequestCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},networkRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == recordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permissão de audio concedida",Toast.LENGTH_SHORT).show();
        }
    }

    private void processSpeech(){
        String            elements           = getSpeechAsText().get(0);
        ArrayList<String> temp               = new ArrayList<>(Arrays.asList(elements.split(" ")));
        String            temp2;

        //CONVERT TEXT NUMBERS TO NUMERALS
        for(int i = 0; i < temp.size(); i++){
            if(i % 2 == 0){
                temp2 = getIntNumberFromText(temp.get(i));

                if (temp2.compareTo("-1") != 0){
                    temp.set(i, temp2);
                }
            }
        }

        //CHECK IF LIST IS IN PATTERN
        if(isArrayListInPattern(temp)){
            setSpeechAsText(temp);
            callListScreen();
        }
        else
            outOfPattern(MainActivity.this);
    }

    //IS ARRAY IN PATTERN 2 RICE, 3 ORANGES?
    static boolean isArrayListInPattern(ArrayList<String> elements) {
        boolean processStatus = false;

        for (int i = 0; i < elements.size(); i++) {

            if (i % 2 == 0) { //IF NUMBER OK
                try {
                    Integer n = Integer.parseInt(elements.get(i));
                } catch (NumberFormatException nfe) {
                    return false;
                }
                processStatus = true;
            }
            else
            {
                try { //IF NOT AN NUMBER OK
                    Integer n = Integer.parseInt(elements.get(i));
                    return false;
                } catch (NumberFormatException nfe) {
                    processStatus = true;
                }
            }
        }
        return processStatus;
    }

    private void callListScreen(){
        Intent intent = new Intent(MainActivity.this, GroceryListActivity.class);
        startActivity(intent);
    }

    static void outOfPattern(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.reconFailure);
        builder.setMessage(R.string.outOfPatternMsg);

        builder.create().show();
    }

    static String getIntNumberFromText(String strNum) {
        int ret = -1;
        if(strNum.contains("um"))
            ret = 1;
        else if(strNum.contains("dois"))
            ret = 2;
        else if(strNum.contains("três"))
            ret = 3;
        else if(strNum.contains("tres"))
            ret = 3;
        else if(strNum.contains("quatro"))
            ret = 4;
        else if(strNum.contains("cinco"))
            ret = 5;
        else if(strNum.contains("seis"))
            ret = 6;
        else if(strNum.contains("sete"))
            ret = 7;
        else if(strNum.contains("oito"))
            ret = 8;
        else if(strNum.contains("nove"))
            ret = 9;

        return String.valueOf(ret);
    }

    static ArrayList<String> getSpeechAsText() {
        return speechAsText;
    }

    private static void setSpeechAsText(ArrayList<String> speechAsText) {
        MainActivity.speechAsText = speechAsText;
    }

    private void checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED))
            notConnectedMenu();
    }

    private void notConnectedMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.internetFailureTitle);
        builder.setMessage(R.string.internetFailureMessage);

        builder.setPositiveButton(R.string.tryAgain, (dialog, which) -> recreate());

        builder.setNegativeButton(R.string.exit, (dialog, which) -> finishAffinity());

        builder.show();
    }

    void audioDescriptionTutorialMain(){
        ttsMain.speak(getResources().getString(R.string.audioTutorialMain), TextToSpeech.QUEUE_FLUSH, null, "mainActivity");
    }

    void initTTSMain(){
        ttsMain = new TextToSpeech(getApplicationContext(), status -> ttsMain.setLanguage(new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getISO3Country())));
    }

    static boolean isEnableAudioDescription() {
        return enableAudioDescription;
    }

    static void setEnableAudioDescription(boolean enableAudioDescription) {
        MainActivity.enableAudioDescription = enableAudioDescription;
    }

}
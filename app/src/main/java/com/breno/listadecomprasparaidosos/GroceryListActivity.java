package com.breno.listadecomprasparaidosos;


import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class GroceryListActivity extends AppCompatActivity {
    public         RecyclerView         recyclerView;
    public         RecycleAdapter       recycleAdapter;
    public         RelativeLayout       relativeLayout;
    private        FloatingActionButton fabMic, fabHelpList;

    private        LottieAnimationView  micAnimationAdd;
    private static SpeechRecognizer    speechRecognizerAdd;

    private ArrayList<String> itemArray = getPreparedArrayList();
    private ArrayList<URL>    imagesURL = GoogleCSE.setImagesOnList(itemArray);

    public GroceryListActivity() throws IOException {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        audioDescriptionTutorialList(MainActivity.isEnableAudioDescription());

        relativeLayout  = findViewById(R.id.relativeLayoutRL);
        recyclerView    = findViewById(R.id.rvList);
        fabMic          = findViewById(R.id.fabMicAdd);
        fabHelpList     = findViewById(R.id.fabHelpList);
        micAnimationAdd = findViewById(R.id.micanimationadd);

        speechRecognizerAdd = SpeechRecognizer.createSpeechRecognizer(this);

        micAnimationAdd.setScale((float) 0.45);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recycleAdapter = new RecycleAdapter(this, itemArray, imagesURL);
        recyclerView.setAdapter(recycleAdapter);

        //FOR SWIPE REMOVE ON ELEMENT ON LIST
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizerAdd.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                try {
                    MainActivity.ttsMain.stop();
                } catch (Exception e){
                    //TTS not speaking - OK
                }

                micAnimationAdd.setVisibility(View.VISIBLE);
                micAnimationAdd.playAnimation();
                Toast.makeText(GroceryListActivity.this, getResources().getString(R.string.listening), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                micAnimationAdd.pauseAnimation();
                micAnimationAdd.cancelAnimation();
                micAnimationAdd.setVisibility(View.INVISIBLE);
                Toast.makeText(GroceryListActivity.this, getResources().getString(R.string.processing), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                processSpeechAdd(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        fabMic.setOnClickListener(v -> speechRecognizerAdd.startListening(speechRecognizerIntent));

        fabHelpList.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GroceryListActivity.this);

            builder.setTitle(getResources().getString(R.string.helpTitleList));
            builder.setMessage(getResources().getString(R.string.helpMsgList));

            builder.setNeutralButton(getResources().getString(R.string.activateTutorial), (dialog, which) -> {
                dialog.dismiss();
                MainActivity.setEnableAudioDescription(true);
                audioDescriptionTutorialList(true);
            });
            builder.setNegativeButton(getResources().getString(R.string.close), (dialog, which) -> dialog.dismiss());

            builder.show();
        });
    }

    //FOR SWIPE REMOVE ON ELEMENT ON LIST
    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Snackbar snackbar = Snackbar.make(relativeLayout, getResources().getString(R.string.removed), Snackbar.LENGTH_LONG);
            snackbar.show();

            MainActivity.speechAsText.remove(viewHolder.getAdapterPosition());
            MainActivity.speechAsText.remove(viewHolder.getAdapterPosition());
            itemArray.remove(viewHolder.getAdapterPosition());

            recycleAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            if(recycleAdapter.getItemCount() == 0){
                finish();
            }
        }
    };

    private ArrayList<String> getPreparedArrayList(){
        ArrayList<String> itemArray = new ArrayList<>();
        int j = 0;

        for(int i = 0; i < MainActivity.getSpeechAsText().size(); i++){
            if (i % 2 == 0){
                itemArray.add(MainActivity.getSpeechAsText().get(i));
            }
            else{
                itemArray.set(j, itemArray.get(j) + " " + MainActivity.getSpeechAsText().get(i));
                j++;
            }
        }
        return itemArray;
    }

    private void processSpeechAdd(ArrayList<String> addList){
        String            elements           = addList.get(0);
        ArrayList<String> temp               = new ArrayList<>(Arrays.asList(elements.split(" ")));
        String            temp2;

        //CONVERT TEXT NUMBERS TO NUMERALS
        for(int i = 0; i < temp.size(); i++){
            if(i % 2 == 0){
                temp2 = MainActivity.getIntNumberFromText(temp.get(i));

                if (temp2.compareTo("-1") != 0){
                    temp.set(i, temp2);
                }
            }
        }

        //CHECK IF LIST IS IN PATTERN
        if(MainActivity.isArrayListInPattern(temp)){
            MainActivity.speechAsText.addAll(temp);

            itemArray = getPreparedArrayList();

            try {
                imagesURL = GoogleCSE.setImagesOnList(itemArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recycleAdapter = new RecycleAdapter(GroceryListActivity.this, itemArray, imagesURL);
            recyclerView.setAdapter(recycleAdapter);
            //recycleAdapter.notifyDataSetChanged();
        }
        else
            MainActivity.outOfPattern(GroceryListActivity.this);
    }

    void audioDescriptionTutorialList(boolean isAudioDescriptionEnabled){
        if (isAudioDescriptionEnabled)
            MainActivity.ttsMain.speak(getResources().getString(R.string.audioTutorialList), TextToSpeech.QUEUE_FLUSH, null, "mainActivity");
    }
}

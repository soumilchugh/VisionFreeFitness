package com.example.voice;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.util.EventHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Future;

public class FoodLoggingActivity extends AppCompatActivity implements
        TextToSpeech.OnInitListener {
    private static String speechSubscriptionKey = "eb7b365364a145f3a83a60e864c67700";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "eastus";
    private TextToSpeech tts;
    private String text = null;
    Bundle params;
    ArrayList<String> foodItems=new ArrayList<String>();
    ArrayList<Integer> calorieItems=new ArrayList<Integer>();
    ListFoodAdapter listAdapter;
    boolean complete = false;
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    void checkspeech() {

        if (text.contains("cheesecake") | text.contains("Cheesecake")) {
            complete = true;

            speakOut("You added Cheesecake");
            foodItems.clear();
            calorieItems.clear();
            addAll();
            foodItems.add(0,"Cheesecake");
            calorieItems.add(0,300);
            listAdapter.notifyDataSetChanged();

        }
        if (text.contains("juice") | text.contains("Juice")) {
            complete = true;
            speakOut("You added Juice");
            foodItems.clear();
            calorieItems.clear();
            addAll();
            foodItems.add(0,"Juice");
            calorieItems.add(0,30);
            listAdapter.notifyDataSetChanged();


        }

    }

    void listen() {

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);

            assert(config != null);

            final SpeechRecognizer reco = new SpeechRecognizer(config);
            reco.recognizing.addEventListener(new EventHandler<SpeechRecognitionEventArgs>() {
                @Override
                public void onEvent(Object o, SpeechRecognitionEventArgs speechRecognitionResultEventArgs) {
                    final String s = speechRecognitionResultEventArgs.getResult().getText();
                    Log.i("SpeechSDKDemo", "Intermediate result received: " + s);
                    text = s;
                    checkspeech();
                    reco.close();
                    // your code goes here
                    // ...
                }
            });

            assert(reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);

            // Note: this will block the UI thread, so eventually, you want to
            //        register for the event (see full samples)
            SpeechRecognitionResult result = task.get();
            assert(result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                text = result.getText();
                // Use a string for speech.
            }
            else {
                //txt.setText("Error recognizing. Did you update the subscription info?" + System.lineSeparator() + result.toString());
            }

            reco.close();
            checkspeech();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
    }

    void initTTS() {

        tts = new TextToSpeech(this, this);
        Voice voiceobj = new Voice("it-it-x-kda#male_2-local", Locale.getDefault(), 1, 1, false, null);
        tts.setVoice(voiceobj);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                Log.d("YourActivity", "TtS Compeleted ");

                    listen();

            }


            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onError(String s) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_logging);
        params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        ListView ll = (ListView) findViewById(R.id.List);
        listAdapter = new
                ListFoodAdapter(this, calorieItems,foodItems);
        ll.setAdapter(listAdapter);
        addAll();
        listAdapter.notifyDataSetChanged();

        initTTS();


    }

    void addAll() {
        foodItems.add(0,"Burger");
        foodItems.add(0,"Cereals");
        foodItems.add(0,"Bagel");
        foodItems.add(0,"Tortila");
        foodItems.add(0,"Muffin");
        calorieItems.add(0,220);
        calorieItems.add(0,103);
        calorieItems.add(0,195);
        calorieItems.add(0,159);
        calorieItems.add(0,162);
    }

    private void speakOut(String data) {

        tts.speak(data, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("Which food item did you have ?");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
}

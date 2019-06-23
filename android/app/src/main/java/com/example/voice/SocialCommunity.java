package com.example.voice;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.Future;

import static com.example.voice.FitnessHistory.randBetween;

public class SocialCommunity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    ArrayList<String> placeItems=new ArrayList<String>();
    ArrayList<String> eventItems=new ArrayList<String>();
    ArrayList<String> timeItems=new ArrayList<String>();
    private static String speechSubscriptionKey = "eb7b365364a145f3a83a60e864c67700";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "eastus";
    ListSocialAdapter listAdapter;
    private TextToSpeech tts;
    Bundle params;
    String text = null;
    boolean exit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_community);
        params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        ListView ll = (ListView) findViewById(R.id.List);
        listAdapter = new
                ListSocialAdapter(this, placeItems, eventItems, timeItems);
        ll.setAdapter(listAdapter);
        placeItems.add(0,"Bay Street");
        placeItems.add(0,"Yonge Street");
        placeItems.add(0,"Jarvis Street");
        placeItems.add(0,"Gerrad Street");
        placeItems.add(0,"Dufferin Street");
        placeItems.add(0,"St Clair West");
        placeItems.add(0,"Church Street");
        eventItems.add(0,"Meditation");
        eventItems.add(0,"Yoga");
        eventItems.add(0,"Walking");
        eventItems.add(0,"Meditation");
        eventItems.add(0,"Yoga");
        eventItems.add(0,"Yoga");
        eventItems.add(0,"Meditation");
        SimpleDateFormat dfDateTime  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            int year = 2019;
            int month = randBetween(0, 6);
            int hour = randBetween(9, 22); //Hours will be displayed in between 9 to 22
            int min = randBetween(0, 59);
            int sec = randBetween(0, 59);


            GregorianCalendar gc = new GregorianCalendar(year, month, 1);
            int day = randBetween(1, gc.getActualMaximum(gc.DAY_OF_MONTH));

            gc.set(year, month, day, hour, min,sec);

            System.out.println(dfDateTime.format(gc.getTime()));
            timeItems.add(0,dfDateTime.format(gc.getTime()));
        }


        listAdapter.notifyDataSetChanged();
        initTTS();
    }

    void startMainActivity() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("FROM", SocialCommunity.class.getSimpleName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    void checkspeech() {
        if (text.contains("Yes") | text.contains("yes")) {

            speakOut("You said Yes. I will remind you on the day of the event.");
            exit = true;
        }
        if (text.contains("No") | text.contains("no")) {

            speakOut("No problem. Whenever you feel like going, you can come back here to reschedule");
            exit = true;
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

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("The most recent fitness event  happening near you is about "+eventItems.get(0)+ "at" + placeItems.get(0) + ". You must go to this event. This will help you relax. Do you want me to remind you 30 mins before the event?");

            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut(String data) {

        tts.speak(data, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
    }

    void initTTS() {

        tts = new TextToSpeech(this, this);
        Voice voiceobj = new Voice("it-it-x-kda#male_2-local", Locale.getDefault(), 1, 1, false, null);
        tts.setVoice(voiceobj);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                Log.d("YourActivity", "TtS Compeleted ");
                if (exit){
                    startMainActivity();
                } else {
                    listen();
                }
            }

            @Override
            public void onError(String utteranceId) {
            }

            @Override
            public void onStart(String utteranceId) {
            }
        });
    }
}

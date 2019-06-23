package com.example.voice;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

public class FitnessHistory extends AppCompatActivity implements
        TextToSpeech.OnInitListener {

    ArrayList<Integer> stepItems=new ArrayList<Integer>();
    ArrayList<Integer> caloriesItems=new ArrayList<Integer>();
    ArrayList<String> timestampItems=new ArrayList<String>();
    ListDisplayAdapter listAdapter;
    private TextToSpeech tts;
    Bundle params;

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
                speakOut("Your most recent data tells me that you covered "+stepItems.get(0)+"steps because of which you have burnt"+caloriesItems.get(0)+"calories");
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
            }

            @Override
            public void onError(String utteranceId) {
            }

            @Override
            public void onStart(String utteranceId) {
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_history);
        params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        ListView ll = (ListView) findViewById(R.id.List);
        listAdapter = new
                ListDisplayAdapter(this, stepItems, caloriesItems, timestampItems);
        ll.setAdapter(listAdapter);
        SimpleDateFormat dfDateTime  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        for (int i = 0; i < 10; i++) {
            int year = 2019;
            int month = randBetween(0, 6);
            int hour = randBetween(9, 22); //Hours will be displayed in between 9 to 22
            int min = randBetween(0, 59);
            int sec = randBetween(0, 59);


            GregorianCalendar gc = new GregorianCalendar(year, month, 1);
            int day = randBetween(1, gc.getActualMaximum(gc.DAY_OF_MONTH));

            gc.set(year, month, day, hour, min,sec);

            System.out.println(dfDateTime.format(gc.getTime()));
            timestampItems.add(0,dfDateTime.format(gc.getTime()));
            int steps = new Random().nextInt((5000 - 500) + 1) + 500;
            stepItems.add(0,steps);
            int calories = new Random().nextInt((500 - 200) + 1) + 200;
            caloriesItems.add(0,calories);
        }

        listAdapter.notifyDataSetChanged();
        initTTS();

    }

    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

}

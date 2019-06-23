package com.example.voice;

import android.content.Intent;
import android.graphics.Camera;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.microsoft.speech.tts.Synthesizer;

import java.util.Locale;
import java.util.concurrent.Future;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements
        TextToSpeech.OnInitListener  {

    private static String speechSubscriptionKey = "eb7b365364a145f3a83a60e864c67700";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "eastus";
    private TextToSpeech tts;
    private String text = null;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initTTS();
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            String name = extras.getString("FROM");
            if (!name.contains("SocialCommunity") | !name.contains("FrameActivity"))
                initTTS();
        } else {
            initTTS();
        }
        // Note: we need to request the permissions
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET, CAMERA}, requestCode);


    }

    void initTTS() {

        tts = new TextToSpeech(this, this);
        Voice voiceobj = new Voice("it-it-x-kda#male_2-local", Locale.getDefault(), 1, 1, false, null);
        tts.setVoice(voiceobj);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                Log.d("YourActivity", "TtS Compeleted ");
                if (text != null) {
                    if (text.contains("doctor")| text.contains("Doctor")) {
                        text = null;
                    }
                    else if (text.contains("exercise") | text.contains("Exercise")) {
                        startCamera();
                    } else if (text.contains("social") | text.contains("Social")) {
                        startSocial();

                    } else if (text.contains("fitness") | text.contains("Fitness")) {
                        startFitness();

                    } else if (text.contains("food") | text.contains("Food")) {
                        startFood();
                    }
                    else
                        listen();
                } else
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

    void startFood() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            text = null;
        }
        Intent intent = new Intent(getApplicationContext(), FoodLoggingActivity.class);
        intent.putExtra("FROM", MainActivity.class.getSimpleName());
        intent.setFlags(0);
        startActivity(intent);
    }


    void startCamera() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            text = null;
        }
        Intent intent = new Intent(getApplicationContext(), FrameActivity.class);
        intent.putExtra("FROM", MainActivity.class.getSimpleName());
        intent.setFlags(0);
        startActivity(intent);
    }

    void startFitness() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        text = null;
        Intent intent = new Intent(getApplicationContext(), FitnessHistory.class);
        intent.putExtra("FROM", MainActivity.class.getSimpleName());
        intent.setFlags(0);
        startActivity(intent);
    }

    void startSocial() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        text = null;
        Intent intent = new Intent(getApplicationContext(), SocialCommunity.class);
        intent.putExtra("FROM", MainActivity.class.getSimpleName());
        intent.setFlags(0);
        startActivity(intent);
    }

    void checkspeech() {
        if (text.contains("exercise")) {

            speakOut("Starting Exercise now");

        }
        if (text.contains("fitness")) {

            speakOut("Lets look at your performance");
        }
        if (text.contains("food")) {
            speakOut("Lets log your food ");
        }
        if (text.contains("doctor")) {
            speakOut("Your doctor says great job keep it up..");
        }
        if (text.contains("social")) {
            speakOut("Let check out events near you");

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
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("Welcome to Vision Free Fitness App. What would you like to do today? Say Exercise or Log Food or Fitness History or Social Community or Listen to a message from your doctor");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    private void speakOut(String data) {

        tts.speak(data, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
    }
}

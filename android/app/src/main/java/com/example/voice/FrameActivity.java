package com.example.voice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Category;
import com.microsoft.projectoxford.vision.contract.Face;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.hardware.Camera.*;


public class FrameActivity extends AppCompatActivity implements SurfaceHolder.Callback,TextToSpeech.OnInitListener, SensorEventListener {

    private Activity mActivity;
    private String TAG = this.getClass().getName();
    EditText mEditText;
    int frameCounter = 0;
    private TextToSpeech tts;
    Bundle params1;
    private static int SMOOTHING_WINDOW_SIZE = 20;
    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;
    public static SensorManager mSensorManager;
    public static Sensor mSensorCount, mSensorAcc;
    private float mRawAccelValues[] = new float[3];
    // smoothing accelerometer signal variables
    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;
    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;

    public static float mStepCounter = 0;
    public static float mStepCounterAndroid = 0;
    public static float mInitialStepCount = 0;
    // The image selected to detect.
    private Bitmap mBitmap;
    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    // The edit to show status and result.

    private VisionServiceClient client;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    private double lastXPoint = 1d;
    double stepThreshold = 1.0d;
    double noiseThreshold = 2d;
    private int windowSize = 10;
    private Handler mHandler;
    private Runnable _timer1;
    private int stepCounter = 0;
    private int lastStep = 0;
    private boolean showedGoalReach = false;
    boolean completed = false;
    private void openCamera() {
        try {
            if (camera == null)
                camera = open();
        }
        catch (RuntimeException e) {
            Log.e(TAG, "failed to open front camera");
        }
    }


    public void doAnalyze() {
        mEditText.setText("Analyzing...");

        try {
            new doRequest1().execute();
        } catch (Exception e)
        {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }


    private String process1() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories"};
        String[] details = {};

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }

    @Override
    public void onSensorChanged (SensorEvent e)
    {
        switch (e.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                if(mInitialStepCount == 0.0){
                    mInitialStepCount = e.values[0];
                }
                mStepCounterAndroid = e.values[0];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mRawAccelValues[0] = e.values[0];
                mRawAccelValues[1] = e.values[1];
                mRawAccelValues[2] = e.values[2];

                lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

                //Source: https://github.com/jonfroehlich/CSE590Sp2018
                for (int i = 0; i < 3; i++) {
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                    mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                    mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                }
                mCurReadIndex++;
                if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                    mCurReadIndex = 0;
                }

                avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                netMag = lastMag - avgMag; //removes gravity effect

                //update graph data points
                mGraph1LastXValue += 1d;
                mSeries1.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 60);

                mGraph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 60);
        }


        peakDetection();

        //calculatedStep.setText(new String("Steps Tracked: " + (int)mStepCounter));
        //android always returns total steps since reboot so subtract all steps recorded before the app started
        //androidStep.setText(new String("Android Steps Tracked: " + (int)(mStepCounterAndroid - mInitialStepCount)));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void peakDetection(){

        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
         *Threshold, stepThreshold was derived by observing people's step graph
         * ASSUMPTIONS:
         * Phone is held vertically in portrait orientation for better results
         */

        double highestValX = mSeries2.getHighestValueX();

        if(highestValX - lastXPoint < windowSize){
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries2.getValues(lastXPoint,highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add); //This requires API 24 or higher

        for(int i = 0; i<dataPointList.size(); i++){
            if(i == 0) continue;
            else if(i < dataPointList.size() - 1){
                forwardSlope = dataPointList.get(i+1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();

                if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    mStepCounter+=1;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class doRequest1 extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest1() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process1();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            mEditText.setText("");
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

                //mEditText.append("Image format: " + result.metadata.format + "\n");
                //mEditText.append("Image width: " + result.metadata.width + ", height:" + result.metadata.height + "\n");
                //mEditText.append("Clip Art Type: " + result.imageType.clipArtType + "\n");
                //mEditText.append("Line Drawing Type: " + result.imageType.lineDrawingType + "\n");
                //mEditText.append("Is Adult Content:" + result.adult.isAdultContent + "\n");
                ///mEditText.append("Adult score:" + result.adult.adultScore + "\n");
                //mEditText.append("Is Racy Content:" + result.adult.isRacyContent + "\n");
                //mEditText.append("Racy score:" + result.adult.racyScore + "\n\n");

//                for (Category category : result.categories) {
                //                  mEditText.append("Category: " + category.name + ", score: " + category.score + "\n");
                //              }

                //            mEditText.append("\n");
                int faceCount = 0;
                for (Face face : result.faces) {
                    faceCount++;
                    //mEditText.append("face " + faceCount + ", gender:" + face.gender + "(score: " + face.genderScore + "), age: " + +face.age + "\n");
                    Log.d(TAG, "left" + face.faceRectangle.left);
                    Log.d(TAG, "width: " + face.faceRectangle.width);
                    Log.d(TAG, "top:" + face.faceRectangle.top);
                    Log.d(TAG, "  height: " + face.faceRectangle.height);
                    //mEditText.append("    left: " + face.faceRectangle.left + ",  top: " + face.faceRectangle.top + ", width: " + face.faceRectangle.width + "  height: " + face.faceRectangle.height + "\n");
                }

            }
        }
    }




    public void doDescribe() {
        mEditText.setText("Describing...");

        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    private void updateView() {
        //Log.d(TAG,"Step"+mStepCounter);
        if (mStepCounter > stepCounter) {
            stepCounter = (int) mStepCounter;
            if (stepCounter >= 500 && !showedGoalReach) {
                showedGoalReach = true;
                Context context = getApplicationContext();
                CharSequence text = "Good Job! You've reached your goal!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateView();
            } finally {
                mHandler.postDelayed(mStatusChecker, 500);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        params1 = new Bundle();
        params1.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        tts = new TextToSpeech(this, this);
        Voice voiceobj = new Voice("it-it-x-kda#male_2-local", Locale.getDefault(), 1, 1, false, null);
        tts.setVoice(voiceobj);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                               @Override
                                               public void onStart(String s) {

                                               }

                                               @Override
            public void onDone(String utteranceId) {
                Log.d("YourActivity", "TtS Compeleted ");
                if (completed == true) {
                    startMainActivity();
                }
            }

                                               @Override
                                               public void onError(String s) {

                                               }
                                           });
        mEditText = (EditText) findViewById(R.id.editText);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);
        mSeries1 = new LineGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();

        mHandler = new Handler();
        startRepeatingTask();
    }
    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.describe(inputStream, 1);

        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }

    private void speakOut(String data) {

        tts.speak(data, TextToSpeech.QUEUE_FLUSH, params1, "UniqueID");
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopRepeatingTask();

        super.onDestroy();
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            mEditText.setText("");
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

                //mEditText.append("Image format: " + result.metadata.format + "\n");
                //mEditText.append("Image width: " + result.metadata.width + ", height:" + result.metadata.height + "\n");
                //mEditText.append("\n");
                if (result.description.captions.size() == 0) {
                    for (String tag: result.description.tags) {
                        mEditText.append("Tag: " + tag + "\n");
                        speakOut(tag);
                        break;
                    }
                } else {
                    for (Caption caption : result.description.captions) {
                        mEditText.append("Caption: " + caption.text);
                        speakOut(caption.text);
                        break;
                    }
                }
                //mEditText.append("\n");


                //doAnalyze();

                //mEditText.append("\n");

                //mEditText.append("\n--- Raw Data ---\n\n");
                //mEditText.append(data);
                //mEditText.setSelection(0);
                //camera.stopPreview();
            }

        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        openCamera();
        if (camera != null) {
            Camera.Parameters param;

            param = camera.getParameters();
            //param.setPreviewFrameRate(1);
            // modify parameter
            param.setPreviewSize(352, 288);
            List<int[]> getSupportedPreviewFpsRange = param.getSupportedPreviewFpsRange();
            param.setPreviewFpsRange(15000, 15000);
            List<String>    focusModes = param.getSupportedFocusModes();
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else
            if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            camera.setParameters(param);
            camera.setDisplayOrientation(270);
            try {
                // The Surface has been created, now tell the camera where to draw
                // the preview.
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
            } catch (Exception e) {
                // check for exceptions
                System.err.println(e);
                return;
            }
        }

    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (camera != null) {
            camera.setPreviewCallback(new PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {


                    int width = 0;
                    int height = 0;
                    if (camera != null) {
                        Camera.Parameters parameters = camera.getParameters();

                        height = parameters.getPreviewSize().height;

                        width = parameters.getPreviewSize().width;


                        ByteArrayOutputStream out = new ByteArrayOutputStream();

// Alter the second parameter of this to the actual format you are receiving
                        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
                        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

// bWidth and bHeight define the size of the bitmap you wish the fill with the preview image
                        byte[] bytes = out.toByteArray();
                        mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        //Log.d(TAG,"COunt is " + bitmap.getByteCount());
                        frameCounter++;
                        if (frameCounter % 45 == 0) {
                            Log.d(TAG, "One second completed");
                            doDescribe();
                            if (mStepCounter >=10.0) {
                                speakOut("You have achieved your goal");
                                completed = true;
                                //startMainActivity();

                            }
                            Log.d(TAG, "Step Count "+mStepCounter);
                        }
                    }
                    //doDescribe();

                }

            });
        }
    }

    void startMainActivity() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopRepeatingTask();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("FROM", FrameActivity.class.getSimpleName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


}

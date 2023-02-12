package com.example.saveimageinstorage;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

//AppCompatActivity
public class CapturePictureAutomatically extends MainActivity{
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Button bTakePicture, btnStopTakePicture, btnStartTakingPhotos,btnShowImage;
    PreviewView previewView;
    private ImageCapture imageCapture;
    Handler handler;
    Runnable runnable;
    Uri imageUri;
    OutputStream outputStream;
    ArrayList<Image> arrayList = new ArrayList<>();
    String timestamp, serverHostName, volume, language, object;
    TextToSpeech t1;
    ArrayList<String> result;
    Thread soundForListen;
    private int mode;
    boolean isrecognizable = false;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private TextToSpeech textToSpeech;
    MediaPlayer mediaPlayer;
    EditText editTextTextPersonName;
    String introductoryWords, instrucionWords;
    boolean callListening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture_automatically);
        // Влиза в режим на слушане за специални думи
        mode = 0;
        introductoryWords = "Добър ден Стартира се програма блайнд хелпър. Какво искате да " +
                "направя за вас. За да разберете повече, кажете думата Инструкции";
        instrucionWords = "Ако искате да намерите даден предмет, трябва да кажете думата намери и" +
                " след нея да кажете обекта, който търсите. Например казвате Намери човек." +
                "За да можете спрете търсенето на обект, кажете намерен е обекта. " +
                "Друга функция на приложението е да ви навигира, тоест да" +
                "каже какво има пред вас и да ви предупреди за него. За да влезете в този режим" +
                "трябва да кажете навигирай ме. В него например приложението ще Ви казва какво да " +
                "направите, ако има предмет пред вас, за да стигнете вървите безопасно напред. " +
                "За се спре навигирането, кажете спри навигиране. Като излезете от двата режима " +
                "програмата Ви слуша какво да прави. Но за да излезете от приложението, кажете " +
                "излез от програмата";


        bTakePicture = findViewById(R.id.bTakePicture);
        btnStopTakePicture = findViewById(R.id.btnStopTakePicture);
        btnStartTakingPhotos = findViewById(R.id.btnStartTakingPhotos);
        btnShowImage = findViewById(R.id.btnShowImage);
        editTextTextPersonName = findViewById(R.id.editTextTextPersonName);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);
         // no need to call prepare(); create() does that for you

        previewView = findViewById(R.id.previewView);


        /*t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    t1.setLanguage(Locale.ENGLISH);
                    //forLanguageTag("bg-BG")
                }
            }
        });*/
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.forLanguageTag("bg-BG"));

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        Log.i("TTS", "TextToSpeech initialized");
                    }
                    speak(introductoryWords);
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });




        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());
        MainActivity obj = new MainActivity();
        btnStartTakingPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Param is optional, to run task on UI thread.
                handler = new Handler(Looper.getMainLooper());
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Do the task...
                        capturePhoto();

                    /*    try {
                            obj.getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        Toast.makeText(CapturePictureAutomatically.this, "Започна да прави снимки", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this, 5000);
                        // Optional, to repeat the task
                    }
                };
                handler.postDelayed(runnable, 5000);

            }
        });


        btnStopTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop a repeating task like this.
                handler.removeCallbacks(runnable);
                Toast.makeText(CapturePictureAutomatically.this, "Успешно спряхте снимките", Toast.LENGTH_SHORT).show();
            }
        });
        bTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto();
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //int minVolume = 0;//audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, minVolume, 0);

                //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            }
        });
        btnShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //obj.getImages();
                startActivity(new Intent(CapturePictureAutomatically.this, MainActivity.class));
                //Uri imagePath = createImage();
                //Intent intent = new Intent();
                //intent.putExtra(MediaStore.EXTRA_OUTPUT,imagePath);
            }
        });
        //Toast.makeText(obj, "slusha me", Toast.LENGTH_SHORT).show();

        //Toast.makeText(obj, "slusha me", Toast.LENGTH_SHORT).show();
        //speakText();
        callListening = true;
        //Toast.makeText(this, "introductoryWords="+introductoryWords\, Toast.LENGTH_SHORT).show();

        //textToSpeech.speak(introductoryWords, TextToSpeech.QUEUE_FLUSH, null);
        soundForListen = new Thread(new Runnable(){
            public void run(){
                while(true){
                    if(mode==0){

                        if(!textToSpeech.isSpeaking()){
                            //soundForListen.interrupt();
                            //soundForListen.stop();
                            mediaPlayer.start();
                            try {
                                Thread.sleep(2000);
                                mediaPlayer.setLooping(true);

                                Thread.sleep(5000);
                                //mediaPlayer.setLooping(false);
                                //speakText();
                                callListening = true;
                                //startSpeechRecognition();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            mode = 1;
                        }
                    }



                }
            }
        });
        //soundForListen.start();
        mode = 1;
        if(callListening){
            mode = 1;
            startSpeechRecognition();
            Toast.makeText(this, "VLizza vo govor", Toast.LENGTH_SHORT).show();
        }

    }

    private void startSpeechRecognition() {
        editTextTextPersonName.setText("mode="+mode);
        //Toast.makeText(this, "VLiza v startSpeechRecognition()", Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "VLiza v startSpeechRecognition()", Toast.LENGTH_SHORT).show();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        //Set up the intent for speech recognition
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);


        // Start listening for speech


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                //editTextTextMultiLine.setText("Pochva da slusha");
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
               // editTextTextMultiLine.setText("END");

            }

            @Override
            public void onError(int i) {
                //editTextShowText.setText("Error " + i);
                speechRecognizer.startListening(recognizerIntent);
                //speechRecognizer.startListening(recognizerIntent);
            }

            @Override
            public void onResults(Bundle results) {
                //editTextShowText.setText(" ");
                // editTextTextMultiLine.setText("");
                result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //editTextTextMultiLine.setText("f"+ result.get(result.size() - 1));
                //File file = new File("res/");

                // Влиза, когато са се прочели въвеждащите думи
                if(mode == 1){
                    //mediaPlayer.start();

                    Toast.makeText(CapturePictureAutomatically.this, "Vliza v mode 1", Toast.LENGTH_SHORT).show();

                    if(result.contains("инструкции")){
                        Toast.makeText(CapturePictureAutomatically.this, "vliza v proverka", Toast.LENGTH_SHORT).show();
                        speak(instrucionWords);
                        //
                        //Toast.makeText(CapturePictureAutomatically.this, "Vliza v razpoznat", Toast.LENGTH_SHORT).show();
                        //speak("Здравейте, почвам да ви слушам");
                        editTextTextPersonName.setText("result"+result);
                        //mode++;
                        //soundForListen.stop();
                        soundForListen.interrupt();
                    }else if(result.contains("навигирай ме")){
                        object = "all";
                        mode = 3;

                        handler = new Handler(Looper.getMainLooper());
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                // Do the task...

                                capturePhoto();
                                //Toast.makeText(CapturePictureAutomatically.this, "Започна да прави снимки", Toast.LENGTH_SHORT).show();
                                handler.postDelayed(this, 5000);
                                // Optional, to repeat the task
                            }
                        };
                        handler.postDelayed(runnable, 5000);

                    }else{

                        AssetManager assetManager = getAssets();
                        try{
                            InputStream inputStream = assetManager.open("recognizableoObjects-bg.txt");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("windows-1251")));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                // do something with the line
                                //result.contains("намери "+ line)
                                if(result.contains("намери "+ line)){
                                    isrecognizable = true;
                                    break;

                                }
                                //editTextTextMultiLine.setText(line+" ");

                                //Toast.makeText(this, "lin="+line, Toast.LENGTH_SHORT).show();
                            }

                            //editTextTextMultiLine.setText("isrecognizable= "+isrecognizable);

                            if(result.contains("инструкции")){
                                Toast.makeText(CapturePictureAutomatically.this, "vliza v proverka", Toast.LENGTH_SHORT).show();
                                speak(instrucionWords);
                                //
                                //Toast.makeText(CapturePictureAutomatically.this, "Vliza v razpoznat", Toast.LENGTH_SHORT).show();
                                //speak("Здравейте, почвам да ви слушам");
                                editTextTextPersonName.setText("result"+result);

                                //soundForListen.stop();
                                soundForListen.interrupt();
                            }

                            if(isrecognizable == true){
                                object = line;
                                soundForListen.interrupt();
                                //soundForListen.stop();
                                mediaPlayer.setLooping(false);
                                speak("Този предмет може да се открие. Почва търсене");
                                // Param is optional, to run task on UI thread.
                                handler = new Handler(Looper.getMainLooper());
                                runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        // Do the task...

                                        capturePhoto();
                                        //Toast.makeText(CapturePictureAutomatically.this, "Започна да прави снимки", Toast.LENGTH_SHORT).show();
                                        handler.postDelayed(this, 5000);
                                        // Optional, to repeat the task
                                    }
                                };
                                handler.postDelayed(runnable, 5000);

                                mode ++;
                                //speak("Този предмет може да се открие");
                            }else{
                                speak("Този предмет не може да се открие");
                            }
                            reader.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                    }


                    //Toast.makeText(CapturePictureAutomatically.this, "Vliza v mode 2", Toast.LENGTH_SHORT).show();

                }else if(mode == 2){
                    Toast.makeText(CapturePictureAutomatically.this, "V rejim tursene", Toast.LENGTH_SHORT).show();
                    if(result.contains("намерен е обекта")) {
                        handler.removeCallbacks(runnable);
                        speak("Спряхте режим търсене на обект. Какво искате да направя за вас?");
                        mediaPlayer.setLooping(true);
                        Toast.makeText(CapturePictureAutomatically.this, "namereno", Toast.LENGTH_SHORT).show();
                        mode = 1;
                    }else if(result.contains("спри търсене")){

                    }

                }else if(mode == 3){
                    if(result.contains("спри навигиране")) {
                        handler.removeCallbacks(runnable);
                        mediaPlayer.setLooping(true);
                        speak("Спряхте режим навигиране. Какво искате да направя за вас?");
                        mode = 1;
                    }

                }
                if(result.contains("излез от програмата")) {
                    System.exit(0);
                }
                speechRecognizer.startListening(recognizerIntent);
                //Toast.makeText(MainActivity.this, "Tova e rezultata: "+result.get(0), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String match = matches.get(matches.size() - 1);
                    //if(match.length() > 12) match = match.substring(match.length() - 12);
                   // editTextTextMultiLine.setText("p"+ match);
/*                    Toast.makeText(MainActivity.this, "Vliza v onPartialResults", Toast.LENGTH_SHORT).show();
                    if(mode == 1){
                        Toast.makeText(MainActivity.this, "Vliza v mode 1", Toast.LENGTH_SHORT).show();
                        if(matches.contains("хей помощник") || matches.contains("hey pomoshtnik")){
                            Toast.makeText(MainActivity.this, "Vliza v razpoznat", Toast.LENGTH_SHORT).show();
                            speak("Здравейте, почвам да ви слушам");
                            mode++;
                        }
                    }else if (mode == 2){
                        Toast.makeText(MainActivity.this, "Vliza v mode 2", Toast.LENGTH_SHORT).show();
                        boolean isrecognizable = false;
                        AssetManager assetManager = getAssets();
                        try{
                            InputStream inputStream = assetManager.open("recognizableoObjects-bg.txt");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,Charset.forName("windows-1251")));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                // do something with the line
                                if(matches.contains(line)){
                                    isrecognizable = true;
                                    break;

                                }
                                editTextTextMultiLine.setText(line+" ");
                                //Toast.makeText(this, "lin="+line, Toast.LENGTH_SHORT).show();
                            }
                            editTextTextMultiLine.setText("isrecognizable="+isrecognizable);
                            if(isrecognizable){
                                textToSpeech.speak("Този предмет може да се открие", TextToSpeech.QUEUE_FLUSH, null);
                                mode++;
                                //speak("Този предмет може да се открие");
                            }else{
                                textToSpeech.speak("Този предмет не може да се открие", TextToSpeech.QUEUE_FLUSH, null);
                                //speak("Този предмет не може да се открие");
                            }
                            reader.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }


                        speak("Здравейте, почвам да ви слушам");

                    }


                    /*for(String s : matches) {
                        editTextShowText.setText("word: "+s);
                    }*/
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
        speechRecognizer.startListening(recognizerIntent);
    }
    private void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void speakText() {
        // intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi speak something");
        // start intent

        try{
            // show dialog
            startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
        }catch (Exception e){

            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }



    // recieve voice input and handle it
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT:{
                if(resultCode == RESULT_OK && data != null){
                    // get text array from voice intent
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (result.contains("snimai") || result.contains("снимай")) {
                        capturePhoto();
                        soundForListen.interrupt();

                    }

                    // set a text view
                    //editTextShowText.setText(result.get(0));



                }
                break;
            }

        }
    }

    private class NetworkRequestTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... params) {
            byte[] imageData = params[0];

            URL url = null;
            HttpURLConnection client = null;
            OutputStream outputPost = null;
            try {
            //http://46.10.208.174:8033
                url = new URL("http://46.10.208.174:8033/?word="+object+"&lang=bg");//"http://46.10.208.174:8033?word=" + URLEncoder.encode("wefewf", StandardCharsets.UTF_8.name()));
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setDoInput(true);
                client.setDoOutput(true);
                outputPost = new BufferedOutputStream(client.getOutputStream());

                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                outputPost.write(imageBytes);
                outputPost.flush();
                String result = "";
                if (client.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            result += line + "\r\n";
                        }
                    }
                }

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //String response = new String(result, "UTF-8");
            //result = "ЕХО как е";
            Toast.makeText(CapturePictureAutomatically.this, result, Toast.LENGTH_SHORT).show();
            Toast.makeText(CapturePictureAutomatically.this, "vliza samo vuv funkciqta", Toast.LENGTH_SHORT).show();
            //result = "";
//            if(result.equalsIgnoreCase("")){
//                Toast.makeText(CapturePictureAutomatically.this, "Vliza v nishto", Toast.LENGTH_SHORT).show();
//                textToSpeech.speak("Нищо не се разпознава", TextToSpeech.QUEUE_FLUSH,null);
            //}else{
                textToSpeech.speak(result, TextToSpeech.QUEUE_ADD,null);
            //}


        }
    }

    private void capturePhoto() {
        /*File photoDir = new File("/storage/emulated/0/111");
        if(!photoDir.exists()){
            photoDir.mkdir();
        }
        Date date = new Date();*/
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        //Toast.makeText(this, " " + str, Toast.LENGTH_SHORT).show();
        String timestamp = "blindHelper-" + str;// +System.currentTimeMillis();//+ Integer.parseInt(String.valueOf(now));

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        /*String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";
        File photoFile = new File(photoFilePath);*/

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(result).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CapturePictureAutomatically.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();

                        new NetworkRequestTask().execute(result.toByteArray());
                        //new NetworkRequestTask().execute("човек".toByteArray());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        //Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
        Toast.makeText(this, " " + imageCapture, Toast.LENGTH_LONG).show();


    }

    /*private void capturePhoto() {
        /*File photoDir = new File("/storage/emulated/0/111");

        if(!photoDir.exists()){
            photoDir.mkdir();
        }
        Date date = new Date();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        Toast.makeText(this, " " + str, Toast.LENGTH_SHORT).show();
        timestamp = "blindHelper-" + str + "-" + System.currentTimeMillis();// +System.currentTimeMillis();//+ Integer.parseInt(String.valueOf(now));

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        /*String photoFilePath = photoDir.getAbsolutePath() + "/" + timestamp + ".jpg";

        File photoFile = new File(photoFilePath);


        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        Toast.makeText(CapturePictureAutomatically.this, "Photo has been saved successfully", Toast.LENGTH_SHORT).show();
                        try {
                            getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        //Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
        Toast.makeText(this, " " + imageCapture, Toast.LENGTH_LONG).show();


    }*/

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        //Camera Selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);


    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    public void getImages() throws IOException {
        arrayList.clear();
//        Toast.makeText(this, "Vikat me", Toast.LENGTH_SHORT).show();
        String filepaths = "/storage/emulated/0/Pictures";
        //Environment.getExternalStorageDirectory() + "/Pictures";
        File file = new File(filepaths);
        Toast.makeText(this, "file="+file, Toast.LENGTH_SHORT).show();
        File[] files = file.listFiles();
        Toast.makeText(this, "file.listFiles()="+file.listFiles(), Toast.LENGTH_SHORT).show();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });
        //Bitmap bitmap = null;
       /* if(files != null){
            for(File file1: files){
                if((file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) && file1.getPath().contains("blindHelper")){
                    FileInputStream fis = null;
                    String filePath = file1.getPath();
                    //String filePath = filepaths + "/"+timestamp+".jpg";
                    Toast.makeText(CapturePictureAutomatically.this, "------------------filePath="+filepaths+"/"+timestamp, Toast.LENGTH_SHORT).show();
                    Toast.makeText(CapturePictureAutomatically.this, "filePath-file11111="+file1.getPath(), Toast.LENGTH_SHORT).show();
                    try {
                        fis = new FileInputStream(filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);

                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    arrayList.add(new Image(file1.getName(),file1.getPath(),file1.length()));
                    //Toast.makeText(this, " file1.getPath()="+file1.getPath()+"; file1.getName()="+file1.getName(), Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(file1.getPath());

                    /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if(bitmap != null){
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        final String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);

                        // Make Post method
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url ="http://bgroutingmap.com/_testGetImage/test55.php";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        Toast.makeText(CapturePictureAutomatically.this, "response="+response, Toast.LENGTH_LONG).show();
                                        if(response.contains("success")){
                                            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Failed to upload image", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }){
                            protected Map<String, String> getParams(){
                                Map<String, String> paramV = new HashMap<>();
                                paramV.put("image", base64Image);
                                return paramV;
                            }
                        };
                        queue.add(stringRequest);
                    }else{
                        Toast.makeText(CapturePictureAutomatically.this, "Select the image first", Toast.LENGTH_SHORT).show();
                    }

                    break;

                }
            }

        }*/

        ImageAdapter adapter = new ImageAdapter(CapturePictureAutomatically.this,arrayList);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListner((view, path) -> startActivity(new Intent(CapturePictureAutomatically.this, ImageViewTarget.class).putExtra("image",path)));
        Bitmap bitmap;
        //bitmap = new Bitmap(file1.getName(),file1.getPath(),file1.length());
        Uri uri = Uri.parse(arrayList.get(0).getPath());
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        Toast.makeText(this, ""+uri, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, ""+arrayList.get(0).getPath(), Toast.LENGTH_SHORT).show();
        //bitmap = arrayList.get(0);



    }

}
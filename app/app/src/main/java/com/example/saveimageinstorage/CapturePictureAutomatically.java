package com.example.saveimageinstorage;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.speech.tts.UtteranceProgressListener;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.common.util.concurrent.ListenableFuture;

import org.chromium.base.Callback;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.text.SimpleDateFormat;

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
    private int mode, intervalPhoto;
    boolean isrecognizable = false;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private TextToSpeech textToSpeech;
    MediaPlayer mediaPlayer;
    EditText editTextTextMultiLine;
    String introductoryWords, instrucionWords;
    boolean callListening;
    HttpURLConnection client;
    boolean recognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture_automatically);
        // Влиза в режим на слушане за специални думи
        recognize = true;
        intervalPhoto = 100;
        mode = 0;
        introductoryWords = "Добър ден Стартира се програма блайнд хелпър. Какво искате да " +
                "направя за вас. За да разберете повече, кажете думата Инструкции";
        instrucionWords = "Ако искате да намерите даден предмет, трябва да кажете думата намери и" +
                " след нея да кажете обекта, който търсите. Например казвате Намери човек." +
                "За да можете спрете търсенето на обект, кажете намерен е обекта. " +
                "Друга функция на приложението е да ви напътства безопасно, тоест да" +
                "каже какво има пред вас и да ви предупреди за него. За да влезете в този режим" +
                "трябва да кажете навигирай ме. В него например приложението ще Ви казва какво да " +
                "направите, ако има предмет пред вас, за да стигнете вървите безопасно напред. " +
                "За се спре напътстването, кажете спри навигиране. Като излезете от двата режима " +
                "програмата Ви слуша какво да прави. Но за да излезете от приложението, кажете " +
                "излез от програмата";


        bTakePicture = findViewById(R.id.bTakePicture);
        btnStopTakePicture = findViewById(R.id.btnStopTakePicture);
        btnStartTakingPhotos = findViewById(R.id.btnStartTakingPhotos);
        btnShowImage = findViewById(R.id.btnShowImage);
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);
         // no need to call prepare(); create() does that for you

        previewView = findViewById(R.id.previewView);

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


        callListening = true;

        /* Когато се казват първоначалните инструкции, програмата чака докато не свършат и тогава
         започва да слуша.*/
        soundForListen = new Thread(new Runnable(){
            public void run(){
                while(true){
                    if(mode==0){
                        //Toast.makeText(obj, "VLizza v Thread", Toast.LENGTH_SHORT).show();
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
                                //Toast.makeText(obj, "Minava ot tuk", Toast.LENGTH_SHORT).show();
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
        runnable = new Runnable() {
            @Override
            public void run() {
                // Do the task...
                capturePhoto();

                //Toast.makeText(CapturePictureAutomatically.this, "Започна да прави снимки", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, intervalPhoto);
            }
        };
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, intervalPhoto);

        //soundForListen.start();
        //mode = 1;
        if(callListening){
            mode = 1;

            startSpeechRecognition();

            //Toast.makeText(this, "VLizza vo govor", Toast.LENGTH_SHORT).show();
        }

    }
    void log(String text) {
        editTextTextMultiLine.setText(text + " "+ editTextTextMultiLine.getText());
    }

    private static class Command {
        public final String[] templates;
        public String param;

        public Command(String[] templates) {
            this.templates = templates;
        }

        public boolean match(String text) {
            for(String template : templates) {

                if(text.contains(template)){

                    return true;
                }
            }
            return false;
        }
    }

    private static final Command commandStop = new Command(new String[] { "стоп", "спри", "стига" });
    private static final Command commandInstructions = new Command(new String[] { "инструкции", "помощ" });
    private static final Command commandFind = new Command(new String[] {"намери", "къде e" });
    private static final Command commandFindAll = new Command(new String[] { "навигирай" });
    private static final Command[] commands = new Command[] {commandInstructions, commandFind};

//    private static Command findCommand(String text) {
//        for(Command command : commands) {
//            if(command.match(text)) return command;
//        }
//        return null;
//    }

    // Функция, която слуша за говор
    private void startSpeechRecognition() {
        log("mode="+mode);
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

            }

            @Override
            public void onError(int i) {

                speechRecognizer.startListening(recognizerIntent);
                //speechRecognizer.startListening(recognizerIntent);
            }

            // Когато свърши записа влиза в onResults и се анализират думите
            @Override
            public void onResults(Bundle results) {

                result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(result.size() == 0) return;
                String lastCommand = result.get(result.size() - 1).toLowerCase();
//                Command command = findCommand(lastCommand);
                if(commandFind.match(lastCommand)){
                    object = lastCommand;
                    recognize = true;
                    new CheckRecognize().execute(" ");

                }

                log("/" + lastCommand + "/");
                //editTextTextMultiLine.setText("f"+ result.get(result.size() - 1));
                if(commandInstructions.match(lastCommand)) {
                    captureRunning = false;
                    mode = 1;
                    log("instructions");
                    speak(instrucionWords);
                }

                if(commandStop.match(lastCommand)){
                    recognize = true;
                    captureRunning = false;
                    speak("Спряхте режима. Какво искате да направя за вас?");
                }

                if(commandFindAll.match(lastCommand)){
                    speak("Пускане на навигация.");
                    captureRunning = true;
                    object = "all";
                    recognize = false;
                }


                // Влиза, когато са се прочели въвеждащите думи
//                if(mode == 1){
//                    //mediaPlayer.start();
//
//                    //Toast.makeText(CapturePictureAutomatically.this, "Vliza v mode 1", Toast.LENGTH_SHORT).show();
//
//                    if(result.contains("инструкции")){
//                        log("instructions");
//                        //Toast.makeText(CapturePictureAutomatically.this, "vliza v proverka", Toast.LENGTH_SHORT).show();
//                        speak(instrucionWords);
//
//                        soundForListen.interrupt();
//                    }else if(result.contains("навигирай ме")){
//                        log("navigate");
//                        object = "all";
//                        captureRunning = true;
//
//                        mode = 3;
//
//                    }else{
//                        log("yes");
//                        captureRunning = true;
//                        mediaPlayer.setLooping(false);
//                        mode = 2;
//                    }
//
//                }else if(mode == 2){
//                    //Toast.makeText(CapturePictureAutomatically.this, "V rejim tursene mode ="+mode, Toast.LENGTH_SHORT).show();
//                     if(result.contains("спри търсене")){
//                         log("stop1");
//                         captureRunning = false;
//                        speak("Спряхте режим търсене на обект. Какво искате да направя за вас?");
//                        mediaPlayer.setLooping(true);
//                        mode = 1;
//
//                    }
//
//                }else if(mode == 3){
//                    //Toast.makeText(CapturePictureAutomatically.this, "V navigirane mode ="+mode, Toast.LENGTH_SHORT).show();
//                    if(result.contains("спри навигиране")) {
//                        log("stop2");
//                        captureRunning = false;
//                        mediaPlayer.setLooping(true);
//                        speak("Спряхте режим навигиране. Какво искате да направя за вас?");
//                        mode = 1;
//                    }
//
//                }
                if(result.contains("излез от програмата")) {
                    log("exit");
                    System.exit(0);
                }
                speechRecognizer.startListening(recognizerIntent);
                //Toast.makeText(MainActivity.this, "Tova e rezultata: "+result.get(0), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

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




    private class NetworkRequestTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... params) {
            byte[] imageData = params[0];

            URL url = null;
            client = null;
            OutputStream outputPost = null;
            try {
                url = new URL("http://46.10.208.174:8033/?word="+object+"&lang=bg");//"http://46.10.208.174:8033?word=" + URLEncoder.encode("wefewf", StandardCharsets.UTF_8.name()));

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setDoInput(true);
                client.setDoOutput(true);
                outputPost = new BufferedOutputStream(client.getOutputStream());

                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                outputPost.write(imageBytes);
                outputPost.flush();
                /*outputPost = new BufferedOutputStream(client.getOutputStream());

                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                outputPost.write(imageBytes);
                outputPost.flush();*/
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
                log("ex2");
                e.printStackTrace();
            } catch (ProtocolException e) {
                log("ex3");
                e.printStackTrace();
            } catch (IOException e) {
                log("ex4");
                e.printStackTrace();
            }
            log("err");
            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            textToSpeech.speak(result, TextToSpeech.QUEUE_ADD,null);

        }
    }

    private class CheckRecognize extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //String imageData = params[0];

            URL url = null;
            client = null;
            OutputStream outputPost = null;
            try {
                url = new URL("http://46.10.208.174:8033/?word="+object+"&lang=recognizable");//"http://46.10.208.174:8033?word=" + URLEncoder.encode("wefewf", StandardCharsets.UTF_8.name()));

                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("POST");
                client.setDoInput(true);
                client.setDoOutput(true);
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
                log("ex2");
                e.printStackTrace();
            } catch (ProtocolException e) {
                log("ex3");
                e.printStackTrace();
            } catch (IOException e) {
                log("ex4");
                e.printStackTrace();
            }
            log("err");
            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.contains("true")){
                log("TRUE");
                captureRunning = true;
                recognize = false;
                speak("Обектът може да бъде разпознат.");
            }else if(result.contains("false")){
                log("FALSE");
                captureRunning = false;
                speak("Обектът не може да бъде разпознат. Опитайте да потърсите нещо друго");
            }

        }
    }


    private boolean captureRunning = false;
    //private boolean captureInProgress = false;
    // Влиза тук за да прави снимка, която директно се праща без да се записва на съответното устройство
    private void capturePhoto() {
        // Влиза когато се е върнал отговор от сървъра или първоначално, когато започва да снима
        if(((textToSpeech != null) && textToSpeech.isSpeaking()) || !captureRunning) return; //|| captureInProgress) return;
        log("c");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        captureRunning = false;
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(result).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                        new NetworkRequestTask().execute(result.toByteArray());

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        captureRunning = false;
                        log("onerror");
                        //Toast.makeText(MainActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );
        //Toast.makeText(this, " " + imageCapture, Toast.LENGTH_LONG).show();


    }

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
}
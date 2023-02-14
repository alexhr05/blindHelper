package com.example.saveimageinstorage;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

//AppCompatActivity
public class CapturePictureAutomatically extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private ImageCapture imageCapture;
    Handler handler;
    Runnable runnable;
    String object;
    ArrayList<String> result;
    Thread soundForListen;
    private int mode, intervalPhoto;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private TextToSpeech textToSpeech;
    MediaPlayer mediaPlayer;
    EditText editTextTextMultiLine;
    String introductoryWords, instrucionWords;
    boolean callListening;
    HttpURLConnection client;
    boolean recognize;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isAudioRecordPermissionGranted = false;
    private boolean isCameraPermissionGranted = false;
    private boolean isStoragePermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture_automatically);

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.RECORD_AUDIO) != null){
                    isAudioRecordPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));
                    log("Minava prez AudioRecord");
                }

                if(result.get(Manifest.permission.CAMERA) != null){
                    isCameraPermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
                    log("Minava prez Camera");
                }

                if(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null){
                    log("Minava prez WriteExternal");
                    isStoragePermissionGranted = Boolean.TRUE.equals(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
                }
            }
        });
        requestPermission();
        // Влиза в режим на слушане за специални думи
        recognize = true;
        intervalPhoto = 700;
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


        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);

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

        // Осигуряване на "жизнения цикъл" на камерата
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
//        MainActivity obj = new MainActivity();


        callListening = true;

        /* Когато се казват първоначалните инструкции, програмата чака докато не свършат и тогава
         започва да слуша.*/
        soundForListen = new Thread(new Runnable(){
            public void run(){
                while(true){
                    if(mode==0){
                        if(!textToSpeech.isSpeaking()){
                            mediaPlayer.start();
                            try {
                                Thread.sleep(2000);
                                mediaPlayer.setLooping(true);

                                Thread.sleep(2000);

                                callListening = true;
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
                // Извиква се функцията през известно време
                capturePhoto();
                handler.postDelayed(this, intervalPhoto);
            }
        };
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, intervalPhoto);

        if(callListening){
            mode = 1;

            startSpeechRecognition();

        }

    }
    private void requestPermission(){
        isAudioRecordPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
        isStoragePermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
        List<String> permissionRequest = new ArrayList<String>();

        if(!isAudioRecordPermissionGranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        if(!isCameraPermissionGranted){
            permissionRequest.add(Manifest.permission.CAMERA);
        }

        if(!isStoragePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }


    }


    void log(String text) {
        editTextTextMultiLine.setText(text + " "+ editTextTextMultiLine.getText());
    }

    private static class Command {
        public final String[] templates;
//        public String param;

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

    // Това са шаблони за какви команди могат да се разпознаят

    private static final Command commandStop = new Command(new String[] { "стоп", "спри", "стига" });
    private static final Command commandInstructions = new Command(new String[] { "инструкции", "помощ" });
    private static final Command commandFind = new Command(new String[] {"намери", "къде e" });
    private static final Command commandFindAll = new Command(new String[] { "навигирай" });
    private static final Command commandExit = new Command(new String[] { "излез от програмата" , "излез" });


    // Функция, която слуша за говор
    private void startSpeechRecognition() {
        //log("mode="+mode);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        // Настройване на Intent за разпознаване на реч
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);


        // Start listening for speech


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

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
            }

            // Когато свърши записа влиза в onResults и се анализират думите
            @Override
            public void onResults(Bundle results) {

                result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(result.size() == 0) return;
                String lastCommand = result.get(result.size() - 1).toLowerCase();

                // Влиза тук когато се каже човекът каже, че иска да му се намери даден обект
                if(commandFind.match(lastCommand)){
                    object = lastCommand;
                    recognize = true;
                    new CheckRecognize().execute(" ");

                }
                // Ако е казано "инструкции", казва на човека какви функционалности има приложението
                //log("/" + lastCommand + "/");
                if(commandInstructions.match(lastCommand)) {
                    captureRunning = false;
                    mode = 1;
  //                  log("instructions");
                    speak(instrucionWords);
                }
                // Спира приложението да снима
                if(commandStop.match(lastCommand)){
                    recognize = true;
                    captureRunning = false;
                    speak("Спряхте режима. Какво искате да направя за вас?");
                }

                // Влиза тук когато се каже думата навигация и започва да ориентира човека какво
                // има пред него
                if(commandFindAll.match(lastCommand)){
                    speak("Пускане на навигация.");
                    captureRunning = true;
                    object = "all";
                    recognize = false;
                }
                // Влиза тук когато потребителят иска да излезе от програмата
                if(commandExit.match(lastCommand)){
                    System.exit(0);
                }

                speechRecognizer.startListening(recognizerIntent);
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

    private class NetworkRequestTask extends AsyncTask<byte[], Void, String> {

        @Override
        protected String doInBackground(byte[]... params) {
            byte[] imageData = params[0];

            URL url = null;
            client = null;
            OutputStream outputPost = null;
            // Прави заявка към сървъра със направената снимка и обектът, който иска да се намери или
            // да знае сървъра, че иска да се навигира само човека
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
//                log("ex2");
                e.printStackTrace();
            } catch (ProtocolException e) {
//                log("ex3");
                e.printStackTrace();
            } catch (IOException e) {
//                log("ex4");
                e.printStackTrace();
            }
//            log("err");
            return "Грешка";
        }

        // Връща отговор от сървърът
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(recognize) return;

//            log("заявка отговор");
            textToSpeech.speak(result, TextToSpeech.QUEUE_ADD,null);
            captureRunning = true;
            capturePhoto();

        }
    }

    private class CheckRecognize extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            URL url = null;
            client = null;

            // Прави се заявката само когато искаме да видим дали предметът, който търси може да
            // бъде разпознат и ако може се изпълнява само по-горната заявка
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
//                log("ex2");
                e.printStackTrace();
            } catch (ProtocolException e) {
//                log("ex3");
                e.printStackTrace();
            } catch (IOException e) {
//                log("ex4");
                e.printStackTrace();
            }
//            log("err");
            return "ERROR";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Ако може да се разпознава, връща отговор true, ако не може, връща отговор false
            if(result.contains("true")){
                //log("TRUE");
                captureRunning = true;
                recognize = false;
                speak("Обектът може да бъде разпознат.");
            }else if(result.contains("false")){
                //log("FALSE");
                captureRunning = false;
                speak("Обектът не може да бъде разпознат. Опитайте да потърсите нещо друго");
            }

        }
    }


    private boolean captureRunning = false;

    // Влиза тук за да прави снимка, която директно се праща без да се записва на съответното устройство
    private void capturePhoto() {
        // Влиза когато се е върнал отговор от сървъра или първоначално, когато започва да снима
        //log(" "+captureRunning);
        if(((textToSpeech != null) && textToSpeech.isSpeaking()) || !captureRunning) return;
        //log("c");

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
                        captureRunning = true;
                        //log("onerror");
                        Toast.makeText(CapturePictureAutomatically.this, "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }


        );

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
package com.example.saveimageinstorage;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<Image> arrayList = new ArrayList<>();

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        try {
                            getImages();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
    );


    private static int REQUEST_CODE = 100;

    Button createButton;
    OutputStream outputStream;
    ImageView board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item_show);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setHasFixedSize(true);

        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }else{
            /*try {
                getImages();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        }
        //TODO Kak Python да върне резултат на телефона

        //createButton = findViewById(R.id.btncreate);
        //board = findViewById(R.id.board);


/*        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                saveImages();
                //getPermission();
            }


        });*/
    }
    public void getImages() throws IOException {
        arrayList.clear();
//        Toast.makeText(this, "Vikat me", Toast.LENGTH_SHORT).show();
        String filepath = "/storage/emulated/0/Pictures";
        //Environment.getExternalStorageDirectory() + "/Pictures";
        File file = new File(filepath);
        Toast.makeText(this, "file="+file, Toast.LENGTH_SHORT).show();
        File[] files = file.listFiles();
        Toast.makeText(this, "file.listFiles()="+file.listFiles(), Toast.LENGTH_SHORT).show();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String str = formatter.format(date);
        //Bitmap bitmap = null;
        if(files != null){
            for(File file1: files){
                if((file1.getPath().endsWith(".png") || file1.getPath().endsWith(".jpg")) && file1.getPath().contains("blindHelper")){
                    FileInputStream fis = null;
                    String filePath = file1.getPath();
                    Toast.makeText(MainActivity.this, "filePath="+filePath, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, " file1.getPath()="+file1.getPath()+"; file1.getName()="+file1.getName(), Toast.LENGTH_SHORT).show();
                    Uri uri = Uri.parse(file1.getPath());

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
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

                                        Toast.makeText(MainActivity.this, "response="+response, Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainActivity.this, "Select the image first", Toast.LENGTH_SHORT).show();
                    }

                    break;

                }
            }

        }

        ImageAdapter adapter = new ImageAdapter(MainActivity.this,arrayList);

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListner((view, path) -> startActivity(new Intent(MainActivity.this, ImageViewTarget.class).putExtra("image",path)));
        Bitmap bitmap;
        //bitmap = new Bitmap(file1.getName(),file1.getPath(),file1.length());
        Uri uri = Uri.parse(arrayList.get(0).getPath());
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        Toast.makeText(this, ""+uri, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, ""+arrayList.get(0).getPath(), Toast.LENGTH_SHORT).show();
        //bitmap = arrayList.get(0);



    }
    /*createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                            startActivityIfNeeded(intent, 101);

                        } catch (Exception exception) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityIfNeeded(intent, 101);

                        }
                    }

                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveImages();
                } else {
                    askPermission();
                }
            }


        });*/

    /*private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImages();
            } else {
                Toast.makeText(this, "Please provide the required permissions", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }*/
    // Vmukvane na snimki v internal storage
/*    private void saveImages() {
        File dir = null;
        Toast.makeText(this, "Vliza V SAveIMAGES", Toast.LENGTH_SHORT).show();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Toast.makeText(this, "Vliza vuv IF", Toast.LENGTH_SHORT).show();
            dir = new File(Environment.getExternalStorageDirectory(), "/SaveImages");

            Toast.makeText(this, " " + Environment.getExternalStorageDirectory(), Toast.LENGTH_SHORT).show();
            if (!dir.exists()) {
                dir.mkdir();
                Toast.makeText(this, "Pravi papka", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, "Създава нова директория", Toast.LENGTH_SHORT).show();
            }
            BitmapDrawable drawable = (BitmapDrawable) board.getDrawable();
            Bitmap bitmap = drawable.getBitmap();


            File file = new File(dir, System.currentTimeMillis() + ".jpg");
            try {
                outputStream = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show();

            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }*/


    // Vmukvane na fail v internal storage
    /*private void getPermission () {
        Toast.makeText(this, "VLiza v Get Permission", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Toast.makeText(this, "VLiza vuv If", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

            String dataFileName = "file.txt";
            String dataFileContent = "Hello friend";

            //String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Chocolate";
            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Save Images";

            File dir = new File(fullPath);
//
            try {

                if(!dir.exists()){
                    dir.mkdir();
                    // vliza v if
                    Toast.makeText(this, " " + dir, Toast.LENGTH_SHORT).show();
                } else{
                    // NE MOZE da vleze
                    Toast.makeText(this, " Ne vliza v exist", Toast.LENGTH_SHORT).show();
                }


                if(dir.exists()) {
                    // dir sashtevuva
                    Toast.makeText(this, "Dir Sushtestvuva", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            File contentFileName = new File(dir, dataFileName);

            try {
                FileOutputStream stream = new FileOutputStream(contentFileName);
                stream.write(dataFileContent.getBytes());
                Toast.makeText(this, "Dir created", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }*/
}
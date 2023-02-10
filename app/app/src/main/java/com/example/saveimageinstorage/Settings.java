package com.example.saveimageinstorage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    EditText serverHostName;
    EditText volume;
    EditText language;
    Button saveBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeInConfigFile();
            }
        });


    }

    public void writeInConfigFile() {

        serverHostName = findViewById(R.id.serverHostName);
        volume = findViewById(R.id.volume);
        language = findViewById(R.id.language);
        String customIinformation = "Server: "+serverHostName.getText().toString()+"\nVolume: "+volume.getText().toString()+"\nLanguage: "+language.getText().toString();
        File file = new File(getFilesDir(), "System.conig.txt");
        FileOutputStream outputStream;
        if (!file.exists()) {

            String defaultSettins = "Server: 192.168.1.3\nVolume: 5\nLanguage: BG-bg";
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(defaultSettins.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
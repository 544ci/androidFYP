package com.example.fyp_android;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AddSMSKeyword extends AppCompatActivity {
    public static final String SHARED_PREFS = "KeywordPrefs";
    public static final String KEYWORD_PREF = "SMSKeyword";
    TextView textView;
    TextView textView1;

    File file;
    File tmp;
    Encryption en = new Encryption(this, new EncryptionListener() {
        @Override
        public void onSDCardEncryptionComplete() {
            makeToast("gg");
        }

        @Override
        public void onSDCardEncryptionFailed(String errMessage) {
            makeToast(errMessage);

        }

        @Override
        public void onSDCardDecryptionComplete() {
            makeToast("gg");

        }

        @Override
        public void onSDCardDecryptionFailed(String errMessage) {
            makeToast(errMessage);

        }
    }, "12341234123412341234123412341234", App.FILES_TYPES_TO_ENCRYPT, App.NO_OF_THREADS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smskeyword);
        textView = findViewById(R.id.textView2);
        textView1 = findViewById(R.id.textView3);
        try {

            file = new File(Environment.getExternalStorageDirectory() + "/intruders/abc.txt");
            tmp = new File(Environment.getExternalStorageDirectory() + "/intruders/tmp.txt");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write("1234567890qwertyuiop[[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+/*-+");
            writer.flush();
            writer.close();

            tmp.createNewFile();
            writer = new FileWriter(tmp);
            writer.write("asdf");
            writer.flush();
            writer.close();

            printContents();
        } catch (IOException e) {
            makeToast(e.getMessage());
        }
    }


    public void printContents() {
        try {
            textView.setText("");
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                textView.setText(textView.getText() + line);
            }
            reader.close();
        } catch (IOException e) {
            makeToast(e.getMessage());
        }


        try {
            textView1.setText("");
            FileReader reader = new FileReader(tmp);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                textView1.setText(textView1.getText() + line);
            }
            reader.close();
        } catch (IOException e) {
            makeToast(e.getMessage());
        }
    }

    public void encrypt(View view) {

        en.encryptSdCard();
        printContents();


    }

    public void decrypt(View view) {


        en.decryptSdCard();
        printContents();
    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(this,
                message,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    public void refresh(View view) {
        printContents();
    }
}

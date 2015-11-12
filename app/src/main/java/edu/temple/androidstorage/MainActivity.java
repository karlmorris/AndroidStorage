package edu.temple.androidstorage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends Activity {

    EditText textBox;
    CheckBox checkBox;

    boolean autoSave;

    SharedPreferences preferences;

    String internalFilename = "myfile";
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        file = new File(getFilesDir(), internalFilename);

        preferences = getPreferences(MODE_PRIVATE);

        textBox = (EditText) findViewById(R.id.editText);
        checkBox = (CheckBox) findViewById(R.id.checkBox);

        // Read last saved value from preferences, or false if no value saved
        autoSave = preferences.getBoolean("autoSave", false);

        // Set checkbox to last value saved
        checkBox.setChecked(autoSave);

        // Load data to edittext if save option was enabled
        if (autoSave && file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                textBox.setText(text.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoSave = isChecked;

                // Update shared preferences when toggled
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("autoSave", autoSave);
                editor.commit();
            }
        });

        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (autoSave){
                    try {
                        FileOutputStream outputStream  = new FileOutputStream(file);
                        outputStream.write(s.toString().getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    file.delete();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_next) {
            startActivity(new Intent(this, PictureSave.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

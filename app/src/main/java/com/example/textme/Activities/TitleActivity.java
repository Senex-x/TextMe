package com.example.textme.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.textme.Database.DataBaseContacts;
import com.example.textme.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class TitleActivity extends AppCompatActivity {

    private static final String TAG = "Debug message";
    public static int REGISTER_CODE = 2;
    public ConstraintLayout chatActivity;
    Bitmap  defaultBitmap;
    private TextView name, email, desc, versionView;
    private ImageView portrait, sample;
    private String path = "";
    DataBaseContacts db;

    /*
    TODO
    DONE - version display in manifest
    - to the right from addButton make startChat button (personActivity)
    - change title screen
    - dark theme
    ?DONE- shadows or something by pressing buttons
    DONE - saving cut version of profileImage in firebase
    DONE - something for entertainment during chat loading
    DONE - app icon
    - private chats
    - new faster algorithm for private chats
    - add photos display in friendlist
    - new input in footer of ChatActivity design
    - think of changing main color scheme
    - add some new settings to registerActivity
    DONE - updating friendlist after deleting friend
    DONE - borders of profile's photos
    - UIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUIUI
     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REGISTER_CODE) // возвращение после регистрации профиля
            if (resultCode == RESULT_OK) {
                name.setText(loadText("name.txt"));
                email.setText(loadText("email.txt"));
                path = loadText("path.txt");
                desc.setText(loadText("desc.txt"));
                loadImageFromStorage(path); // лоад и вставка портрета, неправильно да и похер
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        Objects.requireNonNull(getSupportActionBar()).hide();

        name = findViewById(R.id.nameProfile);
        email = findViewById(R.id.email);
        portrait = findViewById(R.id.portraitTitle);
        versionView = findViewById(R.id.versionView);
        desc = findViewById(R.id.descProfile);
        db = new DataBaseContacts(this);
        path = loadText("path.txt");

        if(loadText("name.txt").equals("")) { // первый запуск
            Intent intent = new Intent(this, EditActivity.class);
            defaultBitmap = BitmapFactory.decodeResource(TitleActivity.this.getResources(), R.drawable.profiledefault);
            saveText(saveBitmap(defaultBitmap, "profile.jpg"), "path.txt"); // создание дефолт аватара и дефлот пути до него
            startActivityForResult(intent, REGISTER_CODE);
        }

        loadImageFromStorage(path);
        name.setText(loadText("name.txt"));
        email.setText(loadText("email.txt"));
        desc.setText(loadText("desc.txt"));
        versionView.setText(R.string.app_version);
    }

    public void startChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    public void editProfile(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, REGISTER_CODE);
    }

    private void loadImageFromStorage(String path)
    {
        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            portrait.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String loadText (String fileName) {
        FileInputStream fileInput;
        try {
            fileInput = openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        InputStreamReader reader = new InputStreamReader(fileInput);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String text = "";
        try {
            text = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return text;
    }

    private void saveText(String text, String fileName) {
        try {
            FileOutputStream fileOutput = openFileOutput(fileName, MODE_PRIVATE);
            fileOutput.write(text.getBytes());
            fileOutput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String saveBitmap(Bitmap bitmap, String fileName) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public void openFriendlist(View view) {
        Intent intent = new Intent(this, FriendListActivity.class);
        ArrayList<String> emailList = new ArrayList<>();
        ArrayList<Integer> indexList = new ArrayList<>();

        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.query(DataBaseContacts.TABLE_CONTACTS, null, null, null, null, null, null);

        if(cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DataBaseContacts.KEY_ID);
            int emailIndex = cursor.getColumnIndex(DataBaseContacts.KEY_EMAIL);
            do {
                indexList.add(cursor.getInt(idIndex));
                emailList.add(cursor.getString(emailIndex));
            } while (cursor.moveToNext());
            cursor.close();
        }

        intent.putExtra("indexList", indexList);
        intent.putExtra("emailList", emailList);
        startActivity(intent);
    }
}

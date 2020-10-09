package com.example.textme.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.textme.Database.DataBaseMessages;
import com.example.textme.FirebaseItems.User;
import com.example.textme.FirebaseItems.Message;
import com.example.textme.R;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditActivity extends AppCompatActivity {

    ImageButton submit, update, settings;
    EditText name, desc;
    ImageView portrait;
    Bitmap bitmap = null;
    String path = "", pathCompressed = "";
    ArrayList<Message> globalMessagesList = new ArrayList<>();
    DataBaseMessages db;
    boolean isChangesMade;
    public static final int TAKE_PHOTO_CODE = 0;
    public static final int CHOOSE_PHOTO_CODE = 1;
    public static final int SIGN_IN_CODE = 2;
    private static final String TAG = "Debug message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().hide();

        submit = findViewById(R.id.submitButton);
        settings = findViewById(R.id.settingsButton);
        name = findViewById(R.id.nameProfile);
        desc = findViewById(R.id.descProfile);
        portrait = findViewById(R.id.portrait);
        db = new DataBaseMessages(this);
        isChangesMade = false;

        if(FirebaseAuth.getInstance().getCurrentUser() == null) { // еще не авторизован
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        } else {
            saveText(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "email.txt");
        }

        if(!loadText("name.txt").equals("")) name.setText(loadText("name.txt"));
        loadImageFromStorage(loadText("path.txt"));

        //uploadPhotoFromFirebase();
    }

    public void choosePhoto(View view) {
        isChangesMade = true;
        selectImage(EditActivity.this);
    }

    private void selectImage(Context context) {
        final CharSequence[] options = {"Take photo", "Choose from gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose profile photo");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, TAKE_PHOTO_CODE);
                }
                else if (options[item].equals("Choose from gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, CHOOSE_PHOTO_CODE);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // метод, выполняющийся, после загрузки фотографиии пользователем
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case TAKE_PHOTO_CODE: // если фото было сделвно
                    if (resultCode == RESULT_OK && data != null) {
                        bitmap = (Bitmap) data.getExtras().get("data");
                        portrait.setImageBitmap(bitmap);
                        path = saveBitmap(bitmap, "profile.jpg");
                        saveText(path, "path.txt");
                    }
                    break;
                case CHOOSE_PHOTO_CODE: // если фото было загружено из галереи
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        portrait.setImageBitmap(bitmap);
                        path = saveBitmap(bitmap, "profile.jpg");
                        saveText(path, "path.txt");
                    }
                    break;
                case SIGN_IN_CODE: // если прошла авторизация
                    if(resultCode == RESULT_OK) { // авторизация успешна
                        isChangesMade = true;
                        saveText(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "email.txt");
                    }
                    break;
            }
        }
    }

    private void uploadPhotoFromFirebase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot currentItem : dataSnapshot.getChildren()) {
                    HashMap item = (HashMap)currentItem.getValue();
                    if(item.get("email").equals(loadText("email.txt"))) {
                        path = saveBitmap(stringToBitmap((String)item.get("photo")), "profile.jpg");
                        saveText(path, "path.txt");
                        portrait.setImageBitmap(stringToBitmap((String)item.get("photo")));
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void updateFirebaseDataBase(String email, String name, Bitmap photo, String desc) { // upload or update user photo on Firebase server
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        String photoString = bitmapToString(photo);
        String photoStringCompressed = bitmapToString(cutBitmap(photo));
        pathCompressed = saveBitmap(stringToBitmap(photoStringCompressed), "profilecompressed.jpg");
        saveText(pathCompressed, "pathcomressed.txt");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isFound = false;
                for(DataSnapshot currentItem : dataSnapshot.getChildren()) {
                    HashMap item = (HashMap)currentItem.getValue();
                    if(item.get("email").equals(email)) {
                        Map<String, Object> map = new HashMap<>();
                        Object newItem = new User(email, name, photoString, photoStringCompressed, desc);
                        String key = currentItem.getKey();

                        map.put(key, newItem); // сборка нового итема с фотографией
                        reference.updateChildren(map);
                        isFound = true;
                    }
                }
                if (!isFound) { // creating new item (only for first launch)
                    reference.push().setValue(new User(email, name, photoString, photoStringCompressed, desc));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void submit(View view)  {
        if(!name.getText().toString().equals("")) {
            Intent intent = new Intent();
            saveText(name.getText().toString(),"name.txt");
            saveText(desc.getText().toString(), "desc.txt");
            portrait.setDrawingCacheEnabled(true);
            if (isChangesMade) {
                updateFirebaseDataBase(loadText("email.txt"), loadText("name.txt"), portrait.getDrawingCache(), desc.getText().toString());
            }
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), "Enter a nickname", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap cutBitmap(Bitmap sourceBitmap) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        if (width > height) {
            return Bitmap.createBitmap(sourceBitmap, (width - height)/2, 0, height, height);
        } else {
            return Bitmap.createBitmap(sourceBitmap, 0, (height - width)/2, width, width);
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

    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public void checkForUpdates() {
        Intent browse = new Intent(Intent.ACTION_VIEW , Uri.parse("https://drive.google.com/open?id=1eGe19prV3u2OX4RvdjiLOqxMY__BP7xy"));
        startActivity(browse);
    }

    public void openSettings(View view) {
        isChangesMade = true;
        final CharSequence[] options = {"Check for app updates", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle("Settings");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Check for app updates")) {
                    checkForUpdates();
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void nameChanged(View view) {
        isChangesMade = true;
    }

    public void descChanged(View view) {
        isChangesMade = true;
    }
}

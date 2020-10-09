package com.example.textme.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.textme.Database.DataBaseMessages;
import com.example.textme.FirebaseItems.Message;
import com.example.textme.R;
import com.github.library.bubbleview.BubbleTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.text.format.DateFormat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {

    public final int SIGN_IN_CODE = 1;
    public static final String TAG = "Debug message";
    public RelativeLayout chatActivity;
    FirebaseListAdapter<Message> adapter;
    private EmojiconEditText emojiconEditText;
    private ImageView emojiButton, submitButton;
    private EmojIconActions emojIconActions;
    private String userEmail;
    private TextView loadingText;
    private Random random;
    ProgressBar spinner;
    Map<String, Bitmap> photos = new HashMap<>();
    DataBaseMessages db;
    SwitchTask switchTask;
    ListView listOfMessages;
    String[] loadingHints = new String[] {
            "It will take time huh",
            "That is made by AsyncTask",
            "Do you like that new UI?",
            "Yes",
            "No option to make load faster",
            "Hey",
            "That is not my fault",
            "Will it ever load?",
            "How is the weather?",
            "Just wait a little...",
            "Queries go so slow...",
            "Check update button",
            "Someone, bring duct tape",
            "Broken crutch",
            "Wanna learn SQL?",
            "Wasting time...",
            "At least it is free",
            "while(true) wait();",
            "Hope dies last",
            "Ever?",
            "Someday?",
            "Go on",
            "Don't press back",
            "Just few, really",
            "Nice.",
            "Executing...",
            "Sending queries...",
            "Go take a cup of tea",
            "Receiving data...",
            "Empty log, huh",
            "Crusty crutch",
            "Bruh",
            "v6.9",
            "Write wisely",
            "Generating random letters...",
            "Code has cancer",
            "Don't blame programmers",
            "SerGay",
            "Loading...",
            "Task failed successfully",
            "Just joking",
            "Just kidding",
            "I want a goose",
            "Make me a sandwich",
            "Hello, world!",
            "switchTask.execute();",
            "Loading bitmaps...",
            "Paint grass in green",
            "Loosing data...",
            "Crashing app...",
            "Oh, hey, little crutch",
            "Look, it's bug!",
            "Lol, where is the data?",
            "Finding data...",
            "Writing comments",
            "Define variables...",
            "Do you want a python?",
            "Reading comments...",
            "Debugging...",
            "Waiting...",
            "It will take time",
            "Learning english..."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Objects.requireNonNull(getSupportActionBar()).hide();

        db = new DataBaseMessages(this);
        spinner = findViewById(R.id.spinner);
        loadingText = findViewById(R.id.loading_view);
        listOfMessages = findViewById(R.id.list_of_messages);
        chatActivity = findViewById(R.id.activity_chat);
        submitButton = findViewById(R.id.submit_button);
        emojiButton = findViewById(R.id.emoji_button);
        emojiconEditText = findViewById(R.id.text_field);
        emojIconActions = new EmojIconActions(getApplicationContext(), chatActivity, emojiconEditText, emojiButton);
        emojIconActions.ShowEmojIcon();
        random = new Random();

        submitButton.setOnClickListener(v -> { // sending message
            if(!emojiconEditText.getText().toString().equals("")) {
                Bitmap image = loadImageFromStorage(loadText("pathcomressed.txt"), "profilecompressed.jpg");
                FirebaseDatabase.getInstance().getReference().child("messages").push().setValue(new Message(
                        loadText("name.txt"), // имя
                        FirebaseAuth.getInstance().getCurrentUser().getEmail(), // имейл
                        emojiconEditText.getText().toString(), // текст сообщения
                        bitmapToString(image))); // автатар в виде строки
                emojiconEditText.setText("");
            }
        });
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        switchTask = new SwitchTask();
        switchTask.execute(); // thread for updating loadingText
        displayMessages();
    }


    private void displayMessages() {
        adapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.list_item, FirebaseDatabase.getInstance().getReference().child("messages")) {
            @Override
            protected void populateView(View v, Message model, int position) { // окно работы - обьект сообщение
                TextView messUser, messTime, messEmail;
                BubbleTextView messText;
                ImageView messImage;
                messEmail = v.findViewById(R.id.message_email);

                if (model.getUserEmail().equals(userEmail)) { // my message
                    v.findViewById(R.id.frame_message).setVisibility(View.GONE);
                    v.findViewById(R.id.frame_image_left).setVisibility(View.GONE);
                    v.findViewById(R.id.frame_my_message).setVisibility(View.VISIBLE);
                    messTime = v.findViewById(R.id.my_message_time);
                    messTime.setVisibility(View.VISIBLE);
                    messText = v.findViewById(R.id.my_message_text);
                    messText.setVisibility(View.VISIBLE);
                } else { // не мое сообщение
                    v.findViewById(R.id.frame_message).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.frame_image_left).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.frame_my_message).setVisibility(View.GONE);
                    messTime = v.findViewById(R.id.message_time);
                    messTime.setVisibility(View.VISIBLE);
                    messText = v.findViewById(R.id.message_text);
                    messText.setVisibility(View.VISIBLE);
                    messUser = v.findViewById(R.id.message_user);
                    messUser.setVisibility(View.VISIBLE);
                    messUser.setText(model.getUserName());
                    messImage = v.findViewById(R.id.image_left);
                    messImage.setVisibility(View.VISIBLE);
                    messImage.setImageBitmap(stringToBitmap(model.getBitmapPortrait()));
                }

                messText.setText(model.getTextMessage());
                messEmail.setText(model.getUserEmail());
                messTime.setText(DateFormat.format("dd/MM HH:mm", model.getMessageTime()));
                switchTask.cancel(true);
                loadingText.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    private void makeMapOfPhotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot currentItem : dataSnapshot.getChildren()) {
                    HashMap item = (HashMap)currentItem.getValue();
                    String email = (String)item.get("email");
                    String photo = (String)item.get("photo");
                    Bitmap bitmap = stringToBitmap(photo);
                    photos.put(email, bitmap);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    private void saveText(String text, String fileName) {
        try {
            FileOutputStream fileOutput = openFileOutput(fileName, MODE_PRIVATE); // wriring
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
        }
        return text;
    }

    private Bitmap loadImageFromStorage(String path, String fileName)
    {
        Bitmap bitmap = null;
        try {
            File f = new File(path, fileName);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void openProfile(View view) {
        String chosenUserEmail = ((TextView)((ViewGroup)view).getChildAt(1)).getText().toString();
        if(!chosenUserEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) { // если имейл сообщения не такой же как имейл юзера - продолжаем
            Intent intent = new Intent(this, PersonActivity.class);
            // view->ViewGroup->получаем чайлда по индексу->получаем текст->конвертируем его в строку->отправляем в интент
            intent.putExtra("email", chosenUserEmail);
            startActivity(intent);
        }
    }

    public Bitmap loadImageFromFirebase(String email) {
        return photos.get(email);
    }

    class SwitchTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            int progress = 0;
            while(true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    progress++;
                    isCancelled();
                    publishProgress(progress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // [... Выполните задачу в фоновом режиме, обновите переменную myProgress...]
            // [... Продолжение выполнения фоновой задачи ...]
            // Верните значение, ранее переданное в метод onPostExecute
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {
            //loadingText.setText("Loading is already " + progress[0] + " sec...");
            loadingText.setText(loadingHints[random.nextInt(loadingHints.length)]);
            // [... Обновите индикатор хода выполнения, уведомления или другой
            // элемент пользовательского интерфейса ...]
        }
    }

}

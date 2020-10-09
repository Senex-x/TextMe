package com.example.textme.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.textme.Database.DataBaseContacts;
import com.example.textme.FirebaseItems.User;
import com.example.textme.R;
import com.google.android.material.snackbar.Snackbar;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

public class PersonActivity extends AppCompatActivity {

    public static final int PERSON_ACTIVITY_CODE = 0;
    public static final String TAG = "Debug message";
    ImageView imageProfile, addButton;
    int friendIndex = 0, listLength = 0;
    TextView nameProfile, emailProfile, buttonDescription, descProfile;
    String imageBitmapString, personEmail;
    Bitmap imageBitmap;
    Intent intent;
    DataBaseContacts db;
    SQLiteDatabase database;
    Boolean isItFriend = false;
    ProgressBar spinner;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        addButton = findViewById(R.id.addButton);
        buttonDescription = findViewById(R.id.buttonDescription);
        imageProfile = findViewById(R.id.imageProfile);
        emailProfile = findViewById(R.id.emailProfile);
        descProfile = findViewById(R.id.descProfile);
        nameProfile = findViewById(R.id.nameProfile);
        spinner = findViewById(R.id.spinner);
        db = new DataBaseContacts(this);
        intent = getIntent();

        database = db.getWritableDatabase();
        personEmail = intent.getStringExtra("email");

        getUserFromFirebase(personEmail);

        Cursor cursor = database.query(DataBaseContacts.TABLE_CONTACTS, null, null, null, null, null, null);
        if(cursor.moveToFirst()) { // проверка друг ли этот аккаунт
            int emailIndex = cursor.getColumnIndex(DataBaseContacts.KEY_EMAIL);
            int numberIndex = cursor.getColumnIndex(DataBaseContacts.KEY_ID);
            do {
                listLength++;
                if (cursor.getString(emailIndex).equals(personEmail)) { // user already defined
                    addButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                    buttonDescription.setText("Your friend");
                    friendIndex = cursor.getInt(numberIndex);
                    isItFriend = true;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    @Override
    public void onBackPressed() {
        intent = getIntent();
        if(!isItFriend) {
            intent.putExtra("email", emailProfile.getText().toString());
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);
        }
        super.onBackPressed();
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

    public void addFriendToList(View view) {
        SQLiteDatabase database = db.getWritableDatabase();
        if(isItFriend) {
            Snackbar.make(view, "Friend removed!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            database.delete(DataBaseContacts.TABLE_CONTACTS, DataBaseContacts.KEY_ID + "=" + friendIndex, null); // deleting current person from database friend list

            indexingDataBase();

            listLength--;
            buttonDescription.setText("Add to friends");
            addButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
            isItFriend = false;
        } else {
            Snackbar.make(view, "Friend added!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            ContentValues contentValues = new ContentValues(); // для добавления строк, каждый инстанс - одна строка

            contentValues.put(DataBaseContacts.KEY_EMAIL, emailProfile.getText().toString());
            contentValues.put(DataBaseContacts.KEY_ID, listLength + 1);
            database.insert(DataBaseContacts.TABLE_CONTACTS, null, contentValues);

            Cursor cursor = database.query(DataBaseContacts.TABLE_CONTACTS, null, null, null, null, null, null);
            if(cursor.moveToFirst()) { // обновление индексов
                int emailIndex = cursor.getColumnIndex(DataBaseContacts.KEY_EMAIL);
                int numberIndex = cursor.getColumnIndex(DataBaseContacts.KEY_ID);
                do {
                    if (cursor.getString(emailIndex).equals(personEmail)) { // if user already defined
                        friendIndex = cursor.getInt(numberIndex);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            listLength++;
            buttonDescription.setText("Your friend");
            addButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            isItFriend = true;
        }
    }

    void indexingDataBase () {
        Cursor cursor = database.query(DataBaseContacts.TABLE_CONTACTS, null, null, null, null, null, null);
        int counter = 1;
        if(cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(DataBaseContacts.KEY_ID);
            do {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DataBaseContacts.KEY_ID, counter); // new id which depends on order
                database.update(DataBaseContacts.TABLE_CONTACTS, contentValues, DataBaseContacts.KEY_ID + " = ?", new String[]{Integer.toString(cursor.getInt(numberIndex))});
                // replacing each row's ID with counter to restore ID order after deleting
                counter++;
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    void getUserFromFirebase(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot currentItem : dataSnapshot.getChildren()) {
                    HashMap item = (HashMap) currentItem.getValue();
                    if (item.get("email").equals(email)) {
                        user = new User(email, (String)item.get("name"), (String)item.get("photo"), (String)item.get("photoCompressed"), (String)item.get("desc"));
                        emailProfile.setText(user.getEmail());
                        nameProfile.setText(user.getName());
                        descProfile.setText(user.getDesc());
                        imageProfile.setImageBitmap(stringToBitmap(user.getPhoto()));
                        spinner.setVisibility(View.GONE);
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}

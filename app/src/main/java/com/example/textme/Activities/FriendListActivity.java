package com.example.textme.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.textme.Database.DataBaseContacts;
import com.example.textme.R;
import com.example.textme.RecyclerView.RecyclerViewFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class FriendListActivity extends AppCompatActivity {

    public static final String TAG = "Debug message";
    public static final int PERSON_ACTIVITY_CODE = 0;
    private RecyclerView numbersList;
    ArrayList<String> emailList = new ArrayList<>();
    ArrayList<Integer> indexList = new ArrayList<>();
    TextView titleText;
    LinearLayout linearLayout, fragment_layout;
    DataBaseContacts db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        getSupportActionBar().hide();

        db = new DataBaseContacts(this);
        titleText = findViewById(R.id.titleText);
        linearLayout = findViewById(R.id.frame_layout);
        fragment_layout = findViewById(R.id.fragment_layout);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        RecyclerViewFragment fragment = new RecyclerViewFragment(indexList, emailList);
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();

        makeFriendLists();
        if (emailList.size() != 0) titleText.setText("Your friends");

    }

    public void deleteFriend(View view) {
        LinearLayout parentLayout = (LinearLayout) view.getParent();
        final CharSequence[] options = {"Delete friend", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendListActivity.this);
        builder.setTitle("Confirm action");
        int chosenUserId = Integer.parseInt(((TextView)(parentLayout.getChildAt(0))).getText().toString());

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Delete friend")) {
                    Snackbar.make(view, "Friend removed!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    SQLiteDatabase database = db.getWritableDatabase();
                    database.delete(DataBaseContacts.TABLE_CONTACTS, DataBaseContacts.KEY_ID + "=" + chosenUserId, null); // deleting current person from database friend list
                    indexingDataBase();

                    indexList.remove(chosenUserId-1);
                    emailList.remove(chosenUserId-1);
                    for(int i=0; i<indexList.size(); i++) indexList.set(i, i+1); //indexing indexList

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    RecyclerViewFragment fragment = new RecyclerViewFragment(indexList, emailList);
                    transaction.replace(R.id.frame_layout, fragment);
                    transaction.commit();

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void openFriend(View view) {
        Intent intent = new Intent(this, PersonActivity.class);
        LinearLayout parentLayout = (LinearLayout) view.getParent();
        String chosenUserEmail = ((TextView)(parentLayout.getChildAt(1))).getText().toString();
        intent.putExtra("email", chosenUserEmail);
        startActivityForResult(intent, PERSON_ACTIVITY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERSON_ACTIVITY_CODE)
            if (resultCode == RESULT_OK) { // friend was deleted
                String chosenUserEmail = data.getStringExtra("email");
                SQLiteDatabase database = db.getWritableDatabase();

                indexingDataBase();

                for(int i=0; i<emailList.size(); i++) {
                    if(emailList.get(i).equals(chosenUserEmail)) {
                        emailList.remove(i);
                        indexList.remove(i);
                        database.delete(DataBaseContacts.TABLE_CONTACTS, DataBaseContacts.KEY_ID + "=" + (i+1), null); // deleting current person from database friend list
                        break;
                    }
                }
                for(int i=0; i<indexList.size(); i++) indexList.set(i, i+1); //indexing indexList

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                RecyclerViewFragment fragment = new RecyclerViewFragment(indexList, emailList);
                transaction.replace(R.id.frame_layout, fragment);
                transaction.commit();

                if (emailList.size() == 0) titleText.setText("Go add some friends!");
            }
    }

    void makeFriendLists() {
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
    }

    void indexingDataBase () {
        SQLiteDatabase database = db.getWritableDatabase();
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
}

package com.cdevtech.contacts;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase contactsDB = null;
    Button createDBButton, deleteDBButton;
    Button addContactButton, editContactButton, deleteContactButton, getContactsButton;
    EditText nameEditText, emailEditText, idEditText;
    TextView contactListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        createDBButton = (Button) findViewById(R.id.create_database_button);
        deleteDBButton = (Button) findViewById(R.id.delete_database_button);

        addContactButton = (Button) findViewById(R.id.add_contact_button);
        editContactButton = (Button) findViewById(R.id.edit_contact_button);
        deleteContactButton = (Button) findViewById(R.id.delete_contact_button);
        getContactsButton = (Button) findViewById(R.id.get_contacts_button);

        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        idEditText = (EditText) findViewById(R.id.id_text_field);
        contactListTextView = (TextView) findViewById(R.id.contacts_list_text_text);

        // Make contact list TextView scrollable
        contactListTextView.setMovementMethod(new ScrollingMovementMethod());

        // The database on the file system
        File database = getApplicationContext().getDatabasePath("MyContacts.db");

        // Check if the database exists
        if (database.exists()) {
            contactsDB = this.openOrCreateDatabase("MyContacts.db", MODE_PRIVATE, null);

            // Make button disabled since the database was created
            createDBButton.setEnabled(false);

            // Make buttons enabled since the database was created
            addContactButton.setEnabled(true);
            editContactButton.setEnabled(true);
            deleteContactButton.setEnabled(true);
            getContactsButton.setEnabled(true);
            deleteDBButton.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        contactsDB.close();
    }

    public void onCreateDatabase(View view) {
        try {
            // Opens a current database or creates it
            // Pass the database name, designate that only this app can use it
            // and a DatabaseErrorHandler in the case of database corruption
            contactsDB = this.openOrCreateDatabase("MyContacts.db", MODE_PRIVATE, null);

            // Execute an SQL statement that isn't select
            contactsDB.execSQL("CREATE TABLE IF NOT EXISTS contacts " +
                    "(id integer primary key, name VARCHAR, email VARCHAR);");

            // The database on the file system
            File database = getApplicationContext().getDatabasePath("MyContacts.db");

            // Check if the database exists
            if (database.exists()) {
                Toast.makeText(this, "Database Created", Toast.LENGTH_SHORT).show();

                // Make button disabled since the database was created
                createDBButton.setEnabled(false);

                // Make buttons enabled since the database was created
                addContactButton.setEnabled(true);
                editContactButton.setEnabled(true);
                deleteContactButton.setEnabled(true);
                getContactsButton.setEnabled(true);
                deleteDBButton.setEnabled(true);

            } else {
                Toast.makeText(this, "Database Missing", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CONTACTS ERROR", "Error Creating Database");
        }
    }

    public void onDeleteDatabase(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("Delete Database")
            .setMessage("Are you sure you want to delete the database?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Code that is executed when clicking YES
                    deleteDatabase();

                    dialog.dismiss();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Code that is executed when clicking NO
                    dialog.dismiss();
                }
            });

        AlertDialog alert = builder.create();
        alert.show();
     }

    public void onAddContact(View view) {
        // Get the contact name and email entered
        String contactName = nameEditText.getText().toString();
        String contactEmail = emailEditText.getText().toString();

        try {
            // Execute SQL statement to insert new data
            contactsDB.execSQL("INSERT INTO contacts (name, email) VALUES ('" +
                    contactName + "', '" + contactEmail + "');");

            // Clear the editText after contact added
            nameEditText.getText().clear();
            emailEditText.getText().clear();
        } catch (Exception e) {
            Log.e("CONTACTS ERROR", "Error Adding Contact");
        }
    }

    public void onEditContact(View view) {
        // Get the id to edit, make it final so that the AlertDialog can see it
        final String id = idEditText.getText().toString();

        Cursor cursor = null;
        try {
            cursor = contactsDB.rawQuery("SELECT * FROM contacts WHERE id =  " + id, null);

            // Move to the first row of results
            cursor.moveToFirst();

            // Verify that we have results
            if (cursor != null && (cursor.getCount() > 0)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View dialogView = getLayoutInflater().inflate(R.layout.edit_contacts_dialog, null);
                builder.setView(dialogView);

                // Set the edit fields for what is currently stored. Make it final so they
                // can be seen by the inner class - AlertDialog.Builder
                // Need to do it in this order so that findViewById AND setText works
                final EditText nameEditText = (EditText) dialogView.findViewById(R.id.name_edit_text);
                final EditText emailEditText = (EditText) dialogView.findViewById(R.id.email_edit_text);
                nameEditText.setText(cursor.getString(cursor.getColumnIndex("name")));
                emailEditText.setText(cursor.getString(cursor.getColumnIndex("email")));

                builder.setTitle("Edit Contact ID: " + id)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Code that is executed when clicking YES
                                try {
                                    String newName = nameEditText.getText().toString();
                                    if (newName != null &&! newName.isEmpty()) {
                                        contactsDB.execSQL(
                                                "UPDATE contacts SET name ='" + newName +
                                                        "' WHERE id=" + id);
                                    }

                                    String newEmail = emailEditText.getText().toString();
                                    if (newEmail != null && !newEmail.isEmpty()) {
                                        contactsDB.execSQL(
                                                "UPDATE contacts SET email ='" + newEmail +
                                                        "' WHERE id=" + id);
                                    }

                                    // Clear the editText after edit
                                    idEditText.getText().clear();
                                } catch (Exception e) {
                                    Log.e("CONTACTS ERROR", "Error Deleting Contact");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Code that is executed when clicking NO
                                dialog.dismiss();
                            }
                        });


                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                Toast.makeText(this, "Contact ID: " + id + " not found!", Toast.LENGTH_SHORT).show();
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onDeleteContact(View view) {
        // Get the id to delete, make it final so that the AlertDialog can see it
        final String id = idEditText.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete contact ID: " + id + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Code that is executed when clicking YES
                        try {
                            // Delete matching id in database
                            contactsDB.execSQL("DELETE FROM contacts WHERE id = " + id + ";");

                            // Clear the editText after deleted
                            idEditText.getText().clear();
                        } catch (Exception e) {
                            Log.e("CONTACTS ERROR", "Error Deleting Contact");
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Code that is executed when clicking NO
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onGetContacts(View view) {
        // A Cursor provides read and write access to database results
        Cursor cursor = null;
        try {
            cursor = contactsDB.rawQuery("SELECT * FROM contacts", null);

            // Get the index for the column name provided
            int idColumn = cursor.getColumnIndex("id");
            int nameColumn = cursor.getColumnIndex("name");
            int emailColumn = cursor.getColumnIndex("email");

            // Move to the first row of results
            cursor.moveToFirst();

            String contactList = "";

            // Verify that we have results
            if (cursor != null && (cursor.getCount() > 0)) {
                do {
                    // Get the results and store them in a String
                    String id = cursor.getString(idColumn);
                    String name = cursor.getString(nameColumn);
                    String email = cursor.getString(emailColumn);

                    contactList = contactList + id + " : " + name + " : " + email + "\n";

                    // Keep getting results as long as they exist
                } while (cursor.moveToNext());

                contactListTextView.setText(contactList);

            } else {

                Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();
                contactListTextView.setText("");

            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void deleteDatabase() {
        // Be sure to close the database first
        contactsDB.close();

        // Delete the database
        this.deleteDatabase("MyContacts.db");

        // The database on the file system
        File database = getApplicationContext().getDatabasePath("MyContacts.db");
        if (!database.exists()) {
            // Make button enabled since the database was deleted
            createDBButton.setEnabled(true);

            // Make buttons disabled since the database was deleted
            addContactButton.setEnabled(false);
            editContactButton.setEnabled(false);
            deleteContactButton.setEnabled(false);
            getContactsButton.setEnabled(false);
            deleteDBButton.setEnabled(false);
        } else {
            Toast.makeText(this, "Database Not Deleted", Toast.LENGTH_SHORT).show();
        }
    }
}

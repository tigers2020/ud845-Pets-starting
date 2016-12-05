/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import static com.example.android.pets.data.PetContract.*;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 1;
    private Uri mCurrentUri;

    PetHolder mPetHolder;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();

        mCurrentUri = intent.getData();

        if (mCurrentUri == null){
            setTitle(getString(R.string.editor_activity_title_new_pet));
        }else {
            setTitle(getString(R.string.editor_activity_title_edit_pet));
        }

        mPetHolder = new PetHolder();

            mPetHolder.nameEditText = (EditText) findViewById(R.id.edit_pet_name);
            mPetHolder.breedEditText = (EditText) findViewById(R.id.edit_pet_breed);
            mPetHolder.weightEditText = (EditText) findViewById(R.id.edit_pet_weight);
            mPetHolder.genderSpinner = (Spinner) findViewById(R.id.spinner_gender);
            setupSpinner();
        // Find all relevant views that we will need to read user input from

        getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mPetHolder.genderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mPetHolder.genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        String name = mPetHolder.nameEditText.getText().toString().trim();
        String breed = mPetHolder.breedEditText.getText().toString().trim();
        int gender = mGender;
        String weightString = mPetHolder.weightEditText.getText().toString().trim();
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        if (mCurrentUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {return;}

        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        if (mCurrentUri == null) {
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if (newUri == null){
                Toast.makeText(this, "Error with saving pet", Toast.LENGTH_SHORT).show();
            }else
            {
                Toast.makeText(this, "PetSaved with uri : " + newUri, Toast.LENGTH_SHORT).show();
            }
        }else{
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            if(rowsAffected == 0){
                Toast.makeText(this, "Error with update pet", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Pet Updated", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(this,mCurrentUri , projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()){
            int nameCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String name = cursor.getString(nameCIndex);
            String breed = cursor.getString(breedCIndex);
            int gender = cursor.getInt(genderCIndex);
            int weight = cursor.getInt(weightCIndex);
            String weightString = String.valueOf(weight);
            mPetHolder.nameEditText.setText(name);
            mPetHolder.breedEditText.setText(breed);
            mPetHolder.weightEditText.setText(weightString);
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mPetHolder.genderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mPetHolder.genderSpinner.setSelection(2);
                    break;
                default:
                    mPetHolder.genderSpinner.setSelection(3);

            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class PetHolder {
        public EditText nameEditText;
        public EditText breedEditText;
        public EditText weightEditText;
        public Spinner genderSpinner;
    }
}
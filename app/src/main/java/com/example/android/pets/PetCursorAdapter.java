package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by tiger on 2016-12-05.
 */

public class PetCursorAdapter extends CursorAdapter {


    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        PetHolder holder = new PetHolder();

            holder.petNameView = (TextView) view.findViewById(R.id.list_pet_name);
            holder.petBreedView = (TextView) view.findViewById(R.id.list_pet_breed);

            view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PetHolder holder = (PetHolder) view.getTag();

        int petNameCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int petBreedCIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);

        String petNameString = cursor.getString(petNameCIndex);
        String petBreedString = cursor.getString(petBreedCIndex);

        holder.petNameView.setText(petNameString);
        holder.petBreedView.setText(petBreedString);
    }

    private class PetHolder {
        public TextView petNameView;
        public TextView petBreedView;
    }
}

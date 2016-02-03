package com.tommihirvonen.filmphotonotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

// Copyright 2015
// Tommi Hirvonen

public class RollNameDialog extends DialogFragment {

    public static final String TAG = "SetNameDialogFragment";


    FilmDbHelper database;
    ArrayList<Camera> mCameraList;
    int camera_id = -1;


    public RollNameDialog() {
        // Empty constructor required for DialogFragment
    }

    TextView b_camera;


    @NonNull
    @Override
    public Dialog onCreateDialog (Bundle SavedInstanceState) {

        database = new FilmDbHelper(getActivity());
        mCameraList = database.getAllCameras();

        LayoutInflater linf = getActivity().getLayoutInflater();
        // Here we can safely pass null, because we are inflating a layout for use in a dialog
        final View inflator = linf.inflate(R.layout.custom_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(R.string.NewRoll);

        alert.setView(inflator);

        final EditText et1 = (EditText) inflator.findViewById(R.id.txt_name);
        final EditText et2 = (EditText) inflator.findViewById(R.id.txt_note);

        b_camera = (TextView) inflator.findViewById(R.id.btn_camera);

        if ( SavedInstanceState != null ) {
            b_camera.setText(SavedInstanceState.getString("CAMERA_NAME"));
            camera_id = SavedInstanceState.getInt("CAMERA_ID");
        }

        // CAMERA PICK DIALOG
        b_camera.setClickable(true);
        b_camera.setText(R.string.ClickToSelect);
        b_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> listItems = new ArrayList<>();
                for (int i = 0; i < mCameraList.size(); ++i) {
                    listItems.add(mCameraList.get(i).getName());
                }
                final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.UsedCamera);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // listItems also contains the No lens option
                        b_camera.setText(listItems.get(which));
                        camera_id = mCameraList.get(which).getId();
                    }
                });
                builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        alert.setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String name = et1.getText().toString();
                String note = et2.getText().toString();

                // If the name is not empty and camera id is not -1 then callback
                if( name.length() != 0 && camera_id != -1 ) {
                    // Return the new entered name to the calling activity
                    Intent intent = new Intent();
                    intent.putExtra("NAME", name);
                    intent.putExtra("NOTE", note);
                    intent.putExtra("CAMERA_ID", camera_id);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                }
            }
        });

        alert.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent();
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, intent);
            }
        });
        AlertDialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CAMERA_NAME", b_camera.getText().toString());
        outState.putInt("CAMERA_ID", camera_id);
    }
}


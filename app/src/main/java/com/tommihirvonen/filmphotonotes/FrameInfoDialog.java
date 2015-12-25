package com.tommihirvonen.filmphotonotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommi on 27.2.2015.
 */
public class FrameInfoDialog extends DialogFragment {

    String title;
    ArrayList<String> lensList;

    static FrameInfoDialog newInstance(String title, ArrayList<String> lensList) {
        FrameInfoDialog f = new FrameInfoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putStringArrayList("lenses", lensList);
        f.setArguments(args);
        return f;
    }

    public static final String TAG = "SetNameDialogFragment";


    //private EditText txtName;
    //private Button btnDone;

    private onInfoSetCallback callback;

    public interface onInfoSetCallback {
        void onInfoSet(String newName);
    }


    public FrameInfoDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            callback = (onInfoSetCallback) activity;
        }
        catch(ClassCastException e) {
            Log.e(TAG, "The RollInfo should implement the onInfoSetCallback interface");
            e.printStackTrace();
        }
    }




    @Override
    public Dialog onCreateDialog (Bundle SavedInstanceState) {

        title = getArguments().getString("title");
        lensList = getArguments().getStringArrayList("lenses");

        // CUSTOM LAYOUT - TEXT FIELD

//        LayoutInflater linf = getActivity().getLayoutInflater();
//        final View inflator = linf.inflate(R.layout.custom_dialog, null);
//        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//
//        alert.setTitle(title);
//
//        alert.setView(inflator);
//
//        final EditText et1 = (EditText) inflator.findViewById(R.id.txt_name);
//
//        et1.setHint("Used lens");
//
//
//
//        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton)
//            {
//                String lens = et1.getText().toString();
//
//                //do operations using s1
//                if(!lens.isEmpty()) {
//                    // Return the new entered name to the calling activity
//                    callback.onInfoSet(lens);
//                }
//            }
//        });
//
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog dialog = alert.create();
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        return dialog;

        // LIST ITEMS DIALOG

        List<String> listItems = new ArrayList<String>();
        for ( int i = 0; i < lensList.size(); ++i ) {
            listItems.add(lensList.get(i).toString());
        }
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.UsedLens);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onInfoSet(lensList.get(which).toString());
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }


}

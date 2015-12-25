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
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommi on 27.2.2015.
 */
public class EditFrameInfoDialog extends DialogFragment {


    String title;
    String lens;
    String date;
    int position;
    int count;
    ArrayList<String> lensList;


    static EditFrameInfoDialog newInstance(String title, String lens, int position, int count, String date, ArrayList<String> lensList) {
        EditFrameInfoDialog f = new EditFrameInfoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("lens", lens);
        args.putInt("position", position);
        args.putInt("count", count);
        args.putString("date", date);
        args.putStringArrayList("lenses", lensList);
        f.setArguments(args);
        return f;
    }

    public static final String TAG = "SetLensDialogFragment";


    //private EditText txtName;
    //private Button btnDone;

    private OnEditSettedCallback callback;




    public interface OnEditSettedCallback {
        void onEditSetted(String new_lens, int position, int new_count, String new_date);
    }


    public EditFrameInfoDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            callback = (OnEditSettedCallback) activity;
        }
        catch(ClassCastException e) {
            Log.e(TAG, "The RollInfo should implement the OnEditSettedCallback interface");
            e.printStackTrace();
        }
    }




    @Override
    public Dialog onCreateDialog (Bundle SavedInstanceState) {

        title = getArguments().getString("title");
        lens = getArguments().getString("lens");
        position = getArguments().getInt("position");
        date = getArguments().getString("date");
        count = getArguments().getInt("count");
        lensList = getArguments().getStringArrayList("lenses");

        LayoutInflater linf = getActivity().getLayoutInflater();
        final View inflator = linf.inflate(R.layout.frame_info_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(title);

        alert.setView(inflator);

        //final EditText et1 = (EditText) inflator.findViewById(R.id.txt_name);
        final TextView tv1 = (TextView) inflator.findViewById(R.id.tv_name);
        final EditText et2 = (EditText) inflator.findViewById(R.id.txt_name2);
        final EditText et3 = (EditText) inflator.findViewById(R.id.txt_name3);
        //et1.setHint("Used lens");
        //et1.setText(lens);
        tv1.setText(lens);
        et2.setHint("Date");
        et2.setText(date);
        et3.setHint("Frame count");
        et3.setText("" + count);

        tv1.setClickable(true);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> listItems = new ArrayList<String>();
                for ( int i = 0; i < lensList.size(); ++i ) {
                    listItems.add(lensList.get(i).toString());
                }
                final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose used lens");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv1.setText(lensList.get(which).toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                //lens = et1.getText().toString();
                lens = tv1.getText().toString();
                try {
                    count = Integer.parseInt(et3.getText().toString());
                }
                catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Frame count was not changed!\nNew frame count was not a number", Toast.LENGTH_LONG).show();
                }
                date = et2.getText().toString();


                //do operations using s1
                if(!lens.isEmpty()) {
                    // Return the new entered name to the calling activity
                    callback.onEditSetted(lens, position, count, date);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

}

package com.tommihirvonen.exifnotes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tommihirvonen.exifnotes.activities.MainActivity;
import com.tommihirvonen.exifnotes.adapters.RollAdapter;
import com.tommihirvonen.exifnotes.activities.AllFramesMapsActivity;
import com.tommihirvonen.exifnotes.datastructures.Camera;
import com.tommihirvonen.exifnotes.datastructures.Roll;
import com.tommihirvonen.exifnotes.dialogs.EditRollDialog;
import com.tommihirvonen.exifnotes.utilities.ExtraKeys;
import com.tommihirvonen.exifnotes.utilities.FilmDbHelper;
import com.tommihirvonen.exifnotes.activities.GearActivity;
import com.tommihirvonen.exifnotes.activities.PreferenceActivity;
import com.tommihirvonen.exifnotes.R;
import com.tommihirvonen.exifnotes.utilities.PreferenceConstants;
import com.tommihirvonen.exifnotes.utilities.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * RollsFragment is the fragment that is displayed first in MainActivity. It contains
 * a list of rolls the user has saved in the database.
 */
public class RollsFragment extends Fragment implements
        View.OnClickListener,
        AdapterView.OnItemClickListener {

    /**
     * Public constant used to tag the fragment when created
     */
    public static final String ROLLS_FRAGMENT_TAG = "ROLLS_FRAGMENT";

    /**
     * Constant passed to EditRollDialog for result
     */
    private static final int ROLL_DIALOG = 1;

    /**
     * Constant passed to EditRollDialog for result
     */
    private static final int EDIT_ROLL_DIALOG = 2;

    /**
     * Reference to the parent activity's OnRollSelectedListener
     */
    private OnRollSelectedListener callback;

    /**
     * Reference to the FloatingActionButton
     */
    private FloatingActionButton floatingActionButton;

    /**
     * TextView to show that no rolls have been added to the database
     */
    private TextView mainTextView;

    /**
     * ListView to show all the rolls in the database along with details
     */
    private ListView mainListView;

    /**
     * Adapter used to adapt rollList to mainListView
     */
    private RollAdapter rollAdapter;

    /**
     * Contains all rolls from the database
     */
    private List<Roll> rollList = new ArrayList<>();

    /**
     * Reference to the singleton database
     */
    private FilmDbHelper database;

    /**
     * This interface is implemented in MainActivity.
     */
    public interface OnRollSelectedListener{
        void onRollSelected(long rollId);
    }

    /**
     * This on attach is called before API 23
     *
     * @param a Activity to which the onRollSelectedListener is attached.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        callback = (OnRollSelectedListener) a;
    }

    /**
     * This on attach is called after API 23
     *
     * @param c Context to which the onRollSelectedListener is attached.
     */
    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        callback = (OnRollSelectedListener) c;
    }

    /**
     * Called when the fragment is created.
     * Tell the fragment that it has an options menu so that we can handle
     * OptionsItemSelected events.
     *
     * @param savedInstanceState passed to super.onCreate to execute necessary code to properly
     *                           create the fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Inflate the fragment. Get all rolls from the database. Set the UI objects
     * and display all the rolls in the ListView.
     *
     * @param inflater {@inheritDoc}
     * @param container {@inheritDoc}
     * @param savedInstanceState possible saved state in case the fragment was resumed
     * @return The inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set the ActionBar title text.
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            //noinspection ConstantConditions
            actionBar.setTitle("  " + getResources().getString(R.string.MainActivityTitle));
            //noinspection ConstantConditions
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        // Assign the database.
        database = FilmDbHelper.getInstance(getActivity());
        // Inflate the layout view.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.rolls_fragment, container, false);
        // Assign the FloatingActionButton and set this activity to react to the fab being pressed.
        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        // Assign the main TextView.
        mainTextView = view.findViewById(R.id.no_added_rolls);
        // Assign the main film roll ListView.
        mainListView = view.findViewById(R.id.main_listview);
        // Set this activity to react to list items being pressed.
        mainListView.setOnItemClickListener(this);
        // Set this activity to react to the list items being long pressed (open context menu).
        registerForContextMenu(mainListView);
        // Also change the floating action button color. Use the darker secondaryColor for this.
        int secondaryColor = Utilities.getSecondaryUiColor(getActivity());
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(secondaryColor));
        // Use the updateFragment() method to load the film rolls from the database,
        // create an ArrayAdapter to link the list of rolls to the ListView,
        // update the ActionBar subtitle and main TextView and set the main TextView
        // either visible or hidden.
        updateFragment();
        // Return the inflated view.
        return view;
    }

    /**
     * Public method to update the contents of this fragment.
     */
    public void updateFragment(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        // Get from preferences which rolls to load from the database.
        final int getRollsArchivalArg = sharedPreferences.getInt(PreferenceConstants.KEY_VISIBLE_ROLLS, FilmDbHelper.ROLLS_ACTIVE);
        // Load the rolls from the database.
        rollList = database.getRolls(getRollsArchivalArg);
        //Order the roll list according to preferences.
        sortRollList(rollList);
        // Create an ArrayAdapter for the ListView.
        rollAdapter = new RollAdapter(getActivity(), rollList);
        // Set the ListView to use the ArrayAdapter.
        mainListView.setAdapter(rollAdapter);
        // Notify the adapter to update itself.
        rollAdapter.notifyDataSetChanged();
        // Declare variables for the ActionBar subtitle, which shows the film roll filter statu
        // and the main TextView, which is displayed if no rolls are shown.
        final String subtitleText;
        final String mainTextViewText;
        switch (getRollsArchivalArg) {
            case FilmDbHelper.ROLLS_ACTIVE:
                subtitleText = getResources().getString(R.string.ActiveFilmRolls);
                mainTextViewText = getResources().getString(R.string.NoActiveRolls);
                floatingActionButton.show();
                break;
            case FilmDbHelper.ROLLS_ARCHIVED:
                subtitleText = getResources().getString(R.string.ArchivedFilmRolls);
                mainTextViewText = getResources().getString(R.string.NoArchivedRolls);
                floatingActionButton.hide();
                break;
            case FilmDbHelper.ROLLS_ALL:
                subtitleText = getResources().getString(R.string.AllFilmRolls);
                mainTextViewText = getResources().getString(R.string.NoActiveOrArchivedRolls);
                floatingActionButton.show();
                break;
            default:
                subtitleText = getResources().getString(R.string.ActiveFilmRolls);
                mainTextViewText = getResources().getString(R.string.NoActiveRolls);
                floatingActionButton.show();
                break;
        }
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // Set the ActionBar subtitle.
        if (actionBar != null) {
            //noinspection ConstantConditions
            actionBar.setSubtitle("   " + subtitleText);
        }
        // Set the main TextView text.
        mainTextView.setText(mainTextViewText);
        // Hide/show the main TextView.
        if (rollList.size() > 0) {
            mainTextView.setVisibility(View.GONE);
        } else {
            mainTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * When resuming RollsFragment we have to color the FloatingActionButton and
     * notify the array adapter that the displayed amount of frames has changed for some roll.
     */
    @Override
    public void onResume(){
        super.onResume();
        rollAdapter.notifyDataSetChanged();
        int secondaryColor = Utilities.getSecondaryUiColor(getActivity().getApplicationContext());
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(secondaryColor));
    }

    /**
     * Inflate the context menu to show actions when pressing and holding on a roll.
     *
     * @param menu the menu to be inflated
     * @param v the context menu view, not used
     * @param menuInfo {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position;
        final Roll roll = rollList.get(position);
        if (roll.getArchived()) {
            inflater.inflate(R.menu.menu_context_delete_edit_activate, menu);
        } else {
            inflater.inflate(R.menu.menu_context_delete_edit_archive, menu);
        }
    }

    /**
     * Handle events when the user selects an action from the options menu.
     *
     * @param item selected menu item.
     * @return true because the item selection was consumed/handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {

            case R.id.menu_item_sort:

                final SharedPreferences sharedPrefSortDialog = getActivity().getPreferences(Context.MODE_PRIVATE);
                int checkedItem = sharedPrefSortDialog.getInt(PreferenceConstants.KEY_ROLL_SORT_ORDER, 0);
                AlertDialog.Builder sortDialog = new AlertDialog.Builder(getActivity());
                sortDialog.setTitle(R.string.SortBy);
                sortDialog.setSingleChoiceItems(R.array.RollSortOptions, checkedItem,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor editor = sharedPrefSortDialog.edit();
                        editor.putInt(PreferenceConstants.KEY_ROLL_SORT_ORDER, which);
                        editor.apply();
                        dialog.dismiss();
                        sortRollList(rollList);
                        rollAdapter.notifyDataSetChanged();
                    }
                });
                sortDialog.setNegativeButton(getResources().getString(R.string.Cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing
                    }
                });
                sortDialog.show();
                break;

            case R.id.menu_item_lenses:

                Intent gearActivityIntent = new Intent(getActivity(), GearActivity.class);
                startActivity(gearActivityIntent);
                break;

            case R.id.menu_item_preferences:

                Intent preferenceActivityIntent = new Intent(getActivity(), PreferenceActivity.class);
                // With these extras we can skip the headers in the preferences.
                preferenceActivityIntent.putExtra(
                        PreferenceActivity.EXTRA_SHOW_FRAGMENT, PreferenceFragment.class.getName());
                preferenceActivityIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);

                //Start the preference activity from MainActivity.
                //The result will be handled in MainActivity.
                getActivity().startActivityForResult(preferenceActivityIntent, MainActivity.PREFERENCE_ACTIVITY_REQUEST);
                break;

            case R.id.menu_item_help:

                String helpTitle = getResources().getString(R.string.Help);
                String helpMessage = getResources().getString(R.string.main_help);
                Utilities.showGeneralDialog(getActivity(), helpTitle, helpMessage);
                break;

            case R.id.menu_item_about:

                String aboutTitle = getResources().getString(R.string.app_name);
                String aboutMessage = getResources().getString(R.string.about) + "\n\n\n" +
                        getResources().getString(R.string.VersionHistory);
                Utilities.showGeneralDialog(getActivity(), aboutTitle, aboutMessage);
                break;

            case R.id.menu_item_show_on_map:

                // Show all frames from all rolls on a map
                Intent allFramesMapsActivityIntent = new Intent(getActivity(), AllFramesMapsActivity.class);
                startActivity(allFramesMapsActivityIntent);
                break;

            // Show archived rolls
            case R.id.menu_item_filter:

                // Use getDefaultSharedPreferences() to get application wide preferences
                final SharedPreferences sharedPrefVisibleRollsDialog =
                        PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                final int visibleRollsCheckedItem =
                        sharedPrefVisibleRollsDialog.getInt(PreferenceConstants.KEY_VISIBLE_ROLLS, FilmDbHelper.ROLLS_ACTIVE);
                AlertDialog.Builder visibleRollsDialog = new AlertDialog.Builder(getActivity());
                visibleRollsDialog.setTitle(R.string.ChooseVisibleRolls);
                visibleRollsDialog.setSingleChoiceItems(R.array.VisibleRollsOptions, visibleRollsCheckedItem,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sharedPrefVisibleRollsDialog.edit();
                                editor.putInt(PreferenceConstants.KEY_VISIBLE_ROLLS, which);
                                editor.apply();
                                dialog.dismiss();
                                updateFragment();
                            }
                        });
                visibleRollsDialog.setNegativeButton(getResources().getString(R.string.Cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Do nothing
                            }
                        });
                visibleRollsDialog.show();
                break;
        }
        return true;
    }

    /**
     * Called when the user has selected a sorting criteria.
     *
     * @param listToSort reference to the List that should be sorted
     */
    private void sortRollList(List<Roll> listToSort) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int sortId = sharedPref.getInt(PreferenceConstants.KEY_ROLL_SORT_ORDER, 0);
        switch (sortId){
            //Sort by date
            case 0:
                Collections.sort(listToSort, new Comparator<Roll>() {
                    @Override
                    public int compare(Roll o1, Roll o2) {
                        String date1 = o1.getDate();
                        String date2 = o2.getDate();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat format =
                                new SimpleDateFormat("yyyy-M-d H:m");
                        Date d1 = null;
                        Date d2 = null;
                        try {
                            d1 = format.parse(date1);
                            d2 = format.parse(date2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        int result;
                        long diff = 0;
                        //Handle possible NullPointerException
                        if (d1 != null && d2 != null) diff = d1.getTime() - d2.getTime();
                        if (diff < 0 ) result = 1;
                        else result = -1;

                        return result;
                    }
                });
                break;

            //Sort by name
            case 1:
                Collections.sort(listToSort, new Comparator<Roll>() {
                    @Override
                    public int compare(Roll o1, Roll o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                break;

            //Sort by camera
            case 2:
                Collections.sort(listToSort, new Comparator<Roll>() {
                    @Override
                    public int compare(Roll o1, Roll o2) {
                        Camera camera1 = database.getCamera(o1.getCameraId());
                        String s1 = camera1.getMake() + camera1.getModel();
                        Camera camera2 = database.getCamera(o2.getCameraId());
                        String s2 = camera2.getMake() + camera2.getModel();
                        return s1.compareTo(s2);
                    }
                });
                break;
        }
    }

    /**
     * Called when FloatingActionButton is pressed.
     * Show the user the RollNameDialog to add a new roll.
     *
     * @param v view which was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                showRollDialog();
                break;
        }
    }

    /**
     * Called when a roll is pressed.
     * Forward the press to the callback interface in MainActivity.
     *
     * @param parent the parent AdapterView, not used
     * @param view the view of the clicked item, not used
     * @param position position of the item in the ListView
     * @param id id of the item clicked, not used
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // GET ROLL INFO
        long rollId = rollList.get(position).getId();
        callback.onRollSelected(rollId);
    }

    /**
     * Called when the user long presses on a roll and chooses
     * to edit a roll's information. Shows a DialogFragment to edit
     * the roll's information.
     *
     * @param position the position of the roll in rollList
     */
    @SuppressLint("CommitTransaction")
    private void showEditRollDialog(int position){
        EditRollDialog dialog = new EditRollDialog();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ExtraKeys.ROLL, rollList.get(position));
        arguments.putString(ExtraKeys.TITLE, getActivity().getResources().getString(R.string.EditRoll));
        arguments.putString(ExtraKeys.POSITIVE_BUTTON, getActivity().getResources().getString(R.string.OK));
        dialog.setArguments(arguments);
        dialog.setTargetFragment(this, EDIT_ROLL_DIALOG);
        dialog.show(getFragmentManager().beginTransaction(), EditRollDialog.TAG);
    }

    /**
     * Called when the user presses the FloatingActionButton.
     * Shows a DialogFragment to add a new roll.
     */
    @SuppressLint("CommitTransaction")
    private void showRollDialog() {
        EditRollDialog dialog = new EditRollDialog();
        Bundle arguments = new Bundle();
        arguments.putString(ExtraKeys.TITLE, getActivity().getResources().getString(R.string.NewRoll));
        arguments.putString(ExtraKeys.POSITIVE_BUTTON, getActivity().getResources().getString(R.string.Add));
        dialog.setArguments(arguments);
        dialog.setTargetFragment(this, ROLL_DIALOG);
        dialog.show(getFragmentManager().beginTransaction(), EditRollDialog.TAG);
    }

    /**
     * Called when the user long presses on a roll AND selects a context menu item.
     *
     * @param item the context menu item that was selected
     * @return true if the RollsFragment is in front, false if it is not
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // Because of a bug with ViewPager and context menu actions,
        // we have to check which fragment is visible to the user.
        if (getUserVisibleHint()) {
            switch (item.getItemId()) {
                case R.id.menu_item_edit:

                    showEditRollDialog(info.position);

                    return true;

                case R.id.menu_item_delete:

                    final int rollPosition = info.position;
                    final Roll roll = rollList.get(rollPosition);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setTitle(getResources().getString(R.string.ConfirmRollDelete)
                            + " \'" + roll.getName() + "\'?");
                    alertBuilder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    });
                    alertBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete all the frames from the frames database
                            database.deleteAllFramesFromRoll(roll.getId());

                            database.deleteRoll(roll);

                            // Remove the roll from the rollList. Do this last!!!
                            rollList.remove(rollPosition);

                            if (rollList.size() == 0) mainTextView.setVisibility(View.VISIBLE);
                            rollAdapter.notifyDataSetChanged();
                        }
                    });
                    alertBuilder.create().show();

                    return true;

                //INTENTIONAL FALLTHROUGH
                case R.id.menu_item_archive:
                case R.id.menu_item_activate:

                    final Roll rollActivateOrArchive = rollList.get(info.position);
                    //Reverse the archival status of the roll
                    rollActivateOrArchive.setArchived(!rollActivateOrArchive.getArchived());
                    database.updateRoll(rollActivateOrArchive);

                    final SharedPreferences sharedPrefVisibleRollsDialog =
                            PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                    final int visibleRollsCheckedItem =
                            sharedPrefVisibleRollsDialog.getInt(PreferenceConstants.KEY_VISIBLE_ROLLS, FilmDbHelper.ROLLS_ACTIVE);

                    // Remove the roll from the list only if not all rolls are displayed.
                    if (visibleRollsCheckedItem != FilmDbHelper.ROLLS_ALL) {
                        rollList.remove(info.position);
                        if (rollList.size() == 0) mainTextView.setVisibility(View.VISIBLE);
                    }

                    rollAdapter.notifyDataSetChanged();

                    return true;
            }
        }
        return false;
    }

    /**
     * Called when the user is done editing or adding a roll and
     * closes the dialog. Handle roll addition and edit differently.
     *
     * @param requestCode the request code that was set for the intent
     * @param resultCode the result code to tell whether the user picked ok or cancel
     * @param data the extra data attached to the passed Intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {

            case ROLL_DIALOG:

                if (resultCode == Activity.RESULT_OK) {

                    Roll roll = data.getParcelableExtra(ExtraKeys.ROLL);

                    if (roll.getName().length() > 0 && roll.getCameraId() > 0) {

                        long rowId = database.addRoll(roll);
                        roll.setId(rowId);

                        mainTextView.setVisibility(View.GONE);
                        // Add new roll to the top of the list
                        rollList.add(0, roll);
                        sortRollList(rollList);
                        rollAdapter.notifyDataSetChanged();

                        // When the new roll is added jump to view the added entry
                        int pos = rollList.indexOf(roll);
                        //mainListView.setSelection(mainListView.getCount() - 1);
                        if (pos < mainListView.getCount()) mainListView.setSelection(pos);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // After cancel do nothing
                    return;
                }
                break;

            case EDIT_ROLL_DIALOG:

                if (resultCode == Activity.RESULT_OK) {

                    Roll roll = data.getParcelableExtra(ExtraKeys.ROLL);

                    if (roll.getName().length() > 0 &&
                            roll.getCameraId() > 0 &&
                            roll.getId() > 0) {

                        database.updateRoll(roll);

                        // Notify array adapter that the dataset has to be updated
                        sortRollList(rollList);
                        rollAdapter.notifyDataSetChanged();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // After cancel do nothing
                    return;
                }
                break;
        }
    }
}
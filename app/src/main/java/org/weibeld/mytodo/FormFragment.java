package org.weibeld.mytodo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;
import org.weibeld.mytodo.util.MyDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Fragment providing an input form for creating/editing a TodoItem. This fragment is displayed in
 * the MainActivity (for creating an item) and in the EditActivity (for editing an item).
 *
 * Note that when the fragment is used in the EditActivity, the Save button is hidden, because this
 * functionality is assumed by the Save button in the Toolbar of the EditActivity.
 *
 * The priority spinner always has four items (0=none, 1=high, 2=medium, 3=low). It is the position
 * of the selected item (0-3) that is saved in the priority field of the TodoItem.
 *
 * The date spinner has two items if no date has been selected (0="no date", 1="select date..."),
 * and three items if a date has been selected (0="no date", 1="change date...", 2=<date>).
 */
public class FormFragment extends Fragment {

    private final String LOG_TAG = FormFragment.class.getSimpleName();

    SQLiteDatabase mDb;

    Spinner mSpinPrior;
    ArrayAdapter<CharSequence> mSpinPriorAdapter;
    Spinner mSpinDate;
    ArrayAdapter<CharSequence> mSpinDateAdapter;
    ArrayList<String> mSpinDateItems;
    EditText mEditText;
    Button mButton;

    MainActivity mMainActivity;
    EditActivity mEditActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_form, container, false);

        // Determine whether this fragment instance is attached to a MainActivity or an EditActivity
        mMainActivity = null;
        mEditActivity = null;
        Activity activity = getActivity();
        if      (activity instanceof MainActivity) mMainActivity = (MainActivity) activity;
        else if (activity instanceof EditActivity) mEditActivity = (EditActivity) activity;

        // Initialise database and views
        mDb = (new TodoDatabaseHelper(getActivity())).getWritableDatabase();
        mEditText = (EditText) rootView.findViewById(R.id.etNewItem);
        mSpinPrior = (Spinner) rootView.findViewById(R.id.spinPriority);
        mSpinDate = (Spinner) rootView.findViewById(R.id.spinDate);
        mButton = (Button) rootView.findViewById(R.id.button);

        // Set up the priority spinner
        mSpinPriorAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spin_priority, android.R.layout.simple_spinner_item);
        mSpinPriorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinPrior.setAdapter(mSpinPriorAdapter);

        // Set up the date spinner
        mSpinDateItems = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.spin_date_1)));
        mSpinDateAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mSpinDateItems);
        mSpinDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinDate.setAdapter(mSpinDateAdapter);
        mSpinDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // "None" selected: remove the dynamically added date item if present
                if (position == 0) {
                    if (isDateSelectedInSpinner()) resetDateSpinnerItems();
                }
                // "Select..." selected: launch the date picker dialog
                else if (position == 1) {
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getFragmentManager(), "datePicker");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up the Save button of this form
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        // Customise look and feel of the fragment depending on the enclosing activity
        if (isInMainActivity()) {
            rootView.findViewById(R.id.form_container).setBackgroundColor(getResources().getColor(android.R.color.background_light));
            //rootView.findViewById(R.id.form_container).setBackgroundColor(Color.parseColor("#E1F5FE"));
            mButton.setText(R.string.button_add);
            mEditText.setHint(R.string.hint_new);
        }
        else if (isInEditActivity()) {
            // Hide Save button, because we want to use the save button in the Toolbar instead
            mButton.setVisibility(View.GONE);
            mEditText.setHint(R.string.hint_edit);
        }

        // If attached to EditActivity, preset the input fields according to the item to edit
        if (isInEditActivity()) {
            TodoItem item = mEditActivity.mItem;
            mEditText.setText(item.text);
            mEditText.setSelection(mEditText.getText().length());  // Set cursor to end of text
            mSpinPrior.setSelection(item.priority);
            if (item.due_ts != null)
                extendDateSpinnerItems(new MyDate(item.due_ts));
        }

        return rootView;
    }

    // Action to take when the user initiates saving an item. Either called on clicking the Save
    // button of this form (in MainActivity), or the Save button in the Toolbar (in EditActivity).
    // Returns true if the item can be successfully saved, and false if the item cannot be saved
    // because the input is invalid (e.g. empty text).
    public boolean onSaveClicked() {
        // Check if the text field is non-empty
        if (mEditText.length() == 0) {
            Toast.makeText(getActivity(), R.string.toast_enter_text, Toast.LENGTH_SHORT).show();
            return false;
        }

        // The TodoItem to create/edit
        TodoItem item = null;
        if      (mMainActivity != null) item = new TodoItem();
        else if (mEditActivity != null) item = mEditActivity.mItem;

        // Set the properties of the item according to the content of the form fields
        item.text = mEditText.getText().toString();
        item.priority = mSpinPrior.getSelectedItemPosition();
        if (mSpinDate.getSelectedItemPosition() == 2)
            item.due_ts = new MyDate((String) mSpinDate.getSelectedItem()).getTimestamp();
        else
            item.due_ts = null;
        // Do not change the creation date if only editing the item
        if (isInMainActivity()) {
            item.creation_ts = new MyDate().getTimestamp();
        }

        // If creating a new item, save it in the database and in the ListView
        if (isInMainActivity()) {
            // Add the new item to the database and to the ArrayList of the ListView
            cupboard().withDatabase(mDb).put(item);
            mMainActivity.mItemsAdapter.add(item);
            mMainActivity.mItemsAdapter.notifyDataSetChanged();
            // Reset input fields
            mEditText.setText("");
            mSpinPrior.setSelection(0);
            if (isDateSelectedInSpinner()) resetDateSpinnerItems();
            // Scroll to end of list
            mMainActivity.mListView.setSelection(mMainActivity.mItemsAdapter.getCount() - 1);
        }
        // If editing an item, send the modified item back to the MainActivity, which will save it
        else if (isInEditActivity()) {
            Intent result = new Intent();
            result.putExtra(MainActivity.EXTRA_CODE_ITEM, item);
            result.putExtra(MainActivity.EXTRA_CODE_ITEM_POS, mEditActivity.mPosition);
            mEditActivity.setResult(RESULT_OK, result);
            mEditActivity.finish();
        }
        return true;
    }

    // Swap in the basic date spinner items (0="no date", 1="select date...")
    private void resetDateSpinnerItems() {
        mSpinDateAdapter.clear();
        mSpinDateAdapter.addAll(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.spin_date_1))));
        mSpinDateAdapter.notifyDataSetChanged();
        mSpinDate.setSelection(0);
    }

    // Called after a date has been selected. If previously no date was selected, swap in the
    // extended spinner items (0="no date", 1="change date...", 2=<date>). If the extended spinner
    // items were already there (because a date was already selected), replace the content of the
    // <date> item (position 2) with the newly selected date.
    private void extendDateSpinnerItems(MyDate date) {
        if (isDateSelectedInSpinner())
            mSpinDateItems.set(2, getDueDateSpinnerString(date));
        else {
            mSpinDateAdapter.clear();
            mSpinDateAdapter.addAll(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.spin_date_2))));
            mSpinDateItems.add(getDueDateSpinnerString(date));
        }
        mSpinDateAdapter.notifyDataSetChanged();
        mSpinDate.setSelection(2);
    }

    // Return true if the date spinner items are in their extended form, and false otherwise
    private boolean isDateSelectedInSpinner() {
        return mSpinDateItems.size() > 2;
    }

    // Return true if this fragment is attached to a MainActivity, and false otherwise
    private boolean isInMainActivity() {
        return mMainActivity != null;
    }

    // Return true if this fragment is attached to an EditActivity, and false otherwise
    private boolean isInEditActivity() {
        return mEditActivity != null;
    }

    // Construct the string for the <date> spinner item (position 2)
    private String getDueDateSpinnerString(MyDate date) {
        return "Due " + date.toString();
    }

    /**
     * Date picker dialog.
     * Created by dw on 27/01/17.
     */
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private final String LOG_TAG = DatePickerFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            FormFragment f = (FormFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
            // If a date is already selected in the spinner, set the date picker to this date
            if (f.isDateSelectedInSpinner()) {
                MyDate date = new MyDate((String) f.mSpinDate.getItemAtPosition(2));
                return new DatePickerDialog(getActivity(), this, date.getYear(), date.getMonth(), date.getDay());
            }
            // If no date is selected in the spinner yet, set the date picker to today's date
            else {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(getActivity(), this, year, month, day);
            }
        }

        // Called when the user selected a date from the date picker dialog
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            MyDate date = new MyDate(year, month, dayOfMonth);
            FormFragment f = (FormFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
            f.extendDateSpinnerItems(date);
        }

        // Called when the user cancelled the date picker dialog
        @Override
        public void onCancel(DialogInterface dialog) {
            FormFragment f = (FormFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
            // Set the selected item away from the currently selected "Select..." item
            if (f.isDateSelectedInSpinner())
                f.mSpinDate.setSelection(2);
            else
                f.mSpinDate.setSelection(0);
        }
    }
}

package org.weibeld.mytodo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.weibeld.mytodo.R.array.date;

/**
 * Created by dw on 06/02/17.
 */

public class FormFragment extends Fragment {

    SQLiteDatabase mDb;

    Spinner mSpinPrior;
    ArrayAdapter<CharSequence> mSpinPriorAdapter;
    Spinner mSpinDate;
    ArrayAdapter<CharSequence> mSpinDateAdapter;
    ArrayList<String> mSpinDateItems;

    EditText mEditText;

    MainActivity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_form, container, false);

        mDb = (new TodoDatabaseHelper(getActivity())).getWritableDatabase();

        mEditText = (EditText) rootView.findViewById(R.id.etNewItem);

        mActivity = (MainActivity) getActivity();

        // Priority spinner (read selected value in onAdditem method)
        mSpinPrior = (Spinner) rootView.findViewById(R.id.spinPriority);
        mSpinPriorAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.priority, android.R.layout.simple_spinner_item);
        mSpinPriorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinPrior.setAdapter(mSpinPriorAdapter);

        // Date spinner (read selected value in onAdditem method)
        mSpinDate = (Spinner) rootView.findViewById(R.id.spinDate);
        mSpinDateItems = new ArrayList<>(Arrays.asList(getResources().getStringArray(date)));
        mSpinDateAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mSpinDateItems);
        mSpinDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinDate.setAdapter(mSpinDateAdapter);
        mSpinDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // "None" selected: remove the dynamically added date item if present
                if (position == 0) {
                    if (mSpinDateItems.size() > 2) {
                        mSpinDateItems.remove(2);
                        mSpinDateAdapter.notifyDataSetChanged();
                    }
                }
                // "Select..." selected: launch the date picker dialog
                if (position == 1) {
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getFragmentManager(), "datePicker");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button button = (Button) rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.length() == 0) {
                    Toast.makeText(getActivity(), R.string.toast_enter_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Create a new TodoItem based on the information in the input fields
                TodoItem item = new TodoItem();
                item.text =  mEditText.getText().toString();;
                item.priority = mSpinPrior.getSelectedItemPosition();
                switch(mSpinDate.getSelectedItemPosition()) {
                    case 0:
                    case 1:
                        item.date = -1;
                        break;
                    case 2:
                        item.date = parseDate((String) mSpinDate.getSelectedItem());
                        break;
                    default:
                        item.date = -1;
                }
                // Add the new item to the database and to the ArrayList
                cupboard().withDatabase(mDb).put(item);
                mActivity.mItemsAdapter.add(item);
                mActivity.mItemsAdapter.notifyDataSetChanged();
                // Reset input fields
                mEditText.setText("");
                mSpinPrior.setSelection(0);
                if (mSpinDateItems.size() > 2) {
                    mSpinDateItems.remove(2);
                    mSpinDateAdapter.notifyDataSetChanged();
                }
                mSpinDate.setSelection(0);
                // Scroll to end of list
                mActivity.mListView.setSelection(mActivity.mItemsAdapter.getCount() - 1);
            }
        });

        return rootView;
    }

    // Parse a string containing a "dd/mm/yy" date and return the UNIX timestamp (ms) of this date
    private long parseDate(String dateStr) {
        Pattern pattern = Pattern.compile("(\\d\\d?)/(\\d\\d?)/(\\d\\d?)");
        Matcher matcher = pattern.matcher(dateStr);
        if (!matcher.find())
            (new Exception("Invalid date string: " + dateStr)).printStackTrace();
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2)) - 1;
        int year = Integer.parseInt(matcher.group(3)) + 2000;
        GregorianCalendar date = new GregorianCalendar();
        date.set(year, month, day);
        return date.getTimeInMillis();
    }

    /**
     * Date picker dialog.
     * Created by dw on 27/01/17.
     */
    // TODO: improve handling of dates (new class providing format and parse methods)
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private final String LOG_TAG = DatePickerFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            // TODO: if a date has been previously selected, set this date as the default date
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        // Called if the user selected a date from the date picker dialog
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            month = month + 1;  // Months are numbered 0-11
            year = year % 100;  // Keep only two-digit year to use less space
            String date = "Due " + dayOfMonth + "/" + month + "/" + year;
            FormFragment f = (FormFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
            // If a date has been selected before, replace it in the spinner, else add the new date
            if (f.mSpinDateItems.size() > 2)
                f.mSpinDateItems.set(2, date);
            else
                f.mSpinDateItems.add(date);
            f.mSpinDateAdapter.notifyDataSetChanged();
            f.mSpinDate.setSelection(2);
        }

        // Called if the user cancelled the date picker dialog
        @Override
        public void onCancel(DialogInterface dialog) {
            FormFragment f = (FormFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_container);
            // Set the selected item away from the currently selected "Select..." item
            if (f.mSpinDateItems.size() > 2)
                f.mSpinDate.setSelection(2);
            else
                f.mSpinDate.setSelection(0);
        }
    }
}

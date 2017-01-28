package org.weibeld.mytodo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;
import static org.weibeld.mytodo.R.array.date;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final int REQUEST_CODE_EDIT = 1;
    static final String EXTRA_CODE_ITEM_POS = "position";
    static final String EXTRA_CODE_ITEM = "item";

    SQLiteDatabase mDb;

    ArrayList<TodoItem> mItems;
    TodoItemAdapter mItemsAdapter;
    ListView mListView;

    Spinner mSpinPrior;
    ArrayAdapter<CharSequence> mSpinPriorAdapter;
    Spinner mSpinDate;
    ArrayAdapter<CharSequence> mSpinDateAdapter;
    ArrayList<String> mSpinDateItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Priority spinner (read selected value in onAdditem method)
        mSpinPrior = (Spinner) findViewById(R.id.spinPriority);
        mSpinPriorAdapter = ArrayAdapter.createFromResource(this, R.array.priority, android.R.layout.simple_spinner_item);
        mSpinPriorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinPrior.setAdapter(mSpinPriorAdapter);

        // Date spinner (read selected value in onAdditem method)
        mSpinDate = (Spinner) findViewById(R.id.spinDate);
        mSpinDateItems = new ArrayList<>(Arrays.asList(getResources().getStringArray(date)));
        mSpinDateAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mSpinDateItems);
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

        mDb = (new TodoDatabaseHelper(this)).getWritableDatabase();

        mListView = (ListView) findViewById(R.id.lvItems);
        readItems();  // Initialises 'mItems'
        mItemsAdapter = new TodoItemAdapter(this, mItems);
        mListView.setAdapter(mItemsAdapter);
        setupListViewListener();
    }

    private void setupListViewListener() {
        // Delete item on long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                cupboard().withDatabase(mDb).delete(mItems.get(position));  //  Delete from DB
                mItems.remove(position);  // Delete from ArrayList
                mItemsAdapter.notifyDataSetChanged();
                return true;
            }
        });
        // Launch EditActivity on short click
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass the selected item to the EditActivity, which will return the updated item
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra(EXTRA_CODE_ITEM, mItems.get(position));
                intent.putExtra(EXTRA_CODE_ITEM_POS, position);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Replace the old version of the item with the edited one
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_EDIT) {
            TodoItem item = (TodoItem) data.getExtras().get(EXTRA_CODE_ITEM);
            int position = data.getExtras().getInt(EXTRA_CODE_ITEM_POS);
            cupboard().withDatabase(mDb).put(item);  // Update item in database
            mItems.set(position, item);  // Update item in ArrayList
            mItemsAdapter.notifyDataSetChanged();
        }
    }

    // Add a new item to the database and to the ArrayList
    public void onAddItem(View view) throws Exception {
        EditText editText = (EditText) findViewById(R.id.etNewItem);
        if (editText.length() == 0) {
            Toast.makeText(this, R.string.toast_enter_text, Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a new TodoItem based on the information in the input fields
        TodoItem item = new TodoItem();
        item.text =  editText.getText().toString();;
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
        mItemsAdapter.add(item);
        mItemsAdapter.notifyDataSetChanged();
        // Reset input fields
        editText.setText("");
        mSpinPrior.setSelection(0);
        if (mSpinDateItems.size() > 2) {
            mSpinDateItems.remove(2);
            mSpinDateAdapter.notifyDataSetChanged();
        }
        mSpinDate.setSelection(0);
        // Scroll to end of list
        mListView.setSelection(mItemsAdapter.getCount() - 1);
    }

    // Parse a string containing a "dd/mm/yy" date and return the UNIX timestamp (ms) of this date
    private long parseDate(String dateStr) throws Exception {
        Pattern pattern = Pattern.compile("(\\d\\d?)/(\\d\\d?)/(\\d\\d?)");
        Matcher matcher = pattern.matcher(dateStr);
        if (!matcher.find())
            throw new Exception("Invalid date string: " + dateStr);
        int day = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2)) - 1;
        int year = Integer.parseInt(matcher.group(3)) + 2000;
        GregorianCalendar date = new GregorianCalendar();
        date.set(year, month, day);
        return date.getTimeInMillis();
    }

    // Initialise the 'mItems' ArrayList with all the items in the database
    private void readItems() {
        mItems = new ArrayList<>();
        Cursor cursor = cupboard().withDatabase(mDb).query(TodoItem.class).getCursor();
        try {
            QueryResultIterable<TodoItem> iter = cupboard().withCursor(cursor).iterate(TodoItem.class);
            for (TodoItem item : iter) mItems.add(item);
        } finally {
            cursor.close();
        }
    }


    /**
     * Custom ArrayAdapter for displaying an item in the ListView.
     */
    public static class TodoItemAdapter extends ArrayAdapter<TodoItem> {

        private final String LOG_TAG = TodoItemAdapter.class.getSimpleName();

        public TodoItemAdapter(Context context, ArrayList<TodoItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TodoItem item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_todo, parent, false);
            }
            TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
            TextView tvPriority = (TextView) convertView.findViewById(R.id.tvPriority);
            TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);

            tvText.setText(item.text);
            switch (item.priority) {
                case 0:
                    tvPriority.setText("");
                    break;
                case 1:
                    tvPriority.setText(R.string.priority_high);
                    tvPriority.setTextColor(Color.RED);
                    break;
                case 2:
                    tvPriority.setText(R.string.priority_medium);
                    tvPriority.setTextColor(Color.parseColor("#FDE541"));  // Readable yellow
                    break;
                case 3:
                    tvPriority.setText(R.string.priority_low);
                    tvPriority.setTextColor(Color.GREEN);
                    break;
                default:
                    tvPriority.setText("");
            }

            Log.v(LOG_TAG, "item.date = " + item.date);
            if (item.date == -1)
                tvDate.setText("");
            else {
                Log.v(LOG_TAG, "item.date = " + item.date);
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy");
                String dateStr = sdf.format(item.date);
                Log.v(LOG_TAG, "date: " + dateStr);
                tvDate.setText(sdf.format(item.date));
                tvDate.setTextColor(Color.GRAY);
            }
            return convertView;
        }
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
            MainActivity a = (MainActivity) getActivity();
            // If a date has been selected before, replace it in the spinner, else add the new date
            if (a.mSpinDateItems.size() > 2)
                a.mSpinDateItems.set(2, date);
            else
                a.mSpinDateItems.add(date);
            a.mSpinDateAdapter.notifyDataSetChanged();
            a.mSpinDate.setSelection(2);
        }

        // Called if the user cancelled the date picker dialog
        @Override
        public void onCancel(DialogInterface dialog) {
            MainActivity a = (MainActivity) getActivity();
            // Set the selected item away from the currently selected "Select..." item
            if (a.mSpinDateItems.size() > 2)
                a.mSpinDate.setSelection(2);
            else
                a.mSpinDate.setSelection(0);
        }
    }
}

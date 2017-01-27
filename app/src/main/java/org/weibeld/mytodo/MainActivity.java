package org.weibeld.mytodo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;

import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final int REQUEST_CODE_EDIT = 1;
    static final String EXTRA_CODE_ITEM_POS = "position";
    static final String EXTRA_CODE_ITEM = "item";

    ArrayList<TodoItem> mItems;
    TodoItemAdapter mItemsAdapter;
    ListView mListView;
    SQLiteDatabase mDb;

    Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpinner = (Spinner) findViewById(R.id.spinPriority);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this, R.array.priority_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinAdapter);

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

    // Add a new item to the database and the ArrayList
    public void onAddItem(View view) {
        EditText editText = (EditText) findViewById(R.id.etNewItem);
        String text = editText.getText().toString();
        TodoItem item = new TodoItem();
        item.text = text;
        item.priority = mSpinner.getSelectedItemPosition();
        cupboard().withDatabase(mDb).put(item);  // Add item to database
        mItemsAdapter.add(item);  // Add item to ArrayList
        mItemsAdapter.notifyDataSetChanged();
        editText.setText("");
        mListView.setSelection(mItemsAdapter.getCount() - 1);
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

            tvText.setText(item.text);
            switch (item.priority) {
                case 0:
                    tvPriority.setText("");
                    break;
                case 1:
                    tvPriority.setText("H");
                    tvPriority.setTextColor(Color.RED);
                    break;
                case 2:
                    tvPriority.setText("M");
                    tvPriority.setTextColor(Color.parseColor("#FDE541"));  // Readable yellow
                    break;
                case 3:
                    tvPriority.setText("L");
                    tvPriority.setTextColor(Color.GREEN);
                    break;
                default:
                    tvPriority.setText("");
            }
            return convertView;
        }
    }
}

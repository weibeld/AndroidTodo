package org.weibeld.mytodo;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final int REQUEST_CODE_EDIT = 1;
    static final String EXTRA_CODE_ITEM_POS = "position";
    static final String EXTRA_CODE_ITEM = "item";

    SQLiteDatabase mDb;

    ArrayList<TodoItem> mItems;
    TodoItemAdapter mItemsAdapter;
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new FormFragment()).commit();
        }

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

}

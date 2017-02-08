package org.weibeld.mytodo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;
import org.weibeld.mytodo.util.MyDate;

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

        // Set Toolbar as the app bar (instead of default ActionBar)
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new FormFragment()).commit();
        }

        mDb = (new TodoDatabaseHelper(this)).getWritableDatabase();

        mListView = (ListView) findViewById(R.id.lvItems);
        readItems();  // Initialises 'mItems'
        mItemsAdapter = new TodoItemAdapter(this, mItems);
        mListView.setAdapter(mItemsAdapter);
        setupListViewListener();
        // Set up contextual action mode (contextual action bar) when selecting multiple items
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new TodoItemMultiChoiceModeListener());
    }

    private void setupListViewListener() {
        // Delete item on long click
//        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                cupboard().withDatabase(mDb).delete(mItems.get(position));  //  Delete from DB
//                mItems.remove(position);  // Delete from ArrayList
//                mItemsAdapter.notifyDataSetChanged();
//                return true;
//            }
//        });
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

            if (item.due_ts == null)
                tvDate.setText("");
            else {
                MyDate date = new MyDate(item.due_ts);
                tvDate.setText(date.toString());
                tvDate.setTextColor(Color.GRAY);
            }
            return convertView;
        }
    }

    // MultiChoiceModeListener for the ListView used in the contextual action mode (initiated by
    // selecting an item by a long click, then more items can be selected by short clicks). The
    // contextual action mode overlays the app bar by the contextual action bar which is displayed
    // as long as the contextual action mode is active.
    private static class TodoItemMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        // Called when the contextual action mode is initiated
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the contextual action bar
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.main_contextual, menu);
            return true;
        }

        // Called when an item is selected/deselected
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            // e.g. update number of selected items in contextual action bar
        }

        // Called when an action (menu item) in the contextual action bar is clicked
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // Show dialog confirming deletion of selected items
                    mode.finish();  // Exit the contextual action mode
                    return true;
                default:
                    return false;
            }
        }

        // Called when exiting the contextual action mode (default action: deselect all items)
        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        // Called after a call to ActionMode.invalidate()
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }
}

package org.weibeld.mytodo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.util.Collections;
import java.util.Comparator;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            // Called when the contextual action mode is initiated
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the contextual action bar
                mode.getMenuInflater().inflate(R.menu.main_contextual, menu);
                return true;
            }

            // Called when an item is selected/deselected. Set title of contextual action bar.
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int n = mListView.getCheckedItemCount();
                if (n == 0)
                    mode.setTitle("");
                else
                    mode.setTitle(getResources().getQuantityString(R.plurals.title_context_bar, n, n));
            }

            // Called when an action (menu item) in the contextual action bar is clicked
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        showDeletionConfirmationDialog(mode);
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
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_priority:
                return true;
            case R.id.action_sort_due:
                return true;
            case R.id.action_sort_creation:
                Collections.sort(mItems, new CreationDateComparator());
                mItemsAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_sort_alphabet:
                Collections.sort(mItems, new AlphabeticComparator());
                mItemsAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeletionConfirmationDialog(ActionMode mode) {
        final ActionMode m = mode;
        new AlertDialog.Builder(this).
                setMessage(R.string.dialog_delete_items).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).
                setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedItems(mListView.getCheckedItemPositions());
                        m.finish();
                    }
                }).
                create().
                show();
    }

    // Delete all the items that are selected. The argument is supposed to be the return value of
    // ListView.getCheckedItemPositions().
    private void deleteSelectedItems(SparseBooleanArray selectedItems) {
        ArrayList<TodoItem> itemsToDelete = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            TodoItem currentItem = mItems.get(selectedItems.keyAt(i));
            cupboard().withDatabase(mDb).delete(currentItem);
            itemsToDelete.add(currentItem);
        }
        mItems.removeAll(itemsToDelete);
        mItemsAdapter.notifyDataSetChanged();
    }

    private void setupListViewListener() {
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
                    tvPriority.setText(R.string.label_priority_h);
                    tvPriority.setTextColor(Color.RED);
                    break;
                case 2:
                    tvPriority.setText(R.string.label_priority_m);
                    tvPriority.setTextColor(Color.parseColor("#FDE541"));  // Readable yellow
                    break;
                case 3:
                    tvPriority.setText(R.string.label_priority_l);
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

    public static class CreationDateComparator implements  Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return Long.signum(o1.creation_ts - o2.creation_ts);
        }
    }

    public static class AlphabeticComparator implements Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return o1.text.compareToIgnoreCase(o2.text);
        }
    }
}

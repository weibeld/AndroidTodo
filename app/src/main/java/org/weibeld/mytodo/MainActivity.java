package org.weibeld.mytodo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    static final String EXTRA_ITEM_POSITION = "position";
    static final String EXTRA_ITEM = "item";

    // Sort orders
    static final int SORT_CREATION_DATE_OLD_TOP = 0;
    static final int SORT_CREATION_DATE_NEW_TOP = 1;
    static final int SORT_ALPHABETICALLY = 2;
    static final int SORT_PRIORITY = 3;
    static final int SORT_DUE_DATE = 4;

    SQLiteDatabase mDb;
    SharedPreferences mSharedPrefs;

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
        mSharedPrefs = getPreferences(Context.MODE_PRIVATE);

        mListView = (ListView) findViewById(R.id.lvItems);
        // Initialise the ArrayList mItems by reading data from the database
        readItems();
        mItemsAdapter = new TodoItemAdapter(this, mItems);
        mListView.setAdapter(mItemsAdapter);
        // Sort the items according to the sort order saved in the SharedPreferences
        sortItems();
        setupListViewListener();

        // Set up contextual action mode (when selecting multiple items)
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

    private void setupListViewListener() {
        // Launch EditActivity on short click
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Pass the selected item to the EditActivity, which will return the updated item
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra(EXTRA_ITEM, mItems.get(position));
                intent.putExtra(EXTRA_ITEM_POSITION, position);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });
    }

    // Initialise the 'mItems' ArrayList with all the TodoItems in the database
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

    // Sort the items of the ListView according to one of the sort orders (the display is immediate)
    public void sortItems() {
        switch (getCurrentSortOrder()) {
            case SORT_CREATION_DATE_OLD_TOP:
                Collections.sort(mItems, new CreationDateComparatorOldTop());
                break;
            case SORT_CREATION_DATE_NEW_TOP:
                Collections.sort(mItems, new CreationDateComparatorNewTop());
                break;
            case SORT_ALPHABETICALLY:
                Collections.sort(mItems, new AlphabeticComparator());
                break;
            case SORT_PRIORITY:
                Collections.sort(mItems, new PriorityComparator());
                break;
            case SORT_DUE_DATE:
                Collections.sort(mItems, new DueDateComparator());
                break;
        }
        mItemsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Check the item of the "Sort" submenu that is currently set in the SharedPreferences
        switch (getCurrentSortOrder()) {
            case SORT_CREATION_DATE_OLD_TOP:
                menu.findItem(R.id.action_sort_creation_old_top).setChecked(true);
                break;
            case SORT_CREATION_DATE_NEW_TOP:
                menu.findItem(R.id.action_sort_creation_new_top).setChecked(true);
                break;
            case SORT_ALPHABETICALLY:
                menu.findItem(R.id.action_sort_alphabet).setChecked(true);
                break;
            case SORT_PRIORITY:
                menu.findItem(R.id.action_sort_priority).setChecked(true);
                break;
            case SORT_DUE_DATE:
                menu.findItem(R.id.action_sort_due).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If one of the items of the "Sort" submenu is selected, sort the elements of the ListView
        // and save the newly selected sort order in the SharedPreferences
        if (item.getGroupId() == R.id.action_group_sort) {
            item.setChecked(true);
            SharedPreferences.Editor e = mSharedPrefs.edit();
            switch (item.getItemId()) {
                case R.id.action_sort_priority:
                    Collections.sort(mItems, new PriorityComparator());
                    e.putInt(getString(R.string.pref_key_sort), SORT_PRIORITY);
                    break;
                case R.id.action_sort_due:
                    Collections.sort(mItems, new DueDateComparator());
                    e.putInt(getString(R.string.pref_key_sort), SORT_DUE_DATE);
                    break;
                case R.id.action_sort_creation_old_top:
                    Collections.sort(mItems, new CreationDateComparatorOldTop());
                    e.putInt(getString(R.string.pref_key_sort), SORT_CREATION_DATE_OLD_TOP);
                    break;
                case R.id.action_sort_creation_new_top:
                    Collections.sort(mItems, new CreationDateComparatorNewTop());
                    e.putInt(getString(R.string.pref_key_sort), SORT_CREATION_DATE_NEW_TOP);
                    break;
                case R.id.action_sort_alphabet:
                    Collections.sort(mItems, new AlphabeticComparator());
                    e.putInt(getString(R.string.pref_key_sort), SORT_ALPHABETICALLY);
                    break;
                default:
                    e.apply();
                    return super.onOptionsItemSelected(item);
            }
            e.apply();
            mItemsAdapter.notifyDataSetChanged();
            mListView.setSelection(0);  // Scroll to the top of the list
            return true;
        }
        return false;
    }

    // Return the sort order, according to the SORT_* constants defined in this activity, that is
    // currently set in the SharedPreferences. Return SORT_CREATION_DATE_OLD_TOP as the default
    // sort order in case no sort order is saved in the SharedPreferences yet (because the app has
    // just been installed)
    public int getCurrentSortOrder() {
        return mSharedPrefs.getInt(getString(R.string.pref_key_sort), SORT_CREATION_DATE_OLD_TOP);
    }

    // Show a dialog for confirming the deletion of all the items selected in contextual action mode
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

    // Delete all the items returned by ListView.getCheckedItemPositions()
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When the EditActivity sends a modified TodoItem back to the MainActivity
        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            TodoItem item = (TodoItem) data.getExtras().get(EXTRA_ITEM);
            // Update the edited item in the database
            cupboard().withDatabase(mDb).put(item);
            // Update the edited item in the ListView
            mItems.set(data.getExtras().getInt(EXTRA_ITEM_POSITION), item);
            sortItems();
            mItemsAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Custom ArrayAdapter for displaying a TodoItem in the ListView.
     */
    public class TodoItemAdapter extends ArrayAdapter<TodoItem> {

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

            // Set text
            tvText.setText(item.text);

            // Set priority (if any)
            switch (item.priority) {
                case 1:
                    tvPriority.setText(R.string.label_priority_h);
                    tvPriority.setTextColor(getResources().getColor(R.color.priorityHigh));
                    break;
                case 2:
                    tvPriority.setText(R.string.label_priority_m);
                    tvPriority.setTextColor(getResources().getColor(R.color.priorityMedium));
                    break;
                case 3:
                    tvPriority.setText(R.string.label_priority_l);
                    tvPriority.setTextColor(getResources().getColor(R.color.priorityLow));
                    break;
                default:
                    tvPriority.setText("");
            }

            // Set due date (if any)
            if (item.due_ts == null)
                tvDate.setText("");
            else
                tvDate.setText(new MyDate(item.due_ts).toString());
            return convertView;
        }
    }

    /**
     * Compare two TodoItems w.r.t. their creation date. A newer date is greater than (comes after)
     * an older date.
     */
    public static class CreationDateComparatorOldTop implements  Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return compareCreationDatesOldTop(o1, o2);
        }
    }

    /**
     * Compare two TodoItems w.r.t. their creation date: a newer date is less than (comes before)
     * an older date.
     */
    public static class CreationDateComparatorNewTop implements  Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return compareCreationDatesNewTop(o1, o2);
        }
    }

    /**
     * Compare two TodoItems w.r.t. their text in alphabetical order (case-insensitive)
     */
    public static class AlphabeticComparator implements Comparator<TodoItem> {
        // Note: omit any secondary sort order in case that two text fields are exactly equal
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            return o1.text.compareToIgnoreCase(o2.text);
        }
    }

    /**
     * Compare two TodoItems according to the following scheme:
     *     - Priority (high > medium > low > none)
     * In case of equal priority:
     *     - Due date (smaller due date > larger due date > no due date)
     * In case of equal due date:
     *     - Creation date (larger creation date > smaller creation date (old on top))
     */
    public static class PriorityComparator implements Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            if (o1.hasHigherPriority(o2))
                return -1;
            else if (o1.hasLowerPriority(o2))
                return 1;
            else {
                if (o1.isMoreUrgent(o2))
                    return -1;
                else if (o1.isLessUrgent(o2))
                    return 1;
                else
                    return compareCreationDatesOldTop(o1, o2);
            }

        }
    }

    /**
     * Compare two TodoItems according to the following scheme:
     *     - Due date (smaller due date > larger due date > no due date)
     * In case of equal due date:
     *     - Priority (high > medium > low > none)
     * In case of equal priority:
     *     - Creation date (larger creation date > smaller creation date (old on top))
     */
    public static class DueDateComparator implements Comparator<TodoItem> {
        @Override
        public int compare(TodoItem o1, TodoItem o2) {
            if (o1.isMoreUrgent(o2))
                return -1;
            else if (o1.isLessUrgent(o2))
                return 1;
            else {
                if (o1.hasHigherPriority(o2))
                    return -1;
                else if (o1.hasLowerPriority(o2))
                    return 1;
                else
                    return compareCreationDatesOldTop(o1, o2);
            }
        }
    }

    // Compare two TodoItems w.r.t. their creation date: larger (newer) date > smaller (older) date
    private static int compareCreationDatesOldTop(TodoItem o1, TodoItem o2) {
        if (o1.isOlder(o2))
            return -1;
        else if (o1.isYounger(o2))
            return 1;
        else
            return 0;
    }

    // Compare two TodoItems w.r.t. their creation date: larger (newer) date < smaller (older) date
    private static int compareCreationDatesNewTop(TodoItem o1, TodoItem o2) {
        return -1 * compareCreationDatesOldTop(o1, o2);
    }
}

package org.weibeld.mytodo;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.weibeld.mytodo.data.DoneItem;
import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.data.TodoItem;
import org.weibeld.mytodo.util.MyDate;
import org.weibeld.mytodo.util.MyListActivity;
import org.weibeld.mytodo.util.Util;

import java.util.ArrayList;
import java.util.Collections;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Activity displaying a list of all the "done" todo items.
 */
public class ArchiveActivity extends MyListActivity<DoneItem> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        // Set up Toolbar as the app bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_archive_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDb = (new TodoDatabaseHelper(this)).getWritableDatabase();

        mListView = (ListView) findViewById(R.id.lvDoneItems);
        readItems();
        mAdapter = new DoneItemAdapter(this, mItems);
        mListView.setAdapter(mAdapter);

        // Set up contextual action mode (when selecting multiple items)
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            // Called when the contextual action mode is initiated
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the contextual action bar
                mode.getMenuInflater().inflate(R.menu.archive_contextual, menu);
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
                        Util.confirmDeleteSelectedItems(getMyActivity(), mode);
                        return true;
                    case R.id.action_put_back:
                        // Create TodoItems from the DoneItems and insert them in the TodoItem table
                        ArrayList<DoneItem> doneItems = Util.getSelectedItems(getMyActivity());
                        for (DoneItem doneItem : doneItems) {
                            TodoItem todoItem = new TodoItem(doneItem);
                            cupboard().withDatabase(mDb).put(todoItem);
                        }
                        Util.deleteSelectedItems(getMyActivity());
                        mode.finish();
                        Util.toast(getMyActivity(), getResources().getQuantityString(R.plurals.toast_done2todo, doneItems.size(), doneItems.size()));
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

    // Initialise the 'mDoneItems' ArrayList with all the DoneItems in the database
    private void readItems() {
        mItems = new ArrayList<>();
        Cursor cursor = cupboard().withDatabase(mDb).query(DoneItem.class).getCursor();
        try {
            QueryResultIterable<DoneItem> iter = cupboard().withCursor(cursor).iterate(DoneItem.class);
            for (DoneItem item : iter) mItems.add(item);
        } finally {
            cursor.close();
        }
        // Reverse the list so that the newest DoneItems are at the top (beginning) of the list
        Collections.reverse(mItems);
    }

    /**
     * Custom ArrayAdapter for displaying a DoneItem in the ListView of ArchiveActivity.
     */
    public class DoneItemAdapter extends ArrayAdapter<DoneItem> {

        private final String LOG_TAG = DoneItemAdapter.class.getSimpleName();

        public DoneItemAdapter(Context context, ArrayList<DoneItem> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DoneItem item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_done, parent, false);
            }

            TextView tvText = (TextView) convertView.findViewById(R.id.tvText);
            TextView tvPriority = (TextView) convertView.findViewById(R.id.tvPriority);
            TextView tvDueDate = (TextView) convertView.findViewById(R.id.tvDueDate);
            TextView tvCreationDate = (TextView) convertView.findViewById(R.id.tvCreationDate);
            TextView tvDoneDate = (TextView) convertView.findViewById(R.id.tvDoneDate);

            // Set text
            tvText.setText(item.text);

            // Set text for priority
            String priority = item.getPriorityString(getApplicationContext());
            tvPriority.setText(String.format(getString(R.string.archive_label_priority), priority));

            // Set text for due date
            String dueDate = (item.due_ts == null) ? getString(R.string.none) : new MyDate(item.due_ts).formatDateLong();
            tvDueDate.setText(String.format(getString(R.string.archive_label_due_date), dueDate));

            // Set text for creation date
            String creationDate = new MyDate(item.creation_ts).formatDateDayTime();
            tvCreationDate.setText(String.format(getString(R.string.archive_label_creation_date), creationDate));

            // Set text for done date
            String doneDate = new MyDate(item.done_ts).formatDateDayTime();
            tvDoneDate.setText(String.format(getString(R.string.archive_label_done_date), doneDate));

            return convertView;
        }
    }

}

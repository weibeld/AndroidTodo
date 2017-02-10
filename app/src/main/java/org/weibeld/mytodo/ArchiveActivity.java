package org.weibeld.mytodo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.weibeld.mytodo.data.DoneItem;
import org.weibeld.mytodo.data.TodoDatabaseHelper;
import org.weibeld.mytodo.util.MyDate;

import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by dw on 10/02/17.
 */
public class ArchiveActivity extends AppCompatActivity {

    SQLiteDatabase mDb;

    ArrayList<DoneItem> mDoneItems;
    DoneItemAdapter mDoneItemsAdapter;
    ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);
        // Set Toolbar as the app bar (instead of default ActionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_archive_activity);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDb = (new TodoDatabaseHelper(this)).getWritableDatabase();

        mListView = (ListView) findViewById(R.id.lvDoneItems);
        readItems();
        mDoneItemsAdapter = new DoneItemAdapter(this, mDoneItems);
        mListView.setAdapter(mDoneItemsAdapter);
    }

    // Initialise the 'mDoneItems' ArrayList with all the TodoItems in the database
    private void readItems() {
        mDoneItems = new ArrayList<>();
        Cursor cursor = cupboard().withDatabase(mDb).query(DoneItem.class).getCursor();
        try {
            QueryResultIterable<DoneItem> iter = cupboard().withCursor(cursor).iterate(DoneItem.class);
            for (DoneItem item : iter) mDoneItems.add(item);
        } finally {
            cursor.close();
        }
    }

    /**
     * Custom ArrayAdapter for displaying a TodoItem in the ListView.
     */
    public class DoneItemAdapter extends ArrayAdapter<DoneItem> {

        private final String LOG_TAG = MainActivity.TodoItemAdapter.class.getSimpleName();

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

            tvText.setText(item.text);
            String priority = item.getPriorityString(getApplicationContext());
            tvPriority.setText(String.format(getString(R.string.archive_label_priority), priority));
            String dueDate;
            if (item.due_ts == null)
                dueDate = getString(R.string.dash);
            else
                dueDate = new MyDate(item.due_ts).formatShort();
            tvDueDate.setText(String.format(getString(R.string.archive_label_due_date), dueDate));
            String creationDate = new MyDate(item.creation_ts).formatLong();
            tvCreationDate.setText(String.format(getString(R.string.archive_label_creation_date), creationDate));
            String doneDate = new MyDate(item.done_ts).formatLong();
            tvDoneDate.setText(String.format(getString(R.string.archive_label_done_date), doneDate));

            return convertView;
        }
    }

}

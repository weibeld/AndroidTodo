package org.weibeld.mytodo.util;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by dw on 13/02/17.
 */

public abstract class MyListActivity<E> extends AppCompatActivity {

    protected MyListActivity mActivity;
    protected SQLiteDatabase mDb;
    protected ArrayList<E> mItems;
    protected ArrayAdapter mAdapter;
    protected ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
    }

    public MyListActivity getMyActivity() {
        return mActivity;
    }

    public SQLiteDatabase getDatabase() {
        return mDb;
    }

    public ArrayList<E> getData() {
        return mItems;
    }

    public ArrayAdapter getAdapter() {
        return mAdapter;
    }

    public ListView getListView() {
        return mListView;
    }
}

package org.weibeld.mytodo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * SQLiteOpenHelper powered by Cupboard https://guides.codepath.com/android/Easier-SQL-with-Cupboard
 */
public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private final String LOG_TAG = TodoDatabaseHelper.class.getSimpleName();

    // If the DB schema is changed (e.g. change column name), this version number must be updated
    private static final int DATABASE_VERSION = 4;
    static final String DATABASE_NAME = "todo.db";

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static {
        cupboard().register(TodoItem.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cupboard().withDatabase(db).upgradeTables();
    }
}

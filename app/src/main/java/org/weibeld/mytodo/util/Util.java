package org.weibeld.mytodo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.widget.Toast;

import org.weibeld.mytodo.ArchiveActivity;
import org.weibeld.mytodo.MainActivity;
import org.weibeld.mytodo.R;
import org.weibeld.mytodo.data.DoneItem;
import org.weibeld.mytodo.data.TodoItem;

import java.util.ArrayList;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by dw on 13/02/17.
 */

public class Util {

    // Show a dialog for confirming the deletion of all the items selected in contextual action mode
    public static void confirmDeleteSelectedItems(final MyListActivity a, final ActionMode mode) {
        new AlertDialog.Builder(a).
                setMessage(R.string.dialog_delete_items).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).
                setPositiveButton(a.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedItems(a);
                        mode.finish();
                    }
                }).
                create().
                show();
    }

    // Return the list of all the items that are selected in the contextual action mode
    public static ArrayList getSelectedItems(MyListActivity a) {
        SparseBooleanArray itemPositions = a.getListView().getCheckedItemPositions();
        ArrayList items = new ArrayList<>();
        for (int i = 0; i < itemPositions.size(); i++)
            items.add(a.getData().get(itemPositions.keyAt(i)));
        return items;
    }

    // Delete all the contextual-action-mode-selected items from the database and the ListView
    public static void deleteSelectedItems(MyListActivity a) {
        ArrayList itemsToDelete = Util.getSelectedItems(a);
        for (Object item : itemsToDelete)
            cupboard().withDatabase(a.getDatabase()).delete(item);
        a.getData().removeAll(itemsToDelete);
        a.getAdapter().notifyDataSetChanged();
    }

    // Move the selected items in a MyListActivity to the other list, either from TodoItem to
    // DoneItem, or from DoneItem to TodoItem, depending on whether this method is called from the
    // MainActivity or ArchiveActivity.
    public static void moveSelectedItems(MyListActivity a, ActionMode mode) {
        // Create DoneItems/TodoItems from the selected TodoItems/DoneItems and save them in the
        // corresponding database table
        ArrayList selectedItems = Util.getSelectedItems(a);
        for (Object item : selectedItems) {
            if (a instanceof MainActivity)
                cupboard().withDatabase(a.getDatabase()).put(new DoneItem((TodoItem) item));
            else if (a instanceof ArchiveActivity)
                cupboard().withDatabase(a.getDatabase()).put(new TodoItem((DoneItem) item));
        }
        // Delete the selected TodoItems/DoneItems from the database and the corresponding ListView
        Util.deleteSelectedItems(a);
        mode.finish();

        // Show toast confirming that the items have been moved
        int n = selectedItems.size();
        String msg = "";
        if (a instanceof MainActivity)
            msg = a.getResources().getQuantityString(R.plurals.toast_todo2done, n, n);
        else if (a instanceof ArchiveActivity)
            msg = a.getResources().getQuantityString(R.plurals.toast_done2todo, n, n);
        toast(a, msg);
    }

    public static ArrayList readItemsFromDb(MyListActivity a) {
        Class type = null;
        if (a instanceof MainActivity)          type = TodoItem.class;
        else if (a instanceof  ArchiveActivity) type = DoneItem.class;

        ArrayList items = new ArrayList();
        Cursor cursor = cupboard().withDatabase(a.getDatabase()).query(type).getCursor();
        try {
            QueryResultIterable i = cupboard().withCursor(cursor).iterate(type);
            for (Object item : i) items.add(item);
        }
        finally {
            cursor.close();
        }
        return items;
    }

    public static void toast(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
    }

}

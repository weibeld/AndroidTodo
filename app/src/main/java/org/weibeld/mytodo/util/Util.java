package org.weibeld.mytodo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.widget.Toast;

import org.weibeld.mytodo.R;

import java.util.ArrayList;

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

    // Delete all the items that are selected in the contextual action mode
    public static void deleteSelectedItems(MyListActivity a) {
        ArrayList itemsToDelete = Util.getSelectedItems(a);
        for (Object item : itemsToDelete)
            cupboard().withDatabase(a.getDatabase()).delete(item);
        a.getData().removeAll(itemsToDelete);
        a.getAdapter().notifyDataSetChanged();
    }

    public static void toast(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
    }

}

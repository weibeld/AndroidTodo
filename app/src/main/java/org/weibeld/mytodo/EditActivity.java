package org.weibeld.mytodo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import org.weibeld.mytodo.data.TodoItem;
import org.weibeld.mytodo.util.MyDate;

public class EditActivity extends AppCompatActivity {

    private final String LOG_TAG = EditActivity.class.getSimpleName();

    TodoItem mItem;
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Add the FormFragment
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new FormFragment()).commit();
        }

        // The TodoItem and position in the list passed from the MainActivity
        mItem = (TodoItem) getIntent().getSerializableExtra(MainActivity.EXTRA_CODE_ITEM);
        mPosition = getIntent().getIntExtra(MainActivity.EXTRA_CODE_ITEM_POS, 0);

        // Set Toolbar as the app bar (instead of default ActionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Possible Android bug: setting the title in XML doesn't work
        toolbar.setTitle(R.string.toolbar_edit_activity);
        setSupportActionBar(toolbar);

        // Set up action for the navigation item to the left of the title in the Toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmExit();
            }
        });

        // Set up action for Save on the right side of the Toolbar
        findViewById(R.id.btnToolbarSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormFragment f = (FormFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
                if (f.onSaveClicked())
                    showToast(R.string.toast_edits_saved);
            }
        });
    }

    // Called when the device back button is pressed for this activity (not keyboard)
    @Override
    public void onBackPressed() {
        confirmExit();
    }

    // Check if any edits have been made so far, if yes, show a dialog, if no, finish the activity
    private void confirmExit() {
        if (isUntouched()) {
            //showToast(R.string.toast_no_changes);
            finish();
        }
        else {
            new AlertDialog.Builder(this).
                    setMessage(R.string.dialog_discard_msg).
                    setNegativeButton(R.string.dialog_discard_neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).
                    setPositiveButton(R.string.dialog_discard_pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //showToast(R.string.toast_no_changes);
                            finish();
                        }
                    }).
                    create().
                    show();
        }
    }

    // Check if any edits to the item have been made so far
    private boolean isUntouched() {
        FormFragment f = (FormFragment) getFragmentManager().findFragmentById(R.id.fragment_container);

        // Get the currently saved properties of the item
        int priorOld = mItem.priority;
        MyDate dateOld = (mItem.due_ts == null) ? null : new MyDate(mItem.due_ts);
        String textOld = mItem.text;

        // Get the properties for the item that are currently selected in the input form
        int priorNew = f.mSpinPrior.getSelectedItemPosition();
        MyDate dateNew = (f.mSpinDate.getSelectedItemPosition() == 2) ?
                new MyDate((String) f.mSpinDate.getSelectedItem()) : null;
        String textNew = f.mEditText.getText().toString();

        return priorOld == priorNew && areDatesEqual(dateOld, dateNew) && textOld.equals(textNew);
    }

    // Test if two MyDate objects (may be null) represent the same date, or are both null
    private boolean areDatesEqual(MyDate date1, MyDate date2) {
        if (date1 == null && date2 != null) return false;
        if (date1 != null && date2 == null) return false;
        if (date1 == null && date2 == null) return true;
        return date1.equals(date2);
    }

    private void showToast(int msgStr) {
        Toast.makeText(this, msgStr, Toast.LENGTH_SHORT).show();
    }
}

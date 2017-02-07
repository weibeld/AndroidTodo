package org.weibeld.mytodo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.weibeld.mytodo.data.TodoItem;

public class EditActivity extends AppCompatActivity {

    private final String LOG_TAG = EditActivity.class.getSimpleName();

    TodoItem mItem;
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Set Toolbar as the app bar (instead of default ActionBar)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_edit_activity);  // Setting the title in XML doesn't work
        setSupportActionBar(toolbar);


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new FormFragment()).commit();
        }

        mItem = (TodoItem) getIntent().getSerializableExtra(MainActivity.EXTRA_CODE_ITEM);
        mPosition = getIntent().getIntExtra(MainActivity.EXTRA_CODE_ITEM_POS, 0);
    }

}

package org.weibeld.mytodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.weibeld.mytodo.data.TodoItem;

public class EditActivity extends AppCompatActivity {

    private final String LOG_TAG = EditActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final TodoItem item = (TodoItem) getIntent().getSerializableExtra(MainActivity.EXTRA_CODE_ITEM);
        final int position = getIntent().getIntExtra(MainActivity.EXTRA_CODE_ITEM_POS, 0);

        final Spinner spinner = (Spinner) findViewById(R.id.spinPriority);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this, R.array.priority_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinAdapter);
        spinner.setSelection(item.priority);

        final EditText editText = (EditText) findViewById(R.id.etEditItem);
        editText.setText(item.text);
        editText.setSelection(editText.getText().length());  // Set cursor to end of text

        Button saveButton = (Button) findViewById(R.id.btnEditSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update item and send it back to the MainActivity
                item.text = editText.getText().toString();
                item.priority = spinner.getSelectedItemPosition();
                Intent result = new Intent();
                result.putExtra(MainActivity.EXTRA_CODE_ITEM, item);
                result.putExtra(MainActivity.EXTRA_CODE_ITEM_POS, position);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}

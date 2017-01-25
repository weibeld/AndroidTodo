package org.weibeld.mytodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends AppCompatActivity {

    private final String LOG_TAG = EditActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final EditText editText = (EditText) findViewById(R.id.etEditItem);
        editText.setText(getIntent().getStringExtra(MainActivity.EXTRA_CODE_ITEM_TEXT));
        // Set cursor to end of text
        editText.setSelection(editText.getText().length());

        Button saveButton = (Button) findViewById(R.id.btnEditSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra(MainActivity.EXTRA_CODE_ITEM_POS, getIntent().getIntExtra(MainActivity.EXTRA_CODE_ITEM_POS, 0));
                result.putExtra(MainActivity.EXTRA_CODE_ITEM_TEXT, editText.getText().toString());
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}

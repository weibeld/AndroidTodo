package org.weibeld.mytodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_CODE_EDIT = 10;
    static final String EXTRA_CODE_ITEM_POS = "position";
    static final String EXTRA_CODE_ITEM_TEXT = "text";

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvItems = (ListView) findViewById(R.id.lvItems);
        readItems();  // Initialises 'items'
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    private void setupListViewListener() {
        // Delete item on long click
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                items.remove(position);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            }
        });
        // Launch EditActivity on short click
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra(EXTRA_CODE_ITEM_POS, position);
                intent.putExtra(EXTRA_CODE_ITEM_TEXT, items.get(position));
                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Replace item with edited item
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_EDIT) {
            String itemText = data.getExtras().getString(EXTRA_CODE_ITEM_TEXT);
            int itemPos = data.getExtras().getInt(EXTRA_CODE_ITEM_POS);
            items.set(itemPos, itemText);
            itemsAdapter.notifyDataSetChanged();
            writeItems();
        }
    }

    // Add and save a new item
    public void onAddItem(View view) {
        EditText editText = (EditText) findViewById(R.id.etNewItem);
        String text = editText.getText().toString();
        itemsAdapter.add(text);
        editText.setText("");
        writeItems();
    }

    // Write all items to a file, overwriting any previous content of this file
    private void writeItems() {
        File currentDir = getFilesDir();
        File todoFile = new File(currentDir, "todo.txt");
        Log.v(LOG_TAG, "Writing file " + todoFile.getAbsolutePath());
        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Read lines from a file and use them as the new items (discarding any previous items)
    private void readItems() {
        File currentDir = getFilesDir();
        File todoFile = new File(currentDir, "todo.txt");
        Log.v(LOG_TAG, "Reading from file: " + todoFile.getAbsolutePath());
        try {
            items = new ArrayList<>(FileUtils.readLines(todoFile));
        } catch (IOException e) {
            // If file does not yet exist (if app is run for the first time), create empty item set
            items = new ArrayList<>();
        }
    }
}

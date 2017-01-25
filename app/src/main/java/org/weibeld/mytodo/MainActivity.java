package org.weibeld.mytodo;

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
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                items.remove(position);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            }
        });
    }

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

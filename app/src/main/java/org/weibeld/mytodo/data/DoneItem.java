package org.weibeld.mytodo.data;

import java.util.GregorianCalendar;

/**
 * Data model of a "done" todo item as saved in the database. This class is translated to an
 * SQLite table by Cupboard.
 */
public class DoneItem extends TodoItem {
    public long done_ts;  // Date the item has been marked as done, UNIX timestamp in ms

    public DoneItem() {}

    /**
     * Create a DoneItem from a TodoItem (used when marking a TodoItem as "Done").
     * @param todo The TodoItem that is marked as "Done".
     */
    public DoneItem(TodoItem todo) {
        this.text = todo.text;
        this.priority = todo.priority;
        this.due_ts = todo.due_ts;
        this.creation_ts = todo.creation_ts;
        this.done_ts = GregorianCalendar.getInstance().getTimeInMillis();
    }
}

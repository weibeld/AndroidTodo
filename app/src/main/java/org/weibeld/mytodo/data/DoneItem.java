package org.weibeld.mytodo.data;

import java.util.GregorianCalendar;

/**
 * Created by dw on 10/02/17.
 */

public class DoneItem extends TodoItem {
    public long done_ts;  // Date the item has been marked as done, UNIX timestamp in ms

    public DoneItem() {}

    public DoneItem(TodoItem todo) {
        this.text = todo.text;
        this.priority = todo.priority;
        this.due_ts = todo.due_ts;
        this.creation_ts = todo.creation_ts;
        this.done_ts = GregorianCalendar.getInstance().getTimeInMillis();
    }
}

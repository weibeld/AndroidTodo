package org.weibeld.mytodo.data;

import android.content.Context;

import org.weibeld.mytodo.R;

import java.io.Serializable;

/**
 * Data model of a todo item as saved in the database. This class is translated to an SQLite table
 * by Cupboard.
 */
public class TodoItem implements Serializable {

    public Long _id;            // Required in all Cupboard models
    public String text = "";
    public int priority = 0;    // 0=none, 1=high, 2=medium, 3=low
    public Long due_ts = null;  // Due date, UNIX timestamp in ms, null means no due date set
    public long creation_ts;    // Creation date, UNIX timestamp in ms

    public TodoItem() {}

    /**
     * Create a TodoItem from a DoneItem (used when putting back an item from "Done" to "Todo")
     * @param doneItem The DoneItem to revert to a TodoItem
     */
    public TodoItem(DoneItem doneItem) {
        this.text = doneItem.text;
        this.priority = doneItem.priority;
        this.due_ts = doneItem.due_ts;
        this.creation_ts = doneItem.creation_ts;
    }

    public boolean hasHigherPriority(TodoItem other) {
        if (this.priority == 1 && (other.priority == 2 || other.priority == 3 || other.priority == 0) ||
            this.priority == 2 && (other.priority == 3 || other.priority == 0) ||
            this.priority == 3 && (other.priority == 0))
            return true;
        else
            return false;
    }

    public boolean hasLowerPriority(TodoItem other) {
        return !this.hasHigherPriority(other) && !(this.priority == other.priority);
    }

    public boolean isMoreUrgent(TodoItem other) {
        if (this.due_ts == null) return false;
        if (other.due_ts == null || other.due_ts > this.due_ts)
            return true;
        else
            return false;
    }

    public boolean isLessUrgent(TodoItem other) {
        return !this.isMoreUrgent(other) && !(this.due_ts == other.due_ts);
    }

    public boolean isOlder(TodoItem other) {
        return this.creation_ts < other.creation_ts;
    }

    public boolean isYounger(TodoItem other) {
        return this.creation_ts > other.creation_ts;
    }

    public String getPriorityString(Context c) {
        switch (this.priority) {
            case 1: return c.getString(R.string.label_priority_h);
            case 2: return c.getString(R.string.label_priority_m);
            case 3: return c.getString(R.string.label_priority_l);
            default: return c.getString(R.string.none);
        }
    }
}
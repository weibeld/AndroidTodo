package org.weibeld.mytodo.data;

import java.io.Serializable;

/**
 * Data model of an item for Cupboard (translated to a table with columns corresponding to the
 * member fields of the class).
 */
public class TodoItem implements Serializable {

    public Long _id;            // Required in all Cupboard models
    public String text = "";
    public int priority = 0;    // 0=none, 1=high, 2=medium, 3=low
    public Long due_ts = null;  // Due date, UNIX timestamp in ms, null means no due date set
    public long creation_ts;    // Creation date, UNIX timestamp in ms

    public TodoItem() {}

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
}
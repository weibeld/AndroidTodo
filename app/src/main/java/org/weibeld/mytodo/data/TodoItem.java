package org.weibeld.mytodo.data;

import java.io.Serializable;

/**
 * Data model of an item for Cupboard (translated to a table with columns corresponding to the
 * member fields of the class).
 */
public class TodoItem implements Serializable {

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";

    public Long _id;  // Required in all Cupboard models
    public String text = "";
    public String priority = PRIORITY_HIGH;

    public TodoItem() {
    }

    public TodoItem(String text, String priority) {
        this.text = text;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return text + " | " + priority;
    }

}

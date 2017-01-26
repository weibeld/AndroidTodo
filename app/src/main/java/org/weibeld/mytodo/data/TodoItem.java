package org.weibeld.mytodo.data;

import java.io.Serializable;

/**
 * Data model of an item for Cupboard (translated to a table with columns corresponding to the
 * member fields of the class).
 */
public class TodoItem implements Serializable {

    public Long _id;  // Required in all Cupboard models
    public String text = "";
    public int priority = 0;

    public TodoItem() {
    }

    @Override
    public String toString() {
        switch (priority) {
            case 0:
                return text;
            case 1:
                return text + " | H";
            case 2:
                return text + " | M";
            case 3:
                return text + " | L";
            default:
                return text;
        }
    }

}

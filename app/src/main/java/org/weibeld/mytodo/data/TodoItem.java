package org.weibeld.mytodo.data;

import java.io.Serializable;

/**
 * Data model of an item for Cupboard (translated to a table with columns corresponding to the
 * member fields of the class).
 */
public class TodoItem implements Serializable {

    public Long _id;  // Required in all Cupboard models
    public String text = "";
    public int priority = 0;  // 0=none, 1=high, 2=medium, 3=low
    public long date = -1;    // Milliseconds since epoch, -1 means no date set

    public TodoItem() {}
}

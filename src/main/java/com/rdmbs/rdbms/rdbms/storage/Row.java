package com.rdmbs.rdbms.rdbms.storage;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Row {
    private List<Object> values;

    public Row() {
        this.values = new ArrayList<>();
    }

    public Row(List<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public Object getValue(int index) {
        return values.get(index);
    }

    public void setValue(int index, Object value) {
        values.set(index, value);
    }

    public int size() {
        return values.size();
    }
}

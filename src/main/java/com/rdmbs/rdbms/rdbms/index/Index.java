package com.rdmbs.rdbms.rdbms.index;

import com.rdmbs.rdbms.rdbms.storage.Row;
import java.util.List;

public interface Index {
    void insert(Row row);
    List<Row> search(Object key);
    void delete(Object key);
    String getIndexName();
    String getTableName();
    String getColumnName();
    boolean isUnique();
}

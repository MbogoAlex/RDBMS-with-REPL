package com.rdmbs.rdbms.rdbms.storage;

import com.rdmbs.rdbms.rdbms.schema.Column;
import com.rdmbs.rdbms.rdbms.schema.DataType;
import com.rdmbs.rdbms.rdbms.schema.Table;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TableStorage {
    private static final String DATA_DIR = "data";
    private static final String TABLE_EXT = ".tbl";
    private final String dataDirectory;

    public TableStorage() {
        this(DATA_DIR);
    }

    public TableStorage(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            Path path = Paths.get(dataDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    public void createTableFile(Table table) throws IOException {
        Path tablePath = getTablePath(table.getName());
        if (!Files.exists(tablePath)) {
            Files.createFile(tablePath);
        }
    }

    public void insertRow(Table table, Row row) throws IOException {
        Path tablePath = getTablePath(table.getName());
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(tablePath.toFile(), true)))) {
            writeRow(dos, table, row);
        }
    }

    public List<Row> readAllRows(Table table) throws IOException {
        Path tablePath = getTablePath(table.getName());
        List<Row> rows = new ArrayList<>();

        if (!Files.exists(tablePath) || Files.size(tablePath) == 0) {
            return rows;
        }

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(tablePath.toFile())))) {
            while (dis.available() > 0) {
                Row row = readRow(dis, table);
                if (row != null) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public void deleteTable(String tableName) throws IOException {
        Path tablePath = getTablePath(tableName);
        Files.deleteIfExists(tablePath);
    }

    public void truncateTable(String tableName) throws IOException {
        Path tablePath = getTablePath(tableName);
        if (Files.exists(tablePath)) {
            Files.delete(tablePath);
            Files.createFile(tablePath);
        }
    }

    private void writeRow(DataOutputStream dos, Table table, Row row) throws IOException {
        for (int i = 0; i < table.getColumns().size(); i++) {
            Column column = table.getColumns().get(i);
            Object value = row.getValue(i);

            switch (column.getDataType()) {
                case INT:
                    dos.writeInt(value == null ? 0 : (Integer) value);
                    break;
                case LONG:
                    dos.writeLong(value == null ? 0L : (Long) value);
                    break;
                case BOOLEAN:
                    dos.writeBoolean(value != null && (Boolean) value);
                    break;
                case DATE:
                case DATETIME:
                case TIMESTAMP:
                    dos.writeLong(value == null ? 0L : (Long) value);
                    break;
                case VARCHAR:
                    String strValue = value == null ? "" : value.toString();
                    dos.writeUTF(strValue);
                    break;
            }
        }
    }

    private Row readRow(DataInputStream dis, Table table) throws IOException {
        Row row = new Row();
        try {
            for (Column column : table.getColumns()) {
                Object value;
                switch (column.getDataType()) {
                    case INT:
                        value = dis.readInt();
                        break;
                    case LONG:
                        value = dis.readLong();
                        break;
                    case BOOLEAN:
                        value = dis.readBoolean();
                        break;
                    case DATE:
                    case DATETIME:
                    case TIMESTAMP:
                        value = dis.readLong();
                        break;
                    case VARCHAR:
                        value = dis.readUTF();
                        break;
                    default:
                        value = null;
                }
                row.addValue(value);
            }
            return row;
        } catch (EOFException e) {
            return null;
        }
    }

    private Path getTablePath(String tableName) {
        return Paths.get(dataDirectory, tableName.toLowerCase() + TABLE_EXT);
    }
}

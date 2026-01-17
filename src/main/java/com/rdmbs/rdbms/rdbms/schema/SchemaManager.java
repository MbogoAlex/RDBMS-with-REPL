package com.rdmbs.rdbms.rdbms.schema;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SchemaManager {
    private static final String SCHEMA_FILE = "data/schema.meta";

    public void saveSchema(Schema schema) throws IOException {
        Path schemaPath = Paths.get(SCHEMA_FILE);
        Files.createDirectories(schemaPath.getParent());

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(schemaPath.toFile()))) {
            oos.writeInt(schema.getAllTables().size());
            
            for (Table table : schema.getAllTables()) {
                writeTable(oos, table);
            }
        }
    }

    public Schema loadSchema() throws IOException {
        Schema schema = new Schema();
        Path schemaPath = Paths.get(SCHEMA_FILE);
        
        if (!Files.exists(schemaPath)) {
            return schema; // Return empty schema if file doesn't exist
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(schemaPath.toFile()))) {
            int tableCount = ois.readInt();
            
            for (int i = 0; i < tableCount; i++) {
                Table table = readTable(ois);
                schema.addTable(table);
            }
        }
        
        return schema;
    }

    private void writeTable(ObjectOutputStream oos, Table table) throws IOException {
        oos.writeUTF(table.getName());
        oos.writeInt(table.getColumns().size());
        
        for (Column column : table.getColumns()) {
            oos.writeUTF(column.getName());
            oos.writeUTF(column.getDataType().name());
            oos.writeInt(column.getSize());
            oos.writeBoolean(column.isNullable());
            oos.writeBoolean(column.isPrimaryKey());
            oos.writeBoolean(column.isUnique());
        }
    }

    private Table readTable(ObjectInputStream ois) throws IOException {
        String tableName = ois.readUTF();
        Table table = new Table(tableName);
        
        int columnCount = ois.readInt();
        for (int i = 0; i < columnCount; i++) {
            String columnName = ois.readUTF();
            String dataTypeName = ois.readUTF();
            int size = ois.readInt();
            boolean nullable = ois.readBoolean();
            boolean primaryKey = ois.readBoolean();
            boolean unique = ois.readBoolean();
            
            DataType dataType = DataType.valueOf(dataTypeName);
            Column column = new Column(columnName, dataType, size);
            column.setNullable(nullable);
            column.setPrimaryKey(primaryKey);
            column.setUnique(unique);
            
            table.addColumn(column);
        }
        
        return table;
    }
}

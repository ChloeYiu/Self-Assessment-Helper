package com.helper.util.api;

import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parses CSV content into a single tabular table.
 *
 * When column names are not provided, the first CSV record is treated as the header row.
 */
public class CsvDocumentParser {
    public TabularTable parse(String csv) {
        return parse(csv, null);
    }

    public TabularTable parse(String csv, String tableName) {
        List<List<String>> records = parseRecords(Objects.requireNonNull(csv, "csv"));
        if (records.isEmpty()) {
            throw new IllegalArgumentException("headered CSV must contain a header row");
        }

        List<String> columnNames = records.getFirst();
        return buildTable(tableName, columnNames, records, 1);
    }

    public TabularTable parse(String csv, String tableName, List<String> columnNames) {
        List<List<String>> records = parseRecords(Objects.requireNonNull(csv, "csv"));
        return buildTable(tableName, Objects.requireNonNull(columnNames, "columnNames"), records, 0);
    }

    private TabularTable buildTable(
            String tableName,
            List<String> columnNames,
            List<List<String>> records,
            int firstDataRowIndex
    ) {
        List<Map<String, String>> rows = createDataRows(columnNames, records, firstDataRowIndex);
        return new TabularTableBuilder<>(
                tableName,
                columnNames,
                rows,
                (row, columnName, rowIndex) -> new TabularCell(row.getOrDefault(columnName, ""))
        ).buildTable();
    }

    private List<Map<String, String>> createDataRows(
            List<String> columnNames,
            List<List<String>> records,
            int firstDataRowIndex
    ) {
        List<Map<String, String>> rows = new ArrayList<>();
        for (int rowIndex = firstDataRowIndex; rowIndex < records.size(); rowIndex++) {
            List<String> record = records.get(rowIndex);
            if (record.stream().allMatch(String::isBlank)) {
                continue;
            }

            Map<String, String> row = new HashMap<>();
            for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                row.put(columnNames.get(columnIndex), getValue(record, columnIndex));
            }
            rows.add(row);
        }

        return rows;
    }

    private String getValue(List<String> record, int columnIndex) {
        if (columnIndex >= record.size()) {
            return "";
        }

        return record.get(columnIndex);
    }

    private List<List<String>> parseRecords(String csv) {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < csv.length(); index++) {
            char value = csv.charAt(index);
            if (value == '"') {
                if (inQuotes && index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (value == ',' && !inQuotes) {
                record.add(field.toString());
                field.setLength(0);
            } else if ((value == '\n' || value == '\r') && !inQuotes) {
                if (value == '\r' && index + 1 < csv.length() && csv.charAt(index + 1) == '\n') {
                    index++;
                }
                record.add(field.toString());
                records.add(record);
                record = new ArrayList<>();
                field.setLength(0);
            } else {
                field.append(value);
            }
        }

        if (!field.isEmpty() || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }

        return records;
    }
}

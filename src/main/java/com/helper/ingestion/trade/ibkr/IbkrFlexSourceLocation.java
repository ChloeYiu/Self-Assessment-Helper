package com.helper.ingestion.trade.ibkr;

import com.helper.util.table.TabularSourceLocation;

/**
 * Source location for a value extracted from an IBKR Flex statement.
 */
public class IbkrFlexSourceLocation implements TabularSourceLocation {
    private final String sectionName;
    private final int rowIndex;
    private final String fieldName;

    public IbkrFlexSourceLocation(String sectionName, int rowIndex, String fieldName) {
        this.sectionName = sectionName;
        this.rowIndex = rowIndex;
        this.fieldName = fieldName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getFieldName() {
        return fieldName;
    }
}

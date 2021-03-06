package com.github.dyna4jdbc.internal.common.typeconverter;

import com.github.dyna4jdbc.internal.common.typeconverter.impl.SQLDataType;

public interface ColumnMetadata {

    enum Nullability { NOT_NULLABLE, NULLABLE, UNKNOWN }

    int MINIMUM_DISPLAY_SIZE = 4;
    
    boolean isConsumesFirstRowValue();

    boolean isCurrency();

    Nullability getNullability();

    boolean isSigned();

    int getColumnDisplaySize();

    String getColumnLabel();

    String getColumnName();

    int getPrecision();

    int getScale();

    SQLDataType getColumnType();

    String getFormatString();
}

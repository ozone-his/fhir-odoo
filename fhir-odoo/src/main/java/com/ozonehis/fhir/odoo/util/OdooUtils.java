package com.ozonehis.fhir.odoo.util;

import com.odoojava.api.Row;

@SuppressWarnings("unchecked")
public class OdooUtils {

    /**
     * Utility method to get a value from a Row or return a default value if the key is not present or the value is null.
     *
     * @param row the Row object
     * @param key the key to look up in the Row
     * @param defaultValue the default value to return if the key is not present or the value is null
     * @param <T> the type of the value
     * @return the value from the Row or the default value
     */
    public static <T> T getOrElse(Row row, String key, T defaultValue) {
        T value = (T) row.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Utility method to get a value from a Row or return null if the key is not present or the value is null.
     *
     * @param row the Row object
     * @param key the key to look up in the Row
     * @param <T> the type of the value
     * @return the value from the Row or null
     */
    public static <T> T get(Row row, String key) {
        return (T) row.get(key);
    }
}

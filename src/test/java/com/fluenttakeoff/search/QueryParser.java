package com.fluenttakeoff.search;

import java.util.HashMap;
import java.util.Map;

public class QueryParser {

    public static Map<String, String> parseLine(String line) {
        Map<String, String> fields = new HashMap<>();
        String[] keyValuePairs = line.split("\\|");
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            if (entry.length == 2) {
                fields.put(entry[0], entry[1]);
            }
        }
        return fields;
    }
}

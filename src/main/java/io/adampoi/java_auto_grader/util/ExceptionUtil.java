package io.adampoi.java_auto_grader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionUtil {
    public static String extractFieldName(String errorMessage) {
        // Extract field name from PostgreSQL "Key (field_name)" pattern
        Pattern keyPattern = Pattern.compile("Key\\s*\\(([a-zA-Z_][a-zA-Z0-9_]*)\\)", Pattern.CASE_INSENSITIVE);
        Matcher keyMatcher = keyPattern.matcher(errorMessage);
        if (keyMatcher.find()) {
            return keyMatcher.group(1);
        }

        // Extract field name from constraint violation messages
        // Common patterns: "column 'field_name'", "key 'field_name'", etc.
        if (errorMessage.contains("column")) {
            Pattern pattern = Pattern.compile("column[\\s'\"]*([a-zA-Z_][a-zA-Z0-9_]*)[\\s'\"]*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        if (errorMessage.contains("key")) {
            Pattern pattern = Pattern.compile("key[\\s'\"]*([a-zA-Z_][a-zA-Z0-9_]*)[\\s'\"]*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        // Try to extract field name from constraint names (e.g., UK_user_email)
        Pattern constraintPattern = Pattern.compile("constraint[\\s'\"]*[a-zA-Z0-9_]*_([a-zA-Z_][a-zA-Z0-9_]*)[\\s'\"]*", Pattern.CASE_INSENSITIVE);
        Matcher constraintMatcher = constraintPattern.matcher(errorMessage);
        if (constraintMatcher.find()) {
            return constraintMatcher.group(1);
        }

        return "value";
    }

    public static String extractRejectedValue(String errorMessage) {
        // Extract value from PostgreSQL "Key (field_name)=(value)" pattern
        Pattern keyValuePattern = Pattern.compile("Key\\s*\\([^)]+\\)=\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher keyValueMatcher = keyValuePattern.matcher(errorMessage);
        if (keyValueMatcher.find()) {
            return keyValueMatcher.group(1);
        }

        // Extract value from constraint violation messages with quotes
        // Pattern: 'value' or "value"
        Pattern quotedValuePattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        Matcher quotedMatcher = quotedValuePattern.matcher(errorMessage);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(1);
        }

        // Extract value from "value=" patterns
        Pattern valuePattern = Pattern.compile("value\\s*=\\s*([^\\s,)]+)", Pattern.CASE_INSENSITIVE);
        Matcher valueMatcher = valuePattern.matcher(errorMessage);
        if (valueMatcher.find()) {
            return valueMatcher.group(1);
        }

        return null; // No value found
    }
}

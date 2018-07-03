package com.rampage.projecttools.util;

public class StringUtils {
    private StringUtils() {
    }
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}

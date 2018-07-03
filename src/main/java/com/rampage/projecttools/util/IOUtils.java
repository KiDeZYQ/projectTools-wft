package com.rampage.projecttools.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    private IOUtils() {
    }
    
    public static void closeQuietly(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        
        for (Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}

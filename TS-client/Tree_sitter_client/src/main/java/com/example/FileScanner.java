package com.example;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {
    public static List<File> collectSourceFiles(File dir, String... extensions) {
        List<File> files = new ArrayList<>();
        scan(dir, extensions, files);
        return files;
    }

    private static void scan(File dir, String[] extensions, List<File> files) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scan(file, extensions, files);
            } else {
                for (String ext : extensions) {
                    if (file.getName().endsWith(ext)) {
                        files.add(file);
                        break;
                    }
                }
            }
        }
    }
}

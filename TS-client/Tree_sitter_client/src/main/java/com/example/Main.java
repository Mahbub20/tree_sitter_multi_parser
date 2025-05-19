package com.example;
import okhttp3.*;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class Main {
    private static final OkHttpClient client = new OkHttpClient();
    public static void main(String[] args) throws IOException {
        String filePath = "/home/mahbub/Documents/Workspace/Projects/Mini Projects/java-mini-project/Array-Oper";
        File project = new File(filePath);
        List<File> files = FileScanner.collectSourceFiles(project, ".py", ".java");

        for (File file : files) {
            String content = Files.readString(file.toPath());
            String lang = detectLanguage(file);

            JsonObject json = new JsonObject();
            json.addProperty("code", content);
            json.addProperty("language", lang);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json")
            );

            Request request = new Request.Builder()
                    .url("http://localhost:5005/parse")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Failed for: " + file.getName());
                    continue;
                }

                System.out.println("File: " + file.getName());
                System.out.println(response.body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String detectLanguage(File file) {
        String name = file.getName();
        if (name.endsWith(".py")) return "python";
        if (name.endsWith(".java")) return "java";
        return "unknown";
    }
}
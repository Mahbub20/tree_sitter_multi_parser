package com.example;
import okhttp3.*;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final OkHttpClient client = new OkHttpClient();
    public static void main(String[] args) throws IOException {
        String filePath = "/home/mahbub/Documents/Workspace/Projects/Mini Projects/java-mini-project/Que-Datastructure ";
        File project = new File(filePath);
        List<File> files = FileScanner.collectSourceFiles(project, ".py", ".java", "cs", "go", "rb");

        // Output file
        Path outputPath = Path.of("parsed_all_output.txt");
        Files.writeString(outputPath, ""); // Clear or create the file
        int totalFileCount = 0;

        Map<String, List<String>> languageOutputMap = new LinkedHashMap<>();

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

//                System.out.println("File: " + file.getName());
//                System.out.println(response.body().string());
//
//                System.out.println("(Syntax tree in json format):\n");
//                System.out.print("<======================================>");
                String parsedResult = "(Syntax tree in json format):\n"+"<======================================>\n"+response.body().string();
                String outputLine = "File: " + file.getName() + "\n\n" + parsedResult;

                // Group by language
                languageOutputMap
                        .computeIfAbsent(lang, k -> new ArrayList<>())
                        .add(outputLine);

                System.out.println("Parsed output collected for: " + file.getName());
                totalFileCount++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Write grouped output
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE)) {
            for (Map.Entry<String, List<String>> entry : languageOutputMap.entrySet()) {
                writer.write("\t\t\t\t\t\t\t\tProject Language: " + entry.getKey().toUpperCase() + "\n"+"\t\t\t\t\t\t\t\tTotal parsed file counts: "+totalFileCount+"\n\n");
                for (String outputLine : entry.getValue()) {
                    writer.write(outputLine + "\n\n");
                }
            }
        }

        System.out.println("All parsed outputs written to: " + outputPath.toAbsolutePath());
    }

    private static String detectLanguage(File file) {
        String name = file.getName();
        if (name.endsWith(".py")) return "python";
        if (name.endsWith(".java")) return "java";
        if (name.endsWith(".cs")) return "csharp";
        if (name.endsWith(".go")) return "go";
        if (name.endsWith(".rs")) return "rust";
        if (name.endsWith(".rb")) return "ruby";
        return "unknown";
    }
}
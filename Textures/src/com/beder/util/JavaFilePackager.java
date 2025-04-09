package com.beder.util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaFilePackager {

    public static void main(String[] args) {
        List<String> inputPaths = new ArrayList<>();
        String outputPath = null;

        if (args.length == 0) {
            // No arguments provided: prompt the user interactively
            Scanner scanner = new Scanner(System.in);
            
            System.out.print("Enter input file/folder paths (separated by commas): ");
            String input = scanner.nextLine();
            String[] parts = input.split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    inputPaths.add(part.trim());
                }
            }
            
            System.out.print("Enter output file path (with .txt extension): ");
            outputPath = scanner.nextLine().trim();
            
            scanner.close();
        } else {
            // Arguments provided: first argument is output file, the rest are input paths
            if (args.length < 2) {
                System.out.println("Usage: java JavaFilePackager <output_file_path> <input_path1> [<input_path2> ...]");
                System.exit(1);
            }
            outputPath = args[0];
            for (int i = 1; i < args.length; i++) {
                inputPaths.add(args[i]);
            }
        }

        // Gather all .java files from the given input paths
        List<File> javaFiles = new ArrayList<>();
        for (String path : inputPaths) {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Path does not exist: " + path);
                continue;
            }
            if (file.isFile()) {
                if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            } else if (file.isDirectory()) {
                gatherJavaFiles(file, javaFiles);
            }
        }
        
        // Write the gathered Java files into the output .txt file with Markdown formatting
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            for (File javaFile : javaFiles) {
                writer.println("**File: " + javaFile.getAbsolutePath() + "**");
                writer.println("```java");
                
                // Read the file content and write it to the output
                List<String> lines = Files.readAllLines(javaFile.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    writer.println(line);
                }
                writer.println("```");  // Closing Markdown code block
                writer.println();       // Blank line between files
            }
            System.out.println("Packaged " + javaFiles.size() + " .java file(s) into " + outputPath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    
    /**
     * Recursively traverse directories to gather all .java files.
     * @param dir The directory to search.
     * @param javaFiles The list that accumulates found Java files.
     */
    private static void gatherJavaFiles(File dir, List<File> javaFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    gatherJavaFiles(file, javaFiles);
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }
}

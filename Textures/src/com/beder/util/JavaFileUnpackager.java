package com.beder.util;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JavaFileUnpackager {

    public static void main(String[] args) {
        // Set the look and feel to match the system's appearance.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default if the system look and feel is not available.
            e.printStackTrace();
        }
        
        // Prompt the user to select the packaged txt file.
        JFileChooser openChooser = new JFileChooser();
        openChooser.setDialogTitle("Select Packaged File (txt)");
        openChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int openResult = openChooser.showOpenDialog(null);
        if (openResult != JFileChooser.APPROVE_OPTION) {
            System.out.println("No packaged file selected. Exiting.");
            System.exit(0);
        }
        File packagedFile = openChooser.getSelectedFile();
        
        // Prompt the user to select the output directory.
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Select Output Directory");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int dirResult = dirChooser.showOpenDialog(null);
        if (dirResult != JFileChooser.APPROVE_OPTION) {
            System.out.println("No output directory selected. Exiting.");
            System.exit(0);
        }
        File outputDirectory = dirChooser.getSelectedFile();

        // Process the packaged file.
        try (BufferedReader br = new BufferedReader(new FileReader(packagedFile))) {
            String line;
            String currentFilePath = null;
            StringBuilder currentFileContent = new StringBuilder();
            boolean readingFileContent = false;
            
            while ((line = br.readLine()) != null) {
                // Detect header line indicating a new file section.
                if (line.startsWith("**File:") && line.endsWith("**")) {
                    // If there's a previous file's content buffered, write it out.
                    if (currentFilePath != null && currentFileContent.length() > 0) {
                        writeFile(outputDirectory, currentFilePath, currentFileContent.toString());
                        currentFileContent.setLength(0);  // Clear buffer for the next file.
                    }
                    // Extract the file path from between "**File:" and "**".
                    currentFilePath = line.substring("**File:".length(), line.length() - 2).trim();
                } else if (line.equals("```java")) {
                    // Beginning of the code block.
                    readingFileContent = true;
                } else if (line.equals("```") && readingFileContent) {
                    // End of the code block.
                    readingFileContent = false;
                } else {
                    // If we're inside a code block, append the line to the file content.
                    if (readingFileContent) {
                        currentFileContent.append(line).append(System.lineSeparator());
                    }
                }
            }
            // Write out the last file (if any) after processing the entire document.
            if (currentFilePath != null && currentFileContent.length() > 0) {
                writeFile(outputDirectory, currentFilePath, currentFileContent.toString());
            }
            System.out.println("Unpackaged Java files have been written to: " + outputDirectory.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error reading packaged file: " + e.getMessage());
        }
    }
    
    /**
     * Writes the extracted file content to a new file in the output directory using the file's base name.
     *
     * @param outputDirectory The directory where files will be saved.
     * @param originalPath    The original file path extracted from the header.
     * @param content         The content to write into the file.
     */
    private static void writeFile(File outputDirectory, String originalPath, String content) {
        // Use only the base file name so that the original directory structure is not recreated.
        File originalFile = new File(originalPath);
        String fileName = originalFile.getName();
        File outputFile = new File(outputDirectory, fileName);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.write(content);
            System.out.println("Written file: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing file " + outputFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }
}

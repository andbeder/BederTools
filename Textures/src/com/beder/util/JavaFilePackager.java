package com.beder.util;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;

public class JavaFilePackager {

    // All extensions to include in the package
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".java",        // Java classes
        ".js",          // LWC / Aura controllers
        ".html",        // LWC templates
        ".css",         // styling
        ".cmp", ".app", // Aura components & apps
        ".evt",         // Aura events
        ".design",      // Aura design files
        ".svg",         // SVG resources
        ".xml"          // meta-XML, e.g. -meta.xml
    ));

    // helper that reads all .gitignore files and applies them
    private static final GitIgnoreMatcher gitIgnore = new GitIgnoreMatcher();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        List<File> sourceFiles = new ArrayList<>();
        List<File> roots       = new ArrayList<>();

        // 1) Build list of roots & gather files
        if (args.length > 0) {
            File input = new File(args[0]);
            if (input.isFile() && shouldInclude(input)) {
                sourceFiles.add(input);
                roots.add(input.getParentFile());
            }
            else if (input.isDirectory()) {
                roots.add(input);
                gatherFiles(input, sourceFiles);
            }
        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Files and/or Folders");
            chooser.setFileSelectionMode(
                JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(null)
                != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            for (File f : chooser.getSelectedFiles()) {
                if (!f.exists()) continue;
                if (f.isFile() && shouldInclude(f)) {
                    sourceFiles.add(f);
                    roots.add(f.getParentFile());
                } else if (f.isDirectory()) {
                    roots.add(f);
                    gatherFiles(f, sourceFiles);
                }
            }
        }

        // Dedupe roots
        Set<File> uniq = new LinkedHashSet<>(roots);
        roots.clear();
        roots.addAll(uniq);

        // 2) Determine output
        File outputFile;
        if (args.length > 1) {
            outputFile = new File(args[1]);
        } else {
            JFileChooser saver = new JFileChooser();
            saver.setDialogTitle("Save Packaged File");
            saver.setFileSelectionMode(
                JFileChooser.FILES_ONLY);
            if (saver.showSaveDialog(null)
                != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            outputFile = saver.getSelectedFile();
        }

        // 3) Package and write
        try {
            String out = packageFiles(sourceFiles, roots);
            try (PrintWriter w = new PrintWriter(
                    new FileWriter(outputFile))) {
                w.print(out);
            }
            System.out.println("Packaged " + sourceFiles.size()
                + " file(s) into " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error packaging files: "
                + e.getMessage());
        }
    }

    public static String packageFiles(
            List<File> sourceFiles,
            List<File> roots) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (File f : sourceFiles) {
            String relPath = computeRelativePath(f, roots);
            sb.append("**File: ").append(relPath).append("**\n");
            sb.append("```\n");
            List<String> lines =
                Files.readAllLines(f.toPath(),
                                  StandardCharsets.UTF_8);
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            sb.append("```\n\n");
        }

        return sb.toString();
    }

    private static boolean shouldInclude(File f) {
        // skip if not one of the allowed extensions
        String name = f.getName().toLowerCase(Locale.ROOT);
        boolean extOk = ALLOWED_EXTENSIONS.stream()
            .anyMatch(name::endsWith);
        if (!extOk) return false;

        // skip if gitIgnore says so
        return !gitIgnore.isIgnored(f.toPath());
    }

    private static void gatherFiles(File dir,
                                    List<File> out) {
        File[] kids = dir.listFiles();
        if (kids == null) return;
        for (File f : kids) {
            Path p = f.toPath();
            if (gitIgnore.isIgnored(p)) {
                // skip ignored files *and* entire ignored dirs
                continue;
            }
            if (f.isDirectory()) {
                gatherFiles(f, out);
            } else if (shouldInclude(f)) {
                out.add(f);
            }
        }
    }

    private static String computeRelativePath(
            File file, List<File> roots) {
        Path filePath = file.toPath().toAbsolutePath();
        Path bestRoot = null;
        int   bestDepth = -1;
        for (File r : roots) {
            Path rp = r.toPath().toAbsolutePath();
            if (filePath.startsWith(rp)
                && rp.getNameCount() > bestDepth) {
                bestDepth = rp.getNameCount();
                bestRoot  = rp;
            }
        }
        if (bestRoot != null) {
            return bestRoot.relativize(filePath).toString();
        } else {
            return file.getName();
        }
    }

    /**
     * Helper that loads .gitignore files and evaluates whether a path
     * should be ignored according to Git’s rules.
     */
    private static class GitIgnoreMatcher {
        private final Map<Path, IgnoreNode> cache = new HashMap<>();

        public boolean isIgnored(Path path) {
            try {
                // walk up from the file’s parent to the FS root
                Path dir = path.getParent();
                while (dir != null) {
                    Path ignoreFile = dir.resolve(".gitignore");
                    IgnoreNode node = cache.computeIfAbsent(
                        ignoreFile,
                        p -> {
                            if (Files.exists(p)) {
                                IgnoreNode n = new IgnoreNode();
                                try (InputStream in =
                                    Files.newInputStream(p)) {
                                    n.parse(in);
                                } catch (IOException ignored) {}
                                return n;
                            }
                            return null;
                        });
                    if (node != null) {
                        // path relative to the .gitignore’s directory
                        Path rel = dir.relativize(path);
                        MatchResult res = node.isIgnored(
                            rel.toString(), Files.isDirectory(path));
                        if (res == MatchResult.IGNORED) {
                            return true;
                        } else if (res == MatchResult.NOT_IGNORED) {
                            return false;
                        }
                        // else: UNKNOWN → keep climbing
                    }
                    dir = dir.getParent();
                }
            } catch (IOException e) {
                // on error, default to “not ignored”
            }
            return false;
        }
    }
}

package info.kgeorgiy.ja.kurkin.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class RecursiveWalk {
    private static void readerData(String[] args) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8)) {
                String line;
                FileVisitor fileVisitor = new FileVisitor(writer);
                try {
                    while ((line = reader.readLine()) != null) {
                        try {
                            Path arg = Path.of(line);
                            Files.walkFileTree(arg, fileVisitor);
                        } catch (SecurityException | InvalidPathException e) {
                            writer.write("0".repeat(64) + " " + line);
                            writer.newLine();
                        }

                    }
                } catch (IOException e) {
                    System.err.println("error for read file: " + e);
                }
            } catch (IOException e) {
                System.err.println("Couldn't open source file: " + e.getMessage());
            }
        } catch (SecurityException | IOException e) {
            System.err.println("Couldn't open destination file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalData");
        } catch (RuntimeException e) {
            System.err.println("RunTimeException: " + e);
        }
    }

    public static void main(String[] args) {
        if ((args == null) || (args.length != 2) || (args[0] == null) || (args[1] == null)) {
            System.err.println("Expected arguments: (input file || output file)");
        } else {
            Path arg1;
            try {
                arg1 = Path.of(args[1]);
            } catch (IllegalArgumentException e) {
                System.out.println("File err");
                System.out.println("Invalid Data");
                return;
            }
            if (arg1.getParent() != null && Files.notExists(arg1.getParent())) {
                try {
                    Files.createDirectories(arg1.getParent());
                } catch (IOException e) {
                    System.out.println("Doesn't create file" + e);
                    return;
                }
            }
            readerData(args);
        }
    }
}
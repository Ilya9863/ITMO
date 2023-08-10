package info.kgeorgiy.ja.kurkin.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    private static final int size = 1024;
    private static final String encryptionMethod = "SHA-256";

    private static byte[] walk(String filePath) {
        try (FileInputStream file = new FileInputStream(filePath)) {
            byte[] buffer = new byte[size];
            MessageDigest md = MessageDigest.getInstance(encryptionMethod);
            int szReader;
            while ((szReader = file.read(buffer)) != -1) {
                md.update(buffer, 0, szReader);
            }
            return md.digest();
        } catch (SecurityException | IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No Provider supports a MessageDigestSpi implementation for the specified algorithm");
        }
        return null;
    }

    private static void readerData(String[] args) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(args[1]), StandardCharsets.UTF_8)) {
                String line;
                byte[] hashFile;
                while ((line = reader.readLine()) != null) {
                    if ((hashFile = Walk.walk(line)) != null) {
                        writer.write(String.format("%0" + (hashFile.length << 1) + "x", new BigInteger(1, hashFile)) + " " + line);
                        writer.newLine();
                    } else {
                        writer.write("0".repeat(64) + " " + line);
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                System.err.println("Couldn't open destination file: " + e.getMessage());
            }
        } catch (SecurityException | IOException e) {
            System.err.println("Couldn't open source file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalData!");
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


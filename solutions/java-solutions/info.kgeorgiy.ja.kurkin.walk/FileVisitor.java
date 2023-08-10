package info.kgeorgiy.ja.kurkin.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;
    private static final int size = 1024;
    private static final String encryptionMethod = "SHA-256";

    public FileVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
        try (FileInputStream file = new FileInputStream(String.valueOf(filePath))) {
            byte[] buffer = new byte[size];
            byte[] hashFile;
            MessageDigest md = MessageDigest.getInstance(encryptionMethod);
            int szReader;
            while ((szReader = file.read(buffer)) != -1) {
                md.update(buffer, 0, szReader);
            }
            hashFile = md.digest();
            writer.write(String.format("%0" + (hashFile.length << 1) + "x", new BigInteger(1, hashFile)) + " " + filePath);
            writer.newLine();
        } catch (IOException e) {
            try {
                writer.write("0".repeat(64) + " " + filePath);
                writer.newLine();
            } catch (IOException ex) {
                System.err.println("Couldn't write in file: " + ex);
            }
            System.err.println(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Couldn't crete file with algorithm: " + e);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        writer.write("0".repeat(64) + " " + path);
        writer.newLine();
        return FileVisitResult.CONTINUE;
    }
}

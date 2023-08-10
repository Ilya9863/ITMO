package info.kgeorgiy.ja.kurkin.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * This class implementing {@link JarImpler}
 * Allows you to use public methods to generate a class by interface
 *
 * @author Kurkin Ilya
 */
public class Implementor implements JarImpler {

    /**
     * default empty class constructor
     */
    public Implementor() {
    }

    /**
     * сhecks the correctness of input arguments, all arguments must not be null
     *
     * @param args <br><b>1)</b> when running with -jar arguments, the class name is a file.jar he generated .jar
     *             file with the implementation of the corresponding class (interface).
     *             <br><b>2)</b>Command line argument: The full name of the class/interface to generate an implementation for.
     *             As a result of the work, the java code of the class with the Impl suffix should be generated,
     *             extending (implementing) the specified class (interface).
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#implement
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#implementJar
     */
    public static void main(String[] args) {
        for (var it : args) {
            if (it == null || (args.length > 4 || args.length < 2)) {
                throw new IllegalArgumentException("Uncorrected data: ones argument is null or incorrect quantity");
            }
        }
        Implementor impl = new Implementor();
        try {
            if (args.length == 3) {
                impl.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (args.length == 4) {
                impl.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (ClassNotFoundException | ImplerException | RuntimeException e) {
            System.err.println("implement can't start " + e);
        }
    }

    /**
     * Getting the path of the class to compile it
     *
     * @param token to create a path
     * @return the path for the compiled class
     * This method of reuse by
     * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
     */
    private static String getClassPath(Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * creates directories at the specified path
     *
     * @param path path to the directory
     *             <br>if it fails to create
     * @throws IOException if the file could not be created
     */
    private void creatingDirectories(Path path) throws IOException {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IOException("Failed to write to output file", e);
        }
    }

    /**
     * Returns the fully qualified package name.
     * If this class is a top level class, then this method returns
     * the fully qualified name of the package that the class is a member of,
     * or the empty string if the class is in an unnamed package.
     *
     * @param token token given {@link Class} to implement
     * @return {@link String} representing package of generated class
     */
    private static String writingPackage(Class<?> token) {
        return token.getPackage().getName();
    }

    /**
     * the class name is generated + "Impl"
     *
     * @param token token token given {@link Class} to implement
     * @return {@link String} returns the part of the path that contains '/' +
     * the name of the interface name that came to the input + "Impl.java"
     */
    private static String writingNameClass(Class<?> token) {
        return "/" + token.getSimpleName() + "Impl.java";
    }

    /**
     * Writes to the {@link StringBuilder} the name of the package in which the generated one lies, if there is no package, then writes "",
     * inserts an empty string and writes to the {@link StringBuilder} the beginning of the class
     *
     * @param token token token given {@link Class} to implement
     * @return {@link StringBuilder}  consisting of <b>package</b> + <b>name of package</b> + '\n + <b>access modifier</b>, file <b>name</b> and <b>"{"</b>
     */
    private static StringBuilder inceptionClass(Class<?> token) {
        StringBuilder sb = new StringBuilder();
        if (!writingPackage(token).equals("")) {
            sb.append("package ").append(writingPackage(token)).append(";");
        }
        return sb.append(System.lineSeparator().repeat(2))
                .append("public class ").append(token.getSimpleName()).append("Impl")
                .append(" implements ").append(token.getCanonicalName())
                .append(" {");
    }

    /**
     * writes the method modifier to the {@link StringBuilder}
     *
     * @param identifier {@link Method} modifier from the list of interface modifiers
     * @return {@link StringBuilder} with the name of the method modifier
     */
    private static StringBuilder identifierMethods(Method identifier) {
        StringBuilder sb = new StringBuilder();
        return sb.append(System.lineSeparator())
                .append("\tpublic ")
                .append(identifier.getReturnType().getCanonicalName())
                .append(" ")
                .append(identifier.getName());
    }

    /**
     * writes the arguments of the method to the {@link StringBuilder}
     *
     * @param params a parameters from the list of parameters
     *               of the input interface for which we are generating a class
     * @return <b>"("</b> + <b>...params</b> + <b>")"</b> + <b>"{"</b>
     */
    private static StringBuilder parametersOfMethod(Method params) {
        StringBuilder sb = new StringBuilder("(");
        String parameters = Arrays.stream(params.getParameters())
                .map(p -> p.getType().getCanonicalName() + " " + p.getName())
                .collect(Collectors.joining(", "));
        sb.append(parameters).append(") {").append(System.lineSeparator());
        return sb;
    }

    /**
     * we collect all the generated code into a single whole
     * piecemeal collected from methods:<br>
     *
     * @param token  token given {@link Class} to implement
     * @param writer {@link BufferedWriter} where to record
     * @throws IOException in case it was not possible to write the code to a file
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#identifierMethods
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#parametersOfMethod
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#checkingReturnType
     */

    private static void generateMethods(Class<?> token, BufferedWriter writer) throws IOException {
        writer.write(inceptionClass(token).toString());
        for (Method iterator : token.getMethods()) {
            writer.write(identifierMethods(iterator).toString());
            writer.write(parametersOfMethod(iterator).toString());
            writer.write(checkingReturnType(iterator));
            writer.write(System.lineSeparator() + "\t  }");
        }
        writer.write(System.lineSeparator() + "}");
    }

    /**
     * Сhecks which type of data the method returns from the task conditions
     *
     * @param returnType {@link Method} current method
     * @return type for a method from all interface methods
     */
    private static String checkingReturnType(Method returnType) {
        Class<?> type = returnType.getReturnType();
        String defaultValue = type.isPrimitive()
                ? (type.equals(boolean.class) ? "false"
                : type.equals(void.class) ? "" : "0")
                : "null";
        return "\t\t return " + defaultValue + ";";
    }

    /**
     * we write all the code to a file
     *
     * @param token      given {@link Class} to implement
     * @param pathToFile {@link Path} the path to the file where we write everything
     * @throws ImplerException throws an error if it was not possible to write
     * @see info.kgeorgiy.ja.kurkin.implementor.Implementor#generateMethods
     */
    private static void constructingClass(Class<?> token, Path pathToFile) throws ImplerException {
        try (BufferedWriter writer = Files.newBufferedWriter(pathToFile)) {
            generateMethods(token, writer);
        } catch (IOException e) {
            throw new ImplerException("Failed to write to output file", e);
        }
    }

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer.
     *
     * @param jarFile {@link Path} the path to the file .jar, where we write
     * @param myDir   {@link Path} the path to the temporary directory
     * @param path    {@link String} the path to the file on the basis of which we do .jar
     * @throws ImplerException if it failed to record
     */
    private void write(Path jarFile, Path myDir, String path) throws ImplerException {
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), new Manifest())) {
            String classPath = path + ".class";
            ZipEntry zip = new ZipEntry(classPath);
            writer.putNextEntry(zip);
            Files.copy(Paths.get(myDir + "/" + classPath), writer);
        } catch (IOException | InvalidPathException e) {
            throw new ImplerException("Failed to write in jar", e);
        }
    }

    /**
     * compiling a class
     * This method of reuse by
     *
     * @param files {@link List<String>} class path parameter
     * @param token given {@link Class} to implement
     * @return compiled file
     * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
     * Getting the path of the class to compile it
     */
    public static int compileFiles(final List<String> files, Class<?> token) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String classpath = getClassPath(token);
        final String[] args = Stream.concat(files.stream(), Stream.of("-cp", classpath, "-encoding", "UTF-8")).toArray(String[]::new);
        return compiler.run(null, null, null, args);
    }

    /**
     * Creates a <var>.java</var> file containing the source code of the class
     *
     * @param token {@link Class} type token to create implementation
     * @param root  root directory.
     * @throws ImplerException if it was not possible to create a class on the path from the token
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't creating class with this token");
        }
        try {
            Path path = Paths.get(root.toString(), (writingPackage(token).split("\\.")));
            creatingDirectories(path);
            Path pathToFile = Path.of(path + writingNameClass(token));
            constructingClass(token, pathToFile);
        } catch (InvalidPathException e) {
            System.err.println("invalid path ex" + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a <var>.jar</var> file in which is located the compiled code of the class
     * implemented by {@link #implement(Class, Path)} on the way by jarFile
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if any error occurred during execution
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            Path myDir = Path.of(".");
            implement(token, myDir);
            String path = token.getPackage().getName().replace(".", "/") + "/" + token.getSimpleName() + "Impl";
            if (compileFiles(List.of(path + ".java"), token) != 0) {
                throw new ImplerException("Can't creating class with this token");
            }
            write(jarFile, myDir, path.toString());
        } catch (InvalidPathException e) {
            System.err.println("Uncorrected path " + e);
        }
    }
}
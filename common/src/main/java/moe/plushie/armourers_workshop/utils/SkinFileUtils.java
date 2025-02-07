package moe.plushie.armourers_workshop.utils;

import com.google.common.collect.Lists;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * because `commons.io` versions on some servers are too low,
 * so we can't directly reference `common.io` in the other source code.
 */
public class SkinFileUtils {

    /**
     * Normalizes a path, removing double and single dot path steps.
     */
    public static String normalize(final String filename) {
        return FilenameUtils.normalize(filename);
    }

    /**
     * Normalizes a path, removing double and single dot path steps.
     */
    public static String normalize(final String filename, final boolean unixSeparator) {
        return FilenameUtils.normalize(filename, unixSeparator);
    }

    /**
     * Normalizes a path, removing double and single dot path steps,
     * and removing any final directory separator.
     */
    public static String normalizeNoEndSeparator(final String filename, final boolean unixSeparator) {
        return FilenameUtils.normalizeNoEndSeparator(filename, unixSeparator);
    }

    /**
     * Concatenates a filename to a base path using normal command line style rules.
     */
    public static String concat(final String basePath, final String fullFilenameToAdd) {
        return FilenameUtils.concat(basePath, fullFilenameToAdd);
    }

    public static List<File> listFiles(final File directory) {
        try {
            var files = directory.listFiles();
            if (files != null) {
                return Lists.newArrayList(files);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return Collections.emptyList();
    }

    public static List<File> listAllFiles(final File directory) {
        var allFiles = new ArrayList<File>();
        allFiles.add(directory);
        for (int i = 0; i < allFiles.size(); ++i) {
            var path = allFiles.get(i);
            if (path.isDirectory()) {
                allFiles.addAll(listFiles(path));
            }
        }
        allFiles.remove(0);
        return allFiles;
    }

    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     */
    public static String getBaseName(final String filename) {
        return FilenameUtils.getBaseName(filename);
    }

    /**
     * Removes the extension from a filename.
     */
    public static String removeExtension(final String filename) {
        return FilenameUtils.removeExtension(filename);
    }

    /**
     * Gets the extension of a filename.
     */
    public static String getExtension(final String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public static String getRelativePath(final String path, final String rootPath) {
        if (path.equals(rootPath)) {
            return "/";
        }
        if (path.startsWith(rootPath)) {
            return path.substring(rootPath.length());
        }
        return path;
    }

    public static String getRelativePath(final String path, final String rootPath, final boolean unixSeparator) {
        return normalize(getRelativePath(path, rootPath), unixSeparator);
    }

    public static String getRelativePath(final File path, final File rootPath) {
        return getRelativePath(path.getAbsolutePath(), rootPath.getAbsolutePath());
    }

    public static String getRelativePath(final File path, final File rootPath, final boolean unixSeparator) {
        return normalize(getRelativePath(path, rootPath), unixSeparator);
    }

    /**
     * Makes a directory, including any necessary but nonexistent parent
     * directories. If a file already exists with specified name but it is
     * not a directory then an IOException is thrown.
     * If the directory cannot be created (or does not already exist)
     * then an IOException is thrown.
     */
    public static void forceMkdir(final File directory) throws IOException {
        FileUtils.forceMkdir(directory);
    }

    /**
     * Makes any necessary but nonexistent parent directories for a given File. If the parent directory cannot be
     * created then an IOException is thrown.
     */
    public static void forceMkdirParent(final File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent != null) {
            forceMkdir(parent);
        }
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     */
    public static boolean deleteQuietly(final File file) {
        return FileUtils.deleteQuietly(file);
    }

    /**
     * Reads the contents of a file into a byte array.
     * The file is always closed.
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    /**
     * Reads the contents of input stream into a byte array.
     * The input stream is always closed.
     */
    public static byte[] readStreamToByteArray(final InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5 * 1024);
        SkinFileUtils.transferTo(inputStream, outputStream);
        IOUtils.closeQuietly(inputStream);
        return outputStream.toByteArray();
    }

    /**
     * Copies bytes from an InputStream source to a file destination.
     * The directories up to destination will be created if they don't already exist.
     * destination will be overwritten if it already exists.
     * The source stream is closed.
     */
    public static void copyInputStreamToFile(final InputStream inputStream, final File destination) throws IOException {
        forceMkdirParent(destination);
        OutputStream outputStream = new FileOutputStream(destination);

        int bytesRead;
        byte[] buffer = new byte[8 * 1024];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);
    }

    /**
     * Copies bytes from an InputStream to an OutputStream.
     * This method buffers the input internally, so there is no need to use a BufferedInputStream.
     * Large streams (over 2GB) will return a bytes copied value of -1 after the copy has completed
     * since the correct number of bytes cannot be returned as an int.
     * For large streams use the copyLarge(InputStream, OutputStream) method.
     */
    public static void transferTo(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        IOUtils.copy(inputStream, outputStream);
    }

    public static void writeNBT(CompoundTag compoundTag, File file) throws IOException {
        writeNBT(compoundTag, new FileOutputStream(file));
    }

    public static void writeNBT(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
        try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    public static CompoundTag readNBT(File file) throws IOException {
        if (file.exists()) {
            return readNBT(new FileInputStream(file));
        }
        return null;
    }

    public static CompoundTag readNBT(InputStream inputStream) throws IOException {
        try (var datainputstream = new DataInputStream(inputStream)) {
            return NbtIo.read(datainputstream);
        }
    }

    public static CompoundTag readNBT(String contents) {
        try {
            return TagParser.parseTag(contents);
        } catch (Exception e) {
            return new CompoundTag();
        }
    }

    public static String dumpTree(Skin skin) {
        var tree = new StringBuilder();
        tree.append("<Skin ");
        tree.append("name=").append(skin.getCustomName()).append(",");
        tree.append("author=").append(skin.getAuthorName()).append(",");
        tree.append("type=").append(skin.getType().getRegistryName().getPath());
        tree.append(">\n");
        for (var part : skin.getParts()) {
            var prefix = "|-- ";
            var subtree = dumpTree(part);
            for (var line : subtree.split("\n")) {
                tree.append(prefix).append(line).append("\n");
                prefix = "| ";
            }
        }
        return tree.toString();
    }

    private static String dumpTree(SkinPart part) {
        var name = part.getName();
        if (name == null) {
            name = "";
        }
        var tree = new StringBuilder();
        tree.append("<SkinPart ");
        tree.append("name=").append(name).append(",");
        tree.append("type=").append(part.getType().getRegistryName().getPath());
        tree.append(">\n");
        for (var childPart : part.getChildren()) {
            var prefix = "|-- ";
            var subtree = dumpTree(childPart);
            for (var line : subtree.split("\n")) {
                tree.append(prefix).append(line).append("\n");
                prefix = "| ";
            }
        }
        for (var geometry : part.getGeometries()) {
            tree.append("<SkinCube ").append("rect=").append(geometry.getShape()).append(",").append("type=").append(geometry.getType().getRegistryName().getPath()).append(">\n");
        }
        return tree.toString();
    }
}

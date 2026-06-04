package dev.unpack;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Main {

    private static final int BUFFER_SIZE = 8 * 1024; // 8 KB

    public static void main(String[] args) {
        try {
            Yaml yaml = new Yaml();
            InputStream cfgStream = new FileInputStream("config.yml");
            Map<String, Object> cfg = yaml.load(cfgStream);

            String archive = (String) cfg.getOrDefault("archive", "data.zip");
            boolean deleteAfter = (Boolean) cfg.getOrDefault("deleteAfter", true);

            System.out.println("Extracting: " + archive);

            if (archive.endsWith(".zip")) {
                unzip(archive);
            } else if (archive.endsWith(".tar.gz")) {
                untarGz(archive);
            } else {
                throw new RuntimeException("Unsupported format: " + archive);
            }

            if (deleteAfter) {
                new File(archive).delete();
            }

            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void streamCopy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buf)) != -1) {
            out.write(buf, 0, read);
        }
    }

    public static void unzip(String file) throws IOException {
        Path destDir = Path.of(".").toAbsolutePath().normalize();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE))) {

            org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {
                String name = entry.getName();
                if (name == null || name.isBlank()) continue;

                try {
                    Path target = destDir.resolve(name).normalize();
                    if (!target.startsWith(destDir)) continue;

                    File out = target.toFile();

                    if (entry.isDirectory()) {
                        out.mkdirs();
                        continue;
                    }

                    File parent = out.getParentFile();
                    if (parent != null) parent.mkdirs();

                    try (BufferedOutputStream fos = new BufferedOutputStream(
                            new FileOutputStream(out), BUFFER_SIZE)) {
                        streamCopy(zis, fos);
                    }
                } catch (java.nio.file.InvalidPathException e) {
                    System.out.println("Skipping: " + name + " (invalid path characters)");
                }
            }
        }
    }

    public static void untarGz(String file) throws IOException {
        Path destDir = Path.of(".").toAbsolutePath().normalize();
        try (TarArchiveInputStream tis = new TarArchiveInputStream(
                new GZIPInputStream(
                        new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)))) {

            TarArchiveEntry entry;

            while ((entry = tis.getNextTarEntry()) != null) {
                String name = entry.getName();
                if (name == null || name.isBlank()) continue;

                Path target = destDir.resolve(name).normalize();
                if (!target.startsWith(destDir)) continue;

                File out = target.toFile();

                if (entry.isDirectory()) {
                    out.mkdirs();
                    continue;
                }

                File parent = out.getParentFile();
                if (parent != null) parent.mkdirs();

                try (BufferedOutputStream fos = new BufferedOutputStream(
                        new FileOutputStream(out), BUFFER_SIZE)) {
                    streamCopy(tis, fos);
                }
            }
        }
    }
}
package dev.unpack;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Main {
    
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

    // zip
    public static void unzip(String file) throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(file))) {

            org.apache.commons.compress.archivers.zip.ZipArchiveEntry entry;

            while ((entry = zis.getNextZipEntry()) != null) {

                File out = new File(entry.getName());

                if (entry.isDirectory()) {
                    out.mkdirs();
                    continue;
                }

                new File(out.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(out)) {
                    zis.transferTo(fos);
                }
            }
        }
    }

    // tar.gz
    public static void untarGz(String file) throws IOException {

        try (TarArchiveInputStream tis = new TarArchiveInputStream(
                new GZIPInputStream(new FileInputStream(file)))) {

            TarArchiveEntry entry;

            while ((entry = tis.getNextTarEntry()) != null) {

                String name = entry.getName();

                // protect form null 
                if (name == null || name.isBlank()) {
                    continue;
                }

                // protect from path traversal
                if (name.contains("..") || name.startsWith("/")) {
                    continue;
                }

                File out = new File(name);

                if (entry.isDirectory()) {
                    out.mkdirs();
                    continue;
                }

                File parent = out.getParentFile();
                if (parent != null)
                    parent.mkdirs();

                try (FileOutputStream fos = new FileOutputStream(out)) {
                    tis.transferTo(fos);
                }
            }
        }
    }
}
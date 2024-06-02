package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AVRDudeDownloader {
    private static final Logger logger = LoggerFactory.getLogger(AVRDudeDownloader.class);
    public static final String WINDOWS = "mingw";
    public static final String LINUX = "linux";
    public static final String MACOS = "apple";
    private static final String ARDUINO_PACKAGE_INDEX = "https://downloads.arduino.cc/packages/package_index.json";

    public static void main(String[] args) throws IOException {
        downloadAVRDude(getArch());
    }


    public static String downloadAVRDude() throws IOException {
        return downloadAVRDude(getArch());
    }

    public static String downloadAVRDude(String arch) throws IOException {
        String url = getURLforOS(arch);
        if (url != null) {
            try {
                return downloadAndExtract(url);
            } catch (IOException e) {
                logger.error("Error downloading avrdude", e);
                throw e;
            }
        }
        return null;
    }

    public static String getArch() {
        String arch = LINUX;
        if (OSHelper.isWindows()) arch = WINDOWS;
        if (OSHelper.isOSX()) arch = MACOS;
        return arch;
    }

    /**
     * @param arch one of WINDOWS, LINUX, MACOS
     * @return URL to download the latest avrdude package for the given OS.
     * @throws IOException
     */
    public static String getURLforOS(String arch) throws IOException {
        JSONObject jsonObject = getJSONFromURL(ARDUINO_PACKAGE_INDEX);
        JSONObject arduinoPackage = getPackageNamedArduino(jsonObject);

        if (arduinoPackage != null) {
            JSONObject avrdudeTool = getLastToolNamedAVRDude(arduinoPackage);

            if (avrdudeTool != null) {
                JSONObject system = getSystemForHost(avrdudeTool, arch);
                if (system != null) {
                    return system.getString("url");
                }
            }
        }
        return null;
    }

    // search the systems list for the one where host contains "apple"
    private static JSONObject getSystemForHost(JSONObject avrdudeTool, String arch) {
        JSONArray systems = avrdudeTool.getJSONArray("systems");
        for (int i = 0; i < systems.length(); i++) {
            JSONObject systemObject = systems.getJSONObject(i);
            if (systemObject.getString("host").contains(arch)) {
                return systemObject;
            }
        }
        return null;
    }

    // find the arduinoPackage element with name=avrdude.  last is latest release.
    private static JSONObject getLastToolNamedAVRDude(JSONObject arduinoPackage) {
        JSONArray tools = arduinoPackage.getJSONArray("tools");
        JSONObject avrdudeTool = null;
        for (int i = 0; i < tools.length(); i++) {
            JSONObject toolObject = tools.getJSONObject(i);
            if (toolObject.getString("name").equals("avrdude")) {
                avrdudeTool = toolObject;
            }
        }
        return avrdudeTool;
    }

    // find the element in packages with name=arduino
    private static JSONObject getPackageNamedArduino(JSONObject jsonObject) {
        JSONArray packages = jsonObject.getJSONArray("packages");
        JSONObject arduinoPackage = null;
        for (int i = 0; i < packages.length(); i++) {
            JSONObject packageObject = packages.getJSONObject(i);
            if (packageObject.getString("name").equals("arduino")) {
                arduinoPackage = packageObject;
                break;
            }
        }
        return arduinoPackage;
    }

    private static JSONObject getJSONFromURL(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String json = EntityUtils.toString(httpResponse.getEntity());
            return new JSONObject(json);
        }
    }

    public static String downloadAndExtract(String urlStr) throws IOException {
        File toDeleteLater = downloadFileToTemp(urlStr);
        String path = extractFile(toDeleteLater, urlStr);
        logger.info("new path: "+path);
        makeExecutable(path);

        return path;
    }

    private static void makeExecutable(String targetPath) throws IOException {
        String app = "avrdude";
        if(OSHelper.isWindows()) app+=".exe";

        File avrDudeFile = tryPath(Paths.get(targetPath, app));
        if(!avrDudeFile.exists()) avrDudeFile = tryPath(Paths.get(targetPath, "bin", app));
        if(!avrDudeFile.exists()) {
            throw new IOException("File " + app + " does not exist");
        }
        if(!avrDudeFile.setExecutable(true)) {
            throw new IOException("Could not set executable permissions for " + avrDudeFile.getAbsolutePath());
        }
    }

    private static File tryPath(Path avrdudePath) {
        logger.info("trying path: " + avrdudePath.toAbsolutePath());
        return avrdudePath.toFile();
    }

    private static File downloadFileToTemp(String urlStr) throws IOException {
        logger.info("download from: " + urlStr);
        URL url = new URL(urlStr);
        String fileExtension = getFileExtension(url.getPath());
        File toDeleteLater = File.createTempFile("download", fileExtension);
        toDeleteLater.deleteOnExit();
        Path tempFile = toDeleteLater.toPath();
        logger.info("temp file: " + tempFile);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(urlStr);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                try (InputStream inputStream = response.getEntity().getContent();
                     OutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                }
            }
        }
        return toDeleteLater;
    }

    private static String extractFile(File toDeleteLater, String urlStr) throws IOException {
        String targetDirStr = System.getProperty("user.home") + File.separator + ".makelangelo" + File.separator;
        String newFolderName = "";

        if (urlStr.endsWith(".zip")) {
            logger.info("Extracting zip file");
            newFolderName = extractZipFile(toDeleteLater, targetDirStr);
        } else if (urlStr.endsWith(".tar.bz2")) {
            logger.info("Extracting tar.bz2 file");
            newFolderName = extractTarBz2File(toDeleteLater, targetDirStr);
        } else if (urlStr.endsWith(".bz2")) {
            logger.info("Extracting bz2 file");
            newFolderName = extractBz2File(toDeleteLater, targetDirStr);
        }

        if(!newFolderName.isEmpty()) {
            newFolderName = targetDirStr + newFolderName;
        }

        return newFolderName;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private static String extractZipFile(File file, String targetDirStr) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry = zipIn.getNextEntry();
            String newFolderName = "";
            while (entry != null) {
                Path filePath = Paths.get(targetDirStr, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zipIn.read(buffer)) > -1) {
                            outputStream.write(buffer, 0, len);
                        }
                    }
                }
                zipIn.closeEntry();
                if (newFolderName.isEmpty() && entry.isDirectory()) {
                    newFolderName = entry.getName();
                }
                entry = zipIn.getNextEntry();
            }
            return newFolderName;
        }
    }

    private static String extractBz2File(File file, String targetDirStr) throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(file))) {
            Path targetFilePath = Paths.get(targetDirStr, file.getName().replace(".bz2", ""));
            Files.createDirectories(targetFilePath.getParent());
            try (OutputStream outputStream = new FileOutputStream(targetFilePath.toFile())) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = bzIn.read(buffer)) > -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
            return targetFilePath.getFileName().toString();
        }
    }

    private static String extractTarBz2File(File file, String targetDirStr) throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(file));
             TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)) {
            TarArchiveEntry entry;
            String newFolderName = "";
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                Path filePath = Paths.get(targetDirStr, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = tarIn.read(buffer)) > -1) {
                            outputStream.write(buffer, 0, len);
                        }
                    }
                }
                if (newFolderName.isEmpty()) {
                    newFolderName = getFolderNameFromEntry(entry.getName());
                }
            }
            return newFolderName;
        }
    }

    private static String getFolderNameFromEntry(String entryName) {
        String[] parts = entryName.split("/");
        if (parts.length > 1) {
            return String.join(File.separator, Arrays.copyOfRange(parts, 0, parts.length - 1)) + File.separator;
        } else {
            return parts[0];
        }
    }
}

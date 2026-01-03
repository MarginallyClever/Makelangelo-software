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
import java.net.URI;
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

    /**
     * Download AVRDude for the current OS architecture.
     * @return the path to the first folder.  If the target is c:\a\b\ and the extracted files are in c:\a\b\c\d\
     *         then this will return c:\a\b\c
     * @throws IOException if the download fails.
     */
    public static String downloadAVRDude() throws IOException {
        return downloadAVRDude(getArch());
    }

    /**
     * Download AVRDude for the given OS architecture.
     * @param arch one of WINDOWS, LINUX, MACOS
     * @return the path to the extracted avrdude executable.
     * @throws IOException if the download fails.
     */
    public static String downloadAVRDude(String arch) throws IOException {
        String url = getURLforOS(arch);
        if (url == null) return null;

        try {
            return downloadAndExtract(url);
        } catch (IOException e) {
            logger.error("Error downloading avrdude", e);
            throw e;
        }
    }

    public static String getArch() {
        if (OSHelper.isWindows()) return WINDOWS;
        if (OSHelper.isOSX()) return MACOS;
        return LINUX;
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

    /**
     * Search the systems list for the one where host contains the given architecture.
     * @param jsonObject the avrdude tool object
     * @param arch the architecture to search for
     * @return the system object that matches the given architecture
     */
    private static JSONObject getSystemForHost(JSONObject jsonObject, String arch) {
        JSONArray systems = jsonObject.getJSONArray("systems");
        for (int i = 0; i < systems.length(); i++) {
            JSONObject systemObject = systems.getJSONObject(i);
            if (systemObject.getString("host").contains(arch)) {
                return systemObject;
            }
        }
        return null;
    }

    /**
     * Search the arduinoPackage for element with name=avrdude.  Compare element version values to get the latest.
     * Versions may be in the format "6.3.0-avrdude9" or "6.3.0-avrdude18" so we need to extract the numeric version.
     * @param arduinoPackage the package to search
     * @return the last item in the tools list with name=avrdude
     */
    private static JSONObject getLastToolNamedAVRDude(JSONObject arduinoPackage) {
        JSONArray tools = arduinoPackage.getJSONArray("tools");
        JSONObject avrdudeTool = null;

        int bestVersion = -1;
        for (int i = 0; i < tools.length(); i++) {
            JSONObject toolObject = tools.getJSONObject(i);
            if (!toolObject.getString("name").equals("avrdude")) continue;
            var version = toolObject.getString("version");
            logger.info("found avrdude tool at index {} with version {}", i, version);
            int v = Integer.parseInt(version.replaceAll("\\D", ""));
            if(v > bestVersion) {
                logger.info("new best version: {}", v);
                bestVersion = v;
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
        logger.info("new path: {}", path);
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
        try {
            var url = new URI(urlStr);
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
        catch (Exception e) {
            throw new IOException("Error downloading file: " + e.getMessage());
        }
    }

    /**
     * Extract the downloaded file to ~/.makelangelo
     * @param toDeleteLater the file to delete after extraction
     * @param urlStr the URL of the file
     * @return the path to the extracted file, which should be ~/.makelangelo/firstSubDirectory
     * @throws IOException if the extraction fails
     */
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

        // sometimes the path returned might be /a/b/... and we only want /a/
        if(newFolderName.contains("/")) {
            newFolderName = newFolderName.split("/")[0];
        }

        return targetDirStr + newFolderName;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    /**
     * Extract a zip file to the target directory.
     * @param file the zip file
     * @param targetDirStr the target directory
     * @return the name of the first directory in the zip file
     * @throws IOException if the extraction fails
     */
    private static String extractZipFile(File file, String targetDirStr) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry = zipIn.getNextEntry();
            String newFolderName = "";
            while (entry != null) {
                Path filePath = Paths.get(targetDirStr, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                    // the first directory is the one we want to return
                    if (newFolderName.isEmpty()) {
                        newFolderName = entry.getName();
                    }
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
                entry = zipIn.getNextEntry();
            }
            return newFolderName;
        }
    }

    /**
     * Extract a bz2 file to the target directory.
     * @param file the zip file
     * @param targetDirStr the target directory
     * @return the name of the first directory in the bz2 file
     * @throws IOException if the extraction fails
     */
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

    /**
     * Extract a .tar.bz2 file to the target directory.
     * @param file the zip file
     * @param targetDirStr the target directory
     * @return the name of the first directory in the bz2 file
     * @throws IOException if the extraction fails
     */
    private static String extractTarBz2File(File file, String targetDirStr) throws IOException {
        try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(file));
             TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)) {
            TarArchiveEntry entry;
            String newFolderName = "";
            while ((entry = tarIn.getNextEntry()) != null) {
                Path filePath = Paths.get(targetDirStr, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                    if (newFolderName.isEmpty()) {
                        newFolderName = getFolderNameFromEntry(entry.getName());
                    }
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

package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.log.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

/**
 * Common methods for downloading firmware from Github
 * @since 7.39.3
 * @author Dan Royer
 */
public class FirmwareDownloader {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareDownloader.class);
    private static final String GITHUB_API = "https://api.github.com/repos/MarginallyClever/Marlin-polargraph/releases/latest";
    private static final String DOWNLOAD_PATH = Log.logDir + File.separator;

    public FirmwareDownloader() {
        super();
    }

    /**
     * Query github.com for the latest release.
     * @return the latest release as a JSONObject
     */
    private JSONObject queryGithub() {
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(GITHUB_API);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return new JSONObject(EntityUtils.toString(entity));
                }
            }
        }
        catch (IOException e) {
            logger.error("Cannot query Github: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Download the hex file from the given URL to the given path.  Set the last modified time to the given timestamp.
     * @param url the URL to download from
     * @param outputPath the path to save the file to
     * @param updatedAt the timestamp to set the last modified time to
     * @throws IOException if the file cannot be downloaded
     */
    private void downloadHexFile(String url, String outputPath, String updatedAt) throws IOException {
        try {
            var website = new URI(url);
            try (ReadableByteChannel rbc = Channels.newChannel(website.toURL().openStream())) {
                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error downloading file: " + e.getMessage());
        }

        // Parse the updated_at timestamp into a FileTime
        Instant instant = Instant.parse(updatedAt);
        FileTime fileTime = FileTime.from(instant);

        // Set the last modified time of the local file
        Path path = Paths.get(outputPath);
        Files.setLastModifiedTime(path, fileTime);
    }

    /**
     * Check if the file exists and has the same last modified time as the given timestamp.
     * @param filename the name of the file to check
     * @param updatedAt the timestamp to check against
     * @return true if the file exists and has the same last modified time as the given timestamp
     */
    private boolean fileExistsWithMatchingTimestamp(String filename, String updatedAt) {
        try {
            // Get the path to the file
            Path path = Paths.get( filename );

            // Check if file exists
            if(Files.exists(path)) {
                // Parse the updated_at timestamp into a FileTime
                Instant instant = Instant.parse(updatedAt);
                FileTime fileTime = FileTime.from(instant);
                // Get the last modified time of the file
                FileTime existingFileTime = Files.getLastModifiedTime(path);
                // Check if the times match
                return fileTime.equals(existingFileTime);
            }
        } catch (Exception e) {
            logger.error("Error while checking file timestamp: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Download the asset information from the latest release on Github.
     * @param name the name of the asset to download
     * @return the asset information as a JSONObject or null if the asset is not found
     */
    private JSONObject downloadAssetInformation(String name) {
        JSONObject latestRelease = queryGithub();
        if(latestRelease==null) return null;

        String releaseName = latestRelease.getString("name");
        logger.info("Latest Release: {}", releaseName);

        // Get the assets array from the release
        JSONArray assets = latestRelease.getJSONArray("assets");
        for(int j = 0; j < assets.length(); j++) {
            JSONObject asset = assets.getJSONObject(j);
            // Check if the asset name ends with .hex
            String assetName = asset.getString("name");
            if (name.equals(assetName)) {
                return asset;
            }
        }
        logger.error("asset not found: {}",name);
        return null;
    }

    public String getDownloadPath(String name) {
        return DOWNLOAD_PATH + name;
    }

    /**
     * Query github.com for the latest release, then download the firmware file to the DOWNLOAD_PATH.
     * @param name the name of the firmware file to download
     */
    public boolean getFirmware(String name) {
        JSONObject asset = downloadAssetInformation(name);
        if(asset==null) return false;

        String localPath = getDownloadPath(name);

        String updatedAt = asset.getString("updated_at");
        if(fileExistsWithMatchingTimestamp(localPath, updatedAt)) {
            logger.info("file already exists: {}",name);
            return true;
        }

        try {
            downloadHexFile(asset.getString("browser_download_url"), localPath, updatedAt);
        } catch (IOException e) {
            logger.error("Download failed: {}", e.getMessage());
            return false;
        }

        logger.info("file downloaded ok.");
        return true;
    }

    public static void main(String[] args) {
        FirmwareDownloader fd = new FirmwareDownloader();
        fd.getFirmware("firmware-m5.hex");
        fd.getFirmware("firmware-huge.hex");
    }
}
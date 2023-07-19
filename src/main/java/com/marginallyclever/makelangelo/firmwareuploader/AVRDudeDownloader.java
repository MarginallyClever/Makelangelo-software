package com.marginallyclever.makelangelo.firmwareuploader;

import com.marginallyclever.convenience.helpers.OSHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AVRDudeDownloader {
    public static final String WINDOWS = "mingw";
    public static final String LINUX = "linux";
    public static final String MACOS = "apple";
    public static void main(String[] args) throws IOException {
        String URL = getURLforUS(getArch());

    }

    public static String getArch() {
        String arch = LINUX;
        if(OSHelper.isWindows()) arch = WINDOWS;
        if(OSHelper.isOSX()) arch = MACOS;
        return arch;
    }

    public static String getURLforUS(String arch) throws IOException {
        JSONObject jsonObject = getJSONFrom("https://downloads.arduino.cc/packages/package_index.json");
        JSONObject arduinoPackage = getPackageNamedArduino(jsonObject);

        if (arduinoPackage != null) {
            JSONObject avrdudeTool = getLastToolNamedAVRDude(arduinoPackage);

            if (avrdudeTool != null) {
                JSONObject system = getSystemForHost(avrdudeTool, arch);
                if (system != null) {
                    String url = system.getString("url");
                    System.out.println("url: " + url);
                }
            }
        }
    }

    private static JSONObject getSystemForHost(JSONObject avrdudeTool, String arch) {
        // search the systems list for the one where host contains "apple"
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

    private static JSONObject getJSONFrom(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String json = EntityUtils.toString(httpResponse.getEntity());
            return new JSONObject(json);
        }
    }
}

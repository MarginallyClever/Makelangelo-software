package com.marginallyclever.makelangelo.makeart.io;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.zip.DataFormatException;

/**
 * Extract the JSON from a Factorio blueprint export.
 */
public class ExtractFactorioJSON {
    /**
     * Process the input string to extract the JSON data.
     * @param text the input string
     * @return the formatted JSON string
     * @throws DataFormatException if the data format is invalid
     * @throws JSONException if the JSON is invalid
     */
    public static String processInput(String text) throws DataFormatException, JSONException {
        // remove leading version byte and check it is ascii character zero.
        if (text.isEmpty()) {
            return "";
        }
        if (text.charAt(0) != '0') {
            return "string should start with version byte 0.";
        }
        // remove leading version byte
        var base64 = text.substring(1);
        var result = decodeBase64andUnZip(base64);
        // confirm json is valid
        var json = new JSONObject(result);
        return result;
    }

    /**
     * Decodes a base64 string and decompresses it using zlib.
     * @param base64 the base64 encoded string
     * @return the decompressed string
     * @throws DataFormatException if the data format is invalid
     */
    private static String decodeBase64andUnZip(String base64) throws DataFormatException {
        // Validate Base64 input
        if (base64 == null || base64.isEmpty()) {
            throw new IllegalArgumentException("Base64 input is empty or null.");
        }

        // decode base64
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);

        // decompress zlib level 9
        java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        inflater.setInput(decoded);

        byte[] decompressed = new byte[1000];
        var sb = new StringBuilder();
        while(!inflater.finished()) {
            int decompressedLength = inflater.inflate(decompressed);
            if (decompressedLength == 0) {
                break;
            }
            sb.append(new String(decompressed, 0, decompressedLength));
        }
        inflater.end();

        return sb.toString();
    }
}

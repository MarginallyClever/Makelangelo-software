package com.marginallyclever.convenience.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResourceHelper {
    public static String [] readResource(Class<?> clazz,String resourceName) throws IOException {
        List<String> lines = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clazz.getResourceAsStream(resourceName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line+"\n");
            }
        }
        return lines.toArray(new String[0]);
    }
}

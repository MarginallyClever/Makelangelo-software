package com.marginallyclever.nodeBasedEditor.model;

import org.json.JSONObject;

import java.awt.*;

public class JSONHelper {
    public static JSONObject rectangleToJSON(Rectangle rectangle) {
        JSONObject r = new JSONObject();
        r.put("x",rectangle.x);
        r.put("y",rectangle.y);
        r.put("width",rectangle.width);
        r.put("height",rectangle.height);
        return r;
    }

    public static Rectangle rectangleFromJSON(JSONObject r) {
        Rectangle rect = new Rectangle();
        rect.x = r.getInt("x");
        rect.y = r.getInt("y");
        rect.width = r.getInt("width");
        rect.height = r.getInt("height");
        return rect;
    }
}

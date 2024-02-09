package com.marginallyclever.convenience;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * W3C web colors, basic and extended.
 * obtained from many places like https://www.w3.org/TR/css-color-3/, https://www.w3.org/wiki/CSS/Properties/color/keywords
 * @author Dan Royer
 * @since 2022-04-25
 */
public class W3CColorNames {
    private static final Map<String, Color> map = new HashMap<>();
    static {
        map.put("aliceblue",new Color(0xF0,0xF8,0xFF));
        map.put("antiquewhite",new Color(0xFA,0xEB,0xD7));
        map.put("aqua",new Color(0x00,0xFF,0xFF));
        map.put("aquamarine",new Color(0x7F,0xFF,0xD4));
        map.put("azure",new Color(0xF0,0xFF,0xFF));
        map.put("beige",new Color(0xF5,0xF5,0xDC));
        map.put("bisque",new Color(0xFF,0xE4,0xC4));
        map.put("black",new Color(0x00,0x00,0x00));
        map.put("blanchedalmond",new Color(0xFF,0xEB,0xCD));
        map.put("blue",new Color(0x00,0x00,0xFF));
        map.put("blueviolet",new Color(0x8A,0x2B,0xE2));
        map.put("brown",new Color(0xA5,0x2A,0x2A));
        map.put("burlywood",new Color(0xDE,0xB8,0x87));
        map.put("cadetblue",new Color(0x5F,0x9E,0xA0));
        map.put("chartreuse",new Color(0x7F,0xFF,0x00));
        map.put("chocolate",new Color(0xD2,0x69,0x1E));
        map.put("coral",new Color(0xFF,0x7F,0x50));
        map.put("cornflowerblue",new Color(0x64,0x95,0xED));
        map.put("cornsilk",new Color(0xFF,0xF8,0xDC));
        map.put("crimson",new Color(0xDC,0x14,0x3C));
        map.put("cyan",new Color(0x00,0xFF,0xFF));
        map.put("darkblue",new Color(0x00,0x00,0x8B));
        map.put("darkcyan",new Color(0x00,0x8B,0x8B));
        map.put("darkgoldenrod",new Color(0xB8,0x86,0x0B));
        map.put("darkgray",new Color(0xA9,0xA9,0xA9));
        map.put("darkgrey",new Color(0xA9,0xA9,0xA9));
        map.put("darkgreen",new Color(0x00,0x64,0x00));
        map.put("darkkhaki",new Color(0xBD,0xB7,0x6B));
        map.put("darkmagenta",new Color(0x8B,0x00,0x8B));
        map.put("darkolivegreen",new Color(0x55,0x6B,0x2F));
        map.put("darkorange",new Color(0xFF,0x8C,0x00));
        map.put("darkorchid",new Color(0x99,0x32,0xCC));
        map.put("darkred",new Color(0x8B,0x00,0x00));
        map.put("darksalmon",new Color(0xE9,0x96,0x7A));
        map.put("darkseagreen",new Color(0x8F,0xBC,0x8F));
        map.put("darkslateblue",new Color(0x48,0x3D,0x8B));
        map.put("darkslategray",new Color(0x2F,0x4F,0x4F));
        map.put("darkslategrey",new Color(0x2F,0x4F,0x4F));
        map.put("darkturquoise",new Color(0x00,0xCE,0xD1));
        map.put("darkviolet",new Color(0x94,0x00,0xD3));
        map.put("deeppink",new Color(0xFF,0x14,0x93));
        map.put("deepskyblue",new Color(0x00,0xBF,0xFF));
        map.put("dimgray",new Color(0x69,0x69,0x69));
        map.put("dimgrey",new Color(0x69,0x69,0x69));
        map.put("dodgerblue",new Color(0x1E,0x90,0xFF));
        map.put("firebrick",new Color(0xB2,0x22,0x22));
        map.put("floralwhite",new Color(0xFF,0xFA,0xF0));
        map.put("forestgreen",new Color(0x22,0x8B,0x22));
        map.put("fuchsia",new Color(0xFF,0x00,0xFF));
        map.put("gainsboro",new Color(0xDC,0xDC,0xDC));
        map.put("ghostwhite",new Color(0xF8,0xF8,0xFF));
        map.put("gold",new Color(0xFF,0xD7,0x00));
        map.put("goldenrod",new Color(0xDA,0xA5,0x20));
        map.put("gray",new Color(0x80,0x80,0x80));
        map.put("grey",new Color(0x80,0x80,0x80));
        map.put("green",new Color(0x00,0x80,0x00));
        map.put("greenyellow",new Color(0xAD,0xFF,0x2F));
        map.put("honeydew",new Color(0xF0,0xFF,0xF0));
        map.put("hotpink",new Color(0xFF,0x69,0xB4));
        map.put("indianred",new Color(0xCD,0x5C,0x5C));
        map.put("indigo",new Color(0x4B,0x00,0x82));
        map.put("ivory",new Color(0xFF,0xFF,0xF0));
        map.put("khaki",new Color(0xF0,0xE6,0x8C));
        map.put("lavender",new Color(0xE6,0xE6,0xFA));
        map.put("lavenderblush",new Color(0xFF,0xF0,0xF5));
        map.put("lawngreen",new Color(0x7C,0xFC,0x00));
        map.put("lemonchiffon",new Color(0xFF,0xFA,0xCD));
        map.put("lightblue",new Color(0xAD,0xD8,0xE6));
        map.put("lightcoral",new Color(0xF0,0x80,0x80));
        map.put("lightcyan",new Color(0xE0,0xFF,0xFF));
        map.put("lightgoldenrodyellow",new Color(0xFA,0xFA,0xD2));
        map.put("lightgray",new Color(0xD3,0xD3,0xD3));
        map.put("lightgrey",new Color(0xD3,0xD3,0xD3));
        map.put("lightgreen",new Color(0x90,0xEE,0x90));
        map.put("lightpink",new Color(0xFF,0xB6,0xC1));
        map.put("lightsalmon",new Color(0xFF,0xA0,0x7A));
        map.put("lightseagreen",new Color(0x20,0xB2,0xAA));
        map.put("lightskyblue",new Color(0x87,0xCE,0xFA));
        map.put("lightslategray",new Color(0x77,0x88,0x99));
        map.put("lightslategrey",new Color(0x77,0x88,0x99));
        map.put("lightsteelblue",new Color(0xB0,0xC4,0xDE));
        map.put("lightyellow",new Color(0xFF,0xFF,0xE0));
        map.put("lime",new Color(0x00,0xFF,0x00));
        map.put("limegreen",new Color(0x32,0xCD,0x32));
        map.put("linen",new Color(0xFA,0xF0,0xE6));
        map.put("magenta",new Color(0xFF,0x00,0xFF));
        map.put("maroon",new Color(0x80,0x00,0x00));
        map.put("mediumaquamarine",new Color(0x66,0xCD,0xAA));
        map.put("mediumblue",new Color(0x00,0x00,0xCD));
        map.put("mediumorchid",new Color(0xBA,0x55,0xD3));
        map.put("mediumpurple",new Color(0x93,0x70,0xDB));
        map.put("mediumseagreen",new Color(0x3C,0xB3,0x71));
        map.put("mediumslateblue",new Color(0x7B,0x68,0xEE));
        map.put("mediumspringgreen",new Color(0x00,0xFA,0x9A));
        map.put("mediumturquoise",new Color(0x48,0xD1,0xCC));
        map.put("mediumvioletred",new Color(0xC7,0x15,0x85));
        map.put("midnightblue",new Color(0x19,0x19,0x70));
        map.put("mintcream",new Color(0xF5,0xFF,0xFA));
        map.put("mistyrose",new Color(0xFF,0xE4,0xE1));
        map.put("moccasin",new Color(0xFF,0xE4,0xB5));
        map.put("navajowhite",new Color(0xFF,0xDE,0xAD));
        map.put("navy",new Color(0x00,0x00,0x80));
        map.put("oldlace",new Color(0xFD,0xF5,0xE6));
        map.put("olive",new Color(0x80,0x80,0x00));
        map.put("olivedrab",new Color(0x6B,0x8E,0x23));
        map.put("orange",new Color(0xFF,0xA5,0x00));
        map.put("orangered",new Color(0xFF,0x45,0x00));
        map.put("orchid",new Color(0xDA,0x70,0xD6));
        map.put("palegoldenrod",new Color(0xEE,0xE8,0xAA));
        map.put("palegreen",new Color(0x98,0xFB,0x98));
        map.put("paleturquoise",new Color(0xAF,0xEE,0xEE));
        map.put("palevioletred",new Color(0xDB,0x70,0x93));
        map.put("papayawhip",new Color(0xFF,0xEF,0xD5));
        map.put("peachpuff",new Color(0xFF,0xDA,0xB9));
        map.put("peru",new Color(0xCD,0x85,0x3F));
        map.put("pink",new Color(0xFF,0xC0,0xCB));
        map.put("plum",new Color(0xDD,0xA0,0xDD));
        map.put("powderblue",new Color(0xB0,0xE0,0xE6));
        map.put("purple",new Color(0x80,0x00,0x80));
        map.put("rebeccapurple",new Color(0x66,0x33,0x99));
        map.put("red",new Color(0xFF,0x00,0x00));
        map.put("rosybrown",new Color(0xBC,0x8F,0x8F));
        map.put("royalblue",new Color(0x41,0x69,0xE1));
        map.put("saddlebrown",new Color(0x8B,0x45,0x13));
        map.put("salmon",new Color(0xFA,0x80,0x72));
        map.put("sandybrown",new Color(0xF4,0xA4,0x60));
        map.put("seagreen",new Color(0x2E,0x8B,0x57));
        map.put("seashell",new Color(0xFF,0xF5,0xEE));
        map.put("sienna",new Color(0xA0,0x52,0x2D));
        map.put("silver",new Color(0xC0,0xC0,0xC0));
        map.put("skyblue",new Color(0x87,0xCE,0xEB));
        map.put("slateblue",new Color(0x6A,0x5A,0xCD));
        map.put("slategray",new Color(0x70,0x80,0x90));
        map.put("slategrey",new Color(0x70,0x80,0x90));
        map.put("snow",new Color(0xFF,0xFA,0xFA));
        map.put("springgreen",new Color(0x00,0xFF,0x7F));
        map.put("steelblue",new Color(0x46,0x82,0xB4));
        map.put("tan",new Color(0xD2,0xB4,0x8C));
        map.put("teal",new Color(0x00,0x80,0x80));
        map.put("thistle",new Color(0xD8,0xBF,0xD8));
        map.put("tomato",new Color(0xFF,0x63,0x47));
        map.put("turquoise",new Color(0x40,0xE0,0xD0));
        map.put("violet",new Color(0xEE,0x82,0xEE));
        map.put("wheat",new Color(0xF5,0xDE,0xB3));
        map.put("white",new Color(0xFF,0xFF,0xFF));
        map.put("whitesmoke",new Color(0xF5,0xF5,0xF5));
        map.put("yellow",new Color(0xFF,0xFF,0x00));
        map.put("yellowgreen",new Color(0x9A,0xCD,0x32));
    }

    /**
     * @param name
     * @return Color matching name, or null if no match.
     */
    public static Color get(String name) {
        return map.get(name);
    }

    /**
     * @param match the color to matcch
     * @return the name matching the given color, or null if no match.
     */
    public static String get(Color match) {
        for(String key: map.keySet()) {
            Color c = map.get(key);
            if(c.equals(match)) return key;
        }
        return null;
    }
}

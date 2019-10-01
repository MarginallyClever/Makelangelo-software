package com.marginallyclever.convenience;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

public class NameThatColor {
	private class NamedColor {
		public String name;
		public long value;

		public NamedColor(String _name, long _value) {
			name = _name;
			value = _value;
		}
	};

	public ArrayList<NamedColor> colors;

	public NameThatColor() {
		colors = new ArrayList<NamedColor>();

		colors.add(new NamedColor("maroon", 0x800000));
		colors.add(new NamedColor("dark red", 0x8B0000));
		colors.add(new NamedColor("brown", 0xA52A2A));
		colors.add(new NamedColor("firebrick", 0xB22222));
		colors.add(new NamedColor("crimson", 0xDC143C));
		colors.add(new NamedColor("red", 0xFF0000));
		colors.add(new NamedColor("tomato", 0xFF6347));
		colors.add(new NamedColor("coral", 0xFF7F50));
		colors.add(new NamedColor("indian red", 0xCD5C5C));
		colors.add(new NamedColor("light coral", 0xF08080));
		colors.add(new NamedColor("dark salmon", 0xE9967A));
		colors.add(new NamedColor("salmon", 0xFA8072));
		colors.add(new NamedColor("light salmon", 0xFFA07A));
		colors.add(new NamedColor("orange red", 0xFF4500));
		colors.add(new NamedColor("dark orange", 0xFF8C00));
		colors.add(new NamedColor("orange", 0xFFA500));
		colors.add(new NamedColor("gold", 0xFFD700));
		colors.add(new NamedColor("dark golden rod", 0xB8860B));
		colors.add(new NamedColor("golden rod", 0xDAA520));
		colors.add(new NamedColor("pale golden rod", 0xEEE8AA));
		colors.add(new NamedColor("dark khaki", 0xBDB76B));
		colors.add(new NamedColor("khaki", 0xF0E68C));
		colors.add(new NamedColor("olive", 0x808000));
		colors.add(new NamedColor("yellow", 0xFFFF00));
		colors.add(new NamedColor("yellow green", 0x9ACD32));
		colors.add(new NamedColor("dark olive green", 0x556B2F));
		colors.add(new NamedColor("olive drab", 0x6B8E23));
		colors.add(new NamedColor("lawn green", 0x7CFC00));
		colors.add(new NamedColor("chart reuse", 0x7FFF00));
		colors.add(new NamedColor("green yellow", 0xADFF2F));
		colors.add(new NamedColor("dark green", 0x006400));
		colors.add(new NamedColor("green", 0x008000));
		colors.add(new NamedColor("forest green", 0x228B22));
		colors.add(new NamedColor("lime", 0x00FF00));
		colors.add(new NamedColor("lime green", 0x32CD32));
		colors.add(new NamedColor("light green", 0x90EE90));
		colors.add(new NamedColor("pale green", 0x98FB98));
		colors.add(new NamedColor("dark sea green", 0x8FBC8F));
		colors.add(new NamedColor("medium spring green", 0x00FA9A));
		colors.add(new NamedColor("spring green", 0x00FF7F));
		colors.add(new NamedColor("sea green", 0x2E8B57));
		colors.add(new NamedColor("medium aqua marine", 0x66CDAA));
		colors.add(new NamedColor("medium sea green", 0x3CB371));
		colors.add(new NamedColor("light sea green", 0x20B2AA));
		colors.add(new NamedColor("dark slate gray", 0x2F4F4F));
		colors.add(new NamedColor("teal", 0x008080));
		colors.add(new NamedColor("dark cyan", 0x008B8B));
		colors.add(new NamedColor("aqua", 0x00FFFF));
		colors.add(new NamedColor("cyan", 0x00FFFF));
		colors.add(new NamedColor("light cyan", 0xE0FFFF));
		colors.add(new NamedColor("dark turquoise", 0x00CED1));
		colors.add(new NamedColor("turquoise", 0x40E0D0));
		colors.add(new NamedColor("medium turquoise", 0x48D1CC));
		colors.add(new NamedColor("pale turquoise", 0xAFEEEE));
		colors.add(new NamedColor("aqua marine", 0x7FFFD4));
		colors.add(new NamedColor("powder blue", 0xB0E0E6));
		colors.add(new NamedColor("cadet blue", 0x5F9EA0));
		colors.add(new NamedColor("steel blue", 0x4682B4));
		colors.add(new NamedColor("corn flower blue", 0x6495ED));
		colors.add(new NamedColor("deep sky blue", 0x00BFFF));
		colors.add(new NamedColor("dodger blue", 0x1E90FF));
		colors.add(new NamedColor("light blue", 0xADD8E6));
		colors.add(new NamedColor("sky blue", 0x87CEEB));
		colors.add(new NamedColor("light sky blue", 0x87CEFA));
		colors.add(new NamedColor("midnight blue", 0x191970));
		colors.add(new NamedColor("navy", 0x000080));
		colors.add(new NamedColor("dark blue", 0x00008B));
		colors.add(new NamedColor("medium blue", 0x0000CD));
		colors.add(new NamedColor("blue", 0x0000FF));
		colors.add(new NamedColor("royal blue", 0x4169E1));
		colors.add(new NamedColor("blue violet", 0x8A2BE2));
		colors.add(new NamedColor("indigo", 0x4B0082));
		colors.add(new NamedColor("dark slate blue", 0x483D8B));
		colors.add(new NamedColor("slate blue", 0x6A5ACD));
		colors.add(new NamedColor("medium slate blue", 0x7B68EE));
		colors.add(new NamedColor("medium purple", 0x9370DB));
		colors.add(new NamedColor("dark magenta", 0x8B008B));
		colors.add(new NamedColor("dark violet", 0x9400D3));
		colors.add(new NamedColor("dark orchid", 0x9932CC));
		colors.add(new NamedColor("medium orchid", 0xBA55D3));
		colors.add(new NamedColor("purple", 0x800080));
		colors.add(new NamedColor("thistle", 0xD8BFD8));
		colors.add(new NamedColor("plum", 0xDDA0DD));
		colors.add(new NamedColor("violet", 0xEE82EE));
		colors.add(new NamedColor("magenta / fuchsia", 0xFF00FF));
		colors.add(new NamedColor("orchid", 0xDA70D6));
		colors.add(new NamedColor("medium violet red", 0xC71585));
		colors.add(new NamedColor("pale violet red", 0xDB7093));
		colors.add(new NamedColor("deep pink", 0xFF1493));
		colors.add(new NamedColor("hot pink", 0xFF69B4));
		colors.add(new NamedColor("light pink", 0xFFB6C1));
		colors.add(new NamedColor("pink", 0xFFC0CB));
		colors.add(new NamedColor("antique white", 0xFAEBD7));
		colors.add(new NamedColor("beige", 0xF5F5DC));
		colors.add(new NamedColor("bisque", 0xFFE4C4));
		colors.add(new NamedColor("blanched almond", 0xFFEBCD));
		colors.add(new NamedColor("wheat", 0xF5DEB3));
		colors.add(new NamedColor("corn silk", 0xFFF8DC));
		colors.add(new NamedColor("lemon chiffon", 0xFFFACD));
		colors.add(new NamedColor("light golden rod yellow", 0xFAFAD2));
		colors.add(new NamedColor("light yellow", 0xFFFFE0));
		colors.add(new NamedColor("saddle brown", 0x8B4513));
		colors.add(new NamedColor("sienna", 0xA0522D));
		colors.add(new NamedColor("chocolate", 0xD2691E));
		colors.add(new NamedColor("peru", 0xCD853F));
		colors.add(new NamedColor("sandy brown", 0xF4A460));
		colors.add(new NamedColor("burly wood", 0xDEB887));
		colors.add(new NamedColor("tan", 0xD2B48C));
		colors.add(new NamedColor("rosy brown", 0xBC8F8F));
		colors.add(new NamedColor("moccasin", 0xFFE4B5));
		colors.add(new NamedColor("navajo white", 0xFFDEAD));
		colors.add(new NamedColor("peach puff", 0xFFDAB9));
		colors.add(new NamedColor("misty rose", 0xFFE4E1));
		colors.add(new NamedColor("lavender blush", 0xFFF0F5));
		colors.add(new NamedColor("linen", 0xFAF0E6));
		colors.add(new NamedColor("old lace", 0xFDF5E6));
		colors.add(new NamedColor("papaya whip", 0xFFEFD5));
		colors.add(new NamedColor("sea shell", 0xFFF5EE));
		colors.add(new NamedColor("mint cream", 0xF5FFFA));
		colors.add(new NamedColor("slate gray", 0x708090));
		colors.add(new NamedColor("light slate gray", 0x778899));
		colors.add(new NamedColor("light steel blue", 0xB0C4DE));
		colors.add(new NamedColor("lavender", 0xE6E6FA));
		colors.add(new NamedColor("floral white", 0xFFFAF0));
		colors.add(new NamedColor("alice blue", 0xF0F8FF));
		colors.add(new NamedColor("ghost white", 0xF8F8FF));
		colors.add(new NamedColor("honeydew", 0xF0FFF0));
		colors.add(new NamedColor("ivory", 0xFFFFF0));
		colors.add(new NamedColor("azure", 0xF0FFFF));
		colors.add(new NamedColor("snow", 0xFFFAFA));
		colors.add(new NamedColor("black", 0x000000));
		colors.add(new NamedColor("dim gray / dim grey", 0x696969));
		colors.add(new NamedColor("gray / grey", 0x808080));
		colors.add(new NamedColor("dark gray / dark grey", 0xA9A9A9));
		colors.add(new NamedColor("silver", 0xC0C0C0));
		colors.add(new NamedColor("light gray / light grey", 0xD3D3D3));
		colors.add(new NamedColor("gainsboro", 0xDCDCDC));
		colors.add(new NamedColor("white smoke", 0xF5F5F5));
		colors.add(new NamedColor("white", 0xFFFFFF));
	}

	public float red  (long arg0) {	return (arg0 >> 16) & 0xFF;	}
	public float green(long arg0) {	return (arg0 >>  8) & 0xFF;	}
	public float blue (long arg0) {	return (arg0      ) & 0xFF;	}

	/**
	 * finds the name of the nearest known color.
	 * 
	 * @param target the color to match
	 * @returns the name of the nearest color.
	 */
	public String find(float r0,float g0,float b0) {
		NamedColor best = null;
		float bestDistance = Long.MAX_VALUE;

		Iterator<NamedColor> i = colors.iterator();
		while (i.hasNext()) {
			NamedColor c = i.next();
			float r1 = red(c.value) - r0;
			float g1 = green(c.value) - g0;
			float b1 = blue(c.value) - b0;
			float dSquared = r1 * r1 + g1 * g1 + b1 * b1;
			if (bestDistance > dSquared) {
				bestDistance = dSquared;
				best = c;
			}
		}
		if (best != null) {
			return best.name;
		}
		return null;
	}
	
	
	/**
	 * finds the name of the nearest known color.
	 * 
	 * @param target the hex RGB to match
	 * @returns the name of the nearest color.
	 */
	public String find(long target) {
		float r0 = red(target);
		float g0 = green(target);
		float b0 = blue(target);

		return find(r0,g0,b0);
	}
	
	
	/**
	 * finds the name of the nearest known color.
	 * 
	 * @param target the Color to match
	 * @returns the name of the nearest color.
	 */
	public String find(Color target) {
		float r0 = target.getRed();
		float g0 = target.getGreen();
		float b0 = target.getBlue();

		return find(r0,g0,b0);
	}
	
	
	/**
	 * finds the name of the nearest known color.
	 * 
	 * @param target the Color to match
	 * @returns the name of the nearest color.
	 */
	public String find(ColorRGB target) {
		float r0 = target.red;
		float g0 = target.green;
		float b0 = target.blue;

		return find(r0,g0,b0);
	}
};
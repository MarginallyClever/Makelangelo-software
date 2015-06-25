package com.marginallyclever.basictypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Color palette for quantization
 * @author danroyer
 * @since 7.1.4-SNAPSHOT
 */
public class ColorPalette {
	
	/**
	 * List of colors in the form of red, green, and blue data values.
	 * 
	 * @see C3
	 */
	private List<C3> colors;
	
	public ColorPalette() {
		colors = new ArrayList<C3>();
		//addColor(new C3(0,0,0));
		//addColor(new C3(255,255,255));
	}
	
	public void addColor(C3 c) {
		colors.add(c);
	}
	
	/**
	 * 
	 * Removes a given color if it exists in {@link ColorPalette#colors}.
	 * 
	 * @param c color to remove.
	 * 
	 * @see <a href="http://stackoverflow.com/a/223929">Iterating through a list, avoiding ConcurrentModificationException when removing in loop</a>
	 */
	public void removeColor(C3 c) {
		for(final Iterator<C3> colorsIterator = colors.iterator(); colorsIterator.hasNext();) {
			final C3 nextColor = colorsIterator.next();
			if(nextColor.equals(c)) {
				colorsIterator.remove();
			}
		}
	}
	
	
	public int numColors() {
		return colors.size();
	}
	
	
	public C3 getColor(int index) {
		return colors.get(index);
	}
	

	public C3 quantize(C3 c) {
		int i = quantizeIndex(c);

	    return this.getColor(i);
	}
	
	
	public int quantizeIndex(C3 c) {
		Iterator<C3> i = colors.iterator();
		assert(i.hasNext());
		
		C3 n, nearest = i.next();
		int index=0;
		int nearest_index=0;
		
		while(i.hasNext()) {
			n = i.next();
			++index;
			if (n.diff(c) < nearest.diff(c)) {
				nearest = n;
				nearest_index = index;
			}
		}

	    return nearest_index;
	}
}

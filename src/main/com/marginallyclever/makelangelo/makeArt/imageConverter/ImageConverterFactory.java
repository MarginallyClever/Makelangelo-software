package com.marginallyclever.makelangelo.makeArt.imageConverter;

public class ImageConverterFactory {
	// storing this list here so it's easier to find.
	public static ImageConverter [] converters = {
		new Converter_Boxes(),
		new Converter_CMYK(),
		new Converter_Crosshatch(),
		new Converter_Moire(),
		new Converter_Multipass(),
		new Converter_Pulse(),
		new Converter_RandomLines(),
		new Converter_Sandy(),
		new Converter_Spiral(),
		new Converter_Spiral_CMYK(),
		new Converter_SpiralPulse(),
		new Converter_VoronoiStippling(),
		new Converter_VoronoiZigZag(),
		new Converter_Wander()
	};

}

package com.marginallyclever.makelangelo.makeart.imageconverter;

public class ImageConverterFactory {
	// storing this list here so that it's easier to find.
	private static ImageConverter [] list;

	/**
	 * List creation is deferred until the first call to getList() so that Translator has time to initialize.
	 * @return the list of all available image converters.
	 */
	public static ImageConverter [] getList() {
		if(list==null) {
			list = new ImageConverter [] {
					new Converter_Boxxy(),
					new Converter_CMYK_Crosshatch(),
					new Converter_CMYK_Spiral(),
					new Converter_CMYK_Circles(),
					new Converter_Crosshatch(),
					new Converter_EdgeDetection(),
					new Converter_FlowField(),
					new Converter_Hexxy(),
					new Converter_IntensityToHeight(),
					new Converter_Moire(),
					new Converter_Multipass(),
					new Converter_Pulse(),
					new Converter_PulseCMYK(),
					new Converter_QuadTreeInstant(),
					new Converter_RandomLines(),
					new Converter_Sandy(),
					new Converter_Spiral(),
					new Converter_SpiralPulse(),
					new Converter_TruchetFromImage(),
					new Converter_VoronoiStippling(),
					new Converter_VoronoiZigZag(),
					new Converter_Wander(),
			};
		}
		return list;
	}
}

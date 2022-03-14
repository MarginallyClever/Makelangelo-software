package com.marginallyclever.makelangelo.makeArt.imageConverter;

public class ImageConverterFactory {
	// storing this list here so it's easier to find.
	public static final ImageConverterPanel [] list = {
		new Converter_Boxxy_Panel(new Converter_Boxxy()),
		new Converter_CMYK_Panel(new Converter_CMYK()),
		new Converter_Crosshatch_Panel(new Converter_Crosshatch()),
		new Converter_Moire_Panel(new Converter_Moire()),
		new Converter_Multipass_Panel(new Converter_Multipass()),
		new Converter_Pulse_Panel(new Converter_Pulse()),
		new Converter_RandomLines_Panel(new Converter_RandomLines()),
		new Converter_Sandy_Panel(new Converter_Sandy()),
		new Converter_Spiral_Panel(new Converter_Spiral()),
		new Converter_Spiral_CMYK_Panel(new Converter_Spiral_CMYK()),
		new Converter_SpiralPulse_Panel(new Converter_SpiralPulse()),
		new Converter_VoronoiStippling_Panel(new Converter_VoronoiStippling()),
		new Converter_VoronoiZigZag_Panel(new Converter_VoronoiZigZag()),
		new Converter_Wander_Panel(new Converter_Wander()),
	};
}

module com.marginallyclever.makelangelo {
	requires java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires jrpicam;
	requires jogamp.fat;
	requires kabeja;
	requires org.slf4j;
	requires jssc;
	requires vecmath;
	requires batik.all;
	requires xml.apis.ext;
	requires junit;
	
	// See also src/resources/META-INF/services/*
	provides com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties with 
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.CartesianProperties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo2Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo3_3Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo3Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo5Properties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.Makelangelo5MarlinProperties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloCustomProperties,
		com.marginallyclever.makelangeloRobot.settings.hardwareProperties.ZarplotterProperties;
	uses com.marginallyclever.makelangeloRobot.settings.hardwareProperties.MakelangeloHardwareProperties;

	uses com.marginallyclever.makelangelo.makeArt.io.LoadResource;
	provides com.marginallyclever.makelangelo.makeArt.io.LoadResource with
		com.marginallyclever.makelangelo.makeArt.io.dxf.LoadDXF,
		com.marginallyclever.makelangelo.makeArt.io.gcode.LoadGCode,
		com.marginallyclever.makelangelo.makeArt.io.image.LoadImage,
		com.marginallyclever.makelangelo.makeArt.io.scratch.LoadScratch2,
		com.marginallyclever.makelangelo.makeArt.io.scratch.LoadScratch3,
		com.marginallyclever.makelangelo.makeArt.io.svg.LoadSVG;
	
	uses com.marginallyclever.makelangelo.makeArt.io.SaveResource;
	provides com.marginallyclever.makelangelo.makeArt.io.SaveResource with
		com.marginallyclever.makelangelo.makeArt.io.dxf.SaveDXF,
		com.marginallyclever.makelangelo.makeArt.io.gcode.SaveGCode,
		com.marginallyclever.makelangelo.makeArt.io.svg.SaveSVG;

	provides com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter with
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Boxes,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_CMYK,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Crosshatch,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Moire,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Multipass,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Pulse,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_RandomLines,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Sandy,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Spiral,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Spiral_CMYK,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_SpiralPulse,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_VoronoiStippling,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_VoronoiZigZag,
		com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Wander;
	uses com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;

	provides com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator with
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Border,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Dragon,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_FibonacciSpiral,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_FillPage,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_GosperCurve,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_GraphPaper,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_HilbertCurve,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_KochCurve,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Lissajous,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_LSystemTree,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Maze,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Package,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Polyeder,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_SierpinskiTriangle,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Spirograph,
		com.marginallyclever.makelangelo.makeArt.turtleGenerator.Generator_Text;
	uses com.marginallyclever.makelangelo.makeArt.turtleGenerator.TurtleGenerator;

	provides com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle with
		com.marginallyclever.makelangeloRobot.machineStyles.CoreXY,
		com.marginallyclever.makelangeloRobot.machineStyles.Cartesian,
		com.marginallyclever.makelangeloRobot.machineStyles.Marlin,
		com.marginallyclever.makelangeloRobot.machineStyles.Polargraph,
		com.marginallyclever.makelangeloRobot.machineStyles.Zarplotter;
	uses com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;
}
module com.marginallyclever.makelangelo {
	requires transitive java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires org.slf4j;
	requires jssc;
	requires logback.core;
	requires jrpicam;
	requires kabeja;
	requires vecmath;
	requires batik.all;
	requires xml.apis.ext;
	requires transitive jogamp.fat;
	
	exports com.marginallyclever.makelangelo;
	exports com.marginallyclever.makelangelo.turtle;
	exports com.marginallyclever.makelangelo.plotter;
	exports com.marginallyclever.makelangelo.plotter.settings;
	exports com.marginallyclever.makelangelo.paper;
	exports com.marginallyclever.convenience;
	exports com.marginallyclever.convenience.log;

	opens com.marginallyclever.makelangelo.plotter.settings;
}
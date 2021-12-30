module com.marginallyclever.makelangelo {
	requires transitive java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires org.slf4j;
	requires logback.core;

	requires transitive jrpicam;
	requires transitive jogamp.fat;
	requires transitive kabeja;
	requires transitive jssc;
	requires transitive vecmath;
	requires transitive batik.all;
	requires transitive xml.apis.ext;
	
	exports com.marginallyclever.makelangelo;
}
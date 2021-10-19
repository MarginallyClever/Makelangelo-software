module com.marginallyclever.makelangelo {
	requires java.desktop;
	requires java.prefs;
	requires java.logging;
	requires org.apache.commons.io;
	requires org.json;
	requires org.jetbrains.annotations;
	requires org.slf4j;
	
	requires junit;
	requires jrpicam;
	requires jogamp.fat;
	requires kabeja;
	requires jssc;
	requires vecmath;
	requires batik.all;
	requires xml.apis.ext;
	
	opens com.marginallyclever.makelangelo;
}
module com.marginallyClever.makelangelo {
    requires jssc;
    requires org.slf4j;
    requires java.desktop;
    requires jogamp.fat;
    requires logback.core;
    requires org.apache.commons.io;
    requires jrpicam;
    requires java.prefs;
    requires kabeja;
    requires org.json;
    requires vecmath;
    requires batik.all;
    requires xml.apis.ext;
    requires org.jetbrains.annotations;

    exports com.marginallyClever.makelangelo.turtle;
    exports com.marginallyClever.convenience;
    exports com.marginallyClever.makelangelo.makeArt.io.vector;
}
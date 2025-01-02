module com.marginallyclever.makelangelo {
    requires org.slf4j;
    requires jssc;
    requires java.desktop;
    requires jogl.all;
    requires ch.qos.logback.core;
    requires org.json;
    requires org.apache.commons.io;
    requires org.apache.commons.compress;
    requires java.prefs;
    requires kabeja;
    requires batik.all;
    requires xml.apis.ext;
    requires vecmath;
    requires jrpicam;
    requires org.jetbrains.annotations;
    requires com.formdev.flatlaf;
    requires org.reflections;

    requires org.locationtech.jts;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    opens com.marginallyclever.convenience;
    opens com.marginallyclever.convenience.voronoi;
    opens com.marginallyclever.makelangelo.makeart.io;
    opens com.marginallyclever.makelangelo.plotter.plottercontrols;
    opens com.marginallyclever.makelangelo.turtle;

    exports com.marginallyclever.communications;
    exports com.marginallyclever.convenience.log to ch.qos.logback.core;
    exports com.marginallyclever.makelangelo;
    exports com.marginallyclever.makelangelo.makeart;
    exports com.marginallyclever.makelangelo.makeart.imagefilter;
    exports com.marginallyclever.makelangelo.makeart.turtletool;
    exports com.marginallyclever.makelangelo.paper;
    exports com.marginallyclever.makelangelo.plotter.plottersettings;
    exports com.marginallyclever.makelangelo.turtle;

    opens com.marginallyclever.convenience.noise;
    opens com.marginallyclever.convenience.helpers;
}
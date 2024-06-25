module com.marginallyclever.makelangelo {
    requires com.marginallyclever.nodegraphcore;
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
    requires org.locationtech.jts;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires modern_docking.api;
    requires modern_docking.single_app;
    requires modern_docking.ui_ext;
    requires com.formdev.flatlaf;
    requires com.github.weisj.jsvg;
    requires org.reflections;
    requires org.apache.commons.lang3;
    requires org.joml;

    opens com.marginallyclever.convenience;
    opens com.marginallyclever.convenience.voronoi;
    opens com.marginallyclever.makelangelo.makeart.io;
    opens com.marginallyclever.makelangelo.apps.plottercontrols;
    opens com.marginallyclever.makelangelo.turtle;

    exports com.marginallyclever.communications;
    exports com.marginallyclever.convenience.log to ch.qos.logback.core;
    exports com.marginallyclever.makelangelo;
    exports com.marginallyclever.makelangelo.makeart;
    exports com.marginallyclever.makelangelo.makeart.imagefilter;
    exports com.marginallyclever.makelangelo.makeart.tools;
    exports com.marginallyclever.makelangelo.paper;
    exports com.marginallyclever.makelangelo.plotter.plottersettings;
    exports com.marginallyclever.makelangelo.turtle;

    exports com.marginallyclever.makelangelo.donatelloimpl to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes to com.marginallyclever.nodegraphcore;
    opens com.marginallyclever.convenience.noise;
    opens com.marginallyclever.convenience.helpers;
    exports com.marginallyclever.makelangelo.apps;
    exports com.marginallyclever.makelangelo.texture;
    exports com.marginallyclever.makelangelo.rangeslider;

    provides com.marginallyclever.nodegraphcore.NodeRegistry with
            com.marginallyclever.makelangelo.donatelloimpl.DonatelloRegistry;

    provides com.marginallyclever.nodegraphcore.DAORegistry with
            com.marginallyclever.makelangelo.donatelloimpl.DonatelloRegistry;
}
import com.marginallyclever.makelangelo.donatelloimpl.MakelangeloRegistry;

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
    requires modern_docking.api;
    requires modern_docking.single_app;
    requires modern_docking.ui_ext;
    requires com.github.weisj.jsvg;
    requires com.marginallyclever.donatello;
    requires com.marginallyclever.nodegraphcore;

    exports com.marginallyclever.communications;
    exports com.marginallyclever.makelangelo.donatelloimpl to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.lines to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.points to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.fractals to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.ports to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.convenience.log to ch.qos.logback.core;
    exports com.marginallyclever.makelangelo.makeart;
    exports com.marginallyclever.makelangelo.makeart.imagefilter;
    exports com.marginallyclever.makelangelo.makeart.turtletool;
    exports com.marginallyclever.makelangelo.paper;
    exports com.marginallyclever.makelangelo.turtle;

    opens com.marginallyclever.convenience;
    opens com.marginallyclever.makelangelo.turtle;
    opens com.marginallyclever.makelangelo;
    opens com.marginallyclever.makelangelo.makeart.io;
    opens com.marginallyclever.makelangelo.texture;
    opens com.marginallyclever.convenience.noise;
    opens com.marginallyclever.convenience.helpers;
    opens com.marginallyclever.convenience.log to ch.qos.logback.core;
    opens com.marginallyclever.makelangelo.preview;
    opens com.marginallyclever.convenience.linecollection;

    // A Java module that wants to implement a service interface from a service interface module must:
    // - Require the service interface module in its own module descriptor.
    // - Implement the service interface with a Java class.
    // - Declare the service interface implementation in its module descriptor.
    // In order to use the service, the client module must declare in its module descriptor that it uses the service.
    // http://tutorials.jenkov.com/java/modules.html
    uses com.marginallyclever.nodegraphcore.NodeRegistry;
    provides com.marginallyclever.nodegraphcore.NodeRegistry with
            MakelangeloRegistry;

    uses com.marginallyclever.nodegraphcore.DAORegistry;
    provides com.marginallyclever.nodegraphcore.DAORegistry with
            MakelangeloRegistry;
}
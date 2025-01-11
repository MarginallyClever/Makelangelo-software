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

    exports com.marginallyclever.makelangelo.donatelloimpl to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes to com.marginallyclever.nodegraphcore;
    exports com.marginallyclever.makelangelo.donatelloimpl.nodes.shapes to com.marginallyclever.nodegraphcore;

    exports com.marginallyclever.convenience.log to ch.qos.logback.core;

    provides com.marginallyclever.nodegraphcore.NodeRegistry with
            com.marginallyclever.makelangelo.donatelloimpl.DonatelloRegistry;

    provides com.marginallyclever.nodegraphcore.DAORegistry with
            com.marginallyclever.makelangelo.donatelloimpl.DonatelloRegistry;

    opens com.marginallyclever.convenience;
    opens com.marginallyclever.makelangelo.turtle;
    opens com.marginallyclever.makelangelo;
    opens com.marginallyclever.makelangelo.makeart.io;
}
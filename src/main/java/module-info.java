module com.marginallyclever.makelangelo {
    requires com.marginallyclever.nodegraphcore;
    requires org.slf4j;
    requires jssc;
    requires java.desktop;
    requires jogamp.fat;
    requires logback.core;
    requires org.json;
    requires org.apache.commons.io;
    requires java.prefs;
    requires kabeja;
    requires batik.all;
    requires xml.apis.ext;
    requires vecmath;
    requires jrpicam;
    requires org.jetbrains.annotations;

    opens com.marginallyclever.convenience;
    opens com.marginallyclever.makelangelo.makeart.io.vector;
    opens com.marginallyclever.makelangelo.plotter.plottercontrols;
    opens com.marginallyclever.makelangelo.turtle;

    uses com.marginallyclever.nodegraphcore.NodeRegistry;
    provides com.marginallyclever.nodegraphcore.NodeRegistry with
            com.marginallyclever.donatello.DonatelloRegistry;

    uses com.marginallyclever.nodegraphcore.DAORegistry;
    provides com.marginallyclever.nodegraphcore.DAORegistry with
            com.marginallyclever.donatello.DonatelloRegistry;
}
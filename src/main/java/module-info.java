module com.marginallyclever.makelangelo {
    requires com.marginallyClever.nodeGraphCore;
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

    uses com.marginallyClever.nodeGraphCore.NodeRegistry;
    provides com.marginallyClever.nodeGraphCore.NodeRegistry with
            com.marginallyclever.donatello.DonatelloRegistry;

    uses com.marginallyClever.nodeGraphCore.DAORegistry;
    provides com.marginallyClever.nodeGraphCore.DAORegistry with
            com.marginallyclever.donatello.DonatelloRegistry;
}
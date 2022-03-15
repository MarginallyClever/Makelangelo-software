module com.marginallyClever.makelangelo {
    requires com.marginallyClever.NodeGraphCore;
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

    opens com.marginallyClever.convenience;
    opens com.marginallyClever.makelangelo.makeArt.io.vector;
    opens com.marginallyClever.makelangelo.plotter.plotterControls;
    opens com.marginallyClever.makelangelo.turtle;


    uses com.marginallyClever.nodeGraphCore.NodeRegistry;
    provides com.marginallyClever.nodeGraphCore.NodeRegistry with
            com.marginallyClever.donatello.DonatelloRegistry;

    uses com.marginallyClever.nodeGraphCore.DAORegistry;
    provides com.marginallyClever.nodeGraphCore.DAORegistry with
            com.marginallyClever.donatello.DonatelloRegistry;
}
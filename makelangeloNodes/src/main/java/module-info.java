module com.marginallyClever.donatelloNodes {
    requires com.marginallyClever.NodeGraphCore;
    requires com.marginallyClever.makelangelo;
    requires java.desktop;
    requires org.json;

    uses com.marginallyClever.nodeGraphCore.NodeRegistry;
    provides com.marginallyClever.nodeGraphCore.NodeRegistry with
            com.marginallyClever.donatelloNodes.MakelangeloNodeRegistry;

    uses com.marginallyClever.nodeGraphCore.DAORegistry;
    provides com.marginallyClever.nodeGraphCore.DAORegistry with
            com.marginallyClever.donatelloNodes.MakelangeloNodeRegistry;
}
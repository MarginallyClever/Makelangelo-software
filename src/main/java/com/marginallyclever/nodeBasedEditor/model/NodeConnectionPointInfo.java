package com.marginallyclever.nodeBasedEditor.model;

/**
 * Utility class
 */
public class NodeConnectionPointInfo {
    public Node node;

    public int nodeVariableIndex;

    /**
     * NodeGraphModel.IN or NodeGraphModel.OUT
     */
    public int flags;

    public NodeConnectionPointInfo() {}

    public NodeConnectionPointInfo(Node node,int nodeVariableIndex,int flags) {
        this.node=node;
        this.nodeVariableIndex=nodeVariableIndex;
        this.flags=flags;
    }

    public NodeVariable<?> getVariable() {
        return node.getVariable(nodeVariableIndex);
    }
}

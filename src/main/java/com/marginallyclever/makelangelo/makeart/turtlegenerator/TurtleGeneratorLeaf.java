package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a factory for TurtleGenerator objects.
 * It provides a static list of available generators, organized in a tree structure.
 * Each generator can be accessed by its name or through the tree structure.
 */
public class TurtleGeneratorLeaf {
    private final String name;
    public final List<TurtleGeneratorLeaf> children = new ArrayList<>();
    public TurtleGenerator generator;

    public String getName() {
        return name;
    }

    public TurtleGeneratorLeaf(String name, TurtleGeneratorLeaf[] kids) {
        this.name = name;
        children.addAll(List.of(kids));
    }

    public TurtleGeneratorLeaf(TurtleGenerator gen) {
        this.name = gen.getName();
        this.generator = gen;
    }

    public TurtleGenerator getGenerator() {
        return generator;
    }

    public List<TurtleGeneratorLeaf> getChildren() {
        return children;
    }
}

package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.OutputInt;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import com.marginallyclever.util.PreferencesHelper;

import java.awt.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import static com.marginallyclever.makelangelo.paper.Paper.*;

/**
 * A node that creates a canvas with a given size and color.
 */
public class CanvasFromPreview extends Node implements PrintWithGraphics {
    private Integer width = 1;
    private Integer height = 1;
    private Color color = Color.WHITE;

    private double paperLeft;
    private double paperRight;
    private double paperBottom;
    private double paperTop;

    private final InputInt layer = new InputInt("layer", 0);
    private final OutputInt outx = new OutputInt("x", 0);
    private final OutputInt outy = new OutputInt("y", 0);
    private final OutputInt outw = new OutputInt("width out", width);
    private final OutputInt outh = new OutputInt("height out", height);

    private static final Preferences paperPreferenceNode
            = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.PAPER);

    private void readConfig() {
        paperLeft = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_LEFT, Double.toString(paperLeft)));
        paperRight = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_RIGHT, Double.toString(paperRight)));
        paperTop = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_TOP, Double.toString(paperTop)));
        paperBottom = Double.parseDouble(paperPreferenceNode.get(PREF_KEY_PAPER_BOTTOM, Double.toString(paperBottom)));

        int colorFromPref = Integer.parseInt(paperPreferenceNode.get(PREF_KEY_PAPER_COLOR, Integer.toString(color.hashCode())));
        color = new Color(colorFromPref);

        width = (int) (-paperLeft + paperRight);
        height = (int) (paperTop - paperBottom);
    }

    public CanvasFromPreview() {
        super("CanvasFromPreview");

        readConfig();

        paperPreferenceNode.addPreferenceChangeListener(evt -> readConfig());

        addPort(outx);
        addPort(outy);
        addPort(outw);
        addPort(outh);
        addPort(layer);
    }

    @Override
    public void update() {
        readConfig();

        var w = Math.max(1, width);
        var h = Math.max(1, height);
        outx.setValue(-w / 2);
        outy.setValue(-h / 2);
        outw.setValue(w);
        outh.setValue(h);
    }


    @Override
    public void print(Graphics g) {
        var x = outx.getValue();
        var y = outy.getValue();
        var w = Math.max(1, width);
        var h = Math.max(1, height);
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }
}

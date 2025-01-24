package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.makeart.truchet.TruchetTile;
import com.marginallyclever.makelangelo.makeart.truchet.TruchetTileFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Create a basic Truchet tile pattern from an image.  the intensity of the image decides the tile type.
 */
public class TruchetTiles extends Node {
    Input<BufferedImage> source = new Input<>("Source", BufferedImage.class, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    Input<Number> spaceBetweenLines = new Input<>("Spacing", Number.class, 10);
    Input<Number> linesPerTileCount = new Input<>("Qty", Number.class, 10);
    Output<Turtle> output = new Output<>("Output", Turtle.class, new Turtle());

    public TruchetTiles() {
        super("TruchetTiles");
        addVariable(source);
        addVariable(spaceBetweenLines);
        addVariable(linesPerTileCount);
        addVariable(output);
    }

    @Override
    public void update() {
        var img = source.getValue();
        int space = Math.max(1,spaceBetweenLines.getValue().intValue());
        int lines = Math.max(1,linesPerTileCount.getValue().intValue());
        int tileSize = space * lines;

        try {
            var c = img.getColorModel().getNumComponents();
            var raster = img.getRaster();
            int []pixels = new int[c];

            Turtle turtle = new Turtle();
            List<TruchetTile> ttgList = new ArrayList<>();

            for(int y=0;y<img.getHeight();y += tileSize) {
                for(int x=0;x<img.getWidth();x += tileSize) {
                    raster.getPixel(x,y,pixels);
                    double avg = (pixels[0]+pixels[1]+pixels[2])/3.0;
                    ttgList.add(TruchetTileFactory.getTile(avg>128?0:1,turtle,space,lines));
                }
            }

            if(!ttgList.isEmpty()) {
                var i = ttgList.iterator();
                for(int y=0;y<img.getHeight();y += tileSize) {
                    for(int x=0;x<img.getWidth();x += tileSize) {
                        i.next().drawTile(x,y);
                    }
                }
            }

            output.send(turtle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

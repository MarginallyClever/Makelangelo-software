package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.truchet.TruchetTile;
import com.marginallyclever.makelangelo.makeart.truchet.TruchetTileFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a basic Truchet tile pattern from an image.  the intensity of the image decides the tile type.
 */
public class TruchetTiles extends Node {
    private final InputImage source = new InputImage("Source");
    private final InputDouble spaceBetweenLines = new InputDouble("Spacing", 10d);
    private final InputInt linesPerTileCount = new InputInt("Qty", 10);
    private final OutputTurtle output = new OutputTurtle("Output");

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
        var space = Math.max(1,spaceBetweenLines.getValue());
        var lines = Math.max(1,linesPerTileCount.getValue());
        double tileSize = space * lines;

        try {
            var c = img.getColorModel().getNumComponents();
            var raster = img.getRaster();
            int []pixels = new int[c];

            Turtle turtle = new Turtle();
            List<TruchetTile> ttgList = new ArrayList<>();

            for (double y = 0; y < img.getHeight(); y += tileSize) {
                for (double x = 0; x < img.getWidth(); x += tileSize) {
                    raster.getPixel((int) x, (int) y, pixels);
                    double avg = (pixels[0] + pixels[1] + pixels[2]) / 3.0;
                    ttgList.add(TruchetTileFactory.getTile(avg > 128 ? 0 : 1, turtle, space, lines));
                }
            }

            if(!ttgList.isEmpty()) {
                var i = ttgList.iterator();
                for(double y=0;y<img.getHeight();y += tileSize) {
                    for(double x=0;x<img.getWidth();x += tileSize) {
                        i.next().drawTile((int)x,(int)y);
                    }
                }
            }

            output.send(turtle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

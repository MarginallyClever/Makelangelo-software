package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes.ParallelLinesTest;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class LineWeightByImageTest {

    /**
     * Use ParallelLines to generate lines 16mm apart on a 256x256 image.
     * Use LineWeightByImage with
     * - a solid black source image
     * - a pen diameter of 2mm.
     * - a max width of 16mm.
     * The resulting lines should all be 2mm apart with no overlap.
     */
    @Test
    public void evenlySpacedNoOverlap() {
        Turtle sourceLines = ParallelLinesTest.createParallelLines();
        var sourceImage = new BufferedImage(256,256, BufferedImage.TYPE_INT_RGB);
        var lineWeightByImage = new LineWeightByImage();
        lineWeightByImage.getPort("turtle").setValue(sourceLines);
        lineWeightByImage.getPort("image").setValue(sourceImage);
        lineWeightByImage.getPort("stepSize").setValue(2d);
        lineWeightByImage.getPort("thickness").setValue(16d);
        lineWeightByImage.getPort("pen diameter").setValue(2d);
        lineWeightByImage.update();
        Turtle result = (Turtle) lineWeightByImage.getPort("result").getValue();

        /*
        // Save the result to a file for visual inspection
        var saver = new SaveTurtle();
        saver.getPort("turtle").setValue(result);
        saver.getPort("filename").setValue("lineWeightByImage.svg");
        saver.update();
        //*/

        // there should be 16 separate groups of lines
        var lines = result.getAsLineCollection();
        var groups = lines.splitByTravel();
        Assertions.assertEquals(16, groups.size());

        // the bounds of any two adjacent groups should be 2mm apart.
        for (int i = 0; i < groups.size() - 1; i++) {
            var groupA = groups.get(i);
            var groupB = groups.get(i + 1);
            Assertions.assertEquals(2, getBounds(groupB).getY() - getBounds(groupA).getMaxY(), 0.1,"i="+i);
        }
    }

    private Rectangle2D getBounds(LineCollection group) {
        if (group.isEmpty()) {
            return new Rectangle2D.Double();
        }
        Rectangle2D bounds=null;
        var first = true;
        for( var line : group) {
            if (first) {
                bounds = new Rectangle2D.Double(
                        line.start.x,
                        line.start.y,
                        line.end.x - line.start.x,
                        line.end.y - line.start.y);
                first = false;
            } else {
                bounds.add(line.start.x,line.start.y);
                bounds.add(line.end.x,line.end.y);
            }
        }
        return bounds;
    }
}

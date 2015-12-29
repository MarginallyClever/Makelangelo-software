package com.marginallyclever.filters;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;


/**
 * Resize and flip horizontally if needed.
 *
 * @author Dan
 */
public class Filter_Resize extends ImageFilter {
  protected int maxWidth, maxHeight;


  public Filter_Resize(Makelangelo gui, MakelangeloRobotSettings mc,
                       Translator ms) {
    super(gui, mc, ms);
    maxWidth = 1000;
    maxHeight = 1000;
  }

  public Filter_Resize(Makelangelo gui, MakelangeloRobotSettings mc,
                       Translator ms, int max_width, int max_height) {
    super(gui, mc, ms);
    maxWidth = max_width;
    maxHeight = max_height;
    //maxWidth=1000;
    //maxHeight=1000;
  }


  protected BufferedImage scaleImage(BufferedImage img, int width, int height) {
    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = newImage.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setBackground(Color.WHITE);
      g.clearRect(0, 0, width, height);
      if (machine.isReverseForGlass()) {
        g.drawImage(img, width, 0, 0, height, 0, 0, img.getWidth(), img.getHeight(), null);
      } else {
        g.drawImage(img, 0, 0, width, height, null);
      }
    } finally {
      g.dispose();
    }
    return newImage;
  }


  public BufferedImage filter(BufferedImage img) {
    int w = img.getWidth();
    int h = img.getHeight();

    // cap the max_w and max_h so that enormous drawbot images don't break the software.
    double paper_w = machine.getPaperWidth();
    double paper_h = machine.getPaperHeight();

    int max_w = maxWidth;
    int max_h = maxHeight;
    if (paper_w > paper_h) {
      max_h *= paper_h / paper_w;
    } else {
      max_w *= paper_w / paper_h;
    }

    // adjust up
    if (w < max_w && h < max_h) {
      if (w > h) {
        h *= (float) max_w / (float) w;
        w = max_w;
      } else {
        w *= (float) max_h / (float) h;
        h = max_h;
      }
    }
    // adjust down
    if (w > max_w) {
      h *= (float) max_w / (float) w;
      w = max_w;
    }
    if (h > max_h) {
      w *= (float) max_h / (float) h;
      h = max_h;
    }
    // now scale the image
    return scaleImage(img, w, h);
  }
}

/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */

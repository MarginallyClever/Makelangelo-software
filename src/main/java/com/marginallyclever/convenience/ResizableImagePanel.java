package com.marginallyclever.convenience;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Displays an {@link BufferedImage} in a {@link JPanel} and allows the user to zoom and pan.
 * @author Dan Royer
 * @since 7.39.9
 */
public class ResizableImagePanel extends JPanel {
    private final BufferedImage image;
    private final Point imagePosition = new Point(0, 0);
    private double zoomFactor = 1.0;
    private Point lastDragPoint;

    public ResizableImagePanel(BufferedImage image) {
        this.image = image;

        // Enable mouse wheel events for zooming
        addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            zoomFactor += rotation * 0.1; // Adjust the zoom speed here if needed
            zoomFactor = Math.max(0.1, zoomFactor); // Ensure zoomFactor doesn't go below 0.1
            repaint();
        });

        // Enable mouse events for dragging the image
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastDragPoint = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastDragPoint.x;
                int dy = e.getY() - lastDragPoint.y;

                // Adjust the translation based on the zoom factor
                dx /= zoomFactor;
                dy /= zoomFactor;

                // Update the last drag point
                lastDragPoint = e.getPoint();

                // Update the image position
                imagePosition.translate(dx, dy);

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            // Calculate the aspect ratio of the image
            double imageAspect = (double) image.getWidth() / image.getHeight();

            // Get the size of the panel
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Calculate the scaled dimensions based on the zoom factor
            int drawWidth = (int) (panelWidth * zoomFactor);
            int drawHeight = (int) (panelHeight * zoomFactor);

            // Adjust the drawing size to maintain the aspect ratio
            if (panelWidth > panelHeight) {
                drawHeight = (int) (drawWidth / imageAspect);
            } else {
                drawWidth = (int) (drawHeight * imageAspect);
            }

            // Calculate the position to center the image
            int x = (panelWidth - drawWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            // Draw the scaled image on the panel
            g.drawImage(image, x + imagePosition.x, y + imagePosition.y, drawWidth, drawHeight, this);
        }
    }

    /**
     * Displays an image in a resizable window.
     *
     * @param image The image to display.
     * @param title The title of the window.
     */
    public static void showImage(BufferedImage image,String title) throws InvalidParameterException {
        if (image == null) throw new InvalidParameterException("image cannot be null.");

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        ResizableImagePanel imagePanel = new ResizableImagePanel(image);
        frame.add(imagePanel);

        frame.setVisible(true);
        imagePanel.revalidate();
        imagePanel.repaint();
    }

    public static void main(String[] args) throws IOException {
        // Replace "path_to_your_image.png" with the actual path to your image file
        BufferedImage image = ImageIO.read(new File("src/test/resources/test.png"));
        showImage(image,"Resizable Image Panel");
    }
}

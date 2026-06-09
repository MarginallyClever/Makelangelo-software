package com.marginallyclever.convenience;


import java.awt.Color;
import java.util.Objects;


/**
 * RGB color class.  each component is 0...255
 *
 * @author Dan Royer
 */
public class ColorRGB {
    public int red;
    public int green;
    public int blue;

    public ColorRGB() {
        this(0, 0, 0);
    }

    public ColorRGB(int red, int green, int blue) {
        super();
        set(red, green, blue);
    }

    public ColorRGB(ColorRGB x) {
        super();
        set(x);
    }

    public ColorRGB(int pixel) {
        super();
        set(pixel);
    }

    public void set(int pixel) {
        this.red   = (pixel >> 16) & 0xff;
        this.green = (pixel >>  8) & 0xff;
        this.blue  = (pixel      ) & 0xff;
    }

    public ColorRGB(Color c) {
        red   = c.getRed();
        green = c.getGreen();
        blue  = c.getBlue();
    }

    public int toInt() {
        return (0xff<<24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
    }

    public void set(ColorRGB x) {
        this.red   = x.red;
        this.green = x.green;
        this.blue  = x.blue;
    }

    public void set(int red, int green, int blue) {
        this.red   = red;
        this.green = green;
        this.blue  = blue;
    }

    public void set(ColorHSB hsb) {
        set(Color.HSBtoRGB(hsb.hue, hsb.saturation, hsb.brightness));
    }

    public ColorRGB sub(ColorRGB x) {
        return new ColorRGB(
                this.red - x.red,
                this.green - x.green,
                this.blue - x.blue);
    }

    public ColorRGB add(ColorRGB x) {
        return new ColorRGB(
                this.red + x.red,
                this.green + x.green,
                this.blue + x.blue);
    }

    public ColorRGB mul(double f) {
        return new ColorRGB(
                (int) (f * this.red),
                (int) (f * this.green),
                (int) (f * this.blue));
    }

    public float diffSquared(ColorRGB other) {
        int r = other.red - this.red;
        int g = other.green - this.green;
        int b = other.blue - this.blue;
        return r * r + g * g + b * b;
    }

    public float diff(ColorRGB other) {
        return (float)Math.sqrt(diffSquared(other));
    }

    public String toString() {
        return "(" + red + "," + green + "," + blue + ")";
    }

    // https://www.codegrepper.com/code-examples/java/rgb+to+hex+java
    public String toHexString() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorRGB colorRGB = (ColorRGB) o;
        return red == colorRGB.red
                && green == colorRGB.green
                && blue == colorRGB.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

}

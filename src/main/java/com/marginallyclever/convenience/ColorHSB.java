package com.marginallyclever.convenience;

import java.awt.*;

/**
 * HSB color class.  Each component is 0...1
 * @author Dan Royer
 * @since 2022-08-31
 */
public class ColorHSB {
    public float hue;
    public float saturation;
    public float brightness;

    public ColorHSB(float hue,float saturation,float brightness) {
        super();
        set(hue,saturation,brightness);
    }

    public void set(float hue,float saturation,float brightness) {
        this.hue = Math.max(0.0f,Math.min(1.0f,hue));
        this.saturation = Math.max(0.0f,Math.min(1.0f,saturation));
        this.brightness = Math.max(0.0f,Math.min(1.0f,brightness));
    }

    public void set(ColorRGB rgb) {
        float[] values = Color.RGBtoHSB(rgb.red,rgb.blue,rgb.green,null);
        set(values[0],values[1],values[2]);
    }

    public ColorHSB sub(ColorHSB x) {
        return new ColorHSB(
                this.hue - x.hue,
                this.saturation - x.saturation,
                this.brightness - x.brightness);
    }

    public ColorHSB add(ColorHSB x) {
        return new ColorHSB(
        this.hue + x.hue,
        this.saturation + x.saturation,
        this.brightness + x.brightness);
    }

    public ColorHSB mul(double f) {
        return new ColorHSB(
                (float)(this.hue * f),
                (float)(this.saturation * f),
                (float)(this.brightness * f));
    }

    public float diffSquared(ColorHSB other) {
        float h = other.hue-this.hue;
        float s = other.saturation-this.saturation;
        float b = other.brightness-this.brightness;
        return h*h + s*s + b*b;
    }

    public float diff(ColorHSB other) {
        return (float)Math.sqrt(diffSquared(other));
    }

    public String toString() {
        return "(" + hue + "," + saturation + "," + brightness + ")";
    }

    public float getHue() { return hue; }
    public float getSaturation() { return saturation; }
    public float getBrightness() { return brightness; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorHSB other = (ColorHSB) o;
        return hue == other.hue
                && saturation == other.saturation
                && brightness == other.brightness;
    }
}

package com.marginallyclever.convenience;

/**
 * A class to represent complex numbers and perform basic operations on them.
 */
public class ComplexNumber {
    public double real;
    public double imag;

    public ComplexNumber(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public ComplexNumber add(ComplexNumber b) {
        return new ComplexNumber(this.real + b.real, this.imag + b.imag);
    }

    public ComplexNumber sub(ComplexNumber b) {
        return new ComplexNumber(this.real - b.real, this.imag - b.imag);
    }

    public ComplexNumber multiply(ComplexNumber b) {
        return new ComplexNumber(this.real * b.real - this.imag * b.imag, this.real * b.imag + this.imag * b.real);
    }

    public ComplexNumber divide(ComplexNumber b) {
        ComplexNumber conjugate = new ComplexNumber(b.real, -b.imag);
        ComplexNumber numerator = this.multiply(conjugate);
        double denominator = b.multiply(conjugate).real;
        return new ComplexNumber(numerator.real / denominator, numerator.imag / denominator);
    }

    public double magnitude() {
        return Math.sqrt(real * real + imag * imag);
    }

    @Override
    public String toString() {
        return "(" + real + ", " + imag + ")";
    }

    public ComplexNumber scale(double k1) {
        return new ComplexNumber(real * k1, imag * k1);
    }

    public ComplexNumber sqrt() {
        double r = Math.sqrt(real * real + imag * imag);
        double x = Math.sqrt((r + real) / 2.0);
        double y = Math.sqrt((r - real) / 2.0);
        if (imag < 0) y = -y;
        return new ComplexNumber(x, y);
    }
}

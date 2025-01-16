package com.marginallyclever.convenience.noise;

public interface Noise {
    /**
     * 1D noise
     * @param xin a double
     * @return a double [0...1]
     */
    double noise(double xin);

    /**
     * 2D noise
     * @param xin a double
     * @param yin a double
     * @return a double [0...1]
     */
    double noise(double xin, double yin);

    /**
     * 3D noise
     * @param xin a double
     * @param yin a double
     * @param zin a double
     * @return a double [0...1]
     */
    double noise(double xin, double yin,double zin);
}

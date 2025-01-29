package com.marginallyclever.convenience.noise;

public interface Noise {
    void setSeed(int seed);

    /**
     * 1D noise
     * @param xin a double
     * @return a double [-1...1]
     */
    double noise(double xin);

    /**
     * 2D noise
     * @param xin a double
     * @param yin a double
     * @return a double [-1...1]
     */
    double noise(double xin, double yin);

    /**
     * 3D noise
     * @param xin a double
     * @param yin a double
     * @param zin a double
     * @return a double [-1...1]
     */
    double noise(double xin, double yin,double zin);
}

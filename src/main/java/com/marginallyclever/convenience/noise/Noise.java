package com.marginallyclever.convenience.noise;

public interface Noise {
    // 1D noise
    double noise(double xin);

    // 2D noise
    double noise(double xin, double yin);

    // 3D noise
    double noise(double xin, double yin,double zin);

    //double noise(double xin, double yin,double zin,double win);
}

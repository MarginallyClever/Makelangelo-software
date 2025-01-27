package com.marginallyclever.convenience.noise;

import java.util.Random;

/**
 * This code is released into the public domain.
 * It is a conversion of Matt Pharr's original C++ implementation.
 * The licensing of the original code follows:
 *
 * <pre>wnoise.cpp
 *
 * Copyright (C) 1998, Matt Pharr <mmp@graphics.stanford.edu>
 *
 * This software is placed in the public domain and is provided as is
 * without express or implied warranty.
 *
 * Basic implementation of Steve Worley's noise function; see proceedings
 * of SIGGRAPH 96.</pre>
 */
public class CellularNoise implements Noise {
    /**
     * The maximum distance that a point can be from another point.
     */
    private final double MAX_DIST = Math.sqrt(3.0)/2.0;
    private final Random random = new Random();

    /**
     * Represents a point in three dimensional space.
     *
     * @author saybur
     *
     */
    private static final class Point {
        private final double x;
        private final double y;
        private final double z;

        /**
         * Creates a point at location (x, y, z).
         *
         * @param x
         *            the x coordinate of the point.
         * @param y
         *            the y coordinate of the point.
         * @param z
         *            the z coordinate of the point.
         */
        private Point(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Provides a fast distance calculation between two points. This is done by
         * not taking the square root of the result.
         *
         * @param other
         *            the coordinate to calculate distance to.
         * @return the distance between this point and the provided point.
         */
        public double distanceSquared(Point other)
        {
            double x2 = x - other.x;
            double y2 = y - other.y;
            double z2 = z - other.z;
            return x2 * x2 + y2 * y2 + z2 * z2;
        }
    }

    @Override
    public void setSeed(int seed) {
        random.setSeed(seed);
    }

    @Override
    public double noise(double xin) {
        return noise(xin,0,0);
    }

    @Override
    public double noise(double xin, double yin) {
        return noise(xin,yin,0);
    }

    /**
     * Gets the noise value at the provided location.
     *
     * @param x
     *            the x coordinate.
     * @param y
     *            the y coordinate.
     * @param z
     *            the z coordinate.
     * @return the noise value at the coordinate.
     */
    public double noise(double x, double y, double z)
    {
        return (minimumDistance(random, new Point(x, y, z)) / MAX_DIST)*2.0-1.0;
    }

    private int floor(double n) {
        return n > 0 ? (int) n : (int) n - 1;
    }

    private double frac(double n) {
        return n >= 0 ? n - (int) (n) : frac(-n);
    }

    /**
     * Checks all voxels near the origin for the closest point to the origin.
     * The returned value will be the distance to the closest point.
     */
    private double minimumDistance(Random r, Point origin) {
        // hack, but easier than handling points that are exactly at negative
        // integer latice-points correctly.
        Point p = new Point(origin.x + 1e-7, origin.y + 1e-7, origin.z + 1e-7);
        // get the coordinate that this point resides at
        int x = floor(p.x);
        int y = floor(p.y);
        int z = floor(p.z);
        // create storage to track lowest values
        double s = Double.MAX_VALUE;
        // first check voxel the point is in
        s = processVoxel(r, p, s, x, y, z);
        // check each of the voxels that share a face with the
        // point's voxel, if they're close enough to possibly
        // make a difference
        // squared distance to the voxel in the +x direction
        double dpx2 = p.x >= 0. ? square(1.0 - frac(p.x)) : square(frac(p.x));
        if(dpx2 < s) {
            s = processVoxel(r, p, s, x + 1, y, z);
        }
        // -x
        double dnx2 = p.x >= 0. ? square(frac(p.x)) : square(1. - frac(p.x));
        if(dnx2 < s) {
            s = processVoxel(r, p, s, x - 1, y, z);
        }
        // +y
        double dpy2 = p.y >= 0. ? square(1. - frac(p.y)) : square(frac(p.y));
        if(dpy2 < s) {
            s = processVoxel(r, p, s, x, y + 1, z);
        }
        // -y
        double dny2 = p.y >= 0. ? square(frac(p.y)) : square(1. - frac(p.y));
        if(dny2 < s) {
            s = processVoxel(r, p, s, x, y - 1, z);
        }
        // +z
        double dpz2 = p.z >= 0. ? square(1. - frac(p.z)) : square(frac(p.z));
        if(dpz2 < s) {
            s = processVoxel(r, p, s, x, y, z + 1);
        }
        // -z
        double dnz2 = p.z >= 0. ? square(frac(p.z)) : square(1. - frac(p.z));
        if(dnz2 < s) {
            s = processVoxel(r, p, s, x, y, z - 1);
        }
        // finally check the remaining adjacent voxels
        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                for(int k = -1; k <= 1; ++k) {
                    // don't check the ones we already did above
                    if(Math.abs(i) + Math.abs(j) + Math.abs(k) <= 1) {
                        continue;
                    }
                    // find squared distance to voxel
                    double vd2 = 0;
                    if(i < 0)      vd2 += dnx2;
                    else if(i > 0) vd2 += dpx2;
                    if(j < 0)      vd2 += dny2;
                    else if(j > 0) vd2 += dpy2;
                    if(k < 0)      vd2 += dnz2;
                    else if(k > 0) vd2 += dpz2;
                    // and check it if it's close enough to matter
                    if(vd2 < s) {
                        s = processVoxel(r, p, s, x + i, y + j, z + k);
                    }
                }
            }
        }
        // provide minimum. be sure to square root it to get the
        // true distance.
        return Math.sqrt(s);
    }

    /**
     * Processes a voxel and calculates the distances of the points within
     * against the provided point. It also tracks the progress of the lowest
     * values yet discovered.
     *
     * @param r
     *            the random number generator.
     * @param p
     *            the point that the locations within this voxel will be tested
     *            against.
     * @param s
     *            the storage that tracks the lowest values currently
     *            encountered.
     * @param x
     *            the x coordinate of the voxel.
     * @param y
     *            the y coordinate of the voxel.
     * @param z
     *            the z coordinate of the voxel.
     * @return the closest distance of the points within the voxel to the
     *         provided point.
     */
    private double processVoxel(Random r, Point p, double s, int x, int y, int z) {
        // reset random number generator for the voxel
        r.setSeed(x + y + z);
        // each voxel always has one point
        Point created = new Point(
                x + r.nextDouble(),
                y + r.nextDouble(),
                z + r.nextDouble());
        // determine the distance between the generated point
        // and the source point we're checking.
        double distance = p.distanceSquared(created);
        // add distance if it is lowest
        return Math.min(distance, s);
    }

    private double square(double n) {
        return n * n;
    }
}

package com.marginallyclever.convenience.noise;

public class NoiseFactory {
    public static String [] getNames() {
        return new String[] {"Perlin","Simplex"};
    }

    public static Noise getNoise(int i) {
        return switch (i) {
            case 0 -> new PerlinNoise();
            case 1 -> new SimplexNoise();
            default -> null;
        };
    }
}

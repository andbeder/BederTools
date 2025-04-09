package com.beder.texture;

import java.awt.image.BufferedImage;
import java.util.Random;

public class Simplex {

    private final OpenSimplex2S noise;
    private final long seed;
    
    public Simplex(long seed) {
        noise = new OpenSimplex2S();
        this.seed = seed;
    }
    
    /**
     * Returns a noise value for the given coordinates using OpenSimplex2S.
     * Assumes OpenSimplex2S.noise2(seed, x, y) returns values in the range [-1, 1].
     */
    public double noise(double x, double y) {
        return noise.noise2(seed, x, y);
    }
    
    /**
     * Generates an OpenSimplex (Simplex) noise pattern as a grayscale image.
     *
     * @param res   The width/height of the image.
     * @param scale A scale factor for the noise (larger scale = larger features).
     * @return A BufferedImage with the noise pattern.
     */
    public static BufferedImage generateSimplexNoise(int res, double scale) {
        Simplex simplex = new Simplex(new Random().nextLong());
        BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                double val = simplex.noise(x / scale, y / scale);
                val = (val + 1) / 2; // Normalize to [0, 1]
                int gray = (int) (val * 255);
                int color = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, color);
            }
        }
        return img;
    }
}

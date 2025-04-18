package com.beder.texturearchive;

import java.awt.image.BufferedImage;

import com.beder.util.OpenSimplex2S;

public class Simplex {

    private final OpenSimplex2S noise;
    private final long seed;

    public Simplex(long seed) {
        this.noise = new OpenSimplex2S();
        this.seed = seed;
    }

    /**
     * Returns a noise value for the given (x, y) coordinate.
     * This method calls OpenSimplex2S.noise2 using the stored seed.
     * Range typically ~[-1, 1].
     */
    public double noise(double x, double y) {
        return noise.noise2(seed, x, y);
    }

    /**
     * Generates a grayscale noise image using the OpenSimplex algorithm,
     * using the provided seed for reproducibility.
     *
     * @param res   The width and height of the output square image.
     * @param scale A scale factor for the noise (larger values => larger features).
     * @param seed  The user-provided seed for consistent results.
     */
    public static BufferedImage generateSimplexNoise(int res, double scale, long seed) {
        Simplex simplex = new Simplex(seed);
        BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                double val = simplex.noise(x / scale, y / scale);
                // Normalize from [-1,1] -> [0,1]
                val = (val + 1) / 2.0;
                int gray = (int)(val * 255);
                int color = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, color);
            }
        }
        return img;
    }
}

package com.beder.texturearchive;

import java.awt.image.BufferedImage;

public class Perlin {

    private final int[] permutation;
    private final int[] p; // doubled permutation array

    /**
     * Constructs a Perlin noise generator with a fixed seed.
     */
    public Perlin(long seed) {
        permutation = new int[256];
        p = new int[512];
        java.util.Random rand = new java.util.Random(seed);

        // Initialize permutation with identity.
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        // Shuffle using the user-provided seed.
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        // Duplicate the permutation array.
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i & 255];
        }
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 7;  // Convert low 3 bits of hash code
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(double x, double y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);

        int aa = p[p[X] + Y];
        int ab = p[p[X] + Y + 1];
        int ba = p[p[X + 1] + Y];
        int bb = p[p[X + 1] + Y + 1];

        double gradAA = grad(aa, x, y);
        double gradBA = grad(ba, x - 1, y);
        double gradAB = grad(ab, x, y - 1);
        double gradBB = grad(bb, x - 1, y - 1);

        double lerpX1 = lerp(u, gradAA, gradBA);
        double lerpX2 = lerp(u, gradAB, gradBB);
        return lerp(v, lerpX1, lerpX2);
    }

    /**
     * Generates Perlin noise using multiple octaves, seeded by the specified seed.
     *
     * @param res         The width/height of the image.
     * @param baseFreq    Base frequency for noise.
     * @param iterations  Number of octaves.
     * @param seed        The user-provided seed for reproducible noise.
     * @return A BufferedImage containing the Perlin noise pattern.
     */
    public static BufferedImage generatePerlinNoise(int res, double baseFreq, int iterations, long seed) {
        Perlin perlin = new Perlin(seed);
        BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                double amplitude = 1.0;
                double frequency = baseFreq;
                double noiseSum = 0;
                double maxValue = 0;
                for (int i = 0; i < iterations; i++) {
                    double n = perlin.noise(x * frequency / res, y * frequency / res);
                    noiseSum += n * amplitude;
                    maxValue += amplitude;
                    amplitude *= 0.5;  // Persistence
                    frequency *= 2.0;  // Lacunarity
                }
                double normalized = (noiseSum / maxValue + 1) / 2;
                int gray = (int)(normalized * 255);
                int color = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                img.setRGB(x, y, color);
            }
        }
        return img;
    }
}

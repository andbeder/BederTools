package com.beder.texture;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.Random;

public class Vegetation {

    /**
     * Generates a vegetation mask image with lifelike splotches.
     *
     * @param res         The resolution (width and height) of the output image.
     * @param size        The base radius (in pixels) of major clumps.
     * @param regularity  A value (0–100) for how regular (circular) the clumps are;
     *                    100 produces near-perfect circles.
     * @param spread      A value (0–100) controlling how gradually the clumps fade out.
     *                    Higher values make a longer gradient falloff.
     * @return A BufferedImage representing an ARGB vegetation mask.
     */
    public static BufferedImage generateVegetation(int res, double size, double regularity, int spread) {
        BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        int background = 0xFFFFFFFF;  // white
        int vegColor = 0xFF006600;    // dark green

        // Fill background.
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                img.setRGB(x, y, background);
            }
        }

        Random rand = new Random();

        
        
        // Determine number of major clumps (1 or 2).
        int numMajor = rand.nextInt(2) + 1;

        // We'll accumulate an intensity map (0.0 to 1.0) for vegetation.
   
        int numSplotches = Math.max(5, res / 200);
        for (int i = 0; i < numSplotches; i++) {
            // Random center for each splotch.
            int cx = rand.nextInt(res);
            int cy = rand.nextInt(res);
            // Random radius between 5 and res/10.
            int radius = 5 + rand.nextInt(Math.max(1, res / 10 - 5));
            // For irregularity, a factor between 0.8 and 1.2.
            double irregularity = 0.8 + rand.nextDouble() * 0.4;
            int effectiveRadius = (int) (radius * irregularity);
            // Draw the splotch (fill a circle in the bounding box).
            int x0 = Math.max(0, cx - effectiveRadius);
            int y0 = Math.max(0, cy - effectiveRadius);
            int x1 = Math.min(res - 1, cx + effectiveRadius);
            int y1 = Math.min(res - 1, cy + effectiveRadius);
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    double dist = Math.hypot(x - cx, y - cy);
                    // With some randomness in the edge.
                    if (dist < effectiveRadius * (0.9 + 0.2 * rand.nextDouble())) {
                        img.setRGB(x, y, vegColor);
                    }
                }
            }
        }
        return img;
    }

    // Blends two ARGB colors given a fraction (0 = color1, 1 = color2)
    private static int blendColors(int color1, int color2, double fraction) {
        int a1 = (color1 >> 24) & 0xff, r1 = (color1 >> 16) & 0xff,
            g1 = (color1 >> 8) & 0xff, b1 = color1 & 0xff;
        int a2 = (color2 >> 24) & 0xff, r2 = (color2 >> 16) & 0xff,
            g2 = (color2 >> 8) & 0xff, b2 = color2 & 0xff;
        int a = (int) (a1 * (1 - fraction) + a2 * fraction);
        int r = (int) (r1 * (1 - fraction) + r2 * fraction);
        int g = (int) (g1 * (1 - fraction) + g2 * fraction);
        int b = (int) (b1 * (1 - fraction) + b2 * fraction);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

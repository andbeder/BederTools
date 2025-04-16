package com.beder.texturearchive;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Holds Operation subclasses that each transform an image, plus record parameters for the stack.
 */
public class Operations {

    public interface Operation {
        /**
         * Applies this operation to the given input image and returns a new image.
         */
        BufferedImage apply(BufferedImage input);

        /**
         * Returns a textual description of the operation and its parameters.
         */
        String getDescription();
    }

    // ------------------ Existing texture-generation classes ------------------

    public static class CellNoiseOperation implements Operation {
        public final int res;
        public final long seed;
        public final int cells;
        public final int gaussianPct;

        public CellNoiseOperation(int res, long seed, int cells, int gaussianPct) {
            this.res = res;
            this.seed = seed;
            this.cells = cells;
            this.gaussianPct = gaussianPct;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            // We ignore the 'input' image and generate a fresh texture.
            Random rand = new java.util.Random(seed);
            double mix = Math.max(0, Math.min(100, gaussianPct)) / 100.0;
            return CellNoise.generateCellNoise(res, cells, mix, rand);
        }

        @Override
        public String getDescription() {
            return "CellNoise: cells=" + cells + ", Gaussian=" + gaussianPct + "%, seed=" + seed;
        }
    }

    public static class VegetationOperation implements Operation {
        public final int res;
        public final long seed;
        public final double size;
        public final double regularity;
        public final int spread;

        public VegetationOperation(int res, long seed, double size, double regularity, int spread) {
            this.res = res;
            this.seed = seed;
            this.size = size;
            this.regularity = regularity;
            this.spread = spread;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            java.util.Random rand = new java.util.Random(seed);
            return Vegetation.generateVegetation(res, size, regularity, spread, rand);
        }

        @Override
        public String getDescription() {
            return "Vegetation: size=" + size + ", reg=" + regularity + ", spread=" + spread + ", seed=" + seed;
        }
    }

    public static class VoronoiOperation implements Operation {
        public final int res;
        public final long seed;
        public final int points;

        public VoronoiOperation(int res, long seed, int points) {
            this.res = res;
            this.seed = seed;
            this.points = points;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            java.util.Random rand = new java.util.Random(seed);
            return Voronoi.generateVoronoi(res, points, rand);
        }

        @Override
        public String getDescription() {
            return "Voronoi: points=" + points + ", seed=" + seed;
        }
    }

    public static class SimplexOperation implements Operation {
        public final int res;
        public final long seed;
        public final double scale;

        public SimplexOperation(int res, long seed, double scale) {
            this.res = res;
            this.seed = seed;
            this.scale = scale;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            // Re-generate from scratch, ignoring 'input.'
            return Simplex.generateSimplexNoise(res, scale, seed);
        }

        @Override
        public String getDescription() {
            return "Simplex: scale=" + scale + ", seed=" + seed;
        }
    }

    public static class PerlinOperation implements Operation {
        public final int res;
        public final long seed;
        public final double frequency;
        public final int iterations;

        public PerlinOperation(int res, long seed, double frequency, int iterations) {
            this.res = res;
            this.seed = seed;
            this.frequency = frequency;
            this.iterations = iterations;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            return Perlin.generatePerlinNoise(res, frequency, iterations, seed);
        }

        @Override
        public String getDescription() {
            return "Perlin: freq=" + frequency + ", iter=" + iterations + ", seed=" + seed;
        }
    }

    // ------------------ New classes to handle copy, mix, blur, level as operations ------------------

    /**
     * CopyOperation: copies a stored 'source' image, ignoring the input image.
     */
    public static class CopyOperation implements Operation {
        private final BufferedImage sourceImage;

        public CopyOperation(BufferedImage sourceImage) {
            // Make a defensive copy so we don't lose the original if it changes externally.
            this.sourceImage = copyOf(sourceImage);
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            // Always return a copy of the source image
            return copyOf(sourceImage);
        }

        @Override
        public String getDescription() {
            return "Copy: from left image";
        }

        private static BufferedImage copyOf(BufferedImage src) {
            BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            java.awt.Graphics g = copy.getGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return copy;
        }
    }

    /**
     * MixOperation: alpha-blends the input image with a stored mixImage.
     * mixPercent is 0.0..1.0, meaning how much of the mixImage is used.
     */
    public static class MixOperation implements Operation {
        private final BufferedImage mixImage;
        private final double mixPercent; // 0..1

        public MixOperation(BufferedImage mixImage, double mixPercent) {
            this.mixImage = copyOf(mixImage);
            this.mixPercent = mixPercent;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            if (input == null) {
                // If there's no input, return a copy of mixImage
                return copyOf(mixImage);
            }
            int w = Math.min(input.getWidth(), mixImage.getWidth());
            int h = Math.min(input.getHeight(), mixImage.getHeight());
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgbA = input.getRGB(x, y);
                    int rgbB = mixImage.getRGB(x, y);

                    int aA = (rgbA >> 24) & 0xff;
                    int rA = (rgbA >> 16) & 0xff;
                    int gA = (rgbA >> 8) & 0xff;
                    int bA = rgbA & 0xff;

                    int aB = (rgbB >> 24) & 0xff;
                    int rB = (rgbB >> 16) & 0xff;
                    int gB = (rgbB >> 8) & 0xff;
                    int bB = rgbB & 0xff;

                    double alphaA = aA / 255.0;
                    double alphaB = aB / 255.0;

                    // Weighted average of color channels
                    double alphaOut = alphaA * (1.0 - mixPercent) + alphaB * mixPercent;
                    double rOut = rA * (1.0 - mixPercent) + rB * mixPercent;
                    double gOut = gA * (1.0 - mixPercent) + gB * mixPercent;
                    double bOut = bA * (1.0 - mixPercent) + bB * mixPercent;

                    int iaOut = (int)(alphaOut * 255.0);
                    int irOut = (int)rOut;
                    int igOut = (int)gOut;
                    int ibOut = (int)bOut;

                    int outRGB = (iaOut << 24) | (irOut << 16) | (igOut << 8) | ibOut;
                    out.setRGB(x, y, outRGB);
                }
            }
            return out;
        }

        @Override
        public String getDescription() {
            return "Mix: " + (int)(mixPercent * 100) + "% with left image";
        }

        private static BufferedImage copyOf(BufferedImage src) {
            BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            java.awt.Graphics g = copy.getGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return copy;
        }
    }

    /**
     * BlurOperation: applies a Gaussian blur of a specified radius to the input image.
     */
    public static class BlurOperation implements Operation {
        private final int radius;

        public BlurOperation(int radius) {
            this.radius = radius;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            if (input == null) return null;
            return gaussianBlur(input, radius);
        }

        @Override
        public String getDescription() {
            return "Blur: radius=" + radius;
        }

        // Reuse the same blur code from TextureGenius or create a local method
        private static BufferedImage gaussianBlur(BufferedImage src, int radius) {
            if (radius < 1) return src;
            int width = src.getWidth(), height = src.getHeight();
            BufferedImage temp = new BufferedImage(width, height, src.getType());
            BufferedImage dst = new BufferedImage(width, height, src.getType());
            int kernelSize = 2 * radius + 1;
            double[] kernel = new double[kernelSize];
            double sigma = radius / 3.0;
            double sigma22 = 2 * sigma * sigma;
            double sqrtSigmaPi2 = Math.sqrt(Math.PI * sigma22);
            double sum = 0;
            for (int i = -radius; i <= radius; i++) {
                double r = i * i;
                kernel[i + radius] = Math.exp(-r / sigma22) / sqrtSigmaPi2;
                sum += kernel[i + radius];
            }
            for (int i = 0; i < kernelSize; i++) {
                kernel[i] /= sum;
            }
            // Horizontal pass
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double a = 0, r = 0, g = 0, b = 0;
                    for (int k = -radius; k <= radius; k++) {
                        int xx = x + k;
                        if (xx < 0) xx = 0;
                        else if (xx >= width) xx = width - 1;
                        int rgb = src.getRGB(xx, y);
                        int ca = (rgb >> 24) & 0xff;
                        int cr = (rgb >> 16) & 0xff;
                        int cg = (rgb >> 8) & 0xff;
                        int cb = rgb & 0xff;
                        double weight = kernel[k + radius];
                        a += ca * weight;
                        r += cr * weight;
                        g += cg * weight;
                        b += cb * weight;
                    }
                    int ia = (int)Math.min(255, Math.max(0, Math.round(a)));
                    int ir = (int)Math.min(255, Math.max(0, Math.round(r)));
                    int ig = (int)Math.min(255, Math.max(0, Math.round(g)));
                    int ib = (int)Math.min(255, Math.max(0, Math.round(b)));
                    int newRgb = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                    temp.setRGB(x, y, newRgb);
                }
            }
            // Vertical pass
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double a = 0, r = 0, g = 0, b = 0;
                    for (int k = -radius; k <= radius; k++) {
                        int yy = y + k;
                        if (yy < 0) yy = 0;
                        else if (yy >= height) yy = height - 1;
                        int rgb = temp.getRGB(x, yy);
                        int ca = (rgb >> 24) & 0xff;
                        int cr = (rgb >> 16) & 0xff;
                        int cg = (rgb >> 8) & 0xff;
                        int cb = rgb & 0xff;
                        double weight = kernel[k + radius];
                        a += ca * weight;
                        r += cr * weight;
                        g += cg * weight;
                        b += cb * weight;
                    }
                    int ia = (int)Math.min(255, Math.max(0, Math.round(a)));
                    int ir = (int)Math.min(255, Math.max(0, Math.round(r)));
                    int ig = (int)Math.min(255, Math.max(0, Math.round(g)));
                    int ib = (int)Math.min(255, Math.max(0, Math.round(b)));
                    int newRgb = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                    dst.setRGB(x, y, newRgb);
                }
            }
            return dst;
        }
    }

    /**
     * LevelOperation: threshold the input image to black/white based on user-specified threshold.
     */
    public static class LevelOperation implements Operation {
        private final int threshold; // 0..255

        public LevelOperation(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public BufferedImage apply(BufferedImage input) {
            if (input == null) return null;
            int w = input.getWidth();
            int h = input.getHeight();
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = input.getRGB(x, y);
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int avg = (r + g + b) / 3;
                    int color = (avg < threshold) ? 0xFF000000 : 0xFFFFFFFF;
                    out.setRGB(x, y, color);
                }
            }
            return out;
        }

        @Override
        public String getDescription() {
            return "Level: threshold=" + threshold;
        }
    }
}

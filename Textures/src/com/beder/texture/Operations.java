package com.beder.texture;

import java.awt.image.BufferedImage;

public class Operations {
    public interface Operation {
        /**
         * Applies this operation to the input image and returns a new image.
         * For the first operation, the input image may be ignored.
         */
        BufferedImage apply(BufferedImage input);
        
        /**
         * Returns a textual description of the operation and its parameters.
         */
        String getDescription();
    }
    
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
            // Use the parameters to generate a new cell noise image.
            // Note: This ignores the input image and returns a fresh image.
            return CellNoise.generateCellNoise(res, cells, gaussianPct / 100.0);
        }
        
        @Override
        public String getDescription() {
            return "CellNoise: cells=" + cells + ", Gaussian=" + gaussianPct + ", seed=" + seed;
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
            return Vegetation.generateVegetation(res, size, regularity, spread);
        }
        
        @Override
        public String getDescription() {
            return "Vegetation: size=" + size + ", regularity=" + regularity + ", spread=" + spread + ", seed=" + seed;
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
            return Voronoi.generateVoronoi(res, points);
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
            return Simplex.generateSimplexNoise(res, scale);
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
            return Perlin.generatePerlinNoise(res, frequency, iterations);
        }
        
        @Override
        public String getDescription() {
            return "Perlin: freq=" + frequency + ", iter=" + iterations + ", seed=" + seed;
        }
    }
}

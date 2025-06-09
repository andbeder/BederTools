package com.beder.texture.scatter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Random;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;
import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;
import com.beder.util.OpenSimplex2S;

public class ScatterOperation extends Operation {
    private final OpenSimplex2S noise = new OpenSimplex2S();

    private static BufferedImage blur(BufferedImage img, int radius) {
        if (radius <= 0) return img;
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        float sigma = radius / 3f;
        float sum = 0f;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float val = (float)Math.exp(-(x*x + y*y) / (2*sigma*sigma));
                data[(y + radius) * size + (x + radius)] = val;
                sum += val;
            }
        }
        for (int i = 0; i < data.length; i++) data[i] /= sum;
        Kernel kernel = new Kernel(size, size, data);
        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(img, out);
        return out;
    }

    public ScatterOperation(Redrawable redraw) {
        super(redraw);
        addParameter("Quantity", CONTROL_TYPE.INT, 10);
        addParameter("Size", CONTROL_TYPE.INT, 64);
        addParameter("StdDev", CONTROL_TYPE.DOUBLE, 10.0);
        addParameter("Seed", CONTROL_TYPE.SEED, new Random().nextLong());
        addParameter("AO", CONTROL_TYPE.BOOLEAN, 0);
        addParameter("Radius", CONTROL_TYPE.INT, 8);
        addParameter("Depth", CONTROL_TYPE.DOUBLE, 0.01);
        addParameter("Scale", CONTROL_TYPE.DOUBLE, 0.5);
        addParameter("Threshold", CONTROL_TYPE.DOUBLE, 0.5);
    }

    @Override
    public ImagePair executeOperation(ImagePair input, Parameters par) {
        // 1. Read parameters
        int quantity  = (int) par.get("Quantity", 10);
        int meanSize  = (int) par.get("Size",     64);
        double stdDev =      par.get("StdDev",   10.0);
        long seed     =   (long) par.get("Seed",    System.currentTimeMillis());
        boolean aoEnabled = par.get("AO", 0) > 0.5;
        int radius   = (int) par.get("Radius", 8);
        double depth =      par.get("Depth", 0.01);
        double noiseScale = par.get("Scale", 0.5);
        double threshold  = par.get("Threshold", 0.5);
        Random rnd    = new Random(seed);

        // 2. Fetch sprites
        SpriteRepository repo = SpriteRepository.getInstance();
        if (repo.getCount() == 0) {
            // No sprites configured → no-op
            return input;
        }

        int res = getRedraw().getRes();
        BufferedImage canvas = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        if (aoEnabled) {
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, res, res);
            g.dispose();
        }
        input.left = canvas;

        for (int i = 0; i < quantity; i++) {
            // 3. Weighted random sprite selection
            int idx = repo.getRandomIndex(rnd);
            BufferedImage sprite = repo.getSprite(idx);

            // 4. Sample size (Gaussian) and rotation angle
            int size = Math.max(1, (int)(rnd.nextGaussian() * stdDev + meanSize));
            double angle = rnd.nextDouble() * Math.PI * 2;

            // 5. Build AffineTransform: scale → rotate around center
            double scaleX = (double) size / sprite.getWidth();
            double scaleY = (double) size / sprite.getHeight();
            AffineTransform tx = new AffineTransform();
            tx.translate(size / 2.0, size / 2.0);
            tx.rotate(angle);
            tx.scale(scaleX, scaleY);
            tx.translate(-sprite.getWidth() / 2.0, -sprite.getHeight() / 2.0);

            // 6. Render transformed sprite into a temp image
            BufferedImage transformed = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = transformed.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(sprite, tx, null);
            g2.dispose();

            // 7. Choose a random placement
            int x0 = rnd.nextInt(res);
            int y0 = rnd.nextInt(res);

            if (!aoEnabled) {
                // 8. Paste with toroidal wrap: pixel‐by‐pixel
                for (int y = 0; y < transformed.getHeight(); y++) {
                    for (int x = 0; x < transformed.getWidth(); x++) {
                        int argb = transformed.getRGB(x, y);
                        int alpha = (argb >>> 24) & 0xFF;
                        if (alpha == 0) continue;
                        int dx = (x0 + x) % res;
                        if (dx < 0) dx += res;
                        int dy = (y0 + y) % res;
                        if (dy < 0) dy += res;
                        canvas.setRGB(dx, dy, argb);
                    }
                }
            } else {
                int ext = size + radius * 2;
                BufferedImage mask = new BufferedImage(ext, ext, BufferedImage.TYPE_INT_ARGB);
                Graphics2D mg = mask.createGraphics();
                mg.drawImage(transformed, radius, radius, null);
                mg.dispose();
                BufferedImage blurred = blur(mask, radius);
                for (int y = 0; y < ext; y++) {
                    for (int x = 0; x < ext; x++) {
                        int alpha = (blurred.getRGB(x, y) >>> 24) & 0xFF;
                        if (alpha == 0) continue;
                        int dx = (x0 + x - radius) % res;
                        if (dx < 0) dx += res;
                        int dy = (y0 + y - radius) % res;
                        if (dy < 0) dy += res;
                        double val = alpha / 255.0;
                        double n = (noise.noise2(seed, (dx) / noiseScale, (dy) / noiseScale) + 1) / 2.0;
                        n = (n - threshold) / (1 - threshold);
                        if (n < 0) n = 0;
                        val *= n * depth;
                        int rgb = canvas.getRGB(dx, dy);
                        int gray = (rgb >> 16) & 0xFF;
                        int newGray = (int) Math.max(0, gray - val * 255);
                        int newRgb = 0xFF000000 | (newGray << 16) | (newGray << 8) | newGray;
                        canvas.setRGB(dx, dy, newRgb);
                    }
                }
                // write sprite itself as white
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int argb = transformed.getRGB(x, y);
                        int alpha = (argb >>> 24) & 0xFF;
                        if (alpha == 0) continue;
                        int dx = (x0 + x) % res;
                        if (dx < 0) dx += res;
                        int dy = (y0 + y) % res;
                        if (dy < 0) dy += res;
                        canvas.setRGB(dx, dy, 0xFFFFFFFF);
                    }
                }
            }
        }

        return input;
    }

    @Override
    public String getTitle() {
        return "Scatter";
    }

    @Override
    public String getDescription() {
        return "Scatter: randomly distributes sprites across the image buffer";
    }
}
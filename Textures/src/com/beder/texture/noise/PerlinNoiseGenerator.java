package com.beder.texture.noise;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;
import com.beder.texturearchive.Perlin;

/**
 * Simple stub implementation of a Perlin noise generator.
 * Replace this with a full-featured algorithm as needed.
 */
public class PerlinNoiseGenerator extends NoiseOperation {
	private JPanel perlinOptionsPanel;
	private JTextField perlinFreqField;
	private JTextField perlinIterField;
	private int[] permutation;
	private int[] p;
	private JPanel perlinTilePanel;

    public PerlinNoiseGenerator(int res) {
    	super(res);
 
     }
    

	@Override
	public JPanel getConfig() {
        perlinOptionsPanel = new JPanel(new FlowLayout());
        perlinOptionsPanel.add(new JLabel("Frequency:"));
        perlinFreqField = new JTextField("4.0", 6);
        perlinOptionsPanel.add(perlinFreqField);
        perlinOptionsPanel.add(new JLabel("Iterations:"));
        perlinIterField = new JTextField("4", 6);
        perlinOptionsPanel.add(perlinIterField);
        
        addSeedConfig(perlinOptionsPanel);
		return perlinOptionsPanel;
	}

	@Override
	public JPanel getOperationTile() {
		if (perlinTilePanel == null) {
			perlinTilePanel = new JPanel(new FlowLayout());
			perlinTilePanel.setBorder(BorderFactory.createEtchedBorder());
			perlinTilePanel.setLayout(new BorderLayout());
			perlinTilePanel.add(new JLabel("Simplex"), BorderLayout.CENTER);
			JPanel tileParamPanel = new JPanel(new FlowLayout());
			perlinTilePanel.add(tileParamPanel, BorderLayout.SOUTH);
			JLabel tileScaleLabel = new JLabel("Freq:");
			tileParamPanel.add(tileScaleLabel);
			JLabel tileFreqValue = new JLabel(perlinFreqField.getText());
			tileParamPanel.add(tileFreqValue);
			JLabel tileIterLabel = new JLabel("Iterx:");
			tileParamPanel.add(tileIterLabel);
			JLabel tileIterValue = new JLabel(perlinIterField.getText());
			tileParamPanel.add(tileIterValue);
		}
		return perlinTilePanel;
	}

	@Override
	public Map<String, String> getUIParameters() {
		Map<String, String> pMap = new TreeMap<String, String>();
		pMap.put("baseFreq", perlinFreqField.getText());
		pMap.put("iterations", perlinIterField.getText());
		return pMap;
	}
	
	
	@Override
	public BufferedImage generateNoise() {
        double baseFreq = Double.parseDouble(perlinFreqField.getText());
        int iterations = Integer.parseInt(perlinIterField.getText());
 
        permutation = new int[256];
        p = new int[512];
        Random rand = new java.util.Random(getSeed());

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
        
        return generatePerlinNoise(baseFreq, iterations);
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
    public BufferedImage generatePerlinNoise(double baseFreq, int iterations) {

        BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                double amplitude = 1.0;
                double frequency = baseFreq;
                double noiseSum = 0;
                double maxValue = 0;
                for (int i = 0; i < iterations; i++) {
                    double n = noise(x * frequency / res, y * frequency / res);
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


	@Override
	public String getDescription() {
		return "Generates Perlin noise using multiple octaves, seeded by the specified seed";
	}


	@Override
	public String getTitle() {
		return "Perlin Noise";
	}
}

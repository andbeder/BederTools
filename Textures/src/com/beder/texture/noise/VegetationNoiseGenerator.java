package com.beder.texture.noise;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;

public class VegetationNoiseGenerator extends NoiseOperation {
	
	private JPanel optionsPanel;
	private JTextField seedCountField;
	private JTextField growthField;
	private JTextField deathRateField;
	private JTextField durationField;
	private final static String PARAM_SEED_COUNT = "Seeds";
	private final static String PARAM_GROWTH = "Growth";
	private final static String PARAM_DEATH = "Death";
	private final static String PARAM_ITER = "Iterations";
	
	public VegetationNoiseGenerator(Redrawable redraw) {
		super(redraw);

        optionsPanel = new JPanel(new FlowLayout());

        // seedCount
		optionsPanel.add(new JLabel("Initial Seeds:"));
		seedCountField = new JTextField("100", 6);
		optionsPanel.add(seedCountField);

		// growth
		optionsPanel.add(new JLabel("Growth (0–1):"));
		growthField = new JTextField("0.5", 6);
		optionsPanel.add(growthField);

		// deathRate
		optionsPanel.add(new JLabel("Death Rate (0–1):"));
		deathRateField = new JTextField("0.2", 6);
		optionsPanel.add(deathRateField);

		// duration
		optionsPanel.add(new JLabel("Iterations:"));
		durationField = new JTextField("50", 6);
		optionsPanel.add(durationField);
	}

	@Override
	public Parameters getUIParameters() {
		Parameters param = new Parameters();
		param.put(PARAM_SEED_COUNT, Double.parseDouble(seedCountField.getText()));
		param.put(PARAM_GROWTH, Double.parseDouble(growthField.getText()));
		param.put(PARAM_DEATH, Double.parseDouble(deathRateField.getText()));
		param.put(PARAM_ITER, Double.parseDouble(durationField.getText()));
		return param;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public JPanel getConfig() {
		return optionsPanel;
	}

	@Override
	public String getTitle() {
		return "Vegetation";
	}

	@Override
    public BufferedImage generateNoise(Parameters param) {
        Random rand = new Random(getSeed());
        BufferedImage fertility = getInput().left;
        int res = getRedraw().getRes();
        int seedCount = (int) param.get(PARAM_SEED_COUNT, 100);
        double growth = param.get(PARAM_GROWTH, 0.5);
        double deathRate = param.get(PARAM_DEATH, 0.2);
        int duration = (int) param.get(PARAM_ITER, 50);
        
        int[][] current = new int[res][res];
        int[][] next = new int[res][res];

        // Seed initial vegetation
        for (int i = 0; i < seedCount; i++) {
            int x = rand.nextInt(res);
            int y = rand.nextInt(res);
            current[y][x] = 1;
        }

        // Run CA cycles
        for (int cycle = 0; cycle < duration; cycle++) {
            for (int y = 0; y < res; y++) {
                for (int x = 0; x < res; x++) {
                    int rgb = fertility.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >>  8) & 0xFF;
                    int b = (rgb      ) & 0xFF;
                    double fert = ((r + g + b) / 3.0) / 255.0;

                    if (current[y][x] == 1) {
                        // Alive cell: survival probability = 1 - deathRate * (1 - fert)
                        double survivalProb = 1 - deathRate * (1 - fert);
                        next[y][x] = (rand.nextDouble() < survivalProb) ? 1 : 0;
                    } else {
                        // Dead cell: may sprout if neighbors exist
                        int aliveNeighbors = countAliveNeighbors(current, x, y);
                        if (aliveNeighbors > 0 && rand.nextDouble() < fert * growth) {
                            next[y][x] = 1;
                        } else {
                            next[y][x] = 0;
                        }
                    }
                }
            }
            // Swap
            int[][] temp = current;
            current = next;
            next = temp;
        }

        // Render output
        BufferedImage output = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                int intensity = current[y][x] == 1 ? 255 : 0;
                int gray = (intensity << 16) | (intensity << 8) | intensity;
                output.setRGB(x, y, 0xFF000000 | gray);
            }
        }
        return output;
    }

    /**
     * Counts alive neighbors around (x, y) in a toroidal grid.
     */
    private int countAliveNeighbors(int[][] grid, int x, int y) {
        int res = getRedraw().getRes();
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = (x + dx + res) % res;
                int ny = (y + dy + res) % res;
                count += grid[ny][nx];
            }
        }
        return count;
    }
}


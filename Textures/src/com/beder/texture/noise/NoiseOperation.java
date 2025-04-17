package com.beder.texture.noise;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;
import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;

public abstract class NoiseOperation extends Operation {

	private JTextField seedField;
	private JButton randomSeedButton;
	private BufferedImage result;
	private Parameters lastPar;
	private long seed;

	public NoiseOperation(Redrawable r) {
		super(r);
		result = null;
		lastPar = new Parameters();
		seed = new Random().nextInt(Integer.MAX_VALUE);
	}

    /**
     * Called by child class to add random seed controls on the edit panel
     */
	protected void addSeedConfig(JPanel panel) {
        seedField = new JTextField(String.valueOf(seed), 8);
        panel.add(seedField);
        
        randomSeedButton = new JButton("Random");
        randomSeedButton.addActionListener(e -> {
            String newSeed = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            seedField.setText(newSeed);
        });
        panel.add(randomSeedButton);
	}
	
	
	public abstract BufferedImage generateNoise();
	
    /**
     * Overriding executeOperation() for Noise values. This will call a new function, generateNoise() instead
     *   if the image needs to be refreshed. It will call getParameters() to get a list of current
     *   parameters to check against historical
     */
	@Override
	public ImagePair executeOperation(ImagePair input, Parameters par) {
		boolean needsRefresh = false;
		for (String parName : par.keySet()) {
			double prev = par.get(parName);
			if (prev != lastPar.get(parName, prev)) {
				needsRefresh = true;
			}
		}
		if (needsRefresh) {
			result = generateNoise();
			lastPar = par;
		}
		input.left = result;
		return input;
	}

	public long getSeed() {
        long seed = Long.parseLong(seedField.getText());
		return seed;
	}
}

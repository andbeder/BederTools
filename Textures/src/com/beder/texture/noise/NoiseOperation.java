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

public abstract class NoiseOperation extends Operation {

	private JTextField seedField;
	private JButton randomSeedButton;
	private BufferedImage result;
	private Map<String, String> par;

	public NoiseOperation(int res) {
		super(res);
		result = null;
		par = new TreeMap<String, String>();
	}

    /**
     * Called by child class to add random seed controls on the edit panel
     */
	public void addSeedConfig(JPanel panel) {
        seedField = new JTextField(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)), 8);
        panel.add(seedField);
        
        randomSeedButton = new JButton("Random");
        randomSeedButton.addActionListener(e -> {
            String newSeed = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            seedField.setText(newSeed);
        });
        panel.add(randomSeedButton);
	}
	
	public abstract Map<String, String> getParameters();
	
	public abstract BufferedImage generateNoise();
	
    /**
     * Overriding apply() for Noise values. This will call a new function, generateNoise() instead
     *   if the image needs to be refreshed. It will call getParameters() to get a list of current
     *   parameters to check against historical
     */
	@Override
	public ImagePair apply(ImagePair input) {
		Map<String, String> curPar = getParameters();
		boolean needsRefresh = false;
		for (Entry<String, String> cur : curPar.entrySet()) {
			String prev = par.get(cur.getKey());
			if (prev == null || !prev.equals(cur.getValue())) {
				needsRefresh = true;
			}
		}
		if (needsRefresh) {
			result = generateNoise();
		}
		input.left = result;
		return input;
	}

	public long getSeed() {
        long seed = Long.parseLong(seedField.getText());
		return seed;
	}
}

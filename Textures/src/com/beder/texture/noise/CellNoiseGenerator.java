package com.beder.texture.noise;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.beder.texture.ImagePair;
import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;
import com.beder.texturearchive.CellNoise;

public class CellNoiseGenerator extends NoiseOperation {
    private final JPanel optionsPanel;
    private final JTextField frequencyField;
    private final JSlider gaussianSlider;
    private final static String PARAM_FREQ = "Frequency";
    private final static String PARAM_GUAS = "Guassian";

    public CellNoiseGenerator(Redrawable redraw) {
        super(redraw);

        optionsPanel = new JPanel(new FlowLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Cell Noise Options"));

        optionsPanel.add(new JLabel("Frequency:"));
        frequencyField = new JTextField("10", 6);
        optionsPanel.add(frequencyField);

        optionsPanel.add(new JLabel("Gaussian (%):"));
        gaussianSlider = new JSlider(0, 100, 40);
        gaussianSlider.setMajorTickSpacing(20);
        gaussianSlider.setMinorTickSpacing(5);
        gaussianSlider.setPaintTicks(true);
        gaussianSlider.setPaintLabels(true);
        optionsPanel.add(gaussianSlider);

        addSeedConfig(optionsPanel);
    }

    @Override
    public JPanel getConfig() {
        return optionsPanel;
    }

    @Override
    public Parameters getUIParameters() {
        Parameters p = new Parameters();
        p.put(PARAM_FREQ, frequencyField.getText());
        p.put(PARAM_GUAS, Integer.toString(gaussianSlider.getValue()));
        return p;
    }

    @Override
    public BufferedImage generateNoise(Parameters param) {
        int res = getRedraw().getRes();
        int cells = (int) param.get(PARAM_FREQ, 10);
        double mix = param.get(PARAM_GUAS, 40) / 100.0;
        long seed = getSeed();
        return CellNoise.generateCellNoise(res, cells, mix, new Random(seed));
    }

     @Override
    public String getDescription() {
        return "CellNoise: cells=" + frequencyField.getText()
             + ", gaussian=" + gaussianSlider.getValue() + "%, seed=" + getSeed();
    }

    @Override
    public String getTitle() {
        return "Cell Noise";
    }
}

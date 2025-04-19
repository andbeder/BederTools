package com.beder.texture.noise;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;
import com.beder.texturearchive.Voronoi;

/**
 * Generates a true Voronoi noise image with toroidal wrapping.
 * The number of seed points and seed are user-configurable.
 */
public class VoronoiNoiseGenerator extends NoiseOperation {
    private final JPanel optionsPanel;
    private final JTextField pointsField;
    private final static String PARAM_POINTS = "Points";

    public VoronoiNoiseGenerator(Redrawable redraw) {
        super(redraw);

        optionsPanel = new JPanel(new FlowLayout());

        optionsPanel.add(new JLabel("Points:"));
        pointsField = new JTextField("20", 6);
        optionsPanel.add(pointsField);

        addSeedConfig(optionsPanel);
    }

    @Override
    public JPanel getConfig() {
        return optionsPanel;
    }

    @Override
    public Parameters getUIParameters() {
        Parameters p = new Parameters();
        p.put("Points", pointsField.getText());
        return p;
    }

    @Override
    public BufferedImage generateNoise(Parameters param) {
        int res = getRedraw().getRes();
        int points = (int) param.get(PARAM_POINTS, 20);
        long seed = getSeed();
        return Voronoi.generateVoronoi(res, points, new Random(seed));
    }

    @Override
    public String getDescription() {
        return "Voronoi: points=" + pointsField.getText() + ", seed=" + getSeed();
    }

    @Override
    public String getTitle() {
        return "Voronoi";
    }
}

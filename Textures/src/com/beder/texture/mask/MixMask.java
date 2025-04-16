package com.beder.texture.mask;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;
import com.beder.texturearchive.Operations;

public class MixMask extends Operation {
	private double mixPercent;
	private JSlider mixSlider;
	private JPanel mixPanel;
	private JTextField mixValueField;
	private JPanel tileParamPanel;
	private JLabel tileScaleLabel;
	private JLabel tileScaleValue;
	
	public MixMask(int res) {
		super(res);
		mixPercent = 50;
		
		mixPanel = new JPanel(new FlowLayout());

        mixSlider = new JSlider(0, 100, 50);  // Default to 50%
        mixSlider.setMajorTickSpacing(20);
        mixSlider.setMinorTickSpacing(5);
        mixSlider.setPaintTicks(true);
        mixSlider.setPaintLabels(true);
        mixSlider.setPreferredSize(new Dimension(300, 100));
        mixSlider.addChangeListener(e -> {
			double percent = mixSlider.getValue();
			String pctStr = percent + "";
			mixValueField.setText(pctStr);				
        });
        mixPanel.add(new JLabel("Mix %:"));
        mixValueField = new JTextField("50", 6);
        mixValueField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
                try {
                    double mixPct = Double.parseDouble(mixValueField.getText());
                    if (mixPct < 0 || mixPct > 100) {
                        throw new NumberFormatException("Range");
                    }
                    mixPercent = mixPct / 100.0;
                 } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mixPanel,
                        "Please enter a valid number between 0 and 100.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                }
			}
			public void focusGained(FocusEvent e) {}
		});
        mixPanel.add(mixValueField);
		mixPanel.add(mixSlider);

		// Tile Controls
		tileParamPanel = new JPanel(new FlowLayout());
		tileScaleLabel = new JLabel("Scale:");
		tileParamPanel.add(tileScaleLabel);
		tileScaleValue = new JLabel(mixValueField.getText());
		tileParamPanel.add(tileScaleValue);
	
	}
	

	
	@Override
	public JPanel getConfig() {
       return  mixPanel;
	}

	@Override
	public ImagePair doApply(ImagePair pair) {
		BufferedImage mixImage = pair.left;
		BufferedImage input = pair.right;
		
        if (input == null) {
            // If there's no input, return a copy of mixImage
            return pair;
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
        pair.right = out;
        return pair;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getTilePanel() {
		return tileParamPanel;
	}

	@Override
	public String getTitle() {
		return "Mix";
	}




}

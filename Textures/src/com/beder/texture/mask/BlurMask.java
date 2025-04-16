package com.beder.texture.mask;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;

public class BlurMask extends Operation {
	private int radius;
	private JPanel blurOptionsPane;
	private JTextField blurRadiusField;
	private JPanel blurTilePanel;
	
	public BlurMask(int res, int radius) {
		super(res);
		this.radius = radius;
	}

	@Override
	public JPanel getConfig() {
        blurOptionsPane = new JPanel(new FlowLayout());
        blurOptionsPane.setBorder(BorderFactory.createTitledBorder("Perlin Options"));
        blurOptionsPane.add(new JLabel("Radius:"));
        blurRadiusField = new JTextField("4.0", 6);
        blurOptionsPane.add(blurRadiusField);

        return blurOptionsPane;
	}

	@Override
	public JPanel getOperationTile() {
		if (blurTilePanel == null) {
			blurTilePanel = new JPanel(new FlowLayout());
			blurTilePanel.setBorder(BorderFactory.createEtchedBorder());
			blurTilePanel.setLayout(new BorderLayout());
			blurTilePanel.add(new JLabel("Simplex"), BorderLayout.CENTER);
			JPanel tileParamPanel = new JPanel(new FlowLayout());
			blurTilePanel.add(tileParamPanel, BorderLayout.SOUTH);
			JLabel tileRaduisParam = new JLabel("Radius:");
			tileParamPanel.add(tileRaduisParam);
			JLabel tileRadiusValue = new JLabel(blurRadiusField.getText());
			tileParamPanel.add(tileRadiusValue);
		}
		return blurTilePanel;
	}
	
	
	@Override
	public ImagePair apply(ImagePair pair) {
		BufferedImage src = pair.right;
		
        if (radius < 1) return pair;
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
        pair.right = dst;
        return pair;
    }

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}

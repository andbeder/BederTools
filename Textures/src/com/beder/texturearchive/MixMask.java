package com.beder.texturearchive;

import com.beder.texture.ImagePair;
import com.beder.texture.Operation;
import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;

import java.awt.image.BufferedImage;

public class MixMask extends Operation {
    public MixMask(Redrawable redraw) {
        super(redraw);
        addParameter("Ratio", CONTROL_TYPE.DOUBLE, 0.5);
    }

    @Override
    public ImagePair executeOperation(ImagePair pair, Parameters par) {
        double ratio = par.get("Ratio", 0.5);
        int w = pair.left.getWidth();
        int h = pair.left.getHeight();
        BufferedImage out = new BufferedImage(w, h, pair.left.getType());
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int leftRGB = pair.left.getRGB(x, y);
                int rightRGB = pair.right.getRGB(x, y);
                int la = (leftRGB >>> 24) & 0xFF;
                int lr = (leftRGB >>> 16) & 0xFF;
                int lg = (leftRGB >>> 8) & 0xFF;
                int lb = leftRGB & 0xFF;
                int ra = (rightRGB >>> 24) & 0xFF;
                int rr = (rightRGB >>> 16) & 0xFF;
                int rg = (rightRGB >>> 8) & 0xFF;
                int rb = rightRGB & 0xFF;
                int oa = (int) (la * (1 - ratio) + ra * ratio);
                int orr = (int) (lr * (1 - ratio) + rr * ratio);
                int og = (int) (lg * (1 - ratio) + rg * ratio);
                int ob = (int) (lb * (1 - ratio) + rb * ratio);
                int rgb = (oa << 24) | (orr << 16) | (og << 8) | ob;
                out.setRGB(x, y, rgb);
            }
        }
        pair.right = out;
        return pair;
    }

    @Override
    public String getDescription() {
        return "Mix: blend left and right images";
    }

    @Override
    public String getTitle() {
        return "Mix";
    }
}

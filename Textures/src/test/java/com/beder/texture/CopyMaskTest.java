package com.beder.texture;

import com.beder.texture.mask.CopyMask;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

public class CopyMaskTest {
    private Redrawable dummy = new Redrawable() {
        public void applyImage(ImagePair pair) {}
        public int getRes() { return 0; }
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    };

    @Test
    public void copyCopiesLeftToRight() {
        CopyMask op = new CopyMask(dummy);
        ImagePair pair = new ImagePair(4);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                pair.left.setRGB(x, y, Color.RED.getRGB());
                pair.right.setRGB(x, y, Color.BLUE.getRGB());
            }
        }
        op.executeOperation(pair, new Parameters());
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                assertEquals(pair.left.getRGB(x, y), pair.right.getRGB(x, y));
            }
        }
    }
}

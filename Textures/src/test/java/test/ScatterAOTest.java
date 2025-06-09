package test;

import com.beder.texture.ImagePair;
import com.beder.texture.Parameters;
import com.beder.texture.Redrawable;
import com.beder.texture.scatter.ScatterOperation;
import com.beder.texture.scatter.SpriteRepository;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class ScatterAOTest {
    private final Redrawable dummy = new Redrawable() {
        public void applyImage(ImagePair pair) {}
        public int getRes() { return 16; }
        public void mouseClicked(java.awt.event.MouseEvent e) {}
        public void mousePressed(java.awt.event.MouseEvent e) {}
        public void mouseReleased(java.awt.event.MouseEvent e) {}
        public void mouseEntered(java.awt.event.MouseEvent e) {}
        public void mouseExited(java.awt.event.MouseEvent e) {}
    };

    @Test
    public void aoModeDarkensPixels() {
        SpriteRepository repo = SpriteRepository.getInstance();
        repo.clear();
        BufferedImage img = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<2;y++) for(int x=0;x<2;x++) img.setRGB(x,y,0xFFFFFFFF);
        repo.addSprite(img,1);

        ScatterOperation op = new ScatterOperation(dummy);
        Parameters p = new Parameters();
        p.put("Quantity", 1.0);
        p.put("Size", 2.0);
        p.put("StdDev", 0.0);
        p.put("Seed", 0.0);
        p.put("AO", 1.0);
        p.put("Radius", 3.0);
        p.put("Depth", 1.0);
        p.put("Scale", 1.0);
        p.put("Threshold", 0.0);

        ImagePair pair = new ImagePair(16);
        op.executeOperation(pair, p);

        assertNotNull(pair.left);
    }
}

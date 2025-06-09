package test;

import com.beder.texture.TextureGenius;
import com.beder.texture.TextureGUI;
import com.beder.texture.ImagePair;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ExportImageTest {
    @Test
    public void exportWritesFiles() throws Exception {
        TextureGenius genius = new TextureGenius(4);
        TextureGUI gui = new TextureGUI(genius);
        ImagePair pair = genius.getCurrentImage();
        pair.left.setRGB(0, 0, 0xFF00FF00);
        pair.right.setRGB(0, 0, 0xFFFF0000);

        File base = File.createTempFile("tex", ".png");
        base.deleteOnExit();
        gui.exportCurrentImage(base);

        String name = base.getName().replaceFirst("(\\.[^.]+)?$", "");
        File left = new File(base.getParentFile(), name + "_left.png");
        File right = new File(base.getParentFile(), name + "_right.png");

        assertFalse(left.exists());
        assertTrue(right.exists());

        BufferedImage rimg = ImageIO.read(right);
        assertEquals(0xFFFF0000, rimg.getRGB(0, 0));

        right.delete();
    }
}

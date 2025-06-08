package com.beder.util;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ImageSplitterTest {
    @Test
    public void splitIntoFourProducesQuadrants() {
        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> parts = ImageSplitter.splitIntoFour(img);
        assertEquals(4, parts.size());
        assertEquals(4, parts.get(0).getWidth());
        assertEquals(3, parts.get(0).getHeight());
        assertEquals(4, parts.get(3).getWidth());
        assertEquals(3, parts.get(3).getHeight());
    }
}

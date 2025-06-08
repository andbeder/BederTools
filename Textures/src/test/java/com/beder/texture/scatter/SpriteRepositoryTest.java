package com.beder.texture.scatter;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SpriteRepositoryTest {
    @Test
    public void testLastDirectorySetAndGet() {
        SpriteRepository repo = SpriteRepository.getInstance();
        File dir = new File(System.getProperty("java.io.tmpdir"));
        repo.setLastDirectory(dir);
        assertEquals(dir, repo.getLastDirectory());
    }

    @Test
    public void testAddSpriteIncreasesCount() {
        SpriteRepository repo = SpriteRepository.getInstance();
        repo.clear();
        BufferedImage img = new BufferedImage(2,2,BufferedImage.TYPE_INT_ARGB);
        repo.addSprite(img, 1);
        assertEquals(1, repo.getCount());
    }
}

package com.beder.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Utility for splitting images into equally sized parts. */
public class ImageSplitter {
    /**
     * Splits the given image into four equally sized rectangles.
     *
     * @param img Source image
     * @return list containing the four quadrants in order: top-left, top-right, bottom-left, bottom-right
     */
    public static List<BufferedImage> splitIntoFour(BufferedImage img) {
        int w = img.getWidth() / 2;
        int h = img.getHeight() / 2;
        List<BufferedImage> parts = new ArrayList<>(4);
        parts.add(img.getSubimage(0, 0, w, h));
        parts.add(img.getSubimage(w, 0, img.getWidth() - w, h));
        parts.add(img.getSubimage(0, h, w, img.getHeight() - h));
        parts.add(img.getSubimage(w, h, img.getWidth() - w, img.getHeight() - h));
        return parts;
    }
}

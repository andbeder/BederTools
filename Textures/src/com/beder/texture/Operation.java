package com.beder.texture;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class Operation implements Comparable<Operation> {
	private Parameters param;
	private Redrawable redraw;
	
	public Operation(Redrawable redraw){
		this.redraw = redraw;
	}
	

	/**
     * Applies this operation to the given input image pair and returns same pair for further operations to be applied to it.
     */
	public abstract ImagePair executeOperation(ImagePair input, Parameters par);

	/****
	 * @return the current set of values chosen as parameters for this operation
	 */
	public abstract Parameters getUIParameters();

	/**
     * Returns a textual description of the operation and its parameters.
     */
	public abstract String getDescription();
    
    /**
     * Applies this operation to the given input image and returns a new image.
     */
	public abstract JPanel getConfig();

	
	public abstract String getTitle();
    
    /**
     * Called by parent GUI to show Swing controls for configuration parameters for that specific operation
     */

    /**
     * Applies this operation to the given input image and returns a new image.
     */
    public BufferedImage copyOf(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        java.awt.Graphics g = copy.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

	@Override
	public int compareTo(Operation o) {
		int hash1 = System.identityHashCode(this);
		int hash2 = System.identityHashCode(o);
		return hash1 - hash2;
	}

	public Redrawable getRedraw() {
		return redraw;
	}
	
	
}

package com.beder.texture;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class Operation implements Comparable<Operation> {
	private ImagePair input;
	private ImagePair output;
	public JPanel configurePanel;
	public int res;
	private JPanel tilePanel;
	
	public Operation(int res){
		configurePanel = null;
		this.res = res;
		this.input = null;
	}
	
    /**
     * Returns a Panel with a set of labels for display in the operation stack.
     */
	protected abstract JPanel getTilePanel();
	
	public JPanel getTile() {
		if (tilePanel == null) {
			tilePanel = new JPanel(new FlowLayout());
			tilePanel.setBorder(BorderFactory.createTitledBorder(getTitle()));
			tilePanel.add(getTilePanel());
		}
		return tilePanel;
	}
	

	/**
     * Applies this operation to the given input image pair and returns same pair for further operations to be applied to it.
     */
	public abstract ImagePair doApply(ImagePair input);

	public ImagePair apply(ImagePair input) {
		this.input = input.copy();
		this.output = doApply(input);
		this.output = output.copy();
		return output;
	}

	
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
    public final void addControls(JPanel parent) {
    	parent.removeAll();
    	if (configurePanel == null) {
    		configurePanel = getConfig();
    	}
    	parent.add(configurePanel);
    }

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

	public ImagePair getInput() {
		return input;
	}

	public void setInput(ImagePair input) {
		this.input = input;
	}

	public ImagePair getOutput() {
		return output;
	}

	public void setOutput(ImagePair output) {
		this.output = output;
	}
}

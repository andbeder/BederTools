package com.beder.texture;

import java.awt.FlowLayout;
import java.awt.event.MouseListener;

import javax.security.auth.Refreshable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Layer {
	private ImagePair input;
	private ImagePair output;
	private Operation op;
	private Parameters param;
	private JPanel tilePanel;
	
	public Layer(Operation op) {
		this.op = op;
		
		tilePanel = new JPanel(new FlowLayout());
		tilePanel.setBorder(BorderFactory.createTitledBorder(op.getTitle()));
		for (String key : param.keySet()) {
			tilePanel.add(new JLabel(key));
			tilePanel.add(new JLabel(Double.toString(param.get(key, 0))));
		}
	}

	public Layer(Operation op, ImagePair input) {
		this(op);
		this.input = input;
	}
	
	public ImagePair apply(ImagePair input) {
		this.input = input.copy();
		param = op.getUIParameters();
		ImagePair out = op.executeOperation(input, param);
		output = out.copy();
		return out;
	}
	
	public JPanel getTilePanel() {
		return tilePanel;
	}

	public ImagePair getInput() {
		return input;
	}

	public ImagePair getOutput() {
		return output;
	}
	
	
}

package com.beder.texture;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class LayerStack {
	private ArrayList<Layer> stack;
	int curPtr;
	private JPanel stackPanel;
	private Main main;
	
	public LayerStack(Main main){
		this.main = main;
		stack = new ArrayList<Layer>();
		curPtr = -1;
		stackPanel = new JPanel(new MigLayout("wrap 1, fillx", "grow"));;
	}
	
	public JPanel getStackPanel() {
		return stackPanel;
	}
	
	/***
	 *  Rebuilds all of the stack tiles on the UI
	 */
	private void buildStackPanel() {
		stackPanel.removeAll();
		for (Layer l : stack) {
			stackPanel.add(l.getTilePanel());
		}
		main.frame.pack();
	}

	/*****
	 * Adds a new operation AFTER the currentPtr.
	 *   Rebuilds the stackPanel
	 *   DOES NOT "apply"
	 * @param op
	 */

	public Layer add(Operation op) {
		ImagePair input = (curPtr == -1) ? new ImagePair(main.res) : stack.get(curPtr).getOutput();
		Layer l = new Layer(op, input);
		stack.add(++curPtr, l);
		buildStackPanel();
		main.applyImage(input);
		return l;
	}

	public Layer getCurrent() {
		return stack.get(curPtr);
	}	
}

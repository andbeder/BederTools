package com.beder.texture;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class OperationStack {
	Vector<Operation> stack;
	int curPtr;
	
	private JPanel stackPanel;
	//private Map<JPanel, Operation> panelMap;
	private Vector<ImagePair> imageVector; // Vector of images RESULTING from each step;
	private Vector<JPanel> panelVector;
	private Main main;
	
	public OperationStack(Main main){
		this.main = main;
		stack = new Vector<Operation>();
		//panelMap = new TreeMap<JPanel, Operation>();
		imageVector = new Vector<ImagePair>();
		panelVector = new Vector<JPanel>();
		curPtr = -1;
		stackPanel = null;
	}
	
	public JPanel getStackTiles() {
		if (stackPanel == null) {
			stackPanel = new JPanel(new MigLayout("wrap 1, fillx", "grow"));
			buildStackPanel();
		}
		return stackPanel;
	}
	
	/***
	 *  Adds all then tiles into the stack UI
	 */
	private void buildStackPanel() {
		stackPanel.removeAll();
		panelVector.clear();
		//panelMap.clear();
		for (Operation op : stack) {
			JPanel tile = op.getTile();
			tile.addMouseListener(main);
			stackPanel.add(tile, "grow");
			panelVector.add(tile);
			//panelMap.put(tile, op);
		}
		main.frame.pack();
	}

	/*****
	 * Applies the operations in the stack up to and including the current operations.
	 *   Saves the ImagePairs associated with each operation
	 * @param pair
	 * @return
	 */

	public ImagePair apply(ImagePair pair) {
		imageVector.clear();
		for (int i = 0;i <= curPtr;i++) {
			pair = stack.elementAt(i).apply(pair);
			imageVector.add(new ImagePair(pair));
		}
		return pair;
	}	
	
	/*****
	 * Adds a new operation AFTER the currentPtr.
	 *   Rebuilds the stackPanel
	 *   DOES NOT "apply"
	 * @param op
	 */

	public void add(Operation op) {
		ImagePair thisImage = null;
		try {
			thisImage = imageVector.elementAt(curPtr);
		} catch (ArrayIndexOutOfBoundsException e) {
			thisImage = new ImagePair(main.res);
		}
		curPtr ++;
		stack.add(curPtr, op);
		op.setInput(thisImage);
		imageVector.add(curPtr, thisImage);
		buildStackPanel();
		//apply(new ImagePair(main.res));
	}
	
	/*****
	 * Gets the ImagePair saved for this particular Operation
	 */
	public ImagePair getImage(Operation o) {
		int i = getIndex(o);
		if (i >= 0) {
			return imageVector.elementAt(i);
		} else {
			return new ImagePair(main.res);
		}
	}
	public ImagePair getInputImage(Operation o) {
		int i = getIndex(o) - 1;
		if (i >= 0) {
			return imageVector.elementAt(i);
		} else {
			return new ImagePair(main.res);
		}
	}
	public void saveImage(ImagePair pair, Operation o) {
		int i = getIndex(o);
		if (i >= 0) {
			imageVector.set(i, pair);
		}
	}
	
	

	/*****
	 * Gets the ImagePair saved for this particular JPanel
	 */
	public ImagePair getImage(JPanel panel) {
		for (int i = 0;i < panelVector.size();i++) {
			if (panelVector.elementAt(i) == panel) {
				return imageVector.elementAt(i);
			}
		}
		return new ImagePair(main.res);
	}
	
	public void setCurrent(Operation op) {
		for (int i = 0;i < stack.size();i++) {
			if (stack.elementAt(i) == op) {
				curPtr = i;
			}
		}
	}
	
	private int getIndex(Operation o) {
		for (int i = 0;i < stack.size();i++) {
			if (stack.elementAt(i) == o) {
				return i;
			}
		}
		return -1;
	}

	/*****
	 * Gets the Operation saved for this particular JPanel
	 */
	public Operation getOperation(Component panel) {
		for (int i = 0;i < panelVector.size();i++) {
			if (panelVector.elementAt(i) == panel) {
				return stack.elementAt(i);
			}
		}
		return null;
	}	

	public Operation getCurrent() {
		return stack.elementAt(curPtr);
	}	
}

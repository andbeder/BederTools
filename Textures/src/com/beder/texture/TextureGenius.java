package com.beder.texture;

import javax.swing.JPanel;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * TextureGenius handles all control and logic: managing the operation stack,
 * current image state, and dirty/save/apply workflows.
 * It delegates UI rendering to TextureGUI.
 */
public class TextureGenius {
    private final int res;
    private final LayerStack stack;
    private ImagePair curImage;
    private boolean isDirty;
    private java.io.File stackFile;
        private TextureGUI gui;

    public static void main(String[] args) {
        // Initialize logic and launch GUI
        TextureGenius genius = new TextureGenius(1024);
        genius.gui = new TextureGUI(genius);
        genius.gui.init();
    }

    public TextureGenius(int res) {
        this.res = res;
        this.stack = new LayerStack(this);
        this.curImage = new ImagePair(res);
        this.isDirty = false;
    }

    /**
     * Returns the configured resolution.
     */
    public int getRes() {
        return res;
    }

    /**
     * Returns true if there are no unapplied changes.
     */
    public boolean isClean() {
        return !isDirty;
    }

    /**
     * Provides the operations stack panel for embedding in the GUI.
     */
    public JPanel getStackPanel() {
        return stack.getStackPanel();
    }

    /**
     * Retrieves the currently selected operation for configuring its UI.
     */
    public Operation getCurrentOperation() {
        return stack.getCurrent().getOperation();
    }

    /**
     * Adds a new operation to the stack and marks the state as dirty.
     */
    public ImagePair addOperation(Operation op) {
    	Layer current = stack.getCurrent();
		ImagePair input = current == null ? new ImagePair(res) : current.getOutput();
		Layer l = new Layer(op, input);
		stack.add(l);
		stack.buildStackPanel();
		gui.applyImage(input);
        this.curImage = input;
        this.isDirty = true;
        return input;
    }

    /**
     * Applies the current operation (without saving), marking the state dirty.
     */
    public ImagePair applyCurrent() {
	    Layer l = stack.getCurrent();
	    // ← grab the sliders/textfields before we execute
	    Parameters p = l.getOperation().getUIParameters();
	    l.setParam(p);
	    ImagePair output = l.apply(l.getInput());        
	    this.curImage = output;
        this.isDirty = true;
        return output;
    }

    /**
     * Saves (applies permanently) the current operation and clears the dirty flag.
     */
    public ImagePair saveCurrent() {
        Layer l = stack.getCurrent();
        Parameters p = l.getOperation().getUIParameters();
        l.setParam(p);
        stack.buildStackPanel();
        ImagePair output = l.apply(l.getInput());
        this.curImage = output;
        this.isDirty = false;
        return output;
    }
    
    public void newCurrent() {
        Layer l = stack.getCurrent();
        if (l != null && l.getOutput() != null) {
            gui.applyImage(l.getOutput());
            gui.showOptions();
        }
    }

    /**
     * Returns the latest ImagePair to display.
     */
    public ImagePair getCurrentImage() {
        return curImage;
    }

    /** Reset the stack and images to a blank state. */
    public void reset() {
        stack.clear();
        curImage = new ImagePair(res);
        isDirty = false;
        stackFile = null;
        gui.applyImage(curImage);
    }

    /** Returns the file used for stack persistence. */
    public java.io.File getStackFile() { return stackFile; }

    /** Save the current operation stack to the given file in JSON format. */
    public void saveStack(java.io.File file) throws IOException {
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Layer l : stack.getLayers()) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("class", l.getOperation().getClass().getName());
            org.json.JSONObject params = new org.json.JSONObject();
            for (java.util.Map.Entry<String,Double> e : l.getParam().entrySet()) {
                params.put(e.getKey(), e.getValue());
            }
            obj.put("params", params);
            arr.put(obj);
        }
        org.json.JSONObject root = new org.json.JSONObject();
        root.put("operations", arr);
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write(root.toString(2));
        }
        stackFile = file;
    }

    /** Load an operation stack from the given JSON file. */
    public void loadStack(java.io.File file) throws Exception {
        reset();
        String txt = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        org.json.JSONObject root = new org.json.JSONObject(txt);
        org.json.JSONArray arr = root.getJSONArray("operations");
        for (int i = 0; i < arr.length(); i++) {
            org.json.JSONObject obj = arr.getJSONObject(i);
            String cls = obj.getString("class");
            java.lang.Class<?> c = java.lang.Class.forName(cls);
            java.lang.reflect.Constructor<?> cons = c.getConstructor(Redrawable.class);
            Operation op = (Operation)cons.newInstance(gui);
            addOperation(op);
            org.json.JSONObject pObj = obj.getJSONObject("params");
            Parameters p = new Parameters();
            for (String key : pObj.keySet()) {
                p.put(key, pObj.getDouble(key));
            }
            stack.getCurrent().setParam(p);
            saveCurrent();
        }
        stackFile = file;
    }

	public TextureGUI getGUI() {
		return gui;
	}
}

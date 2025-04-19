package com.beder.texture;

import com.beder.texture.mask.CopyMask;
import com.beder.texture.noise.CellNoiseGenerator;
import com.beder.texture.noise.PerlinNoiseGenerator;
import com.beder.texture.noise.SimplexNoiseGenerator;
import com.beder.texture.noise.VegetationNoiseGenerator;
import com.beder.texture.noise.VoronoiNoiseGenerator;
import com.beder.texturearchive.MixMask;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * TextureGUI is responsible for building and displaying the Swing UI,
 * and delegates all control/logic operations to TextureGenius.
 */
public class TextureGUI implements Redrawable, MouseListener {
    private final TextureGenius genius;
    private final int res;
    private ImagePair curImage;

    protected JFrame frame;
    private JPanel mainPanel;
    private JPanel imagePanel;
    private JPanel opControlPanel;
    private ImageIcon leftIcon;
    private ImageIcon rightIcon;
    private JButton applyButton;
    private JButton saveButton;

    public TextureGUI(TextureGenius genius) {
        this.genius = genius;
        this.res = genius.getRes();
        this.curImage = genius.getCurrentImage();
    }

    /**
     * Initialize and display the UI.
     */
    public void init() {
        frame = new JFrame("Texture Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel(new BorderLayout());

        // Center: image display with arrows
        imagePanel = new JPanel(new MigLayout("gapx 10px", "", "[center][center]"));
        leftIcon = new ImageIcon(curImage.left.getScaledInstance(512, 512, Image.SCALE_SMOOTH));
        rightIcon = new ImageIcon(curImage.right.getScaledInstance(512, 512, Image.SCALE_SMOOTH));
        imagePanel.add(new JLabel(leftIcon));

        JPanel arrowsPanel = new JPanel();
        arrowsPanel.setLayout(new BoxLayout(arrowsPanel, BoxLayout.Y_AXIS));
        JButton copyButton = new JButton("Copy →");
        JButton mixButton  = new JButton("Mix ↔");
        arrowsPanel.add(copyButton);
        arrowsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        arrowsPanel.add(mixButton);

        imagePanel.add(arrowsPanel);
        imagePanel.add(new JLabel(rightIcon));

        mainPanel.add(imagePanel, BorderLayout.CENTER);

        // East: operations stack panel from genius
        JPanel stackPanel = genius.getStackPanel();
        mainPanel.add(stackPanel, BorderLayout.EAST);

        // South: buttons to add new operations
        JPanel opPanel = new JPanel(new FlowLayout());
        JButton simplexButton   = new JButton("Simplex");
        JButton cellNoiseButton = new JButton("Cell Noise");
        JButton perlinButton = new JButton("Perlin");
        JButton voronoiButton = new JButton("Voronoi");
        JButton vegetationButton = new JButton("Vegetation");
        opPanel.add(simplexButton);
        opPanel.add(cellNoiseButton);
        opPanel.add(perlinButton);
        opPanel.add(voronoiButton);
        opPanel.add(vegetationButton);
        mainPanel.add(opPanel, BorderLayout.SOUTH);

        // North: operation configuration panel
        opControlPanel = new JPanel(new FlowLayout());
        applyButton = new JButton("Apply");
        saveButton  = new JButton("Save");
        mainPanel.add(opControlPanel, BorderLayout.NORTH);

        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // --- Action Listeners ---
        saveButton.addActionListener(e -> {
            ImagePair img = genius.saveCurrent();
            applyImage(img);
        });
        applyButton.addActionListener(e -> {
            ImagePair img = genius.applyCurrent();
            applyImage(img);
        });

        simplexButton.addActionListener(e -> {
        	addOperation(new SimplexNoiseGenerator(this));
        });
        cellNoiseButton.addActionListener(e -> {
        	addOperation(new SimplexNoiseGenerator(this));
        });
        perlinButton.addActionListener(e -> {
        	addOperation(new PerlinNoiseGenerator(this));
        });
        voronoiButton.addActionListener(e -> {
        	addOperation(new VoronoiNoiseGenerator(this));
        });
        vegetationButton.addActionListener(e -> {
        	addOperation(new VegetationNoiseGenerator(this));
        });
        copyButton.addActionListener(e -> {
        	addOperation(new CopyMask(this));
        });
        mixButton.addActionListener(e -> {
        	addOperation(new MixMask(this));
        });
    }
    
    /****
     * Called by button action listeners to create a new operation (layer)
     * @param o
     */
    private void addOperation(Operation op) {
        if (genius.isClean()) {
            ImagePair img = genius.addOperation(op);
            applyImage(img);
            showOptions();
            frame.repaint();
        }
    }

    /**
     * Display the configuration controls for the currently selected operation.
     */
    private void showOptions() {
        opControlPanel.removeAll();
        Operation op = genius.getCurrentOperation();
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder(op.getTitle() + " Options"));
        controlPanel.add(op.getConfig());
        controlPanel.add(applyButton);
        opControlPanel.add(controlPanel);
        opControlPanel.add(saveButton);
        opControlPanel.revalidate();
        opControlPanel.repaint();
    }

    /**
     * Update the displayed images based on the given ImagePair.
     */
    @Override
    public void applyImage(ImagePair current) {
        this.curImage = current;
        leftIcon.setImage(current.left.getScaledInstance(512, 512, Image.SCALE_SMOOTH));
        rightIcon.setImage(current.right.getScaledInstance(512, 512, Image.SCALE_SMOOTH));
        imagePanel.repaint();
    }

    @Override
    public int getRes() {
        return res;
    }

    // --- MouseListener stubs (not used directly) ---
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}

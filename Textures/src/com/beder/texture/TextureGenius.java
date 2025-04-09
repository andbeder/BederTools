package com.beder.texture;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class TextureGenius extends JFrame {

    // Top controls
    private JTextField resolutionField;
    private JTextField seedField;
    private JButton randomSeedButton;
    private JComboBox<String> algorithmComboBox;

    // Option panels for each algorithm.
    private JPanel cardPanel;
    private JPanel cellNoiseOptionsPanel;
    private JPanel vegetationOptionsPanel;
    private JPanel voronoiOptionsPanel;
    private JPanel simplexOptionsPanel;
    private JPanel perlinOptionsPanel;

    // Cell Noise controls.
    private JTextField frequencyField;
    private JSlider gaussianSlider;
    private JButton haltButton;
    
    // Vegetation controls.
    private JTextField sizeField;
    private JTextField regularityField;
    private JSlider spreadSlider;
    
    // Voronoi controls.
    private JTextField pointsField; // Number of seed points

    // Simplex controls.
    private JTextField simplexScaleField;  // Scale factor for Simplex noise

    // Perlin controls.
    private JTextField perlinFreqField; // Base frequency for Perlin noise.
    private JTextField perlinIterField; // Number of iterations/octaves.
    
    // Common action buttons.
    private JButton generateButton;  // "Generate"
    private JButton blurButton;
    private JButton levelButton;
    private JButton saveButton;      // Save button (right aligned)
     
    // Arrow buttons for copying/mixing images.
    private JButton copyButton;
    private JButton mixButton;

    // Additional "Apply Stack" button for the operations stack.
    private JButton applyStackButton;

    // Image labels.
    private JLabel leftImageLabel;
    private JLabel rightImageLabel;
    
    // The full-resolution images.
    private BufferedImage currentImage; // generated image (left)
    private BufferedImage rightImage;   // right image (initially black)

    // Operations stack UI and list.
    private JPanel operationsStackPanel;  // Displays a vertical list of operation panels.
    private List<Operations.Operation> opStack = new ArrayList<>();

    // Halt flag.
    public static volatile boolean haltRequested = false;

    public TextureGenius(int defaultRes, int defaultCells) {
        super("Texture Genius");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Top Controls Panel ---
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Resolution:"));
        resolutionField = new JTextField(String.valueOf(defaultRes), 6);
        topPanel.add(resolutionField);

        topPanel.add(new JLabel("Seed:"));
        seedField = new JTextField(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)), 8);
        topPanel.add(seedField);

        randomSeedButton = new JButton("Random");
        randomSeedButton.addActionListener(e -> {
            String newSeed = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
            seedField.setText(newSeed);
        });
        topPanel.add(randomSeedButton);

        topPanel.add(new JLabel("Algorithm:"));
        algorithmComboBox = new JComboBox<>(new String[] { "Cell Noise", "Vegetation", "Voronoi", "Simplex", "Perlin" });
        topPanel.add(algorithmComboBox);
        add(topPanel, BorderLayout.NORTH);

        // --- Option Panels using CardLayout ---
        cardPanel = new JPanel(new CardLayout());
        
        // Cell Noise Options Panel.
        cellNoiseOptionsPanel = new JPanel(new FlowLayout());
        cellNoiseOptionsPanel.setBorder(BorderFactory.createTitledBorder("Cell Noise Options"));
        cellNoiseOptionsPanel.add(new JLabel("Frequency:"));
        frequencyField = new JTextField(String.valueOf(defaultCells), 6);
        cellNoiseOptionsPanel.add(frequencyField);
        cellNoiseOptionsPanel.add(new JLabel("Gaussian (%):"));
        gaussianSlider = new JSlider(0, 100, 40);  // Default to 40%
        gaussianSlider.setMajorTickSpacing(20);
        gaussianSlider.setMinorTickSpacing(5);
        gaussianSlider.setPaintTicks(true);
        gaussianSlider.setPaintLabels(true);
        gaussianSlider.setPreferredSize(new Dimension(300, 100));
        cellNoiseOptionsPanel.add(gaussianSlider);
        haltButton = new JButton("Halt");
        cellNoiseOptionsPanel.add(haltButton);
        
        // Vegetation Options Panel.
        vegetationOptionsPanel = new JPanel(new FlowLayout());
        vegetationOptionsPanel.setBorder(BorderFactory.createTitledBorder("Vegetation Options"));
        vegetationOptionsPanel.add(new JLabel("Size:"));
        sizeField = new JTextField("200", 6);
        vegetationOptionsPanel.add(sizeField);
        vegetationOptionsPanel.add(new JLabel("Regularity (%):"));
        regularityField = new JTextField("80", 6);
        vegetationOptionsPanel.add(regularityField);
        vegetationOptionsPanel.add(new JLabel("Spread:"));
        spreadSlider = new JSlider(0, 100, 50);
        spreadSlider.setMajorTickSpacing(20);
        spreadSlider.setMinorTickSpacing(5);
        spreadSlider.setPaintTicks(true);
        spreadSlider.setPaintLabels(true);
        spreadSlider.setPreferredSize(new Dimension(300, 100));
        vegetationOptionsPanel.add(spreadSlider);
        
        // Voronoi Options Panel.
        voronoiOptionsPanel = new JPanel(new FlowLayout());
        voronoiOptionsPanel.setBorder(BorderFactory.createTitledBorder("Voronoi Options"));
        voronoiOptionsPanel.add(new JLabel("Points:"));
        pointsField = new JTextField("50", 6);
        voronoiOptionsPanel.add(pointsField);
        
        // Simplex Options Panel.
        simplexOptionsPanel = new JPanel(new FlowLayout());
        simplexOptionsPanel.setBorder(BorderFactory.createTitledBorder("Simplex Options"));
        simplexOptionsPanel.add(new JLabel("Scale:"));
        simplexScaleField = new JTextField("200", 6);
        simplexOptionsPanel.add(simplexScaleField);
        
        // Perlin Options Panel.
        perlinOptionsPanel = new JPanel(new FlowLayout());
        perlinOptionsPanel.setBorder(BorderFactory.createTitledBorder("Perlin Options"));
        perlinOptionsPanel.add(new JLabel("Frequency:"));
        perlinFreqField = new JTextField("4.0", 6);
        perlinOptionsPanel.add(perlinFreqField);
        perlinOptionsPanel.add(new JLabel("Iterations:"));
        perlinIterField = new JTextField("4", 6);
        perlinOptionsPanel.add(perlinIterField);
        
        cardPanel.add(cellNoiseOptionsPanel, "Cell Noise");
        cardPanel.add(vegetationOptionsPanel, "Vegetation");
        cardPanel.add(voronoiOptionsPanel, "Voronoi");
        cardPanel.add(simplexOptionsPanel, "Simplex");
        cardPanel.add(perlinOptionsPanel, "Perlin");
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, "Cell Noise");

        // --- Bottom Action Controls ---
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        generateButton = new JButton("Generate");
        actionPanel.add(generateButton);
        actionPanel.add(Box.createHorizontalStrut(10));
        blurButton = new JButton("Blur");
        actionPanel.add(blurButton);
        actionPanel.add(Box.createHorizontalStrut(10));
        levelButton = new JButton("Level");
        actionPanel.add(levelButton);
        actionPanel.add(Box.createHorizontalGlue());
        saveButton = new JButton("Save");
        actionPanel.add(saveButton);

        // --- Operations Stack Panel (East) ---
        operationsStackPanel = new JPanel();
        operationsStackPanel.setLayout(new BoxLayout(operationsStackPanel, BoxLayout.Y_AXIS));
        operationsStackPanel.setBorder(BorderFactory.createTitledBorder("Operations Stack"));
        applyStackButton = new JButton("Apply Stack");
        applyStackButton.addActionListener(e -> applyOperationsStack());
        operationsStackPanel.add(applyStackButton);
        JScrollPane opsScroll = new JScrollPane(operationsStackPanel);
        opsScroll.setPreferredSize(new Dimension(250, 512));

        // --- Center: Two Image Windows and Arrow Buttons ---
        // Left image panel (without scrollbars).
        JPanel leftPanel = new JPanel();
        leftImageLabel = new JLabel();
        leftPanel.add(leftImageLabel);
        leftPanel.setPreferredSize(new Dimension(512, 512));
        
        // Right image panel.
        JPanel rightPanel = new JPanel();
        rightImageLabel = new JLabel();
        rightImage = new BufferedImage(defaultRes, defaultRes, BufferedImage.TYPE_INT_ARGB);
        Graphics2D initGfx = rightImage.createGraphics();
        initGfx.setColor(Color.BLACK);
        initGfx.fillRect(0, 0, defaultRes, defaultRes);
        initGfx.dispose();
        rightImageLabel.setIcon(new ImageIcon(rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH)));
        rightPanel.add(rightImageLabel);
        rightPanel.setPreferredSize(new Dimension(512, 512));
        
        // Arrow buttons panel.
        JPanel arrowsPanel = new JPanel();
        arrowsPanel.setLayout(new BoxLayout(arrowsPanel, BoxLayout.Y_AXIS));
        copyButton = new JButton("Copy →");
        mixButton = new JButton("Mix ↔");
        arrowsPanel.add(copyButton);
        arrowsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        arrowsPanel.add(mixButton);
        
        // Combine left image, arrows, and right image.
        JPanel imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.X_AXIS));
        imagesPanel.add(leftPanel);
        imagesPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        imagesPanel.add(arrowsPanel);
        imagesPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        imagesPanel.add(rightPanel);
        
        // --- Main Controls Panel ---
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(cardPanel, BorderLayout.NORTH);
        controlsPanel.add(actionPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(controlsPanel, BorderLayout.SOUTH);
        add(imagesPanel, BorderLayout.CENTER);
        add(opsScroll, BorderLayout.EAST);

        // --- Listeners ---
        algorithmComboBox.addActionListener(e -> {
            String alg = (String) algorithmComboBox.getSelectedItem();
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, alg);
            pack();
        });
        
        generateButton.addActionListener(e -> {
            haltRequested = false;
            try {
                int res = Integer.parseInt(resolutionField.getText());
                long seed = Long.parseLong(seedField.getText());
                String alg = (String) algorithmComboBox.getSelectedItem();
                Operations.Operation op = null;
                if ("Cell Noise".equals(alg)) {
                    int cells = Integer.parseInt(frequencyField.getText());
                    int gauss = gaussianSlider.getValue();
                    double gaussMix = Math.max(0, Math.min(gauss, 100)) / 100.0;
                    currentImage = CellNoise.generateCellNoise(res, cells, gaussMix);
                    op = new Operations.CellNoiseOperation(res, seed, cells, gauss);
                } else if ("Vegetation".equals(alg)) {
                    double size = Double.parseDouble(sizeField.getText());
                    double reg = Double.parseDouble(regularityField.getText());
                    int spread = Integer.parseInt(spreadSlider.getValue() + "");
                    currentImage = Vegetation.generateVegetation(res, size, reg, spread);
                    op = new Operations.VegetationOperation(res, seed, size, reg, spread);
                } else if ("Voronoi".equals(alg)) {
                    int points = Integer.parseInt(pointsField.getText());
                    currentImage = Voronoi.generateVoronoi(res, points);
                    op = new Operations.VoronoiOperation(res, seed, points);
                } else if ("Simplex".equals(alg)) {
                    double scale = Double.parseDouble(simplexScaleField.getText());
                    currentImage = Simplex.generateSimplexNoise(res, scale);
                    op = new Operations.SimplexOperation(res, seed, scale);
                } else if ("Perlin".equals(alg)) {
                    double freq = Double.parseDouble(perlinFreqField.getText());
                    int iterations = Integer.parseInt(perlinIterField.getText());
                    currentImage = Perlin.generatePerlinNoise(res, freq, iterations);
                    op = new Operations.PerlinOperation(res, seed, freq, iterations);
                }
                Image scaledLeft = currentImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
                leftImageLabel.setIcon(new ImageIcon(scaledLeft));
                leftImageLabel.setPreferredSize(new Dimension(512, 512));
                if (op != null) {
                    opStack.add(op);
                    addOperationPanel(op);
                }
                pack();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numeric values in the controls.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        saveButton.addActionListener(e -> {
            if (rightImage == null) {
                JOptionPane.showMessageDialog(this,
                        "No image available to save. Please copy or mix first.",
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Image");
            int selection = chooser.showSaveDialog(this);
            if (selection == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getParentFile(), file.getName() + ".png");
                }
                try {
                    ImageIO.write(rightImage, "png", file);
                    JOptionPane.showMessageDialog(this,
                            "Image saved successfully.", "Save", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving image: " + ex.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        blurButton.addActionListener(e -> {
            if (rightImage == null) {
                JOptionPane.showMessageDialog(this,
                        "No image available to blur on the right. Please copy or mix first.",
                        "Blur Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter blur radius (in pixels):",
                    "Blur", JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                try {
                    int radius = Integer.parseInt(input);
                    if (radius < 1) {
                        JOptionPane.showMessageDialog(this,
                                "Blur radius must be at least 1.",
                                "Blur Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    rightImage = gaussianBlur(rightImage, radius);
                    Image scaled = rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
                    rightImageLabel.setIcon(new ImageIcon(scaled));
                    pack();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid integer for blur radius.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        copyButton.addActionListener(e -> {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(this,
                        "No image available on the left to copy. Please generate first.",
                        "Copy Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int w = currentImage.getWidth();
            int h = currentImage.getHeight();
            rightImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gRight = rightImage.createGraphics();
            gRight.drawImage(currentImage, 0, 0, null);
            gRight.dispose();
            Image scaled = rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
            rightImageLabel.setIcon(new ImageIcon(scaled));
            pack();
        });
        
        mixButton.addActionListener(e -> {
            if (currentImage == null || rightImage == null) {
                JOptionPane.showMessageDialog(this,
                        "Both left and right images must be available. Please generate and copy first.",
                        "Mix Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this, "Enter mix percentage (0-100):",
                    "Mix", JOptionPane.PLAIN_MESSAGE);
            if (input != null) {
                try {
                    double mixPercent = Double.parseDouble(input);
                    if (mixPercent < 0 || mixPercent > 100) {
                        JOptionPane.showMessageDialog(this,
                                "Mix percentage must be between 0 and 100.",
                                "Mix Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    rightImage = mixImages(currentImage, rightImage, mixPercent / 100.0);
                    Image scaled = rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
                    rightImageLabel.setIcon(new ImageIcon(scaled));
                    pack();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid number for mix percentage.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        levelButton.addActionListener(e -> {
            if (rightImage == null) {
                JOptionPane.showMessageDialog(this,
                        "No image available on the right. Please copy or mix first.",
                        "Level Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            LevelFrame levelFrame = new LevelFrame(copyImage(rightImage));
            levelFrame.setVisible(true);
        });
        
        // --- Initialize left image ---
        try {
            int res = Integer.parseInt(resolutionField.getText());
            int cells = Integer.parseInt(frequencyField.getText());
            int gauss = gaussianSlider.getValue();
            double mixVal = Math.max(0, Math.min(gauss, 100)) / 100.0;
            currentImage = CellNoise.generateCellNoise(res, cells, mixVal);
            Image scaled = currentImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
            leftImageLabel.setIcon(new ImageIcon(scaled));
            leftImageLabel.setPreferredSize(new Dimension(512, 512));
        } catch (NumberFormatException ex) {
            leftImageLabel = new JLabel();
        }
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Returns a deep copy of the given BufferedImage.
     */
    private BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics g = copy.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }
    
    /**
     * Mixes two images of the same dimensions using linear interpolation.
     * @param left The left image.
     * @param right The right image.
     * @param mix A value between 0 and 1: 0 means 100% right; 1 means 100% left.
     * @return A new BufferedImage containing the mixed image.
     */
    private BufferedImage mixImages(BufferedImage left, BufferedImage right, double mix) {
        int w = left.getWidth();
        int h = left.getHeight();
        BufferedImage mixed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgbLeft = left.getRGB(x, y);
                int rgbRight = right.getRGB(x, y);
                int aLeft = (rgbLeft >> 24) & 0xff;
                int rLeft = (rgbLeft >> 16) & 0xff;
                int gLeft = (rgbLeft >> 8) & 0xff;
                int bLeft = rgbLeft & 0xff;
                
                int aRight = (rgbRight >> 24) & 0xff;
                int rRight = (rgbRight >> 16) & 0xff;
                int gRight = (rgbRight >> 8) & 0xff;
                int bRight = rgbRight & 0xff;
                
                int a = (int)(aLeft * mix + aRight * (1 - mix));
                int r = (int)(rLeft * mix + rRight * (1 - mix));
                int g = (int)(gLeft * mix + gRight * (1 - mix));
                int b = (int)(bLeft * mix + bRight * (1 - mix));
                int rgb = (a << 24) | (r << 16) | (g << 8) | b;
                mixed.setRGB(x, y, rgb);
            }
        }
        return mixed;
    }
    
    /**
     * Applies a threshold level to the given image. For each pixel, computes the average
     * intensity (r + g + b)/3 and sets the pixel black if below the threshold; otherwise white.
     * @param src The source image.
     * @param threshold A threshold value between 0 and 255.
     * @return A new BufferedImage with the threshold applied.
     */
    private BufferedImage applyLevel(BufferedImage src, int threshold) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage leveled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int avg = (r + g + b) / 3;
                int color = (avg < threshold) ? 0xFF000000 : 0xFFFFFFFF;
                leveled.setRGB(x, y, color);
            }
        }
        return leveled;
    }
    
    /**
     * Inner class LevelFrame: opens a new frame with a slider (0-255), an Apply button,
     * a Cancel button and a preview window displaying a copy of the given image,
     * with thresholding applied live.
     */
    private class LevelFrame extends JFrame {
        private JSlider thresholdSlider;
        private JButton applyButton;
        private JButton cancelButton;
        private JLabel previewLabel;
        
        public LevelFrame(BufferedImage baseImage) {
            super("Level");
            setLayout(new BorderLayout());
            setSize(600, 600);
            setLocationRelativeTo(TextureGenius.this);
            
            previewLabel = new JLabel(new ImageIcon(baseImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH)));
            previewLabel.setPreferredSize(new Dimension(512, 512));
            add(previewLabel, BorderLayout.CENTER);
            
            JPanel sliderPanel = new JPanel(new FlowLayout());
            thresholdSlider = new JSlider(0, 255, 128);
            thresholdSlider.setMajorTickSpacing(50);
            thresholdSlider.setMinorTickSpacing(10);
            thresholdSlider.setPaintTicks(true);
            thresholdSlider.setPaintLabels(true);
            sliderPanel.add(new JLabel("Threshold:"));
            sliderPanel.add(thresholdSlider);
            add(sliderPanel, BorderLayout.NORTH);
            
            JPanel buttonsPanel = new JPanel(new FlowLayout());
            applyButton = new JButton("Apply");
            cancelButton = new JButton("Cancel");
            buttonsPanel.add(applyButton);
            buttonsPanel.add(cancelButton);
            add(buttonsPanel, BorderLayout.SOUTH);
            
            thresholdSlider.addChangeListener(e -> {
                int thresh = thresholdSlider.getValue();
                BufferedImage newPreview = applyLevel(baseImage, thresh);
                previewLabel.setIcon(new ImageIcon(newPreview.getScaledInstance(512, 512, Image.SCALE_SMOOTH)));
            });
            
            applyButton.addActionListener(e -> {
                int thresh = thresholdSlider.getValue();
                rightImage = applyLevel(baseImage, thresh);
                Image scaled = rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
                rightImageLabel.setIcon(new ImageIcon(scaled));
                dispose();
            });
            
            cancelButton.addActionListener(e -> dispose());
        }
    }
    
    /**
     * Adds an OperationPanel for the given operation to the operations stack panel.
     */
    private void addOperationPanel(Operations.Operation op) {
        OperationPanel opPanel = new OperationPanel(op);
        operationsStackPanel.add(opPanel);
        operationsStackPanel.revalidate();
        operationsStackPanel.repaint();
    }
    
    /**
     * Applies all operations in the stack sequentially, starting with the left image.
     * The final result is displayed in the right image panel.
     */
    private void applyOperationsStack() {
        if (opStack.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No operations in the stack.",
                    "Apply Stack", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        BufferedImage result = copyImage(currentImage);
        for (Operations.Operation op : opStack) {
            result = op.apply(result);
        }
        rightImage = result;
        Image scaled = rightImage.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
        rightImageLabel.setIcon(new ImageIcon(scaled));
        pack();
    }
    
    /**
     * Inner class OperationPanel: displays the operation's details and a Delete button.
     */
    private class OperationPanel extends JPanel {
        private Operations.Operation op;
        private JLabel descriptionLabel;
        private JButton deleteButton;
        
        public OperationPanel(Operations.Operation op) {
            this.op = op;
            setLayout(new BorderLayout());
            descriptionLabel = new JLabel(op.getDescription());
            add(descriptionLabel, BorderLayout.CENTER);
            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> {
                opStack.remove(op);
                operationsStackPanel.remove(this);
                operationsStackPanel.revalidate();
                operationsStackPanel.repaint();
            });
            add(deleteButton, BorderLayout.EAST);
        }
    }
    
    // --- Gaussian Blur Implementation (unchanged) ---
    public static BufferedImage gaussianBlur(BufferedImage src, int radius) {
        if (radius < 1) return src;
        int width = src.getWidth(), height = src.getHeight();
        BufferedImage temp = new BufferedImage(width, height, src.getType());
        BufferedImage dst = new BufferedImage(width, height, src.getType());
        int kernelSize = 2 * radius + 1;
        double[] kernel = new double[kernelSize];
        double sigma = radius / 3.0;
        double sigma22 = 2 * sigma * sigma;
        double sqrtSigmaPi2 = Math.sqrt(Math.PI * sigma22);
        double sum = 0;
        for (int i = -radius; i <= radius; i++) {
            double r = i * i;
            kernel[i + radius] = Math.exp(-r / sigma22) / sqrtSigmaPi2;
            sum += kernel[i + radius];
        }
        for (int i = 0; i < kernelSize; i++) {
            kernel[i] /= sum;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double a = 0, r = 0, g = 0, b = 0;
                for (int k = -radius; k <= radius; k++) {
                    int xx = x + k;
                    if (xx < 0) xx = 0;
                    else if (xx >= width) xx = width - 1;
                    int rgb = src.getRGB(xx, y);
                    int ca = (rgb >> 24) & 0xff;
                    int cr = (rgb >> 16) & 0xff;
                    int cg = (rgb >> 8) & 0xff;
                    int cb = rgb & 0xff;
                    double weight = kernel[k + radius];
                    a += ca * weight;
                    r += cr * weight;
                    g += cg * weight;
                    b += cb * weight;
                }
                int ia = Math.min(255, Math.max(0, (int) Math.round(a)));
                int ir = Math.min(255, Math.max(0, (int) Math.round(r)));
                int ig = Math.min(255, Math.max(0, (int) Math.round(g)));
                int ib = Math.min(255, Math.max(0, (int) Math.round(b)));
                int newRgb = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                temp.setRGB(x, y, newRgb);
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double a = 0, r = 0, g = 0, b = 0;
                for (int k = -radius; k <= radius; k++) {
                    int yy = y + k;
                    if (yy < 0) yy = 0;
                    else if (yy >= height) yy = height - 1;
                    int rgb = temp.getRGB(x, yy);
                    int ca = (rgb >> 24) & 0xff;
                    int cr = (rgb >> 16) & 0xff;
                    int cg = (rgb >> 8) & 0xff;
                    int cb = rgb & 0xff;
                    double weight = kernel[k + radius];
                    a += ca * weight;
                    r += cr * weight;
                    g += cg * weight;
                    b += cb * weight;
                }
                int ia = Math.min(255, Math.max(0, (int) Math.round(a)));
                int ir = Math.min(255, Math.max(0, (int) Math.round(r)));
                int ig = Math.min(255, Math.max(0, (int) Math.round(g)));
                int ib = Math.min(255, Math.max(0, (int) Math.round(b)));
                int newRgb = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                dst.setRGB(x, y, newRgb);
            }
        }
        return dst;
    }
    
    public static void main(String[] args) {
        int defaultRes = 1024, defaultCells = 8;
        try {
            defaultRes = Integer.parseInt(args.length >= 1 ? args[0] : "1024");
        } catch (NumberFormatException e) {
            System.out.println("Invalid resolution argument. Using default: 1024.");
        }
        try {
            defaultCells = Integer.parseInt(args.length >= 2 ? args[1] : "8");
        } catch (NumberFormatException e) {
            System.out.println("Invalid cell count argument. Using default: 8.");
        }
        final int finalRes = defaultRes, finalCells = defaultCells;
        SwingUtilities.invokeLater(() -> new TextureGenius(finalRes, finalCells));
    }
}

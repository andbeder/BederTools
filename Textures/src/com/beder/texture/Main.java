package com.beder.texture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.beder.texture.mask.CopyMask;
import com.beder.texture.mask.MixMask;
import com.beder.texture.noise.SimplexNoiseGenerator;

import net.miginfocom.swing.MigLayout;

/**
 * Main entry point for testing texture generation using layered noise.
 */
public class Main implements MouseListener, Redrawable {
	private LayerStack stack;
	private ImagePair curImage;
	protected JFrame frame;
	private JPanel mainPanel;
	public final int res;
	private ImageIcon leftIcon;
	private ImageIcon rightIcon;
	private JButton applyButton;
	private JPanel opControlPanel;
	private JPanel imagePanel;
	
	
	public Main(int res) {
		this.res = res;
		stack = new LayerStack(this);
		curImage = new ImagePair(res);
	}
	
	public void init() {
        frame = new JFrame("Display Two BufferedImages");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());

        // ImagePanel is the center row and contains the images and arrow panel
        imagePanel = new JPanel(new MigLayout("gapx 10px", "", "[center][center]"));
        
        // Add the images in JLabels using ImageIcon for easy display.
        Image scaledLeft = curImage.left.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
        Image scaledRight = curImage.right.getScaledInstance(512, 512, Image.SCALE_SMOOTH);

        leftIcon = new ImageIcon(scaledLeft);
        rightIcon = new ImageIcon(scaledRight);
                
        // Arrow buttons panel.
        JPanel arrowsPanel = new JPanel();
        arrowsPanel.setLayout(new BoxLayout(arrowsPanel, BoxLayout.Y_AXIS));
        JButton copyButton = new JButton("Copy →");
        JButton mixButton = new JButton("Mix ↔");
        arrowsPanel.add(copyButton);
        arrowsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        arrowsPanel.add(mixButton);

        // Put image panel together
        imagePanel.add(new JLabel(leftIcon));
        imagePanel.add(arrowsPanel);
        imagePanel.add(new JLabel(rightIcon));
        
        mainPanel.add(imagePanel, BorderLayout.CENTER);
        
        JPanel stackPanel = stack.getStackTiles();
        mainPanel.add(stackPanel, BorderLayout.EAST);

        
        /***
         * Add in the buttons which create Operations on the stack
         */
        JPanel opPanel = new JPanel(new FlowLayout());
        JButton simplexButton = new JButton("Simplex");
        opPanel.add(simplexButton);
        
        mainPanel.add(opPanel, BorderLayout.SOUTH);
        
        /******
         * Create UI panel for the operation controls
         */
        opControlPanel = new JPanel(new FlowLayout());
        applyButton = new JButton("Apply");
        mainPanel.add(opControlPanel, BorderLayout.NORTH);

        /***
         * Put it all together and present it
         */
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame on the screen.
        frame.setVisible(true);
        
        /*****
         * Button action methods
         */
        
        applyButton.addActionListener(e -> {
			Operation op = stack.getCurrent();
			ImagePair input = stack.getInputImage(op);
			Parameters par = op.getUIParameters();
			ImagePair output = op.executeOperation(input, par);
			stack.saveImage(output, op);
			applyImage(output);
        });
        simplexButton.addActionListener(e -> {
        	addOperation(new SimplexNoiseGenerator(this));
        });
        copyButton.addActionListener(e -> {
        	addOperation(new CopyMask(this));
       });
        mixButton.addActionListener(e -> {
        	addOperation(new MixMask(this));
        });
		
	}
	
	private void addOperation(Operation op) {
      	stack.add(op);
      	ImagePair pair = stack.getInputImage(op);
      	applyImage(pair);
    	setCurrent(op);
    	frame.repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Component panel = e.getComponent();
		Operation o = stack.getOperation(panel);
		setCurrent(o);
	}
	
	
	/****
	 *  Called when a new or existing operation is 'selected'
	 *    Takes care of:
	 *    	- Displaying new images based on what's stored in the Operation Stack
	 *      - Updating the UI with the operation's specific controls.
	 */
	private void setCurrent(Operation op) {
		// Change the images
		ImagePair newImage = stack.getImage(op);
		applyImage(newImage);
		
		//  Show UI updates
		opControlPanel.removeAll();
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(BorderFactory.createTitledBorder(op.getTitle() + " Options"));
		controlPanel.add(op.getConfig());
		controlPanel.add(applyButton);
		opControlPanel.add(controlPanel);
		stack.setCurrent(op);
	}
	
	
	/****
	 * Takes and ImagePair and makes it visible in the icons
	 * @param current
	 */
	
	public void applyImage(ImagePair current) {
		curImage = current;
		curImage = (curImage == null) ? new ImagePair(res) : curImage;
        Image scaledLeft = curImage.left.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
        Image scaledRight = curImage.right.getScaledInstance(512, 512, Image.SCALE_SMOOTH);
		leftIcon.setImage(scaledLeft);
		rightIcon.setImage(scaledRight);
		imagePanel.repaint();
	}
	
    public static void main(String[] args) {
    	Main app = new Main(1024);
    	app.init();
    }


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRes() {
		// TODO Auto-generated method stub
		return res;
	}
}

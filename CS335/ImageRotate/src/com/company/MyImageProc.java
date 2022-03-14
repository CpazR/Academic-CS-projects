package com.company;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

/***************************************************************************

 Loads an image (JPEG or GIF), displays it, selects from
 a small set of image processing routines, and shows the results
 ***************************************************************************/

public class MyImageProc extends JFrame {

    // This frame will hold the primary components:
    // 	An object to hold the buffered image and its associated operations
    //	Components to control the interface

    // Instance variables
    private BufferedImage image;   // the image
    private MyImageObj view;       // a component in which to display an image
    private JLabel infoLabel;      // an informative label for the simple GUI
    private JButton SharpenButton; // Button to trigger sharpen operator
    private JButton BlurButton;    // Button to trigger blur operator
    private JButton EdgeButton;    // Button to trigger edge detect operator
    private JButton OriginalButton;// Button to restore original image
    private JButton CustomButton;  // Button to hold custom filter
    private JButton rotleft;
    private JButton rotright;
    private JTextField filterfield[];
    private int x, y;              // Store x, y mouse position for paint
    private boolean firstdrag = true;// Flag to toggle draw mode
    private JLabel ThreshLabel;   // Label for threshold slider
    private JSlider thresholdslider;
    private float customfiltervalues[];
    private int rotation = 0;

    // Constructor for the frame
    public MyImageProc() {

        super();                // call JFrame constructor

        this.buildMenus();        // helper method to build menus
        this.buildComponents();        // helper method to set up components
        this.buildDisplay();        // Lay out the components on the display
    }


    //  Builds the menus to be attached to this JFrame object
    //  Primary side effect:  menus are added via the setJMenuBar call
    //  		Action listeners for the menu items are anonymous inner
    //		classes here
    //  This helper method is called once by the constructor

    private void buildMenus() {

        final JFileChooser fc = new JFileChooser(".");
        JMenuBar bar = new JMenuBar();
        this.setJMenuBar(bar);
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileopen = new JMenuItem("Open");
        JMenuItem fileexit = new JMenuItem("Exit");

        fileopen.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int returnVal = fc.showOpenDialog(MyImageProc.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                image = ImageIO.read(file);
                            } catch (IOException e1) {
                            }
                            ;

                            view.setImage(image);
                            view.showImage();
                        }
                    }
                }
        );
        fileexit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                }
        );

        fileMenu.add(fileopen);
        fileMenu.add(fileexit);
        bar.add(fileMenu);
    }

    //  Allocate components (these are all instance vars of this frame object)
    //  and set up action listeners for each of them
    //  This is called once by the constructor

    private void buildComponents() {

        // Create components to in which to display image and GUI controls
        // reads a default image
        view = new MyImageObj(readImage("boat.gif"));
        //view = new MyImageObj();
        infoLabel = new JLabel("Original Image");
        OriginalButton = new JButton("Original");
        SharpenButton = new JButton("Sharpen");
        BlurButton = new JButton("Blur");
        EdgeButton = new JButton("Edges");
        CustomButton = new JButton("Custom");
        rotleft = new JButton("<<");
        rotright = new JButton(">>");
        filterfield = new JTextField[9];
        customfiltervalues = new float[9];
        for (int i = 0; i < filterfield.length; i++) {
            filterfield[i] = new JTextField(5);
            filterfield[i].setText("0.0");
        }
        filterfield[4].setText("1.0");

        ThreshLabel = new JLabel("Threshold Value: 128");
        thresholdslider = new JSlider(SwingConstants.VERTICAL, 0, 255, 10);
        thresholdslider.setMajorTickSpacing(10);
        thresholdslider.setPaintTicks(true);

        // slider event triggers a display of thresholded image
        thresholdslider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        view.ThresholdImage(thresholdslider.getValue());
                        infoLabel.setText("Thresholded Image");
                        ThreshLabel.setText("Threshold Value: " +
                                Integer.toString(thresholdslider.getValue()));
                    }
                }
        );

        // Listen for mouse events to allow painting on image
        view.addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent event) {
                        Graphics g = view.getGraphics();
                        g.setColor(Color.white);
                        if (firstdrag) {
                            x = event.getX();
                            y = event.getY();
                            firstdrag = false;
                        } else {
                            view.showImage();
                            x = event.getX();
                            y = event.getY();
                            int w = view.getImage().getWidth();
                            int h = view.getImage().getHeight();
                            g.fillOval(x - 5, y - 5, 10, 10);
                            g.drawLine(0, 0, x, y);
                            g.drawLine(0, h, x, y);
                            g.drawLine(w, h, x, y);
                            g.drawLine(w, 0, x, y);
                        }
                    }
                }
        );

        // Listen for mouse release to detect when we've stopped painting
        view.addMouseListener(
                new MouseAdapter() {
                    public void mouseReleased(MouseEvent event) {
                        Graphics g = view.getGraphics();
                        firstdrag = true;
                        x = event.getX();
                        y = event.getY();
                        int w = view.getImage().getWidth();
                        int h = view.getImage().getHeight();
                        g.fillOval(x - 5, y - 5, 10, 10);
                        g.drawLine(0, 0, x, y);
                        g.drawLine(0, h, x, y);
                        g.drawLine(w, h, x, y);
                        g.drawLine(w, 0, x, y);


                    }
                }
        );

        // Button listeners activate the buffered image object in order
        // to display appropriate function
        OriginalButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        view.showImage();
                        infoLabel.setText("Original");
                    }
                }
        );
        SharpenButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        view.SharpenImage();
                        infoLabel.setText("Sharpened");
                    }
                }
        );
        BlurButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        view.BlurImage();
                        infoLabel.setText("Blur");
                    }
                }
        );
        EdgeButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        view.EdgeImage();
                        view.repaint();
                        infoLabel.setText("Edges");
                    }
                }
        );
        CustomButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        loadcustomvalues();
                        view.CustomImage();
                        infoLabel.setText("Custom");
                    }
                }
        );
        rotleft.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        rotation -= 10;
                        view.ApplyAffine(rotation);
                        infoLabel.setText(">>");
                    }
                }
        );
        rotright.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        rotation += 10;
                        view.ApplyAffine(rotation);
                        infoLabel.setText("<<");
                    }
                }
        );
    }

    private void loadcustomvalues() {
        for (int i = 0; i < customfiltervalues.length; i++)
            customfiltervalues[i] = Float.parseFloat(filterfield[i].getText());
    }

    // This helper method adds all components to the content pane of the
    // JFrame object.  Specific layout of components is controlled here

    private void buildDisplay() {

        // Build first JPanel
        JPanel controlPanel = new JPanel();
        GridLayout grid = new GridLayout(1, 5);
        controlPanel.setLayout(grid);
        controlPanel.add(infoLabel);
        controlPanel.add(OriginalButton);
        controlPanel.add(SharpenButton);
        controlPanel.add(BlurButton);
        controlPanel.add(EdgeButton);
        controlPanel.add(CustomButton);
        controlPanel.add(rotleft);
        controlPanel.add(rotright);

        // Build second JPanel
        JPanel thresholdcontrolPanel = new JPanel();
        BorderLayout layout = new BorderLayout(5, 5);
        thresholdcontrolPanel.setLayout(layout);
        thresholdcontrolPanel.add(ThreshLabel, BorderLayout.NORTH);
        thresholdcontrolPanel.add(thresholdslider, BorderLayout.CENTER);

        // Build third JPanel
        JPanel filterPanel = new JPanel();
        grid = new GridLayout(3, 3);
        filterPanel.setLayout(grid);
        for (int i = 0; i < filterfield.length; i++)
            filterPanel.add(filterfield[i]);

        // Add panels and image data component to the JFrame
        Container c = this.getContentPane();
        c.add(view, BorderLayout.EAST);
        c.add(controlPanel, BorderLayout.SOUTH);
        c.add(thresholdcontrolPanel, BorderLayout.WEST);
        c.add(filterPanel, BorderLayout.NORTH);

    }

    // This method reads an Image object from a file indicated by
    // the string provided as the parameter.  The image is converted
    // here to a BufferedImage object, and that new object is the returned
    // value of this method.
    // The mediatracker in this method can throw an exception

    public BufferedImage readImage(String file) {

        Image image = Toolkit.getDefaultToolkit().getImage(file);
        MediaTracker tracker = new MediaTracker(new Component() {
        });
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }
        BufferedImage bim = new BufferedImage
                (image.getWidth(this), image.getHeight(this),
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bim.createGraphics();
        big.drawImage(image, 0, 0, this);
        return bim;
    }

    // The main method allocates the "window" and makes it visible.
    // The windowclosing event is handled by an anonymous inner (adapter)
    // class
    // Command line arguments are ignored.

    public static void main(String[] argv) {

        JFrame frame = new MyImageProc();
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }

    /*****************************************************************

     This is a helper object, which could reside in its own file, that
     extends JLabel so that it can hold a BufferedImage

     I've added the ability to apply image processing operators to the
     image and display the result
     ***************************************************************************/

    public class MyImageObj extends JLabel {

        // instance variable to hold the buffered image
        private BufferedImage bim = null;
        private BufferedImage filteredbim = null;

        //  tell the paintcomponent method what to draw
        private boolean showfiltered = false;

        // here are a few kernels to try
        private final float[] LOWPASS3x3 =
                {0.1f, 0.1f, 0.1f, 0.1f, 0.2f, 0.1f, 0.1f, 0.1f, 0.1f};
        private final float[] SHARPEN3x3 =
                {0.f, -1.f, 0.f, -1.f, 5.f, -1.f, 0.f, -1.f, 0.f};
        private final float[] EDGE3x3 =
                {0.f, -1.f, 0.f, -1.f, 4.0f, -1.f, 0.f, -1.f, 0.f};

        // Default constructor
        public MyImageObj() {
        }

        // This constructor stores a buffered image passed in as a parameter
        public MyImageObj(BufferedImage img) {
            bim = img;
            filteredbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
            setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));

            this.repaint();
        }

        // This mutator changes the image by resetting what is stored
        // The input parameter img is the new image;  it gets stored as an
        //     instance variable
        public void setImage(BufferedImage img) {
            if (img == null) return;
            bim = img;
            filteredbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_INT_RGB);
            setPreferredSize(new Dimension(bim.getWidth(), bim.getHeight()));
            showfiltered = false;
            this.repaint();
        }

        // accessor to get a handle to the bufferedimage object stored here
        public BufferedImage getImage() {
            return bim;
        }

        //  apply the sharpen operator
        public void SharpenImage() {
            if (bim == null) return;
            Kernel kernel = new Kernel(3, 3, SHARPEN3x3);
            ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // make a copy of the buffered image
            BufferedImage newbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            Graphics2D big = newbim.createGraphics();
            big.drawImage(bim, 0, 0, null);

            // apply the filter the copied image
            // result goes to a filtered copy
            cop.filter(newbim, filteredbim);
            showfiltered = true;
            this.repaint();
        }

        //  apply the blur operator
        public void BlurImage() {
            if (bim == null) return;
            Kernel kernel = new Kernel(3, 3, LOWPASS3x3);
            ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // make a copy of the buffered image
            BufferedImage newbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            Graphics2D big = newbim.createGraphics();
            big.drawImage(bim, 0, 0, null);

            // apply the filter the copied image
            // result goes to a filtered copy
            cop.filter(newbim, filteredbim);
            showfiltered = true;
            this.repaint();
        }

        //  apply the detect-edge operator
        public void EdgeImage() {
            if (bim == null) return;
            Kernel kernel = new Kernel(3, 3, EDGE3x3);
            ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // make a copy of the buffered image
            BufferedImage newbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            Graphics2D big = newbim.createGraphics();
            big.drawImage(bim, 0, 0, null);

            // apply the filter the copied image
            // result goes to a filtered copy
            cop.filter(newbim, filteredbim);
            showfiltered = true;
            this.repaint();
        }

        //  apply the detect-edge operator
        public void CustomImage() {
            if (bim == null) return;
            Kernel kernel = new Kernel(3, 3, customfiltervalues);
            ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

            // make a copy of the buffered image
            BufferedImage newbim = new BufferedImage
                    (bim.getWidth(), bim.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            Graphics2D big = newbim.createGraphics();
            big.drawImage(bim, 0, 0, null);

            // apply the filter the copied image
            // result goes to a filtered copy
            cop.filter(newbim, filteredbim);
            showfiltered = true;
            this.repaint();
        }

        //  apply the threshold operator via colormap lookup table
        //  input parameter is an integer indicating the threshold value
        public void ThresholdImage(int value) {
            if (bim == null) return;
            int i;
            byte thresh[] = new byte[256];
            if ((value < 0) || (value > 255))
                value = 128;
            for (i = 0; i < value; i++)
                thresh[i] = 0;
            for (int j = i; j < 255; j++)
                thresh[j] = (byte) 255;
            ByteLookupTable blut = new ByteLookupTable(0, thresh);
            LookupOp lop = new LookupOp(blut, null);
            lop.filter(bim, filteredbim);
            showfiltered = true;
            this.repaint();
        }

        public void ApplyAffine(int rot) {
            float radianrot = (float) ((double) rot / (double) 180.0 * (double) (Math.PI));
            float x = (float) bim.getWidth() / (float) 2.0;
            float y = (float) bim.getHeight() / (float) 2.0;

            if (bim == null) return;
            AffineTransform transform = new
                    AffineTransform(Math.cos(radianrot), Math.sin(radianrot),
                    -Math.sin(radianrot), Math.cos(radianrot),
                    x - x * Math.cos(radianrot) + y * Math.sin(radianrot),
                    y - x * Math.sin(radianrot) - y * Math.cos(radianrot));
            //AffineTransform transform = new
            //AffineTransform(Math.cos(radianrot), Math.sin(radianrot),
            //-Math.sin(radianrot), Math.cos(radianrot),
            //0.0, 0.0);
            //AffineTransform transform = new
            //AffineTransform(1.0, 4.3, -1.1, 1.0,
            //8.0, -10.0);


            Graphics2D g2 = filteredbim.createGraphics();
            g2.fillRect(0, 0, (int) x * 2, (int) y * 2);
            g2.setTransform(transform);
            g2.setColor(new Color(0, 0, 0));
            g2.drawImage(bim, 0, 0, null);
            g2.dispose();

            showfiltered = true;
            this.repaint();
        }

        //  show current image by a scheduled call to paint()
        public void showImage() {
            if (bim == null) return;
            showfiltered = false;
            this.repaint();
        }

        //  get a graphics context and show either filtered image or
        //  regular image
        public void paintComponent(Graphics g) {
            Graphics2D big = (Graphics2D) g;
            if (showfiltered)
                big.drawImage(filteredbim, 0, 0, this);
            else
                big.drawImage(bim, 0, 0, this);
        }
    }

}

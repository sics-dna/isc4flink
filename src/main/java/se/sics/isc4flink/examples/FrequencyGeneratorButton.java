package se.sics.isc4flink.examples;


        import javax.swing.AbstractButton;
        import javax.swing.JButton;
        import javax.swing.JPanel;
        import javax.swing.JFrame;

        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.awt.event.KeyEvent;

public class FrequencyGeneratorButton extends JPanel
        implements ActionListener {
    protected JButton b1, b3;
    static boolean anomaly = false;


    public FrequencyGeneratorButton() {

        b1 = new JButton("Break things");
        b1.setVerticalTextPosition(AbstractButton.CENTER);
        b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        b1.setMnemonic(KeyEvent.VK_D);
        b1.setActionCommand("disable");


        b3 = new JButton("Back to normal");
        //Use the default text position of CENTER, TRAILING (RIGHT).
        b3.setMnemonic(KeyEvent.VK_E);
        b3.setActionCommand("enable");
        b3.setEnabled(false);

        //Listen for actions on buttons 1 and 3.
        b1.addActionListener(this);
        b3.addActionListener(this);

        //Add Components to this container, using the default FlowLayout.
        add(b1);
        add(b3);
    }

    public void actionPerformed(ActionEvent e) {
        if ("disable".equals(e.getActionCommand())) {
            b1.setEnabled(false);
            b3.setEnabled(true);
            anomaly = true;
            System.out.println("+++++++++ messing things up +++++++++++");
        } else {
            b1.setEnabled(true);
            b3.setEnabled(false);
            anomaly = false;
            System.out.println("+++++++++ back to normal ++++++++++++");
        }
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI() {

        //Create and set up the window.
        JFrame frame = new JFrame("PoissonSource");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        FrequencyGeneratorButton newContentPane = new FrequencyGeneratorButton();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

package edu.ucdenver.ccp.knowtator.view.graph;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.menu.GraphMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class GraphViewDialog extends JDialog {
  private JPanel contentPane;
  private GraphView graphView;
  private KnowtatorView view;

  public GraphViewDialog(KnowtatorView view) {

    this.view = view;
    $$$setupUI$$$();

    setSize(new Dimension(800, 800));
    setLocationRelativeTo(view);

    setContentPane(contentPane);
    setModal(false);

    setJMenuBar(new JMenuBar());
    GraphMenu graphMenu = new GraphMenu(view);
    getJMenuBar().add(graphMenu);

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    addWindowFocusListener(
        new WindowFocusListener() {
          @Override
          public void windowGainedFocus(WindowEvent e) {

          }

          @Override
          public void windowLostFocus(WindowEvent e) {
            if (e.getOppositeWindow() != SwingUtilities.getWindowAncestor(view)) {
              setAlwaysOnTop(false);
              toBack();
            }
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

  private void createUIComponents() {
    graphView = new GraphView(this, view);
  }

  public void setController(KnowtatorController controller) {
    graphView.setController(controller);
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
   * call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout(0, 0));
    contentPane.add(graphView.$$$getRootComponent$$$(), BorderLayout.CENTER);
  }

  /** @noinspection ALL */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }

  //    public static void main(String[] args) {
  //        GraphViewDialog dialog = new GraphViewDialog();
  //        dialog.pack();
  //        dialog.setVisible(true);
  //        System.exit(0);
  //    }
}

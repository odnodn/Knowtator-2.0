package edu.ucdenver.ccp.knowtator.view.graph;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import edu.ucdenver.ccp.knowtator.model.collection.AddEvent;
import edu.ucdenver.ccp.knowtator.model.collection.KnowtatorCollectionListener;
import edu.ucdenver.ccp.knowtator.model.collection.RemoveEvent;
import edu.ucdenver.ccp.knowtator.model.collection.SelectionChangeEvent;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.view.KnowtatorComponent;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphView extends JPanel implements KnowtatorCollectionListener<GraphSpace>, KnowtatorComponent {
    private JButton removeCellButton;
    private JButton addAnnotationNodeButton;
    private JButton applyLayoutButton;
    private JButton previousGraphSpaceButton;
    private JButton nextGraphSpaceButton;
    private mxGraphComponent graphComponent;
    private JPanel panel1;
    private GraphSpaceChooser graphSpaceChooser;
    private JButton addGraphSpaceButton;
    private JButton removeGraphSpaceButton;
    private JSlider zoomSlider;
    private JButton graphMenuButton;
    private JButton renameButton;
    private final JDialog dialog;
    private List<JComponent> graphSpaceButtons;


    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(GraphView.class);

    private final KnowtatorView view;
    private final KnowtatorCollectionListener<TextSource> textSourceCollectionListener;


    GraphView(JDialog dialog, KnowtatorView view) {
        this.dialog = dialog;
        this.view = view;
        $$$setupUI$$$();
        makeButtons();

        GraphView graphView = this;

        textSourceCollectionListener = new KnowtatorCollectionListener<TextSource>() {
            @Override
            public void added(AddEvent<TextSource> addedObject) {

            }

            @Override
            public void removed(RemoveEvent<TextSource> removedObject) {

            }

            @Override
            public void changed() {

            }

            @Override
            public void emptied() {

            }

            @Override
            public void firstAdded() {

            }

            @Override
            public void updated(TextSource updatedItem) {

            }

            @Override
            public void selected(SelectionChangeEvent<TextSource> event) {
                if (event.getOld() != null) {
                    event.getOld().getGraphSpaceCollection().removeCollectionListener(graphView);
                }
                if (event.getNew() != null) {
                    event.getNew().getGraphSpaceCollection().addCollectionListener(graphView);
                    if (event.getNew().getGraphSpaceCollection().getSelection() != null) {
                        showGraph(event.getNew().getGraphSpaceCollection().getSelection());
                    } else {
                        showGraph(event.getNew().getGraphSpaceCollection().first());
                    }
                }
            }
        };
    }

    private void createUIComponents() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        graphSpaceChooser = new GraphSpaceChooser(view);
        mxGraph testGraph = new mxGraph();
        graphComponent = new mxGraphComponent(testGraph);
    }

    private void makeButtons() {

        graphSpaceButtons = new ArrayList<>();

        graphMenuButton.addActionListener(e -> GraphActions.showGraphMenuDialog(view));

        zoomSlider.addChangeListener(e -> GraphActions.zoomGraph(graphComponent, zoomSlider.getValue()));
        renameButton.addActionListener(e -> GraphActions.renameGraphSpace(view));
        addGraphSpaceButton.addActionListener(e -> GraphActions.addGraphSpace(view));
        removeGraphSpaceButton.addActionListener(e -> GraphActions.removeGraphSpace(view));
        previousGraphSpaceButton.addActionListener(e -> GraphActions.selectPreviousGraphSpace(view));
        nextGraphSpaceButton.addActionListener(e -> GraphActions.selectNextGraphSpace(view));
        removeCellButton.addActionListener(e -> GraphActions.removeSelectedCell(view));
        addAnnotationNodeButton.addActionListener(e -> GraphActions.addAnnotationNode(view));
        applyLayoutButton.addActionListener(e -> GraphActions.applyLayout(view));

        graphSpaceButtons.add(renameButton);
        graphSpaceButtons.add(removeCellButton);
        graphSpaceButtons.add(removeGraphSpaceButton);
        graphSpaceButtons.add(previousGraphSpaceButton);
        graphSpaceButtons.add(nextGraphSpaceButton);
        graphSpaceButtons.add(addAnnotationNodeButton);
        graphSpaceButtons.add(applyLayoutButton);
        graphSpaceButtons.add(zoomSlider);
        graphSpaceButtons.add(addGraphSpaceButton);
    }

    private void showGraph(GraphSpace graphSpace) {
        graphComponent.setGraph(graphSpace);
        graphSpace.setParentWindow(dialog);

        graphComponent.setName(graphSpace.getId());

        graphSpace.setupListeners();

        graphSpace.reDrawGraph();
        graphComponent.refresh();
    }

    @Override
    public void updated(GraphSpace updatedItem) {

    }

    @Override
    public void selected(SelectionChangeEvent<GraphSpace> event) {
        if (event.getNew() != null && event.getNew() != graphComponent.getGraph()) {
            showGraph(event.getNew());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            view.getController().getTextSourceCollection().addCollectionListener(textSourceCollectionListener);
            graphSpaceChooser.reset();
            TextSource textSource = view.getController().getTextSourceCollection().getSelection();
            if (textSource != null) {
                textSource.getGraphSpaceCollection().addCollectionListener(this);
                if (textSource.getGraphSpaceCollection().size() > 0) {
                    GraphSpace graphSpace = textSource.getGraphSpaceCollection().getSelection();
                    if (graphSpace != null) {
                        showGraph(graphSpace);
                    } else {
                        showGraph(textSource.getGraphSpaceCollection().first());
                    }
                } else {
                    GraphActions.addGraphSpace(view);
                }
            }
        } else {
            view.getController().getTextSourceCollection().removeCollectionListener(textSourceCollectionListener);
        }
    }

    @Override
    public void reset() {
        graphSpaceChooser.reset();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void added(AddEvent<GraphSpace> addedObject) {

    }

    @Override
    public void removed(RemoveEvent<GraphSpace> removedObject) {

    }

    @Override
    public void changed() {

    }

    @Override
    public void emptied() {
        graphSpaceButtons.forEach(c -> c.setEnabled(false));
        addGraphSpaceButton.setEnabled(true);
    }

    @Override
    public void firstAdded() {
        graphSpaceButtons.forEach(c -> c.setEnabled(true));
    }

    mxGraphComponent getGraphComponent() {
        return graphComponent;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setAlignmentX(0.0f);
        panel1.setAlignmentY(0.0f);
        panel1.setMinimumSize(new Dimension(400, 400));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, BorderLayout.NORTH);
        addAnnotationNodeButton = new JButton();
        addAnnotationNodeButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
        addAnnotationNodeButton.setText("");
        panel2.add(addAnnotationNodeButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        removeCellButton = new JButton();
        removeCellButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
        removeCellButton.setText("");
        panel2.add(removeCellButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applyLayoutButton = new JButton();
        applyLayoutButton.setText("Apply Layout");
        panel2.add(applyLayoutButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        graphMenuButton = new JButton();
        graphMenuButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-menu-24.png")));
        graphMenuButton.setText("");
        panel2.add(graphMenuButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel1.add(panel3, BorderLayout.CENTER);
        graphComponent.setCenterPage(false);
        graphComponent.setGridVisible(true);
        panel3.add(graphComponent, BorderLayout.CENTER);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, BorderLayout.SOUTH);
        previousGraphSpaceButton = new JButton();
        previousGraphSpaceButton.setText("Previous");
        panel4.add(previousGraphSpaceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        graphSpaceChooser.setMaximumSize(new Dimension(200, 32767));
        graphSpaceChooser.setMinimumSize(new Dimension(80, 30));
        panel4.add(graphSpaceChooser, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addGraphSpaceButton = new JButton();
        addGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
        addGraphSpaceButton.setText("");
        panel4.add(addGraphSpaceButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeGraphSpaceButton = new JButton();
        removeGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
        removeGraphSpaceButton.setText("");
        panel4.add(removeGraphSpaceButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        zoomSlider = new JSlider();
        zoomSlider.setMaximum(100);
        panel4.add(zoomSlider, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(194, 16), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        renameButton = new JButton();
        renameButton.setText("Rename");
        panel4.add(renameButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextGraphSpaceButton = new JButton();
        nextGraphSpaceButton.setText("Next");
        panel4.add(nextGraphSpaceButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    /**
     * Taken from https://tips4java.wordpress.com/2010/03/14/dialog-focus/
     */
    static class RequestFocusListener implements AncestorListener {
        private final boolean removeListener;

        /*
         *  Convenience constructor. The listener is only used once and then it is
         *  removed from the component.
         */
        RequestFocusListener() {
            this(true);
        }

        /*
         *  Constructor that controls whether this listen can be used once or
         *  multiple times.
         *
         *  @param removeCollectionListener when true this listener is only invoked once
         *                        otherwise it can be invoked multiple times.
         */
        @SuppressWarnings("SameParameterValue")
        RequestFocusListener(boolean removeListener) {
            this.removeListener = removeListener;
        }

        @Override
        public void ancestorAdded(AncestorEvent e) {
            JComponent component = e.getComponent();
            component.requestFocusInWindow();

            if (removeListener) component.removeAncestorListener(this);
        }

        @Override
        public void ancestorMoved(AncestorEvent e) {
        }

        @Override
        public void ancestorRemoved(AncestorEvent e) {
        }
    }


}

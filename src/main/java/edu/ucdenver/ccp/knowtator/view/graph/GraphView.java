package edu.ucdenver.ccp.knowtator.view.graph;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;
import edu.ucdenver.ccp.knowtator.listeners.ProjectListener;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.TextSourceCollectionListener;
import edu.ucdenver.ccp.knowtator.model.text.concept.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.text.graph.AnnotationNode;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpaceCollectionListener;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.chooser.GraphSpaceChooser;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class GraphView extends JPanel implements ProjectListener, GraphSpaceCollectionListener {
    private JButton removeCellButton;
    private JButton addAnnotationNodeButton;
    private JButton applyLayoutButton;
    private JButton previousGraphSpaceButton;
    private JButton nextGraphSpaceButton;
    private mxGraphComponent graphComponent;
    private JButton zoomOutButton;
    private JButton zoomInButton;
    private JPanel panel1;
    private GraphSpaceChooser graphSpaceChooser;
    private JRadioButton someRadioButton;
    private JRadioButton onlyRadioButton;
    private JRadioButton exactlyRadioButton;
    private JRadioButton minRadioButton;
    private JRadioButton maxRadioButton;
    private JTextField exactlyValueTextField;
    private JCheckBox negateCheckBox;
    private JPanel propertyValuePanel;
    private JTextField minValueTextField;
    private JTextField maxValueTextField;
    private JDialog dialog;

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(GraphView.class);

    private KnowtatorView view;

    GraphView(JDialog dialog, KnowtatorView view) {
        this.dialog = dialog;
        this.view = view;
        $$$setupUI$$$();
        makeButtons();

        GraphView graphView = this;

        TextSourceCollectionListener textSourceCollectionListener = new TextSourceCollectionListener() {
            @Override
            public void updated(TextSource updatedItem) {

            }

            @Override
            public void noSelection(TextSource previousSelection) {

            }

            @Override
            public void selected(TextSource previousSelection, TextSource currentSelection) {
                if (previousSelection != null) {
                    previousSelection.getGraphSpaceCollection().removeCollectionListener(graphView);
                }
                currentSelection.getGraphSpaceCollection().addCollectionListener(graphView);
            }

            @Override
            public void added(TextSource addedObject) {

            }

            @Override
            public void removed(TextSource removedObject) {

            }

            @Override
            public void emptied(TextSource object) {

            }

            @Override
            public void firstAdded(TextSource object) {

            }
        };
        view.getController().getTextSourceCollection().addCollectionListener(textSourceCollectionListener);
    }

    private void createUIComponents() {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        graphSpaceChooser = new GraphSpaceChooser(view);
        mxGraph testGraph = new mxGraph();
        graphComponent = new mxGraphComponent(testGraph);
    }

    private void quantifierButtonAction(String quantifier) {
        view.getController()
                .getTextSourceCollection().getSelection()
                .getGraphSpaceCollection().getSelection()
                .getRelationSelectionManager()
                .setSelectedPropertyQuantifer(quantifier);

        if (quantifier.equals("some") || quantifier.equals("only")) {
            view.getController()
                    .getTextSourceCollection().getSelection()
                    .getGraphSpaceCollection().getSelection()
                    .getRelationSelectionManager()
                    .setSelectedRelationQuantifierValue("");
        }
    }

    private void quantifierTextFieldAction(String quantifierValue) {
        view.getController()
                .getTextSourceCollection().getSelection()
                .getGraphSpaceCollection().getSelection()
                .getRelationSelectionManager()
                .setSelectedRelationQuantifierValue(quantifierValue);
    }

    private void makeButtons() {
        someRadioButton.addActionListener(e -> quantifierButtonAction("some"));
        onlyRadioButton.addActionListener(e -> quantifierButtonAction("only"));
        minRadioButton.addActionListener(e -> quantifierButtonAction("min"));
        maxRadioButton.addActionListener(e -> quantifierButtonAction("max"));
        exactlyRadioButton.addActionListener(e -> quantifierButtonAction("exactly"));
        makeQuantifierTextField(minValueTextField, minRadioButton);
        makeQuantifierTextField(maxValueTextField, maxRadioButton);
        makeQuantifierTextField(exactlyValueTextField, exactlyRadioButton);
        negateCheckBox.addActionListener(
                e -> view.getController()
                        .getTextSourceCollection().getSelection()
                        .getGraphSpaceCollection().getSelection()
                        .getRelationSelectionManager()
                        .setNegatation(negateCheckBox.isSelected()));

        graphSpaceChooser.addActionListener(
                e -> {
                    JComboBox comboBox = (JComboBox) e.getSource();
                    if (comboBox.getSelectedItem() != null
                            && comboBox.getSelectedItem()
                            != view.getController()
                            .getTextSourceCollection().getSelection()
                            .getGraphSpaceCollection().getSelection()) {
                        view.getController()
                                .getTextSourceCollection().getSelection()
                                .getGraphSpaceCollection().setSelection((GraphSpace) comboBox.getSelectedItem());
                    }

                });

        zoomOutButton.addActionListener(e -> graphComponent.zoomOut());
        zoomInButton.addActionListener(e -> graphComponent.zoomIn());
        previousGraphSpaceButton.addActionListener(
                e -> view.getController()
                        .getTextSourceCollection().getSelection()
                        .getGraphSpaceCollection()
                        .selectPrevious());
        nextGraphSpaceButton.addActionListener(
                e -> view.getController()
                        .getTextSourceCollection().getSelection()
                        .getGraphSpaceCollection()
                        .selectNext());
        removeCellButton.addActionListener(e -> removeSelectedCell());
        addAnnotationNodeButton.addActionListener(
                e -> {
                    ConceptAnnotation conceptAnnotation =
                            view.getController()
                                    .getTextSourceCollection().getSelection()
                                    .getConceptAnnotationCollection().getSelection();

                    AnnotationNode vertex =
                            view.getController()
                                    .getTextSourceCollection().getSelection()
                                    .getGraphSpaceCollection().getSelection()
                                    .makeOrGetAnnotationNode(conceptAnnotation, null);


                    goToVertex(vertex);
                });
        applyLayoutButton.addActionListener(e -> applyLayout());
    }

    private void makeQuantifierTextField(JTextField quantifierValueTextField, JRadioButton quantifierRadioButton) {
        quantifierValueTextField
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                quantifierTextFieldAction(quantifierValueTextField.getText());
                                quantifierRadioButton.doClick();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                quantifierTextFieldAction(quantifierValueTextField.getText());
                                quantifierRadioButton.doClick();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                            }
                        });
    }

    @SuppressWarnings("unused")
    public void goToAnnotationVertex(GraphSpace graphSpace, ConceptAnnotation conceptAnnotation) {
        if (conceptAnnotation != null && graphSpace != null) {
            view.getController()
                    .getTextSourceCollection().getSelection()
                    .getGraphSpaceCollection().setSelection(graphSpace);
            List<Object> vertices = graphSpace.getVerticesForAnnotation(conceptAnnotation);
            if (vertices.size() > 0) {
                graphSpace.setSelectionCells(vertices);
                goToVertex(vertices.get(0));
            }
        }
    }

    private void goToVertex(Object vertex) {
        dialog.requestFocusInWindow();
        graphComponent.scrollCellToVisible(vertex, true);
    }

    private void showGraph(GraphSpace graphSpace) {
        graphComponent.setGraph(graphSpace);

        graphComponent.setName(graphSpace.getId());
        exactlyValueTextField.setText("");
        minValueTextField.setText("");
        maxValueTextField.setText("");

        graphSpace.setupListeners();

        graphSpace.reDrawGraph();
        graphComponent.refresh();
    }

    private void applyLayout() {
        GraphSpace graph;
        graph =
                view.getController()
                        .getTextSourceCollection().getSelection()
                        .getGraphSpaceCollection().getSelection();

        //		graph.reDrawGraph();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
        layout.setOrientation(SwingConstants.WEST);
        layout.setIntraCellSpacing(50);
        layout.setInterRankCellSpacing(125);
        layout.setOrientation(SwingConstants.NORTH);

        try {
            graph.getModel().beginUpdate();
            try {
                layout.execute(graph.getDefaultParent());
            } finally {
                mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

                morph.addListener(mxEvent.DONE, (arg0, arg1) -> graph.getModel().endUpdate());

                morph.startAnimation();
            }
        } finally {
            graph.getModel().endUpdate();
            graphComponent.zoomAndCenter();
        }
    }

    private void removeSelectedCell() {
        view.getController()
                .getTextSourceCollection().getSelection()
                .getGraphSpaceCollection().getSelection()
                .removeSelectedCell();
    }

    @Override
    public void projectClosed() {
        graphComponent.setGraph(new mxGraph());
    }

    @Override
    public void projectLoaded() {
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
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setAlignmentX(0.0f);
        toolBar1.setAlignmentY(0.0f);
        toolBar1.setFloatable(false);
        toolBar1.setMinimumSize(new Dimension(630, 100));
        panel2.add(toolBar1, BorderLayout.NORTH);
        zoomOutButton = new JButton();
        zoomOutButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-zoom-out-filled-50 (Custom).png")));
        zoomOutButton.setText("");
        zoomOutButton.setToolTipText(ResourceBundle.getBundle("ui").getString("zoom.out"));
        toolBar1.add(zoomOutButton);
        zoomInButton = new JButton();
        zoomInButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-zoom-in-filled-50 (Custom).png")));
        zoomInButton.setText("");
        zoomInButton.setToolTipText(ResourceBundle.getBundle("ui").getString("zoom.in"));
        toolBar1.add(zoomInButton);
        applyLayoutButton = new JButton();
        this.$$$loadButtonText$$$(applyLayoutButton, ResourceBundle.getBundle("ui").getString("apply.layout"));
        applyLayoutButton.setToolTipText(ResourceBundle.getBundle("ui").getString("apply.layout1"));
        toolBar1.add(applyLayoutButton);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel3.setMaximumSize(new Dimension(100, 100));
        panel3.setMinimumSize(new Dimension(24, 48));
        toolBar1.add(panel3);
        previousGraphSpaceButton = new JButton();
        previousGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-sort-up-24.png")));
        previousGraphSpaceButton.setPreferredSize(new Dimension(24, 16));
        previousGraphSpaceButton.setText("");
        previousGraphSpaceButton.setToolTipText(ResourceBundle.getBundle("ui").getString("previous.graph.space"));
        panel3.add(previousGraphSpaceButton, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        label1.setPreferredSize(new Dimension(24, 16));
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("ui").getString("graph.space"));
        panel3.add(label1, BorderLayout.CENTER);
        nextGraphSpaceButton = new JButton();
        nextGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-sort-down-24.png")));
        nextGraphSpaceButton.setPreferredSize(new Dimension(24, 16));
        nextGraphSpaceButton.setText("");
        nextGraphSpaceButton.setToolTipText(ResourceBundle.getBundle("ui").getString("next.graph.space"));
        panel3.add(nextGraphSpaceButton, BorderLayout.SOUTH);
        graphSpaceChooser.setMaximumSize(new Dimension(200, 32767));
        graphSpaceChooser.setMinimumSize(new Dimension(80, 30));
        toolBar1.add(graphSpaceChooser);
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel2.add(toolBar2, BorderLayout.CENTER);
        addAnnotationNodeButton = new JButton();
        addAnnotationNodeButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
        addAnnotationNodeButton.setText("");
        addAnnotationNodeButton.setToolTipText(ResourceBundle.getBundle("ui").getString("add.conceptAnnotation.node"));
        toolBar2.add(addAnnotationNodeButton);
        removeCellButton = new JButton();
        removeCellButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
        removeCellButton.setText("");
        removeCellButton.setToolTipText(ResourceBundle.getBundle("ui").getString("remove.item"));
        toolBar2.add(removeCellButton);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel1.add(panel4, BorderLayout.CENTER);
        graphComponent.setCenterPage(false);
        graphComponent.setGridVisible(true);
        panel4.add(graphComponent, BorderLayout.CENTER);
        propertyValuePanel = new JPanel();
        propertyValuePanel.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        propertyValuePanel.setMaximumSize(new Dimension(2147483647, 2147483647));
        propertyValuePanel.setMinimumSize(new Dimension(30, 55));
        panel1.add(propertyValuePanel, BorderLayout.EAST);
        someRadioButton = new JRadioButton();
        someRadioButton.setSelected(true);
        this.$$$loadButtonText$$$(someRadioButton, ResourceBundle.getBundle("log4j").getString("some1"));
        propertyValuePanel.add(someRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(58, 23), null, 0, false));
        onlyRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(onlyRadioButton, ResourceBundle.getBundle("log4j").getString("only1"));
        propertyValuePanel.add(onlyRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(58, 23), null, 0, false));
        exactlyRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(exactlyRadioButton, ResourceBundle.getBundle("log4j").getString("exactly1"));
        propertyValuePanel.add(exactlyRadioButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        minRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(minRadioButton, ResourceBundle.getBundle("log4j").getString("min1"));
        propertyValuePanel.add(minRadioButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(maxRadioButton, ResourceBundle.getBundle("log4j").getString("max1"));
        propertyValuePanel.add(maxRadioButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exactlyValueTextField = new JTextField();
        propertyValuePanel.add(exactlyValueTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer1 = new Spacer();
        propertyValuePanel.add(spacer1, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        minValueTextField = new JTextField();
        propertyValuePanel.add(minValueTextField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        maxValueTextField = new JTextField();
        propertyValuePanel.add(maxValueTextField, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        negateCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(negateCheckBox, ResourceBundle.getBundle("log4j").getString("negate"));
        propertyValuePanel.add(negateCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(someRadioButton);
        buttonGroup.add(onlyRadioButton);
        buttonGroup.add(exactlyRadioButton);
        buttonGroup.add(minRadioButton);
        buttonGroup.add(maxRadioButton);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    @Override
    public void updated(GraphSpace updatedItem) {

    }

    @Override
    public void noSelection(GraphSpace previousSelection) {

    }

    @Override
    public void selected(GraphSpace previousSelection, GraphSpace currentSelection) {
        if (currentSelection != null) {
            String quantifier = currentSelection.getRelationSelectionManager().getSelectedRelationQuantifier();
            if (quantifier != null) {
                switch (quantifier) {
                    case "only":
                        onlyRadioButton.setSelected(true);
                        break;
                    case "exactly":
                        exactlyRadioButton.setSelected(true);
                        break;
                    case "min":
                        minRadioButton.setSelected(true);
                        break;
                    case "max":
                        maxRadioButton.setSelected(true);
                        break;
                    default:
                        someRadioButton.setSelected(true);
                        break;
                }
            }

            negateCheckBox.setSelected(currentSelection.getRelationSelectionManager().isSelectedNegation());
            if (graphComponent.getGraph() != currentSelection) {
                showGraph(currentSelection);
            }
        }
    }

    @Override
    public void added(GraphSpace addedObject) {

    }

    @Override
    public void removed(GraphSpace removedObject) {

    }

    @Override
    public void emptied(GraphSpace object) {

    }

    @Override
    public void firstAdded(GraphSpace object) {

    }
}

/*
 *  MIT License
 *
 *  Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.view.graph;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;
import edu.ucdenver.ccp.knowtator.model.BaseModel;
import edu.ucdenver.ccp.knowtator.model.ModelListener;
import edu.ucdenver.ccp.knowtator.model.collection.event.ChangeEvent;
import edu.ucdenver.ccp.knowtator.model.object.*;
import edu.ucdenver.ccp.knowtator.view.KnowtatorComponent;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.actions.graph.GraphActions;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.GraphSpaceAction;
import edu.ucdenver.ccp.knowtator.view.chooser.GraphSpaceChooser;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.ADD;
import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.REMOVE;

public class GraphView extends JPanel implements KnowtatorComponent, ModelListener {
	@SuppressWarnings("unused")
	private Logger log = Logger.getLogger(GraphView.class);

	private final JDialog dialog;
	private final KnowtatorView view;
	private final AddRelationListener addRelationListener;
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
	private JButton renameButton;
	private JButton exportToImagePNGButton;
	private List<JComponent> graphSpaceButtons;
	private final mxEventSource.mxIEventListener removeCellsListener;
	private final mxEventSource.mxIEventListener moveCellsListener = (sender, evt) -> {
		if (sender instanceof GraphSpace) {
			reDrawGraph((GraphSpace) sender);
		}
	};
	private final ChangeSelectionListener changeSelectionListener = new ChangeSelectionListener(this);


	GraphView(JDialog dialog, KnowtatorView view) {
		this.dialog = dialog;
		this.view = view;
		setVisible(false);
		$$$setupUI$$$();
		makeButtons();

		addRelationListener = new AddRelationListener(view, this);
		removeCellsListener = (sender, evt) -> {
			if (sender instanceof GraphSpace) {
				reDrawGraph((GraphSpace) sender);
			}
		};

	}

	private void makeButtons() {

		exportToImagePNGButton.addActionListener(e ->
				view.getModel().ifPresent(model -> model.getSelectedTextSource()
						.ifPresent(textSource -> textSource.getSelectedGraphSpace()
								.ifPresent(graphSpace -> {
									JFileChooser fileChooser = new JFileChooser();
									fileChooser.setCurrentDirectory(model.getSaveLocation());
									FileFilter fileFilter = new FileNameExtensionFilter("PNG", "png");
									fileChooser.setFileFilter(fileFilter);
									fileChooser.setSelectedFile(new File(String.format("%s_%s.png", textSource.getId(), graphSpace.getId())));
									if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
										BufferedImage image =
												mxCellRenderer.createBufferedImage(graphSpace, null, 1, Color.WHITE, true, null);
										try {
											ImageIO.write(
													image, "PNG", new File(fileChooser.getSelectedFile().getAbsolutePath()));
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									}
								}))));

		zoomSlider.addChangeListener(e -> graphComponent.zoomTo(zoomSlider.getValue() / 50.0, false));
		renameButton.addActionListener(e -> view.getModel().flatMap(BaseModel::getSelectedTextSource)
				.ifPresent(textSource ->
						textSource.getSelectedGraphSpace()
								.ifPresent(graphSpace -> getGraphNameInput(view, textSource, null)
										.ifPresent(graphSpace::setId))));
		addGraphSpaceButton.addActionListener(e ->
				view.getModel()
						.flatMap(BaseModel::getSelectedTextSource)
						.ifPresent(this::makeGraph));
		removeGraphSpaceButton.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this graph?") == JOptionPane.YES_OPTION) {
				view.getModel()
						.ifPresent(model ->
								model.getSelectedTextSource()
										.ifPresent(textSource ->
												model.registerAction(
														new GraphSpaceAction(model, REMOVE, null, textSource))));

			}
		});
		previousGraphSpaceButton.addActionListener(e ->
				view.getModel()
						.flatMap(BaseModel::getSelectedTextSource)
						.ifPresent(TextSource::selectPreviousGraphSpace));
		nextGraphSpaceButton.addActionListener(e ->
				view.getModel().flatMap(BaseModel::getSelectedTextSource)
						.ifPresent(TextSource::selectNextGraphSpace));
		removeCellButton.addActionListener(e ->
				view.getModel().ifPresent(model ->
						model.getSelectedTextSource()
								.ifPresent(textSource ->
										textSource.getSelectedGraphSpace()
												.ifPresent(graphSpace ->
														model.registerAction(
																new GraphActions.removeCellsAction(model, graphSpace))))));

		addAnnotationNodeButton.addActionListener(e ->
				view.getModel().ifPresent(model ->
						model.getSelectedTextSource()
								.ifPresent(textSource -> textSource.getSelectedGraphSpace()
										.ifPresent(graphSpace ->
												textSource.getSelectedAnnotation()
														.ifPresent(conceptAnnotation ->
																model.registerAction(
																		new GraphActions.AddAnnotationNodeAction(view, model, graphSpace, conceptAnnotation)))))));

		applyLayoutButton.addActionListener(e ->
				view.getModel().ifPresent(model ->
						model.getSelectedTextSource()
								.ifPresent(textSource ->
										textSource.getSelectedGraphSpace()
												.ifPresent(graphSpace -> model.registerAction(
														new GraphActions.applyLayoutAction(view, model, graphSpace))))));
		graphSpaceButtons = Arrays.asList(
				renameButton,
				removeCellButton,
				removeGraphSpaceButton,
				previousGraphSpaceButton,
				nextGraphSpaceButton,
				addAnnotationNodeButton,
				applyLayoutButton,
				zoomSlider,
				addGraphSpaceButton
		);
	}

	private void showGraph(GraphSpace graphSpace) {
		graphSpace.removeListener(addRelationListener);
		graphSpace.removeListener(moveCellsListener);
		graphSpace.removeListener(removeCellsListener);
		graphSpace.getSelectionModel().removeListener(changeSelectionListener);

		graphComponent.setGraph(graphSpace);
		graphComponent.setName(graphSpace.getId());

		graphSpace.addListener(mxEvent.ADD_CELLS, addRelationListener);
		graphSpace.addListener(mxEvent.MOVE_CELLS, moveCellsListener);
		graphSpace.addListener(mxEvent.REMOVE_CELLS, removeCellsListener);
		graphSpace.getSelectionModel().addListener(mxEvent.CHANGE, changeSelectionListener);

		reDrawGraph(graphSpace);
		graphComponent.refresh();
	}

	private void createUIComponents() {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		graphSpaceChooser = new GraphSpaceChooser(view);
		mxGraph testGraph = new mxGraph();
		graphComponent = new mxGraphComponent(testGraph);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			graphSpaceChooser.reset();
			view.getModel()
					.flatMap(BaseModel::getSelectedTextSource)
					.ifPresent(textSource -> {
						Optional<GraphSpace> graphSpaceOptional = textSource.getSelectedGraphSpace();
						if (graphSpaceOptional.isPresent()) {
							graphSpaceOptional.ifPresent(this::showGraph);
						} else {
							textSource.selectNextGraphSpace();
							graphSpaceOptional = textSource.getSelectedGraphSpace();
							if (graphSpaceOptional.isPresent()) {
								graphSpaceOptional.ifPresent(this::showGraph);
							} else {
								makeGraph(textSource);
							}
						}
					});
		}
	}

	private void makeGraph(TextSource textSource) {
		view.getModel().ifPresent(model ->
				getGraphNameInput(view, textSource, null)
						.ifPresent(graphName -> model.registerAction(
								new GraphSpaceAction(model, ADD, graphName, textSource))));

	}

	private static Optional<String> getGraphNameInput(KnowtatorView view, TextSource textSource, JTextField field1) {
		if (field1 == null) {
			field1 = new JTextField();

			JTextField finalField = field1;
			field1.getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void insertUpdate(DocumentEvent e) {
							warn();
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							warn();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							warn();
						}

						private void warn() {
							if (textSource.containsID(finalField.getText())) {
								try {
									finalField.getHighlighter().addHighlight(
											0,
											finalField.getText().length(),
											new DefaultHighlighter.DefaultHighlightPainter(Color.RED));
								} catch (BadLocationException e1) {
									e1.printStackTrace();
								}
							} else {
								finalField.getHighlighter().removeAllHighlights();
							}
						}
					});
		}
		Object[] message = {
				"Graph Title", field1,
		};
		field1.addAncestorListener(new GraphView.RequestFocusListener());
		field1.setText(String.format("Graph Space %d", textSource.getNumberOfGraphSpaces()));
		int option =
				JOptionPane.showConfirmDialog(
						view,
						message,
						"Enter a name for this graph",
						JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			if (textSource

					.containsID(field1.getText())) {
				JOptionPane.showMessageDialog(field1, "Graph name already in use");
				return getGraphNameInput(view, textSource, field1);
			} else {
				return Optional.of(field1.getText());
			}
		}

		return Optional.empty();
	}

	RelationOptionsDialog getRelationOptionsDialog(String propertyID) {
		RelationOptionsDialog relationOptionsDialog = new RelationOptionsDialog(dialog, propertyID);
		relationOptionsDialog.pack();
		relationOptionsDialog.setAlwaysOnTop(true);
		relationOptionsDialog.setLocationRelativeTo(dialog);
		relationOptionsDialog.requestFocus();
		relationOptionsDialog.setVisible(true);
		return relationOptionsDialog;
	}

	@Override
	public void reset() {
		graphSpaceChooser.reset();
	}

	@Override
	public void dispose() {
		graphSpaceChooser.dispose();
	}

	public mxGraphComponent getGraphComponent() {
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
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, BorderLayout.NORTH);
		final JToolBar toolBar1 = new JToolBar();
		toolBar1.setFloatable(false);
		panel2.add(toolBar1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
		addAnnotationNodeButton = new JButton();
		addAnnotationNodeButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
		addAnnotationNodeButton.setText("");
		toolBar1.add(addAnnotationNodeButton);
		removeCellButton = new JButton();
		removeCellButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
		removeCellButton.setText("");
		toolBar1.add(removeCellButton);
		final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
		toolBar1.add(toolBar$Separator1);
		applyLayoutButton = new JButton();
		applyLayoutButton.setText("Apply Layout");
		toolBar1.add(applyLayoutButton);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new BorderLayout(0, 0));
		panel1.add(panel3, BorderLayout.CENTER);
		graphComponent.setCenterPage(false);
		graphComponent.setGridVisible(true);
		panel3.add(graphComponent, BorderLayout.CENTER);
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel4, BorderLayout.SOUTH);
		graphSpaceChooser.setMaximumSize(new Dimension(200, 32767));
		graphSpaceChooser.setMinimumSize(new Dimension(80, 30));
		panel4.add(graphSpaceChooser, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		zoomSlider = new JSlider();
		zoomSlider.setMaximum(100);
		panel4.add(zoomSlider, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(194, 16), null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel4.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		previousGraphSpaceButton = new JButton();
		previousGraphSpaceButton.setText("Previous");
		panel5.add(previousGraphSpaceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		nextGraphSpaceButton = new JButton();
		nextGraphSpaceButton.setText("Next");
		panel5.add(nextGraphSpaceButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel4.add(panel6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		renameButton = new JButton();
		renameButton.setText("Rename");
		panel6.add(renameButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		exportToImagePNGButton = new JButton();
		exportToImagePNGButton.setText("Export to Image (PNG)");
		panel6.add(exportToImagePNGButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel7 = new JPanel();
		panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel4.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		addGraphSpaceButton = new JButton();
		addGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
		addGraphSpaceButton.setText("");
		panel7.add(addGraphSpaceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		removeGraphSpaceButton = new JButton();
		removeGraphSpaceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
		removeGraphSpaceButton.setText("");
		panel7.add(removeGraphSpaceButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel1;
	}

	@Override
	public void filterChangedEvent() {

	}


	@Override
	public void modelChangeEvent(ChangeEvent<ModelObject> event) {
		view.getModel()
				.filter(model -> isVisible())
				.ifPresent(model -> {
					model.getSelectedTextSource().ifPresent(textSource -> {
						if (textSource.getNumberOfGraphSpaces() == 0) {
							graphSpaceButtons.forEach(c -> c.setEnabled(false));
							addGraphSpaceButton.setEnabled(true);
						} else {
							graphSpaceButtons.forEach(c -> c.setEnabled(true));
						}
					});
					event.getNew()
							.filter(modelObject -> modelObject instanceof GraphSpace)
							.map(modelObject -> (GraphSpace) modelObject)
							.filter(graphSpace -> graphSpace != graphComponent.getGraph())
							.ifPresent(GraphView.this::showGraph);
					event.getNew()
							.filter(modelObject -> modelObject instanceof TextSource)
							.map(modelObject -> (TextSource) modelObject)
							.filter(textSource -> isVisible())
							.ifPresent(textSource -> textSource.getSelectedGraphSpace()
									.ifPresent(GraphView.this::showGraph));
					event.getNew()
							.filter(modelObject -> modelObject instanceof AnnotationNode)
							.map(modelObject -> (AnnotationNode) modelObject)
							.ifPresent(annotationNode -> this.showGraph(annotationNode.getGraphSpace()));
					event.getNew()
							.filter(modelObject -> modelObject instanceof RelationAnnotation)
							.map(modelObject -> (RelationAnnotation) modelObject)
							.ifPresent(relationAnnotation -> this.showGraph(relationAnnotation.getGraphSpace()));
				});
	}

	void reDrawGraph(@Nonnull GraphSpace graphSpace) {
		view.getModel()
				.filter(model -> view.isVisible())
				.filter(BaseModel::isNotLoading)
				.ifPresent(model -> {
					graphSpace.getModel().beginUpdate();
					try {
						Arrays.stream(graphSpace.getChildVertices(graphSpace.getDefaultParent()))
								.forEach(
										vertex -> {
											if (vertex instanceof AnnotationNode) {
												graphSpace.setVertexStyle((AnnotationNode) vertex);
											}
											graphSpace.updateCellSize(vertex);

											graphSpace.getView().validateCell(vertex);
										});
						Arrays.stream(graphSpace.getChildEdges(graphSpace.getDefaultParent()))
								.forEach(
										edge -> {
											graphSpace.updateCellSize(edge);
											//                if (edge instanceof RelationAnnotation) {
											//                    ((RelationAnnotation) edge).setValue(((RelationAnnotation) edge).getValue());
											//                }
											graphSpace.getView().validateCell(edge);
										});
					} finally {
						graphSpace.getModel().endUpdate();
						graphSpace.refresh();
					}
				});

	}

	@Override
	public void colorChangedEvent() {
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
		 *  @param removeListener when true this listener is only invoked once
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
		public void ancestorRemoved(AncestorEvent e) {
		}

		@Override
		public void ancestorMoved(AncestorEvent e) {
		}
	}


}

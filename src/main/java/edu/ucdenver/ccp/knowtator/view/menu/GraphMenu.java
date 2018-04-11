package edu.ucdenver.ccp.knowtator.view.menu;

import com.mxgraph.util.mxCellRenderer;
import edu.ucdenver.ccp.knowtator.KnowtatorController;

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

public class GraphMenu extends JMenu {

	private KnowtatorController controller;

	public GraphMenu(KnowtatorController controller) {
		super("Graph");
		this.controller = controller;

		add(addNewGraphCommand());
		add(renameGraphCommand());
		add(saveToImageCommand());
		add(deleteGraphCommand());
	}

	private JMenuItem renameGraphCommand() {
		JMenuItem menuItem = new JMenuItem("Rename Graph");
		menuItem.addActionListener(
				e -> {
					String graphName = getGraphNameInput(null);
					if (graphName != null) {
						controller.getSelectionManager().getActiveGraphSpace().setId(graphName);
					}
				});

		return menuItem;
	}

	private JMenuItem saveToImageCommand() {
		JMenuItem menuItem = new JMenuItem("Save as PNG");
		menuItem.addActionListener(
				e -> {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(controller.getProjectManager().getProjectLocation());
					FileFilter fileFilter = new FileNameExtensionFilter("PNG", "png");
					fileChooser.setFileFilter(fileFilter);
					if (fileChooser.showSaveDialog(controller.getView()) == JFileChooser.APPROVE_OPTION) {
						BufferedImage image =
								mxCellRenderer.createBufferedImage(
										controller.getSelectionManager().getActiveGraphSpace(),
										null,
										1,
										Color.WHITE,
										true,
										null);
						try {
							ImageIO.write(
									image, "PNG", new File(fileChooser.getSelectedFile().getAbsolutePath()));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});

		return menuItem;
	}

	private JMenuItem deleteGraphCommand() {
		JMenuItem deleteGraphMenuItem = new JMenuItem("Delete graph");
		deleteGraphMenuItem.addActionListener(
				e -> {
					if (JOptionPane.showConfirmDialog(
							controller.getView(), "Are you sure you want to delete this graph?")
							== JOptionPane.YES_OPTION) {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.removeGraphSpace(controller.getSelectionManager().getActiveGraphSpace());
					}
				});

		return deleteGraphMenuItem;
	}

	private JMenuItem addNewGraphCommand() {
		JMenuItem addNewGraphMenuItem = new JMenuItem("Create new graph");
		addNewGraphMenuItem.addActionListener(
				e -> {
					if (controller.getSelectionManager().getActiveTextSource() != null) {

						String graphName = getGraphNameInput(null);

						if (graphName != null) {
							controller
									.getSelectionManager()
									.getActiveTextSource()
									.getAnnotationManager()
									.addGraphSpace(graphName);
						}
					}
				});

		return addNewGraphMenuItem;
	}

	private String getGraphNameInput(JTextField field1) {
		if (field1 == null) {
			field1 = new JTextField();

			JTextField finalField = field1;
			field1
					.getDocument()
					.addDocumentListener(
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
									if (controller
											.getSelectionManager()
											.getActiveTextSource()
											.getAnnotationManager()
											.getGraphSpaceCollection()
											.containsID(finalField.getText())) {
										try {
											finalField
													.getHighlighter()
													.addHighlight(
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
		field1.addAncestorListener(new RequestFocusListener());
		int option =
				JOptionPane.showConfirmDialog(
						controller.getView(),
						message,
						"Enter a name for this graph",
						JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			if (controller
					.getSelectionManager()
					.getActiveTextSource()
					.getAnnotationManager()
					.getGraphSpaceCollection()
					.containsID(field1.getText())) {
				JOptionPane.showMessageDialog(field1, "Graph name already in use");
				return getGraphNameInput(field1);
			} else {
				return field1.getText();
			}
		}
		return null;
	}

	/**
	 * Taken from https://tips4java.wordpress.com/2010/03/14/dialog-focus/
	 */
	private class RequestFocusListener implements AncestorListener {
		private boolean removeListener;

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

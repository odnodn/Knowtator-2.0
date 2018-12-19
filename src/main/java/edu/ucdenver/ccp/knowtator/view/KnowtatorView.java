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

package edu.ucdenver.ccp.knowtator.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mxgraph.swing.util.mxGraphTransferable;
import edu.ucdenver.ccp.knowtator.iaa.IAAException;
import edu.ucdenver.ccp.knowtator.iaa.KnowtatorIAA;
import edu.ucdenver.ccp.knowtator.io.brat.BratStandoffUtil;
import edu.ucdenver.ccp.knowtator.model.*;
import edu.ucdenver.ccp.knowtator.model.collection.event.ChangeEvent;
import edu.ucdenver.ccp.knowtator.model.object.GraphSpace;
import edu.ucdenver.ccp.knowtator.model.object.ModelObject;
import edu.ucdenver.ccp.knowtator.model.object.TextSource;
import edu.ucdenver.ccp.knowtator.view.actions.ActionUnperformableException;
import edu.ucdenver.ccp.knowtator.view.actions.collection.ActionParameters;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.FilterAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.SpanActions;
import edu.ucdenver.ccp.knowtator.view.chooser.TextSourceChooser;
import edu.ucdenver.ccp.knowtator.view.graph.GraphViewDialog;
import edu.ucdenver.ccp.knowtator.view.label.AnnotationAnnotatorLabel;
import edu.ucdenver.ccp.knowtator.view.label.AnnotationClassLabel;
import edu.ucdenver.ccp.knowtator.view.label.AnnotationIDLabel;
import edu.ucdenver.ccp.knowtator.view.list.*;
import edu.ucdenver.ccp.knowtator.view.textpane.KnowtatorTextPane;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static edu.ucdenver.ccp.knowtator.view.actions.collection.AbstractKnowtatorCollectionAction.pickAction;
import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.ADD;
import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.REMOVE;
import static edu.ucdenver.ccp.knowtator.view.actions.collection.KnowtatorCollectionType.*;
import static edu.ucdenver.ccp.knowtator.view.actions.modelactions.ProfileAction.assignColorToClass;

/**
 * Main class for GUI
 *
 * @author Harrison Pielke-Lombardo
 */
public class KnowtatorView extends AbstractOWLClassViewComponent implements DropTargetListener, KnowtatorComponent, ModelListener {

	private static final Logger log = Logger.getLogger(KnowtatorView.class);
	public static final Preferences PREFERENCES = Preferences.userRoot().node("knowtator");

	private KnowtatorModel model;
	private GraphViewDialog graphViewDialog;
	private JComponent cardPanel;

	private KnowtatorTextPane textPane;
	private JTabbedPane header;
	private GraphSpaceList graphSpaceList;
	private AnnotationIDLabel annotationIDLabel;
	private AnnotationAnnotatorLabel annotationAnnotatorLabel;
	private AnnotationClassLabel annotationClassLabel;
	private AnnotationNotes annotationNotes;
	private SpanList spanList;
	private JButton shrinkEndButton;
	private JButton growEndButton;
	private JButton growStartButton;
	private JButton shrinkStartButton;
	private JButton previousSpanButton;
	private JButton nextSpanButton;
	private JButton addAnnotationButton;
	private JButton removeAnnotationButton;
	private JCheckBox profileFilterCheckBox;
	private JCheckBox owlClassFilterCheckBox;
	private JButton showGraphViewerButton;
	private JButton previousTextSourceButton;
	private JButton nextTextSourceButton;
	private JButton addTextSourceButton;
	private JButton removeTextSourceButton;
	private TextSourceChooser textSourceChooser;
	private JSlider fontSizeSlider;
	private JButton undoButton;
	private JButton redoButton;
	private JButton assignColorToClassButton;
	private JProgressBar progressBar;
	private JList<String> fileList;
	private JButton removeProfileButton;
	private ProfileList profileList;
	private JButton addProfileButton;
	private ColorList colorList;
	private AnnotationList annotationsForSpannedTextList;
	private JTextField annotationsContainingTextTextField;
	private JCheckBox exactMatchCheckBox;
	private JLabel owlClassLabel;
	private AnnotationList annotationsForClassList;
	private JCheckBox includeClassDescendantsCheckBox;
	private JLabel owlPropertyLabel;
	private RelationList relationsForPropertyList;
	private JButton previousReviewObjectButton;
	private JButton nextReviewObjectButton;
	private JCheckBox includePropertyDescendantsCheckBox;
	private JTextField searchTextField;
	private JButton nextMatchButton;
	private JButton previousMatchButton;
	private JButton findTextInOntologyButton;
	private JCheckBox onlyAnnotationsCheckBox;
	private JCheckBox regexCheckBox;
	private JCheckBox caseSensitiveCheckBox;
	private JTabbedPane reviewTabbedPane;
	private JButton refreshReviewPaneButton;
	private JPanel mainPanel;
	private JButton backButton;
	private JPanel filePanel;
	private JFileChooser fileChooser;
	private JSplitPane body;
	private JPanel infoPane;
	private JPanel spanPane;
	private JPanel annotationPaneButtons;
	private JPanel graphSpacePane;
	private JPanel undoPane;
	private JPanel filterPane;
	private JPanel externalButtonsPane;
	private JComponent rootPane;
	private JPanel textSourcePane;
	private JPanel iaaPane;
	private JButton runIAAButton;
	private JCheckBox iaaSpanCheckBox;
	private JCheckBox iaaClassAndSpanCheckBox;
	private JCheckBox iaaClassCheckBox;
	private JButton captureImageButton;

	private final List<KnowtatorComponent> knowtatorComponents;
	private HashMap<JButton, ActionListener> spanSizeButtons;
	private HashMap<JButton, ActionListener> selectionSizeButtons;


	/**
	 * Creates all components and sets up its model
	 */
	public KnowtatorView() {
		knowtatorComponents = new ArrayList<>();
		spanSizeButtons = new HashMap<>();
		selectionSizeButtons = new HashMap<>();

		$$$setupUI$$$();
		header.setSelectedIndex(1);
		makeButtons();

		// This is necessary to force OSGI to load the mxGraphTransferable class to allow node dragging.
		// It is kind of a hacky fix, but it works for now.
		log.warn("Don't worry about the following exception. Just forcing loading of a class needed by mxGraph");
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(String.format("%s; class=com.mxgraph.swing.util.mxGraphTransferable", DataFlavor.javaJVMLocalObjectMimeType), null, mxGraphTransferable.class.getClassLoader());
		} catch (ClassNotFoundException ignored) {

		}
	}

	private static void changeFont(Component component, Font font) {
		component.setFont(font);
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				changeFont(child, font);
			}
		}
	}

	public Optional<KnowtatorModel> getModel() {
		return Optional.ofNullable(model);
	}

	/**
	 * Inherited but not used
	 */
	@Override
	public void initialiseClassView() {
		changeFont(this, this.getParent().getFont());
	}

	/**
	 * Creates custom UI components like chooser boxes and labels that listen to the model.
	 */
	private void createUIComponents() {
		DropTarget dt = new DropTarget(this, this);
		dt.setActive(true);
		rootPane = this;

		searchTextField = new JTextField();
		regexCheckBox = new JCheckBox();
		onlyAnnotationsCheckBox = new JCheckBox();
		caseSensitiveCheckBox = new JCheckBox();
		includeClassDescendantsCheckBox = new JCheckBox();
		includePropertyDescendantsCheckBox = new JCheckBox();
		exactMatchCheckBox = new JCheckBox();
		owlClassLabel = new JLabel();
		owlPropertyLabel = new JLabel();

		textPane = new KnowtatorTextPane(this, searchTextField, onlyAnnotationsCheckBox, regexCheckBox, caseSensitiveCheckBox);
		graphViewDialog = new GraphViewDialog(this);

		annotationAnnotatorLabel = new AnnotationAnnotatorLabel(this);
		annotationClassLabel = new AnnotationClassLabel(this);
		annotationIDLabel = new AnnotationIDLabel(this);
		annotationNotes = new AnnotationNotes(this);

		textSourceChooser = new TextSourceChooser(this);

		graphSpaceList = new GraphSpaceList(this);
		spanList = new SpanList(this);
		profileList = new ProfileList(this);
		colorList = new ColorList(this);

		annotationsContainingTextTextField = new JTextField();

		annotationsForClassList = new AnnotationListForOWLClass(this, includeClassDescendantsCheckBox, owlClassLabel);
		annotationsForSpannedTextList = new AnnotationListForSpannedText(this, exactMatchCheckBox, annotationsContainingTextTextField);
		relationsForPropertyList = new RelationList(this, includePropertyDescendantsCheckBox, owlPropertyLabel);

		knowtatorComponents.addAll(Arrays.asList(
				profileList,
				colorList,
				textPane,
				graphViewDialog,
				annotationNotes,
				annotationIDLabel,
				annotationAnnotatorLabel,
				annotationClassLabel,
				spanList,
				graphSpaceList,
				textSourceChooser,
				annotationsForSpannedTextList,
				annotationsForClassList,
				relationsForPropertyList));


		// The following methods keep the graph view dialog on top only when the view is active.
		KnowtatorView view = this;
		addAncestorListener(
				new AncestorListener() {
					@Override
					public void ancestorAdded(AncestorEvent event) {
						Window ancestor = SwingUtilities.getWindowAncestor(view);
						ancestor.addWindowFocusListener(
								new WindowFocusListener() {
									@Override
									public void windowGainedFocus(WindowEvent e) {
										graphViewDialog.setAlwaysOnTop(true);
									}

									@Override
									public void windowLostFocus(WindowEvent e) {
										if (e.getOppositeWindow() != graphViewDialog) {
											graphViewDialog.setAlwaysOnTop(false);
											graphViewDialog.toBack();
										}
									}
								});
					}

					@Override
					public void ancestorRemoved(AncestorEvent event) {
					}

					@Override
					public void ancestorMoved(AncestorEvent event) {
					}
				});
	}

	/**
	 * Makes the buttons in the main display pane
	 */
	private void makeButtons() {
		captureImageButton.addActionListener(e -> getModel()
				.ifPresent(model -> model.getSelectedTextSource()
						.ifPresent(textSource -> {
							JFileChooser fileChooser = new JFileChooser(model.getSaveLocation());
							fileChooser.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
							fileChooser.setSelectedFile(new File(String.format("%s_annotations.png", textSource.getId())));
							if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
								textSource.setSelectedConceptAnnotation(null);
								BufferedImage image = textPane.getScreenShot();
								try {
									ImageIO.write(image, "png", fileChooser.getSelectedFile());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						})));


		backButton.addActionListener(e -> {
			if (model.isNotLoading()) {
				CardLayout cl = (CardLayout) cardPanel.getLayout();
				cl.show(cardPanel, "Main");
				header.setSelectedIndex(1);
			}
		});

		header.addChangeListener(e -> {
			if (header.getTitleAt(header.getSelectedIndex()).equals("File")) {
				CardLayout cl = (CardLayout) cardPanel.getLayout();
				cl.show(cardPanel, "File");
				fileList.setSelectedIndex(0);
			}
		});

		findTextInOntologyButton.addActionListener(e ->
				getModel()
						.ifPresent(model1 -> model1.searchForString(searchTextField.getText())));
		nextMatchButton.addActionListener(e -> textPane.searchForward());
		previousMatchButton.addActionListener(e -> textPane.searchPrevious());

		makeReviewPane();

		addProfileButton.addActionListener(e -> Optional.ofNullable(JOptionPane.showInputDialog(this, "Enter a name for the profile"))
				.ifPresent(profileName -> pickAction(this, profileName, null, new ActionParameters(ADD, PROFILE))));
		removeProfileButton.addActionListener(e -> Optional.of(JOptionPane.showConfirmDialog(this, "Are you sure you wish to remove this profile?"))
				.filter(result -> JOptionPane.OK_OPTION == result)
				.ifPresent(result -> pickAction(this, null, null, new ActionParameters(REMOVE, PROFILE))));

		addAnnotationButton.addActionListener(e -> pickAction(this, null, null, new ActionParameters(ADD, ANNOTATION), new ActionParameters(ADD, SPAN)));
		removeAnnotationButton.addActionListener(e -> pickAction(this, null, null, new ActionParameters(REMOVE, ANNOTATION), new ActionParameters(REMOVE, SPAN)));

		makeSpanButtons();

		fontSizeSlider.setValue(textPane.getFont().getSize());
		fontSizeSlider.addChangeListener(e -> textPane.setFontSize(fontSizeSlider.getValue()));
		showGraphViewerButton.addActionListener(e -> graphViewDialog.setVisible(true));
		previousTextSourceButton.addActionListener(e ->
				getModel().ifPresent(BaseModel::selectPreviousTextSource));
		nextTextSourceButton.addActionListener(e ->
				getModel().ifPresent(BaseModel::selectNextTextSource));
		addTextSourceButton.addActionListener(e -> {
			JFileChooser fileChooser = new JFileChooser();
			getModel().ifPresent(model1 -> {
				fileChooser.setCurrentDirectory(model1.getArticlesLocation());

				if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					pickAction(this, null, fileChooser.getSelectedFile(), new ActionParameters(ADD, DOCUMENT));
				}
			});

		});

		assignColorToClassButton.addActionListener(e ->
				getModel()
						.flatMap(OWLModel::getSelectedOWLClass)
						.ifPresent(owlClass ->
								assignColorToClass(this, owlClass)));

		removeTextSourceButton.addActionListener(e -> pickAction(this, null, null, new ActionParameters(REMOVE, DOCUMENT)));

		undoButton.addActionListener(e -> getModel()
				.filter(UndoManager::canUndo)
				.ifPresent(UndoManager::undo));
		redoButton.addActionListener(e -> getModel()
				.filter(UndoManager::canRedo)
				.ifPresent(UndoManager::redo));

		owlClassFilterCheckBox.setSelected(false);
		profileFilterCheckBox.setSelected(false);

		profileFilterCheckBox.addItemListener(e ->
				getModel()
						.ifPresent(knowtatorModel ->
						{
							try {
								knowtatorModel.registerAction(
										new FilterAction(knowtatorModel, FilterType.PROFILE, profileFilterCheckBox.isSelected()));
							} catch (ActionUnperformableException e1) {
								JOptionPane.showMessageDialog(this, e1.getMessage());
							}
						}));
		owlClassFilterCheckBox.addItemListener(e ->
				getModel()
						.ifPresent(knowtatorModel ->
						{
							try {
								knowtatorModel.registerAction(
										new FilterAction(knowtatorModel, FilterType.OWLCLASS, owlClassFilterCheckBox.isSelected()));
							} catch (ActionUnperformableException e1) {
								JOptionPane.showMessageDialog(this, e1.getMessage());
							}
						}));


		Optional.ofNullable(KnowtatorView.PREFERENCES.get("Last Project", null))
				.map(File::new)
				.filter(File::exists)
				.map(file -> {
					fileChooser.setCurrentDirectory(file);
					return file;
				})
				.map(File::listFiles)
				.flatMap(files -> Arrays.stream(files).filter(file1 -> file1.getName().endsWith(".knowtator")).findAny())
				.ifPresent(fileChooser::setSelectedFile);
		fileChooser.addActionListener(e -> Optional.ofNullable(e)
				.filter(event -> event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
				.ifPresent(event -> open()));

		fileList.addListSelectionListener(e -> {
			switch (fileList.getSelectedValue()) {
				case "Open":
					FileFilter fileFilter = new FileNameExtensionFilter("Knowtator", "knowtator");
					fileChooser.setFileFilter(fileFilter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					break;
				case "New":
					fileChooser.setFileFilter(null);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					break;
				case "Import":
					fileFilter = new FileNameExtensionFilter(
							"ConceptAnnotation File (XML, ann, a1)",
							"xml", "ann", "a1");
					fileChooser.setFileFilter(fileFilter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					break;
				case "Export":
					fileChooser.setFileFilter(null);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					break;
			}
		});

		runIAAButton.addActionListener(e -> getModel().ifPresent(model -> {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(model.getSaveLocation());
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			//
			// disable the "All files" option.
			//
			fileChooser.setAcceptAllFileFilterUsed(false);
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File outputDirectory = fileChooser.getSelectedFile();

				try {
					KnowtatorIAA knowtatorIAA = new KnowtatorIAA(outputDirectory, model);

					if (iaaClassCheckBox.isSelected()) {
						knowtatorIAA.runClassIAA();
					}
					if (iaaSpanCheckBox.isSelected()) {
						knowtatorIAA.runSpanIAA();
					}
					if (iaaClassAndSpanCheckBox.isSelected()) {
						knowtatorIAA.runClassAndSpanIAA();
					}

					knowtatorIAA.closeHTML();
				} catch (IAAException e1) {
					e1.printStackTrace();
				}

			}
		}));
	}

	private void open() {
		switch (fileList.getSelectedValue()) {
			case "Open":
				new Loader(this, fileChooser.getSelectedFile(), progressBar, header, cardPanel)
						.execute();
				break;
			case "New":
				Optional.ofNullable(JOptionPane.showInputDialog(this, "Enter a name for the project"))
						.filter(projectName -> !projectName.equals(""))
						.ifPresent(projectName -> {
							File projectDirectory = new File(fileChooser.getSelectedFile(), projectName);
							new Loader(this,
									projectDirectory,
									progressBar,
									header,
									cardPanel)
									.execute();
						});
				break;
			case "Import":
				getModel().ifPresent(model1 -> model1.loadWithAppropriateFormat(model1.getTextSources(), fileChooser.getSelectedFile()));
				break;
			case "Export":
				model.saveToFormat(BratStandoffUtil.class, model.getTextSources(), fileChooser.getSelectedFile());
				break;
		}

	}

	private void makeSpanButtons() {
		nextSpanButton.addActionListener(e -> getModel()
				.flatMap(BaseModel::getSelectedTextSource)
				.ifPresent(TextSource::selectNextSpan));
		previousSpanButton.addActionListener(e -> getModel()
				.flatMap(BaseModel::getSelectedTextSource)
				.ifPresent(TextSource::selectPreviousSpan));

		spanSizeButtons.put(shrinkEndButton, e -> getModel()
				.ifPresent(model1 -> model1.getSelectedTextSource()
						.ifPresent(textSource -> textSource.getSelectedAnnotation()
								.ifPresent(conceptAnnotation -> conceptAnnotation.getSelection()
										.ifPresent(span -> {
											try {
												model1.registerAction(
														new SpanActions.ModifySpanAction(model1, SpanActions.END, SpanActions.SHRINK, span));
											} catch (ActionUnperformableException e1) {
												JOptionPane.showMessageDialog(this, e1.getMessage());
											}
										})))));

		spanSizeButtons.put(shrinkStartButton, e -> getModel()
				.ifPresent(model1 -> model1.getSelectedTextSource()
						.ifPresent(textSource -> textSource.getSelectedAnnotation()
								.ifPresent(conceptAnnotation -> conceptAnnotation.getSelection()
										.ifPresent(span -> {
											try {
												model1.registerAction(new SpanActions.ModifySpanAction(model1, SpanActions.START, SpanActions.SHRINK, span));
											} catch (ActionUnperformableException e1) {
												JOptionPane.showMessageDialog(this, e1.getMessage());
											}
										})))));
		spanSizeButtons.put(growEndButton, e -> getModel()
				.ifPresent(model1 -> model1.getSelectedTextSource()
						.ifPresent(textSource -> textSource.getSelectedAnnotation()
								.ifPresent(conceptAnnotation -> conceptAnnotation.getSelection()
										.ifPresent(span -> {
											try {
												model1.registerAction(new SpanActions.ModifySpanAction(model1, SpanActions.END, SpanActions.GROW, span));
											} catch (ActionUnperformableException e1) {
												JOptionPane.showMessageDialog(this, e1.getMessage());
											}
										})))));
		spanSizeButtons.put(growStartButton, e -> getModel()
				.ifPresent(model1 -> model1.getSelectedTextSource()
						.ifPresent(textSource -> textSource.getSelectedAnnotation()
								.ifPresent(conceptAnnotation -> conceptAnnotation.getSelection()
										.ifPresent(span -> {
											try {
												model1.registerAction(new SpanActions.ModifySpanAction(model1, SpanActions.START, SpanActions.GROW, span));
											} catch (ActionUnperformableException e1) {
												JOptionPane.showMessageDialog(this, e1.getMessage());
											}
										})))));

		selectionSizeButtons.put(shrinkEndButton, e -> SpanActions.modifySelection(this, SpanActions.END, SpanActions.SHRINK));
		selectionSizeButtons.put(shrinkStartButton, e -> SpanActions.modifySelection(this, SpanActions.START, SpanActions.SHRINK));
		selectionSizeButtons.put(growEndButton, e -> SpanActions.modifySelection(this, SpanActions.END, SpanActions.GROW));
		selectionSizeButtons.put(growStartButton, e -> SpanActions.modifySelection(this, SpanActions.START, SpanActions.GROW));
	}

	private void makeReviewPane() {
		//TODO The Knowtator list doesn't seem to be being found so none of these methods are working
		refreshReviewPaneButton.addActionListener(e -> getKnowtatorList()
				.ifPresent(KnowtatorList::reset));

		nextReviewObjectButton.addActionListener(e -> getKnowtatorList()
				.ifPresent(knowtatorList -> {
					knowtatorList.setSelectedIndex(Math.min(knowtatorList.getSelectedIndex() + 1, knowtatorList.getModel().getSize() - 1));
					knowtatorList.ensureIndexIsVisible(knowtatorList.getSelectedIndex());
					knowtatorList.reactToClick();
				}));
		previousReviewObjectButton.addActionListener(e -> getKnowtatorList()
				.ifPresent(knowtatorList -> {
					knowtatorList.setSelectedIndex(Math.max(knowtatorList.getSelectedIndex() - 1, 0));
					knowtatorList.ensureIndexIsVisible(knowtatorList.getSelectedIndex());
					knowtatorList.reactToClick();
				}));
	}

	private Optional<KnowtatorList> getKnowtatorList() {
		return Arrays.stream(((JPanel) reviewTabbedPane.getSelectedComponent()).getComponents())
				.filter(component -> component instanceof JScrollPane)
				.findFirst()
				.map(component -> (JScrollPane) component)
				.map(jScrollPane -> jScrollPane.getViewport().getComponent(0))
				.map(component -> (KnowtatorList) component);
	}

	@Override
	protected OWLClass updateView(OWLClass selectedClass) {
		return selectedClass;
	}

	@Override
	public void reset() {
		knowtatorComponents.forEach(KnowtatorComponent::reset);
		getModel().ifPresent(model1 -> model1.addModelListener(this));
		getModel().ifPresent(model1 -> model1.addOWLModelManagerListener(annotationClassLabel));

	}

	/**
	 * @return The graph view dialog
	 */
	public GraphViewDialog getGraphViewDialog() {
		return graphViewDialog;
	}


	/**
	 * @return the Knowtator text pane
	 */
	public KnowtatorTextPane getTextPane() {
		return textPane;
	}

	/**
	 * Calls dispose on the model and all components
	 */
	@Override
	public void disposeView() {
		getModel().ifPresent(BaseModel::dispose);
		knowtatorComponents.forEach(KnowtatorComponent::dispose);
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent e) {
	}

	@Override
	public void dragExit(DropTargetEvent e) {
	}

	@Override
	public void drop(DropTargetDropEvent e) {
	}

	@Override
	public void dragEnter(DropTargetDragEvent e) {
	}

	@Override
	public void dragOver(DropTargetDragEvent e) {
	}

	public void loadProject(File file, ModelListener progressListener) throws IOException {
		if (!getModel().isPresent() || getModel().get().isNotLoading()) {
			getModel().ifPresent(BaseModel::dispose);
			if (getOWLWorkspace() != null) {
				setModel(new KnowtatorModel(file, getOWLWorkspace()));
			} else {
				setModel(new KnowtatorModel(file, null));
			}
			if (progressListener != null) {
				getModel().ifPresent(model1 -> model1.addModelListener(progressListener));
			}
			log.info(String.format("Opening from %s", file.getAbsolutePath()));
			getModel().ifPresent(OWLModel::load);
			if (progressListener != null) {
				getModel().ifPresent(model1 -> model1.removeModelListener(progressListener));
			}
			reset();

			getModel().ifPresent(BaseModel::selectFirstTextSource);
			textPane.showTextSource();

			getModel().ifPresent(model1 -> KnowtatorView.PREFERENCES.put("Last Project", model1.getProjectLocation().getAbsolutePath()));

			try {
				KnowtatorView.PREFERENCES.flush();
			} catch (BackingStoreException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void setModel(KnowtatorModel model) {
		this.model = model;
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
		rootPane.setLayout(new BorderLayout(0, 0));
		cardPanel = new JPanel();
		cardPanel.setLayout(new CardLayout(0, 0));
		rootPane.add(cardPanel, BorderLayout.CENTER);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 0));
		cardPanel.add(mainPanel, "Main");
		body = new JSplitPane();
		mainPanel.add(body, BorderLayout.CENTER);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout(0, 0));
		body.setRightComponent(panel1);
		infoPane = new JPanel();
		infoPane.setLayout(new GridLayoutManager(11, 2, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.setMaximumSize(new Dimension(200, 2147483647));
		infoPane.setMinimumSize(new Dimension(200, 158));
		panel1.add(infoPane, BorderLayout.CENTER);
		infoPane.setBorder(BorderFactory.createTitledBorder(null, "Info", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 18, infoPane.getFont())));
		spanPane = new JPanel();
		spanPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.add(spanPane, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		spanPane.setBorder(BorderFactory.createTitledBorder(null, "Spans", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 16, spanPane.getFont())));
		final JScrollPane scrollPane1 = new JScrollPane();
		spanPane.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		Font spanListFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, spanList.getFont());
		if (spanListFont != null) spanList.setFont(spanListFont);
		scrollPane1.setViewportView(spanList);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
		spanPane.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		growStartButton = new JButton();
		Font growStartButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, growStartButton.getFont());
		if (growStartButtonFont != null) growStartButton.setFont(growStartButtonFont);
		growStartButton.setText("Grow Start");
		panel2.add(growStartButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		shrinkStartButton = new JButton();
		Font shrinkStartButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, shrinkStartButton.getFont());
		if (shrinkStartButtonFont != null) shrinkStartButton.setFont(shrinkStartButtonFont);
		shrinkStartButton.setText("Shrink Start");
		panel2.add(shrinkStartButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		shrinkEndButton = new JButton();
		Font shrinkEndButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, shrinkEndButton.getFont());
		if (shrinkEndButtonFont != null) shrinkEndButton.setFont(shrinkEndButtonFont);
		shrinkEndButton.setText("Shrink End");
		panel2.add(shrinkEndButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		growEndButton = new JButton();
		Font growEndButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, growEndButton.getFont());
		if (growEndButtonFont != null) growEndButton.setFont(growEndButtonFont);
		growEndButton.setText("Grow End");
		panel2.add(growEndButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel2.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		graphSpacePane = new JPanel();
		graphSpacePane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.add(graphSpacePane, new GridConstraints(9, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		graphSpacePane.setBorder(BorderFactory.createTitledBorder(null, "Graph Spaces", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 16, graphSpacePane.getFont())));
		final JScrollPane scrollPane2 = new JScrollPane();
		graphSpacePane.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		Font graphSpaceListFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, graphSpaceList.getFont());
		if (graphSpaceListFont != null) graphSpaceList.setFont(graphSpaceListFont);
		scrollPane2.setViewportView(graphSpaceList);
		final JScrollPane scrollPane3 = new JScrollPane();
		infoPane.add(scrollPane3, new GridConstraints(3, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane3.setBorder(BorderFactory.createTitledBorder(null, "Notes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, scrollPane3.getFont())));
		Font annotationNotesFont = this.$$$getFont$$$("Verdana", -1, 12, annotationNotes.getFont());
		if (annotationNotesFont != null) annotationNotes.setFont(annotationNotesFont);
		scrollPane3.setViewportView(annotationNotes);
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel3.setBorder(BorderFactory.createTitledBorder(null, "ID", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel3.getFont())));
		Font annotationIDLabelFont = this.$$$getFont$$$("Verdana", -1, -1, annotationIDLabel.getFont());
		if (annotationIDLabelFont != null) annotationIDLabel.setFont(annotationIDLabelFont);
		annotationIDLabel.setText("");
		panel3.add(annotationIDLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel4.setBorder(BorderFactory.createTitledBorder(null, "Annotator", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel4.getFont())));
		Font annotationAnnotatorLabelFont = this.$$$getFont$$$("Verdana", -1, -1, annotationAnnotatorLabel.getFont());
		if (annotationAnnotatorLabelFont != null) annotationAnnotatorLabel.setFont(annotationAnnotatorLabelFont);
		panel4.add(annotationAnnotatorLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel5 = new JPanel();
		panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel5.setBorder(BorderFactory.createTitledBorder(null, "OWL CLass", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel5.getFont())));
		Font annotationClassLabelFont = this.$$$getFont$$$("Verdana", -1, -1, annotationClassLabel.getFont());
		if (annotationClassLabelFont != null) annotationClassLabel.setFont(annotationClassLabelFont);
		annotationClassLabel.setText("");
		panel5.add(annotationClassLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		infoPane.add(spacer2, new GridConstraints(3, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane4 = new JScrollPane();
		body.setLeftComponent(scrollPane4);
		textPane.setMinimumSize(new Dimension(200, 22));
		textPane.setPreferredSize(new Dimension(500, 500));
		scrollPane4.setViewportView(textPane);
		header = new JTabbedPane();
		Font headerFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 14, header.getFont());
		if (headerFont != null) header.setFont(headerFont);
		header.setPreferredSize(new Dimension(788, 200));
		header.setTabLayoutPolicy(0);
		header.setTabPlacement(1);
		mainPanel.add(header, BorderLayout.NORTH);
		final JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		header.addTab("File", panel6);
		final Spacer spacer3 = new Spacer();
		panel6.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel7 = new JPanel();
		panel7.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
		header.addTab("Home", panel7);
		final Spacer spacer4 = new Spacer();
		panel7.add(spacer4, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		undoPane = new JPanel();
		undoPane.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel7.add(undoPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		undoButton = new JButton();
		undoButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-undo-32.png")));
		undoButton.setText("");
		undoPane.add(undoButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		redoButton = new JButton();
		redoButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-redo-32.png")));
		redoButton.setText("");
		undoPane.add(redoButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		externalButtonsPane = new JPanel();
		externalButtonsPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel7.add(externalButtonsPane, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		assignColorToClassButton = new JButton();
		assignColorToClassButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-color-dropper-filled-50 (Custom).png")));
		assignColorToClassButton.setText("");
		externalButtonsPane.add(assignColorToClassButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		showGraphViewerButton = new JButton();
		Font showGraphViewerButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, showGraphViewerButton.getFont());
		if (showGraphViewerButtonFont != null) showGraphViewerButton.setFont(showGraphViewerButtonFont);
		showGraphViewerButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-tree-structure-32.png")));
		showGraphViewerButton.setText("");
		externalButtonsPane.add(showGraphViewerButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer5 = new Spacer();
		panel7.add(spacer5, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		filterPane = new JPanel();
		filterPane.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel7.add(filterPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		owlClassFilterCheckBox = new JCheckBox();
		Font owlClassFilterCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, owlClassFilterCheckBox.getFont());
		if (owlClassFilterCheckBoxFont != null) owlClassFilterCheckBox.setFont(owlClassFilterCheckBoxFont);
		owlClassFilterCheckBox.setText("OWL Class");
		filterPane.add(owlClassFilterCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		profileFilterCheckBox = new JCheckBox();
		Font profileFilterCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, profileFilterCheckBox.getFont());
		if (profileFilterCheckBoxFont != null) profileFilterCheckBox.setFont(profileFilterCheckBoxFont);
		profileFilterCheckBox.setText("Profile");
		filterPane.add(profileFilterCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		textSourcePane = new JPanel();
		textSourcePane.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel7.add(textSourcePane, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		textSourcePane.setBorder(BorderFactory.createTitledBorder(null, "Document", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, textSourcePane.getFont())));
		textSourceChooser.setPreferredSize(new Dimension(150, 24));
		textSourcePane.add(textSourceChooser, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel8 = new JPanel();
		panel8.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		textSourcePane.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		previousTextSourceButton = new JButton();
		previousTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24 (reversed).png")));
		previousTextSourceButton.setText("");
		panel8.add(previousTextSourceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		nextTextSourceButton = new JButton();
		nextTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24.png")));
		nextTextSourceButton.setText("");
		panel8.add(nextTextSourceButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		addTextSourceButton = new JButton();
		addTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
		addTextSourceButton.setText("");
		panel8.add(addTextSourceButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		removeTextSourceButton = new JButton();
		removeTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
		removeTextSourceButton.setText("");
		panel8.add(removeTextSourceButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		fontSizeSlider = new JSlider();
		fontSizeSlider.setInverted(false);
		fontSizeSlider.setMajorTickSpacing(8);
		fontSizeSlider.setMaximum(28);
		fontSizeSlider.setMinimum(8);
		fontSizeSlider.setMinorTickSpacing(1);
		fontSizeSlider.setSnapToTicks(true);
		fontSizeSlider.setValue(16);
		textSourcePane.add(fontSizeSlider, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		annotationPaneButtons = new JPanel();
		annotationPaneButtons.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
		panel7.add(annotationPaneButtons, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		annotationPaneButtons.setBorder(BorderFactory.createTitledBorder(null, "Annotation", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, annotationPaneButtons.getFont())));
		addAnnotationButton = new JButton();
		Font addAnnotationButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, addAnnotationButton.getFont());
		if (addAnnotationButtonFont != null) addAnnotationButton.setFont(addAnnotationButtonFont);
		addAnnotationButton.setHorizontalTextPosition(0);
		addAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
		addAnnotationButton.setText("");
		addAnnotationButton.setVerticalAlignment(0);
		addAnnotationButton.setVerticalTextPosition(3);
		annotationPaneButtons.add(addAnnotationButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		removeAnnotationButton = new JButton();
		Font removeAnnotationButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, removeAnnotationButton.getFont());
		if (removeAnnotationButtonFont != null) removeAnnotationButton.setFont(removeAnnotationButtonFont);
		removeAnnotationButton.setHorizontalTextPosition(0);
		removeAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
		removeAnnotationButton.setText("");
		removeAnnotationButton.setVerticalTextPosition(3);
		annotationPaneButtons.add(removeAnnotationButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final Spacer spacer6 = new Spacer();
		annotationPaneButtons.add(spacer6, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		previousSpanButton = new JButton();
		previousSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24 (reversed).png")));
		previousSpanButton.setText("");
		annotationPaneButtons.add(previousSpanButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		nextSpanButton = new JButton();
		nextSpanButton.setHorizontalAlignment(0);
		nextSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24.png")));
		nextSpanButton.setText("");
		annotationPaneButtons.add(nextSpanButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JPanel panel9 = new JPanel();
		panel9.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		header.addTab("Profile", panel9);
		final JScrollPane scrollPane5 = new JScrollPane();
		panel9.add(scrollPane5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane5.setBorder(BorderFactory.createTitledBorder(null, "Colors", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, scrollPane5.getFont())));
		Font colorListFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, colorList.getFont());
		if (colorListFont != null) colorList.setFont(colorListFont);
		scrollPane5.setViewportView(colorList);
		final JPanel panel10 = new JPanel();
		panel10.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel9.add(panel10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel10.setBorder(BorderFactory.createTitledBorder(null, "Profiles", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel10.getFont())));
		final JScrollPane scrollPane6 = new JScrollPane();
		panel10.add(scrollPane6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		Font profileListFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 12, profileList.getFont());
		if (profileListFont != null) profileList.setFont(profileListFont);
		scrollPane6.setViewportView(profileList);
		final JPanel panel11 = new JPanel();
		panel11.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		panel10.add(panel11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		addProfileButton = new JButton();
		Font addProfileButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, addProfileButton.getFont());
		if (addProfileButtonFont != null) addProfileButton.setFont(addProfileButtonFont);
		addProfileButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
		addProfileButton.setText("");
		panel11.add(addProfileButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		removeProfileButton = new JButton();
		removeProfileButton.setEnabled(true);
		Font removeProfileButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, removeProfileButton.getFont());
		if (removeProfileButtonFont != null) removeProfileButton.setFont(removeProfileButtonFont);
		removeProfileButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
		removeProfileButton.setText("");
		panel11.add(removeProfileButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer7 = new Spacer();
		panel11.add(spacer7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		iaaPane = new JPanel();
		iaaPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
		panel9.add(iaaPane, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		iaaPane.setBorder(BorderFactory.createTitledBorder(null, "IAA", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, iaaPane.getFont())));
		runIAAButton = new JButton();
		Font runIAAButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, runIAAButton.getFont());
		if (runIAAButtonFont != null) runIAAButton.setFont(runIAAButtonFont);
		this.$$$loadButtonText$$$(runIAAButton, ResourceBundle.getBundle("log4j").getString("run.iaa"));
		iaaPane.add(runIAAButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		iaaSpanCheckBox = new JCheckBox();
		Font iaaSpanCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, iaaSpanCheckBox.getFont());
		if (iaaSpanCheckBoxFont != null) iaaSpanCheckBox.setFont(iaaSpanCheckBoxFont);
		this.$$$loadButtonText$$$(iaaSpanCheckBox, ResourceBundle.getBundle("ui").getString("span1"));
		iaaPane.add(iaaSpanCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		iaaClassAndSpanCheckBox = new JCheckBox();
		Font iaaClassAndSpanCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, iaaClassAndSpanCheckBox.getFont());
		if (iaaClassAndSpanCheckBoxFont != null) iaaClassAndSpanCheckBox.setFont(iaaClassAndSpanCheckBoxFont);
		this.$$$loadButtonText$$$(iaaClassAndSpanCheckBox, ResourceBundle.getBundle("ui").getString("class.and.span"));
		iaaPane.add(iaaClassAndSpanCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		iaaClassCheckBox = new JCheckBox();
		Font iaaClassCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, iaaClassCheckBox.getFont());
		if (iaaClassCheckBoxFont != null) iaaClassCheckBox.setFont(iaaClassCheckBoxFont);
		this.$$$loadButtonText$$$(iaaClassCheckBox, ResourceBundle.getBundle("log4j").getString("class1"));
		iaaPane.add(iaaClassCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer8 = new Spacer();
		panel9.add(spacer8, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel12 = new JPanel();
		panel12.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
		header.addTab("Search", panel12);
		Font searchTextFieldFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, searchTextField.getFont());
		if (searchTextFieldFont != null) searchTextField.setFont(searchTextFieldFont);
		panel12.add(searchTextField, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 25), new Dimension(-1, 25), 0, false));
		final JPanel panel13 = new JPanel();
		panel13.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel12.add(panel13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		nextMatchButton = new JButton();
		Font nextMatchButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, nextMatchButton.getFont());
		if (nextMatchButtonFont != null) nextMatchButton.setFont(nextMatchButtonFont);
		nextMatchButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24.png")));
		nextMatchButton.setText("");
		panel13.add(nextMatchButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		previousMatchButton = new JButton();
		Font previousMatchButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, previousMatchButton.getFont());
		if (previousMatchButtonFont != null) previousMatchButton.setFont(previousMatchButtonFont);
		previousMatchButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24 (reversed).png")));
		previousMatchButton.setText("");
		panel13.add(previousMatchButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		findTextInOntologyButton = new JButton();
		Font findTextInOntologyButtonFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, findTextInOntologyButton.getFont());
		if (findTextInOntologyButtonFont != null) findTextInOntologyButton.setFont(findTextInOntologyButtonFont);
		this.$$$loadButtonText$$$(findTextInOntologyButton, ResourceBundle.getBundle("log4j").getString("find.in.ontology1"));
		panel12.add(findTextInOntologyButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		Font onlyAnnotationsCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, onlyAnnotationsCheckBox.getFont());
		if (onlyAnnotationsCheckBoxFont != null) onlyAnnotationsCheckBox.setFont(onlyAnnotationsCheckBoxFont);
		onlyAnnotationsCheckBox.setText("Only in Annotations");
		panel12.add(onlyAnnotationsCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		Font regexCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, regexCheckBox.getFont());
		if (regexCheckBoxFont != null) regexCheckBox.setFont(regexCheckBoxFont);
		regexCheckBox.setText("Regex");
		panel12.add(regexCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		Font caseSensitiveCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, caseSensitiveCheckBox.getFont());
		if (caseSensitiveCheckBoxFont != null) caseSensitiveCheckBox.setFont(caseSensitiveCheckBoxFont);
		caseSensitiveCheckBox.setText("Case Sensitive");
		panel12.add(caseSensitiveCheckBox, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer9 = new Spacer();
		panel12.add(spacer9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer10 = new Spacer();
		panel12.add(spacer10, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel14 = new JPanel();
		panel14.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
		header.addTab("Review", panel14);
		reviewTabbedPane = new JTabbedPane();
		Font reviewTabbedPaneFont = this.$$$getFont$$$("Verdana", -1, 14, reviewTabbedPane.getFont());
		if (reviewTabbedPaneFont != null) reviewTabbedPane.setFont(reviewTabbedPaneFont);
		reviewTabbedPane.setTabPlacement(2);
		panel14.add(reviewTabbedPane, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		final JPanel panel15 = new JPanel();
		panel15.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		reviewTabbedPane.addTab("Text", panel15);
		final JScrollPane scrollPane7 = new JScrollPane();
		panel15.add(scrollPane7, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane7.setViewportView(annotationsForSpannedTextList);
		Font exactMatchCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 12, exactMatchCheckBox.getFont());
		if (exactMatchCheckBoxFont != null) exactMatchCheckBox.setFont(exactMatchCheckBoxFont);
		this.$$$loadButtonText$$$(exactMatchCheckBox, ResourceBundle.getBundle("log4j").getString("exact.match"));
		panel15.add(exactMatchCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel16 = new JPanel();
		panel16.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel15.add(panel16, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel16.setBorder(BorderFactory.createTitledBorder(null, "Text", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel16.getFont())));
		panel16.add(annotationsContainingTextTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JPanel panel17 = new JPanel();
		panel17.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		reviewTabbedPane.addTab("Concept", panel17);
		final JScrollPane scrollPane8 = new JScrollPane();
		panel17.add(scrollPane8, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane8.setViewportView(annotationsForClassList);
		Font includeClassDescendantsCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 12, includeClassDescendantsCheckBox.getFont());
		if (includeClassDescendantsCheckBoxFont != null)
			includeClassDescendantsCheckBox.setFont(includeClassDescendantsCheckBoxFont);
		this.$$$loadButtonText$$$(includeClassDescendantsCheckBox, ResourceBundle.getBundle("log4j").getString("include.descendants"));
		panel17.add(includeClassDescendantsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel18 = new JPanel();
		panel18.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel17.add(panel18, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel18.setBorder(BorderFactory.createTitledBorder(null, "OWL Class", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel18.getFont())));
		owlClassLabel.setText("");
		panel18.add(owlClassLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel19 = new JPanel();
		panel19.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		reviewTabbedPane.addTab("Relation", panel19);
		final JScrollPane scrollPane9 = new JScrollPane();
		panel19.add(scrollPane9, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane9.setViewportView(relationsForPropertyList);
		Font includePropertyDescendantsCheckBoxFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 12, includePropertyDescendantsCheckBox.getFont());
		if (includePropertyDescendantsCheckBoxFont != null)
			includePropertyDescendantsCheckBox.setFont(includePropertyDescendantsCheckBoxFont);
		this.$$$loadButtonText$$$(includePropertyDescendantsCheckBox, ResourceBundle.getBundle("log4j").getString("include.descendants1"));
		panel19.add(includePropertyDescendantsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel20 = new JPanel();
		panel20.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel19.add(panel20, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		panel20.setBorder(BorderFactory.createTitledBorder(null, "OWL Object Property", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Verdana", -1, 14, panel20.getFont())));
		Font owlPropertyLabelFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, owlPropertyLabel.getFont());
		if (owlPropertyLabelFont != null) owlPropertyLabel.setFont(owlPropertyLabelFont);
		owlPropertyLabel.setText("");
		panel20.add(owlPropertyLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		refreshReviewPaneButton = new JButton();
		refreshReviewPaneButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-synchronize-32.png")));
		refreshReviewPaneButton.setText("");
		panel14.add(refreshReviewPaneButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		nextReviewObjectButton = new JButton();
		nextReviewObjectButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24.png")));
		nextReviewObjectButton.setText("");
		panel14.add(nextReviewObjectButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		previousReviewObjectButton = new JButton();
		previousReviewObjectButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24 (reversed).png")));
		previousReviewObjectButton.setText("");
		panel14.add(previousReviewObjectButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer11 = new Spacer();
		panel14.add(spacer11, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		filePanel = new JPanel();
		filePanel.setLayout(new GridLayoutManager(5, 6, new Insets(0, 0, 0, 0), -1, -1));
		cardPanel.add(filePanel, "File");
		final Spacer spacer12 = new Spacer();
		filePanel.add(spacer12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer13 = new Spacer();
		filePanel.add(spacer13, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final Spacer spacer14 = new Spacer();
		filePanel.add(spacer14, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		filePanel.add(progressBar, new GridConstraints(2, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane10 = new JScrollPane();
		filePanel.add(scrollPane10, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		fileList = new JList();
		Font fileListFont = this.$$$getFont$$$("Verdana", Font.BOLD, 14, fileList.getFont());
		if (fileListFont != null) fileList.setFont(fileListFont);
		final DefaultListModel defaultListModel1 = new DefaultListModel();
		defaultListModel1.addElement("Open");
		defaultListModel1.addElement("New");
		fileList.setModel(defaultListModel1);
		scrollPane10.setViewportView(fileList);
		final Spacer spacer15 = new Spacer();
		filePanel.add(spacer15, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		backButton = new JButton();
		backButton.setText("Back");
		filePanel.add(backButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer16 = new Spacer();
		filePanel.add(spacer16, new GridConstraints(0, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final Spacer spacer17 = new Spacer();
		filePanel.add(spacer17, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		fileChooser = new JFileChooser();
		filePanel.add(fileChooser, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
		if (currentFont == null) return null;
		String resultName;
		if (fontName == null) {
			resultName = currentFont.getName();
		} else {
			Font testFont = new Font(fontName, Font.PLAIN, 10);
			if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
				resultName = fontName;
			} else {
				resultName = currentFont.getName();
			}
		}
		return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
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
		return rootPane;
	}

	@Override
	public void filterChangedEvent() {

	}

	@Override
	public void modelChangeEvent(ChangeEvent<ModelObject> event) {
		getModel().ifPresent(model1 -> event.getNew().ifPresent(o -> {
			if (o instanceof GraphSpace && isVisible()) {
				graphViewDialog.setVisible(true);
			}
		}));

		getModel().ifPresent(model1 -> {
			Optional optional = model1.getSelectedTextSource()
					.flatMap(TextSource::getSelectedAnnotation);
			if (optional.isPresent()) {
				spanSizeButtons.keySet()
						.forEach(jButton -> Arrays.stream(jButton.getActionListeners())
								.forEach(jButton::removeActionListener));
				spanSizeButtons.forEach(AbstractButton::addActionListener);

			} else {
				spanSizeButtons.keySet()
						.forEach(jButton -> Arrays.stream(jButton.getActionListeners())
								.forEach(jButton::removeActionListener));
				selectionSizeButtons.forEach(AbstractButton::addActionListener);
			}
		});
	}


	@Override
	public void colorChangedEvent() {

	}
}

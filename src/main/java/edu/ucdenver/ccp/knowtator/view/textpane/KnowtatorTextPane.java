package edu.ucdenver.ccp.knowtator.view.textpane;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.events.*;
import edu.ucdenver.ccp.knowtator.listeners.ProjectListener;
import edu.ucdenver.ccp.knowtator.listeners.SelectionListener;
import edu.ucdenver.ccp.knowtator.model.Profile;
import edu.ucdenver.ccp.knowtator.model.Span;
import edu.ucdenver.ccp.knowtator.model.TextSource;
import edu.ucdenver.ccp.knowtator.view.AnnotationPopupMenu;
import edu.ucdenver.ccp.knowtator.view.RectanglePainter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class KnowtatorTextPane extends JTextPane
		implements SelectionListener, ProjectListener {

	KnowtatorController controller;

	KnowtatorTextPane(KnowtatorController controller) {
		super();
		this.controller = controller;
		controller.getSelectionManager().addListener(this);

		getCaret().setVisible(true);
		addCaretListener(controller.getSelectionManager());

		setupListeners();
		requestFocusInWindow();
		select(0, 0);
	}

	abstract void showTextPane(TextSource textSource);

	private void setupListeners() {
		addMouseListener(
				new MouseListener() {
					int press_offset;

					@Override
					public void mousePressed(MouseEvent e) {
						press_offset = viewToModel(e.getPoint());
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						handleMouseRelease(e, press_offset, viewToModel(e.getPoint()));
					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}

					@Override
					public void mouseClicked(MouseEvent e) {
					}
				});
	}

	private void handleMouseRelease(MouseEvent e, int press_offset, int release_offset) {
		if (controller.getSelectionManager().getActiveTextSource() != null) {
			AnnotationPopupMenu popupMenu = new AnnotationPopupMenu(e, this, controller);

			Set<Span> spansContainingLocation = getSpans(press_offset);

			if (SwingUtilities.isRightMouseButton(e)) {
				if (spansContainingLocation.size() == 1) {
					Span span = spansContainingLocation.iterator().next();
					controller.getSelectionManager().setSelected(span);
				}
				popupMenu.showPopUpMenu(release_offset);
			} else if (press_offset == release_offset) {
				if (spansContainingLocation.size() == 1) {
					Span span = spansContainingLocation.iterator().next();
					controller.getSelectionManager().setSelected(span);
				} else if (spansContainingLocation.size() > 1) {
					popupMenu.chooseAnnotation(spansContainingLocation);
				}

			} else {
				setSelectionAtWordLimits(press_offset, release_offset);
			}
		}
	}

	private void setSelectionAtWordLimits(int press_offset, int release_offset) {

		try {
			int start = Utilities.getWordStart(this, min(press_offset, release_offset));
			int end = Utilities.getWordEnd(this, max(press_offset, release_offset));
			requestFocusInWindow();
			select(start, end);

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	void refreshHighlights() {
		if (controller.getSelectionManager().getActiveTextSource() != null) {

			if (controller.getSelectionManager().getSelectedSpan() != null) {
				try {
					scrollRectToVisible(
							modelToView(controller.getSelectionManager().getSelectedSpan().getStart()));
				} catch (BadLocationException | NullPointerException ignored) {

				}
			}

			Profile profile = controller.getSelectionManager().getActiveProfile();

			// Remove all previous highlights in case a span has been deleted
			getHighlighter().removeAllHighlights();

			// Always highlight the selected annotation first so its color and border show up
			highlightSelectedAnnotation();

			// Highlight overlaps first, then spans
			Span lastSpan = null;
			Color lastColor = null;

			Set<Span> spans = getSpans(null);
			for (Span span : spans) {
				if (lastSpan != null) {
					if (span.intersects(lastSpan)) {
						try {
							highlightSpan(
									span.getStart(),
									min(span.getEnd(), lastSpan.getEnd()),
									new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY));
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
					if (span.getEnd() > lastSpan.getEnd()) {
						try {
							highlightSpan(
									lastSpan.getStart(),
									lastSpan.getEnd(),
									new DefaultHighlighter.DefaultHighlightPainter(lastColor));
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}
				lastSpan = span;

				OWLClass owlClass = span.getAnnotation().getOwlClass();
				lastColor = profile.getColor(owlClass, span.getAnnotation().getOwlClassID());
			}
			if (lastSpan != null) {

				// Highlight remaining span
				try {
					highlightSpan(
							lastSpan.getStart(),
							lastSpan.getEnd(),
							new DefaultHighlighter.DefaultHighlightPainter(lastColor));
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}

			revalidate();
			repaint();
		}
	}

	public abstract void highlightSpan(
			int start, int end, DefaultHighlighter.DefaultHighlightPainter highlighter)
			throws BadLocationException;

	protected abstract Set<Span> getSpans(Integer loc);

	private void highlightSelectedAnnotation() {
		if (controller.getSelectionManager().getSelectedAnnotation() != null) {
			for (Span span :
					controller.getSelectionManager().getSelectedAnnotation().getSpanCollection().getData()) {
				try {
					if (span.equalStartAndEnd(controller.getSelectionManager().getSelectedSpan())) {
						highlightSpan(span.getStart(), span.getEnd(), new RectanglePainter(Color.BLACK));
					} else {
						highlightSpan(span.getStart(), span.getEnd(), new RectanglePainter(Color.GRAY));
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void selectedAnnotationChanged(AnnotationChangeEvent e) {
		refreshHighlights();
	}

	@Override
	public void selectedSpanChanged(SpanChangeEvent e) {
		refreshHighlights();
	}

	@Override
	public void activeGraphSpaceChanged(GraphSpaceChangeEvent e) {
	}

	@Override
	public void activeTextSourceChanged(TextSourceChangeEvent e) {
		showTextPane(e.getNew());
		refreshHighlights();
	}

	@Override
	public void activeProfileChange(ProfileChangeEvent e) {
		refreshHighlights();
	}

	@Override
	public void owlPropertyChangedEvent(OWLObjectProperty value) {
	}

	@Override
	public void projectClosed() {
	}

	@Override
	public void projectLoaded() {
		showTextPane(controller.getSelectionManager().getActiveTextSource());
	}

	public void decreaseFontSize() {
		StyledDocument doc = getStyledDocument();
		MutableAttributeSet attrs = getInputAttributes();
		Font font = doc.getFont(attrs);
		StyleConstants.setFontSize(attrs, font.getSize() - 2);
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
		repaint();
	}

	public void increaseFindSize() {
		StyledDocument doc = getStyledDocument();
		MutableAttributeSet attrs = getInputAttributes();
		Font font = doc.getFont(attrs);
		StyleConstants.setFontSize(attrs, font.getSize() + 2);
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
		repaint();
	}

	public void growStart() {
		select(getSelectionStart() - 1, getSelectionEnd());
	}

	public void shrinkStart() {
		select(getSelectionStart() + 1, getSelectionEnd());
	}

	public void shrinkEnd() {
		select(getSelectionStart(), getSelectionEnd() - 1);
	}

	public void growEnd() {
		select(getSelectionStart(), getSelectionEnd() + 1);
	}
}

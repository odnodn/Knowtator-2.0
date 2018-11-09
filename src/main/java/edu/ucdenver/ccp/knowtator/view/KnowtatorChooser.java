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

import edu.ucdenver.ccp.knowtator.model.KnowtatorDataObjectInterface;
import edu.ucdenver.ccp.knowtator.model.collection.*;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.concept.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.text.concept.span.Span;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;

import javax.swing.*;
import java.awt.event.ActionListener;

public abstract class KnowtatorChooser<K extends KnowtatorDataObjectInterface> extends JComboBox<K> implements KnowtatorComponent {

	private final ActionListener al;
	private KnowtatorCollection<K> collection;
	protected final KnowtatorView view;

	protected KnowtatorChooser(KnowtatorView view) {
		this.view = view;

		al = e -> {
			JComboBox comboBox = (JComboBox) e.getSource();
			if (comboBox.getSelectedItem() != null) {
				this.collection.setSelection(getItemAt(getSelectedIndex()));
			}
		};

		new TextBoundModelListener(view.getController()) {

			@Override
			public void respondToConceptAnnotationModification() {
				react();
			}

			@Override
			public void respondToSpanModification() {
				react();
			}

			@Override
			public void respondToGraphSpaceModification() {
				react();
			}

			@Override
			public void respondToGraphSpaceCollectionFirstAddedEvent() {
				react();
			}

			@Override
			public void respondToGraphSpaceCollectionEmptiedEvent() {
				react();
			}

			@Override
			public void respondToGraphSpaceRemovedEvent(RemoveEvent<GraphSpace> event) {
				react();
			}

			@Override
			public void respondToGraphSpaceAddedEvent(AddEvent<GraphSpace> event) {
				react();
			}

			@Override
			public void respondToGraphSpaceSelectionEvent(SelectionEvent<GraphSpace> event) {
				react();
			}

			@Override
			public void respondToConceptAnnotationCollectionEmptiedEvent() {
				react();
			}

			@Override
			public void respondToConceptAnnotationRemovedEvent(RemoveEvent<ConceptAnnotation> event) {
				react();
			}

			@Override
			public void respondToConceptAnnotationAddedEvent(AddEvent<ConceptAnnotation> event) {
				react();
			}

			@Override
			public void respondToConceptAnnotationCollectionFirstAddedEvent() {
				react();
			}

			@Override
			public void respondToSpanCollectionFirstAddedEvent() {
				react();
			}

			@Override
			public void respondToSpanCollectionEmptiedEvent() {
				react();
			}

			@Override
			public void respondToSpanRemovedEvent(RemoveEvent<Span> event) {
				react();
			}

			@Override
			public void respondToSpanAddedEvent(AddEvent<Span> event) {
				react();
			}

			@Override
			public void respondToSpanSelectionEvent(SelectionEvent<Span> event) {
				react();
			}

			@Override
			public void respondToConceptAnnotationSelectionEvent(SelectionEvent<ConceptAnnotation> event) {
				react();
			}

			@Override
			public void respondToTextSourceSelectionEvent(SelectionEvent<TextSource> event) {
				react();
			}

			@Override
			public void respondToTextSourceAddedEvent(AddEvent<TextSource> event) {
				react();
			}

			@Override
			public void respondToTextSourceRemovedEvent(RemoveEvent<TextSource> event) {
				react();
			}

			@Override
			public void respondToTextSourceCollectionEmptiedEvent() {
				react();
			}

			@Override
			public void respondToTextSourceCollectionFirstAddedEvent() {
				react();
			}
		};


	}

	protected abstract void react();

	protected void setCollection(KnowtatorCollection<K> collection) {
		dispose();

		this.collection = collection;
		if (collection.size() == 0) {
			setEnabled(false);
		} else {
			setEnabled(true);
			removeActionListener(al);
			collection.forEach(this::addItem);
			addActionListener(al);
		}
	}

	protected void setSelected() throws NoSelectionException {
		setSelectedItem(collection.getSelection());
	}

	@Override
	public void dispose() {
		removeAllItems();

		setSelectedItem(null);
	}

}

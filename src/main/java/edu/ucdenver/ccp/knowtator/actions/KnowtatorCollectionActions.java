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

package edu.ucdenver.ccp.knowtator.actions;

import com.mxgraph.util.mxEvent;
import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.model.NoSelectedOWLClassException;
import edu.ucdenver.ccp.knowtator.model.collection.NoSelectionException;
import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.concept.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.text.concept.span.Span;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.ucdenver.ccp.knowtator.actions.CollectionActionType.REMOVE;

public class KnowtatorCollectionActions {

	public static void pickAction(KnowtatorView view, String id, File file, ActionParameters... actionParametersList) {
		List<AbstractKnowtatorCollectionAction> actions = new ArrayList<>();

		Arrays.asList(actionParametersList).forEach(parameters -> {
			KnowtatorCollectionType collectionType = parameters.getCollectionType();
			CollectionActionType actionType = parameters.getActionType();

			switch (collectionType) {
				case ANNOTATION:
					view.getController().getTextSourceCollection().getSelection()
							.ifPresent(textSource -> actions.add(new ConceptAnnotationAction(
									parameters.getActionType(),
									view.getController(),
									textSource)));
					break;
				case SPAN:
					view.getController().getTextSourceCollection().getSelection()
							.ifPresent(textSource -> textSource.getConceptAnnotationCollection().getSelection()
									.ifPresent(conceptAnnotation -> actions.add(new SpanAction(actionType, view.getController(), conceptAnnotation))));
					break;
				case PROFILE:
					actions.add(new ProfileAction(actionType, view.getController(), id));
					break;
				case DOCUMENT:
					actions.add(new TextSourceAction(actionType, view.getController(), file));
			}


		});

		if (!actions.isEmpty()) {
			AbstractKnowtatorAction action;
			if (actions.size() == 1) {
				action = actions.get(0);
			} else {
				int response = JOptionPane.showOptionDialog(view,
						"Choose an option",
						"New Concept Annotation or Span",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.PLAIN_MESSAGE,
						null,
						actions.stream().map(AbstractKnowtatorAction::getPresentationName).toArray(),
						2);
				action = actions.get(response);
			}

			view.getController().registerAction(action);
		}
	}

	public static class ConceptAnnotationAction extends AbstractKnowtatorCollectionAction<ConceptAnnotation> {

		private final KnowtatorController controller;
		private ConceptAnnotation newConceptAnnotation;
		private TextSource textSource;

		ConceptAnnotationAction(CollectionActionType actionType, KnowtatorController controller, TextSource textSource) {
			super(actionType, "concept annotation", textSource.getConceptAnnotationCollection());
			this.textSource = textSource;
			this.controller = controller;
		}

		@Override
		public void cleanUpAdd() {
		}

		@Override
		public void cleanUpRemove() {
			textSource.getGraphSpaceCollection().forEach(graphSpace -> graphSpace.getModel().removeListener(edit, mxEvent.UNDO));
		}


		@Override
		void prepareRemove() throws ActionUnperformableException {
			super.prepareRemove();
			edit.setObject(object);
//			edit = new KnowtatorCollectionEdit<>(REMOVE, collection, object, getPresentationName(), edit.isSignificant());
			textSource.getGraphSpaceCollection().forEach(graphSpace -> graphSpace.getModel().addListener(mxEvent.UNDO, edit));
		}

		@Override
		void prepareAdd() throws ActionUnperformableException {
			try {
				OWLClass owlClass = controller.getOWLModel().getSelectedOWLClass();
				Profile annotator = controller.getProfileCollection().getSelection();
				String owlClassID = controller.getOWLModel().getOWLEntityRendering(owlClass);


				newConceptAnnotation = new ConceptAnnotation(controller, textSource, null, owlClass, owlClassID, null, annotator, "identity", "");
				newConceptAnnotation.getSpanCollection().add(new Span(controller, textSource, newConceptAnnotation, null, controller.getSelectionModel().getStart(), controller.getSelectionModel().getEnd()));
				setObject(newConceptAnnotation);
			} catch (NoSelectedOWLClassException e) {
				throw new ActionUnperformableException();
			}
		}

	}

	public static class SpanAction extends AbstractKnowtatorCollectionAction<Span> {

		private final KnowtatorController controller;
		private final ConceptAnnotation conceptAnnotation;

		SpanAction(CollectionActionType actionName, KnowtatorController controller, ConceptAnnotation conceptAnnotation) {
			super(actionName, "span", conceptAnnotation.getSpanCollection());
			this.conceptAnnotation = conceptAnnotation;

			this.controller = controller;
		}

		@Override
		void prepareAdd() {
			int start = controller.getSelectionModel().getStart();
			int end = controller.getSelectionModel().getEnd();

			Span newSpan = new Span(controller, conceptAnnotation.getTextSource(), conceptAnnotation, null, start, end);
			setObject(newSpan);
		}

		@Override
		void prepareRemove() throws ActionUnperformableException {
			// If the concept annotation only has one, remove the annotation instead
			if (conceptAnnotation.getSpanCollection().size() == 1) {
				AbstractKnowtatorAction action = new ConceptAnnotationAction(REMOVE, controller, conceptAnnotation.getTextSource());
				controller.registerAction(action);
				edit.addKnowtatorEdit(action.getEdit());
			} else {
				super.prepareRemove();
			}
		}

		@Override
		public void execute() throws ActionUnperformableException {
			if (this.actionType.equals(REMOVE) && conceptAnnotation.getSpanCollection().size() == 1) {
				try {
					super.execute();
				} catch (ActionUnperformableException ignored) {

				}
			} else {
				super.execute();
			}
		}

		@Override
		void cleanUpRemove() {

		}

		@Override
		public void cleanUpAdd() {

		}

	}

	static class ProfileAction extends AbstractKnowtatorCollectionAction<Profile> {
		private final KnowtatorController controller;
		private final String profileId;

		ProfileAction(CollectionActionType actionType, KnowtatorController controller, String profileId) {
			super(actionType, "Add profile", controller.getProfileCollection());
			this.controller = controller;
			this.profileId = profileId;
		}

		@Override
		void prepareRemove() {
			setObject(collection.get(profileId));

		}

		@Override
		void prepareAdd() {
			Profile profile = new Profile(controller, profileId);
			setObject(profile);
		}

		@Override
		void cleanUpRemove() throws ActionUnperformableException {
			for (TextSource textSource : controller.getTextSourceCollection()) {
				// Cast to array to avoid concurrent modification exceptions
				Object[] array = textSource.getConceptAnnotationCollection().getCollection().toArray();
				for (Object o : array) {
					ConceptAnnotation conceptAnnotation = (ConceptAnnotation) o;
					if (conceptAnnotation.getAnnotator().equals(object)) {
						AbstractKnowtatorAction action = new ConceptAnnotationAction(REMOVE, controller, conceptAnnotation.getTextSource());
						conceptAnnotation.getTextSource().getConceptAnnotationCollection().setSelection(conceptAnnotation);
						action.execute();
						edit.addKnowtatorEdit(action.getEdit());
					}
				}
			}
		}

		@Override
		public void cleanUpAdd() {

		}
	}

	static class TextSourceAction extends AbstractKnowtatorCollectionAction<TextSource> {
		private final KnowtatorController controller;
		private final File file;

		TextSourceAction(CollectionActionType actionType, KnowtatorController controller, File file) {
			super(actionType, "text source", controller.getTextSourceCollection());
			this.controller = controller;
			this.file = file;
		}

		@Override
		void prepareAdd() {
			setObject(new TextSource(controller, file, file.getName()));
			if (!file.getParentFile().equals(controller.getTextSourceCollection().getArticlesLocation())) {
				try {
					FileUtils.copyFile(file, new File(controller.getTextSourceCollection().getArticlesLocation(), file.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			setObject(new TextSource(controller, null, file.getName()));
		}

		@Override
		void cleanUpRemove() {

		}

		@Override
		void cleanUpAdd() {

		}
	}

	public static class GraphSpaceAction extends AbstractKnowtatorCollectionAction<GraphSpace> {
		private final KnowtatorController controller;
		private final String graphName;

		public GraphSpaceAction(CollectionActionType actionType, KnowtatorController controller, String graphName) throws NoSelectionException {
			super(actionType, "graph space", controller.getTextSourceCollection().getSelection().getGraphSpaceCollection());
			this.controller = controller;
			this.graphName = graphName;
		}

		@Override
		void prepareAdd() throws ActionUnperformableException {
			try {
				GraphSpace newGraphSpace = new GraphSpace(controller, controller.getTextSourceCollection().getSelection(), graphName);
				setObject(newGraphSpace);
			} catch (NoSelectionException e) {
				throw new ActionUnperformableException();
			}
		}

		@Override
		void cleanUpRemove() {

		}

		@Override
		void cleanUpAdd() {

		}
	}
}

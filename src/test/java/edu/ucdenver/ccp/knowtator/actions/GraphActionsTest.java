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

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.TestingHelpers;
import edu.ucdenver.ccp.knowtator.model.NoSelectedOWLPropertyException;
import edu.ucdenver.ccp.knowtator.model.collection.NoSelectionException;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.graph.AnnotationNode;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class GraphActionsTest {

	private static final KnowtatorController controller = TestingHelpers.getLoadedController();

	@Test
	public void removeSelectedAnnotationNode() throws NoSelectionException {
		TextSource textSource = controller.getTextSourceCollection().getSelection();
		textSource.getGraphSpaceCollection().selectNext();
		GraphSpace graphSpace = textSource.getGraphSpaceCollection().getSelection();
		Object cell = graphSpace.getModel().getChildAt(graphSpace.getDefaultParent(), 0);
		graphSpace.setSelectionCell(cell);
		TestingHelpers.testKnowtatorAction(controller, new GraphActions.removeCellsAction(controller),
				TestingHelpers.defaultExpectedTextSources,
				TestingHelpers.defaultExpectedConceptAnnotations,
				TestingHelpers.defaultExpectedSpans,
				TestingHelpers.defaultExpectedGraphSpaces,
				TestingHelpers.defaultExpectedProfiles,
				TestingHelpers.defaultExpectedHighlighters,
				TestingHelpers.defaultExpectedAnnotationNodes - 1,
				TestingHelpers.defaultExpectedTriples - 1);
	}

	@Test
	public void removeSelectedTriple() throws NoSelectionException {
		TextSource textSource = controller.getTextSourceCollection().getSelection();
		textSource.getGraphSpaceCollection().selectNext();
		GraphSpace graphSpace = textSource.getGraphSpaceCollection().getSelection();
		Object cell = graphSpace.getModel().getChildAt(graphSpace.getDefaultParent(), 2);
		graphSpace.setSelectionCell(cell);
		TestingHelpers.testKnowtatorAction(controller, new GraphActions.removeCellsAction(controller),
				TestingHelpers.defaultExpectedTextSources,
				TestingHelpers.defaultExpectedConceptAnnotations,
				TestingHelpers.defaultExpectedSpans,
				TestingHelpers.defaultExpectedGraphSpaces,
				TestingHelpers.defaultExpectedProfiles,
				TestingHelpers.defaultExpectedHighlighters,
				TestingHelpers.defaultExpectedAnnotationNodes,
				TestingHelpers.defaultExpectedTriples - 1);
	}

	@Test
	public void addAnnotationNode() throws NoSelectionException {
		TextSource textSource = controller.getTextSourceCollection().getSelection();
		textSource.getGraphSpaceCollection().selectNext();
		textSource.getConceptAnnotationCollection().selectNext();
		TestingHelpers.testKnowtatorAction(controller, new GraphActions.AddAnnotationNodeAction(null, controller),
				TestingHelpers.defaultExpectedTextSources,
				TestingHelpers.defaultExpectedConceptAnnotations,
				TestingHelpers.defaultExpectedSpans,
				TestingHelpers.defaultExpectedGraphSpaces,
				TestingHelpers.defaultExpectedProfiles,
				TestingHelpers.defaultExpectedHighlighters,
				TestingHelpers.defaultExpectedAnnotationNodes + 1,
				TestingHelpers.defaultExpectedTriples);
	}

	@Test
	public void addTriple() throws NoSelectionException, NoSelectedOWLPropertyException {
		TextSource textSource = controller.getTextSourceCollection().getSelection();
		textSource.getGraphSpaceCollection().selectNext();
		textSource.getConceptAnnotationCollection().selectNext();
		GraphSpace graphSpace = textSource.getGraphSpaceCollection().getSelection();
		AnnotationNode source = (AnnotationNode) graphSpace.getChildVertices(graphSpace.getDefaultParent())[0];
		AnnotationNode target = (AnnotationNode) graphSpace.getChildVertices(graphSpace.getDefaultParent())[1];
		OWLObjectProperty property = controller.getOWLModel().getSelectedOWLObjectProperty();
		TestingHelpers.testKnowtatorAction(controller, new GraphActions.AddTripleAction(controller,
						source,
						target,
						property, null,
						"some", null,
						false, ""),
				TestingHelpers.defaultExpectedTextSources,
				TestingHelpers.defaultExpectedConceptAnnotations,
				TestingHelpers.defaultExpectedSpans,
				TestingHelpers.defaultExpectedGraphSpaces,
				TestingHelpers.defaultExpectedProfiles,
				TestingHelpers.defaultExpectedHighlighters,
				TestingHelpers.defaultExpectedAnnotationNodes,
				TestingHelpers.defaultExpectedTriples + 1);
	}

	@Test
	public void applyLayout() throws NoSelectionException {
		//TODO: This test only makes sure that the layout application doesn't change to graph space model. It needs to check the positions
		TextSource textSource = controller.getTextSourceCollection().getSelection();
		textSource.getGraphSpaceCollection().selectNext();
		TestingHelpers.testKnowtatorAction(controller, new GraphActions.applyLayoutAction(null, controller),
				TestingHelpers.defaultExpectedTextSources,
				TestingHelpers.defaultExpectedConceptAnnotations,
				TestingHelpers.defaultExpectedSpans,
				TestingHelpers.defaultExpectedGraphSpaces,
				TestingHelpers.defaultExpectedProfiles,
				TestingHelpers.defaultExpectedHighlighters,
				TestingHelpers.defaultExpectedAnnotationNodes,
				TestingHelpers.defaultExpectedTriples);
	}

}
package edu.ucdenver.ccp.knowtator.model.text.concept;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.io.brat.BratStandoffIO;
import edu.ucdenver.ccp.knowtator.io.brat.StandoffTags;
import edu.ucdenver.ccp.knowtator.io.knowtator.*;
import edu.ucdenver.ccp.knowtator.model.OWLModel;
import edu.ucdenver.ccp.knowtator.model.collection.KnowtatorCollection;
import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.concept.span.Span;
import edu.ucdenver.ccp.knowtator.model.text.concept.span.SpanCollection;
import edu.ucdenver.ccp.knowtator.model.text.graph.AnnotationNode;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.util.OWLEntityCollector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConceptAnnotationCollection extends KnowtatorCollection<ConceptAnnotation> implements OWLOntologyChangeListener, KnowtatorXMLIO, BratStandoffIO {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(ConceptAnnotationCollection.class);

    private final KnowtatorController controller;

    private SpanCollection allSpanCollection;
    private TextSource textSource;

    public ConceptAnnotationCollection(KnowtatorController controller, TextSource textSource) {
        super(controller);
        this.controller = controller;
        this.textSource = textSource;

        controller.getOWLModel().addOntologyChangeListener(this);

        allSpanCollection = new SpanCollection(controller, textSource, null);
    }

    /*
    ADDERS
     */

    public ConceptAnnotation addAnnotation(String annotationID, OWLClass owlClass, String owlClassID, String owlClassLabel, Profile annotator, String annotation_type) {
        ConceptAnnotation newConceptAnnotation = new ConceptAnnotation(controller, annotationID, owlClass, owlClassID, owlClassLabel, annotator, annotation_type, textSource);
        add(newConceptAnnotation);
        return newConceptAnnotation;
    }

    public void addSelectedAnnotation() {
        OWLEntity owlClass = controller.getOWLModel().getSelectedOWLEntity();
        if (owlClass instanceof OWLClass) {
            Profile annotator = controller.getProfileCollection().getSelection();
            int start = controller.getSelectionModel().getStart();
            int end = controller.getSelectionModel().getEnd();


            String owlClassID = controller.getOWLModel().getOWLEntityRendering(owlClass);
            ConceptAnnotation newConceptAnnotation = addAnnotation(null, (OWLClass) owlClass, owlClassID, null, annotator, "identity");
            newConceptAnnotation.getSpanCollection().addSpan(null, start, end);
        }

    }

    public void addSpanToSelectedAnnotation() {
        getSelection().getSpanCollection().addSpan(null,
                controller.getSelectionModel().getStart(),
                controller.getSelectionModel().getEnd());
    }


    /*
    REMOVERS
     */
    @Override
    public void remove(ConceptAnnotation conceptAnnotationToRemove) {
        for (Span span : conceptAnnotationToRemove.getSpanCollection()) {
            allSpanCollection.remove(span);
        }
        super.remove(conceptAnnotationToRemove);
    }

    public void removeSpanFromAnnotation(Span span) {
        span.getConceptAnnotation().getSpanCollection().removeSpan(span);
        allSpanCollection.remove(span);
    }

    public void removeSpanFromSelectedAnnotation() {
        ConceptAnnotation conceptAnnotation = getSelection();
        removeSpanFromAnnotation(conceptAnnotation.getSpanCollection().getSelection());
    }



    /*
    MODIFIERS
     */
    public void modifySelection(int startModification, int endModification) {
        Span span = getSelection().getSpanCollection().getSelection();
        allSpanCollection.remove(span);
        span.modifySpan(startModification, endModification, textSource.getContent().length());
        allSpanCollection.add(span);

        collectionListeners.forEach(listener -> listener.updated(getSelection()));
    }

    /*
    GETTERS
     */

    /**
     * @param loc Location filter
     */
    public TreeSet<Span> getSpans(Integer loc, int start, int end) {
        Supplier<TreeSet<Span>> supplier = TreeSet::new;

        Set<OWLClass> activeOWLClassDescendents = new HashSet<>();
        if (controller.getFilterModel().isFilterByOWLClass()) {
            activeOWLClassDescendents.addAll(controller.getOWLModel().getDescendants((OWLClass) controller.getOWLModel().getSelectedOWLEntity()));
            activeOWLClassDescendents.add((OWLClass) controller.getOWLModel().getSelectedOWLEntity());
        }


        return allSpanCollection
                .stream()
                .filter(
                        span ->
                                (loc == null || span.contains(loc))
                                        && (start <= span.getStart() && span.getEnd() <= end)
                                        && (!controller.getFilterModel().isFilterByOWLClass() || activeOWLClassDescendents.contains(span.getConceptAnnotation().getOwlClass()))
                                        && (!controller.getFilterModel().isFilterByProfile() || span.getConceptAnnotation().getAnnotator().equals(controller.getProfileCollection().getSelection())))
                .collect(Collectors.toCollection(supplier));
    }

    public TreeSet<ConceptAnnotation> getAnnotations(int start, int end) {
        Supplier<TreeSet<ConceptAnnotation>> supplier = TreeSet::new;
        return stream()
                .filter(annotation -> (annotation.contains(start) && annotation.contains(end)))
                .collect(Collectors.toCollection(supplier));
    }

    @SuppressWarnings("unused")
    public void findOverlaps() {
        List<Span> overlappingSpans = new ArrayList<>();
        allSpanCollection.forEach(
                span -> {
                    List<Span> toRemove = new ArrayList<>();
                    overlappingSpans.forEach(
                            span1 -> {
                                if (span.intersects(span1)) {
                                    span.getConceptAnnotation().addOverlappingAnnotation(span1.getConceptAnnotation());
                                    span1.getConceptAnnotation().addOverlappingAnnotation(span.getConceptAnnotation());
                                } else {
                                    toRemove.add(span1);
                                }
                            });
                    overlappingSpans.removeAll(toRemove);

                    overlappingSpans.add(span);
                });
    }

    public void getNextSpan() {
        Span nextSpan = allSpanCollection.getNext(getSelection().getSpanCollection().getSelection());
        setSelection(nextSpan.getConceptAnnotation());
        nextSpan.getConceptAnnotation().getSpanCollection().setSelection(nextSpan);
    }

    public void getPreviousSpan() {
        Span previousSpan = allSpanCollection.getPrevious(getSelection().getSpanCollection().getSelection());
        setSelection(previousSpan.getConceptAnnotation());
        previousSpan.getConceptAnnotation().getSpanCollection().setSelection(previousSpan);

    }

    /*
    SETTERS
     */

    public void setSelectedAnnotation(Span newSpan) {

        if (newSpan == null) {
            setSelection(null);
        }
        else if (getSelection() != newSpan.getConceptAnnotation()) {
            setSelection(newSpan.getConceptAnnotation());
            newSpan.getConceptAnnotation().getSpanCollection().setSelection(newSpan);
            controller.getOWLModel().setSelectedOWLEntity(newSpan.getConceptAnnotation().getOwlClass());
        }
    }

    /*
    SETUP
     */

    @Override
    public void setSelection(ConceptAnnotation selection) {
        if (getSelection()!= null && getSelection() != selection) {
            getSelection().getSpanCollection().setSelection(null);
        }
        super.setSelection(selection);
    }

    private void setOWLClassForAnnotations() {
        Map<String, List<ConceptAnnotation>> unmatchedAnnotations = new HashMap<>();
        for (ConceptAnnotation conceptAnnotation : this) {
            OWLClass owlClass = controller.getOWLModel().getOWLClassByID(conceptAnnotation.getOwlClassID());
            if (owlClass == null) {
                List<ConceptAnnotation> conceptAnnotationList = unmatchedAnnotations.computeIfAbsent(conceptAnnotation.getOwlClassID(), k -> new ArrayList<>());
                conceptAnnotationList.add(conceptAnnotation);
            } else {
                conceptAnnotation.setOwlClass(owlClass);
            }
        }
        if (!unmatchedAnnotations.isEmpty()) {
            log.warn("The following classes could not be matched to annotations");
            unmatchedAnnotations.forEach((concept, conceptAnnotationList) -> {
                log.warn(String.format("Concept: %s", concept));
                log.warn("\tAnnotations:");
                conceptAnnotationList.forEach(conceptAnnotation -> log.warn(String.format("\t%s", conceptAnnotation.getId())));
            });
        }
    }

    /*
    WRITERS
     */

    @Override
    public void writeToKnowtatorXML(Document dom, Element textSourceElement) {
        forEach(
                annotation -> annotation.writeToKnowtatorXML(dom, textSourceElement));
    }

    @Override
    public void writeToBratStandoff(
            Writer writer,
            Map<String, Map<String, String>> annotationsConfig,
            Map<String, Map<String, String>> visualConfig)
            throws IOException {
        Iterator<ConceptAnnotation> annotationIterator = iterator();
        for (int i = 0; i < size(); i++) {
            ConceptAnnotation conceptAnnotation = annotationIterator.next();
            conceptAnnotation.setBratID(String.format("T%d", i));

            conceptAnnotation.writeToBratStandoff(writer, annotationsConfig, visualConfig);
        }

        // Not adding relations due to complexity of relation types in Brat Standoff
    /*int lastNumTriples = 0;
    for (GraphSpace graphSpace : graphSpaceCollection) {
      Object[] edges = graphSpace.getChildEdges(graphSpace.getDefaultParent());
      int bound = edges.length;
      for (int i = 0; i < bound; i++) {
        Object edge = edges[i];
        RelationAnnotation triple = (RelationAnnotation) edge;
        triple.setBratID(String.format("R%d", lastNumTriples + i));
        String propertyID;
        try {
          propertyID =
              controller.getOWLAPIDataExtractor().getOWLEntityRendering(triple.getProperty());
        } catch (OWLEntityNullException | OWLWorkSpaceNotSetException e) {
          propertyID = triple.getValue().toString();
        }
        writer.append(
            String.format(
                "%s\t%s Arg1:%s Arg2:%s\n",
                triple.getBratID(),
                propertyID,
                ((AnnotationNode) triple.getSource()).getConceptAnnotation().getBratID(),
                ((AnnotationNode) triple.getTarget()).getConceptAnnotation().getBratID()));
      }
    }*/
    }

    /*
    READERS
     */

    @Override
    public void readFromKnowtatorXML(File file, Element parent) {
        for (Node annotationNode :
                KnowtatorXMLUtil.asList(parent.getElementsByTagName(KnowtatorXMLTags.ANNOTATION))) {
            Element annotationElement = (Element) annotationNode;

            String annotationID = annotationElement.getAttribute(KnowtatorXMLAttributes.ID);
            String profileID = annotationElement.getAttribute(KnowtatorXMLAttributes.ANNOTATOR);
            String type = annotationElement.getAttribute(KnowtatorXMLAttributes.TYPE);

            Profile profile = controller.getProfileCollection().get(profileID);
            profile = profile == null ? controller.getProfileCollection().getDefaultProfile() : profile;
            String owlClassID =
                    ((Element) annotationElement.getElementsByTagName(KnowtatorXMLTags.CLASS).item(0))
                            .getAttribute(KnowtatorXMLAttributes.ID);
            String owlClassLabel =
                    ((Element) annotationElement.getElementsByTagName(KnowtatorXMLTags.CLASS).item(0))
                            .getAttribute(KnowtatorXMLAttributes.LABEL);

            ConceptAnnotation newConceptAnnotation = addAnnotation(annotationID, null, owlClassID, owlClassLabel, profile, type);
            newConceptAnnotation.readFromKnowtatorXML(null, annotationElement);
        }

        setOWLClassForAnnotations();
    }

    @Override
    public void readFromOldKnowtatorXML(File file, Element parent) {

        Map<String, Element> slotToClassIDMap = KnowtatorXMLUtil.getslotsFromXml(parent);
        Map<String, Element> classMentionToClassIDMap = KnowtatorXMLUtil.getClassIDsFromXml(parent);
        Map<ConceptAnnotation, Element> annotationToSlotMap = new HashMap<>();

        for (Node annotationNode :
                KnowtatorXMLUtil.asList(parent.getElementsByTagName(OldKnowtatorXMLTags.ANNOTATION))) {
            Element annotationElement = (Element) annotationNode;

            Profile profile;
            try {
                String profileID =
                        annotationElement
                                .getElementsByTagName(OldKnowtatorXMLTags.ANNOTATOR)
                                .item(0)
                                .getTextContent();
                profile = controller.getProfileCollection().addProfile(profileID);
            } catch (NullPointerException npe) {
                try {
                    String profileID = annotationElement.getAttribute(OldKnowtatorXMLAttributes.ANNOTATOR);
                    profile = controller.getProfileCollection().addProfile(profileID);
                } catch (NullPointerException npe2) {
                    profile = controller.getProfileCollection().getDefaultProfile();
                }
            }

            String annotationID =
                    ((Element) annotationElement.getElementsByTagName(OldKnowtatorXMLTags.MENTION).item(0))
                            .getAttribute(OldKnowtatorXMLAttributes.ID);
            Element classElement = classMentionToClassIDMap.get(annotationID);

            String owlClassID =
                    ((Element) classElement.getElementsByTagName(OldKnowtatorXMLTags.MENTION_CLASS).item(0))
                            .getAttribute(OldKnowtatorXMLAttributes.ID);
            String owlClassName =
                    classElement
                            .getElementsByTagName(OldKnowtatorXMLTags.MENTION_CLASS)
                            .item(0)
                            .getTextContent();

            ConceptAnnotation newConceptAnnotation = addAnnotation(annotationID, null, owlClassID, owlClassName, profile, "identity");

            newConceptAnnotation.readFromOldKnowtatorXML(null, annotationElement);

            // No need to keep annotations with no allSpanCollection
            if (newConceptAnnotation.getSpanCollection().size() == 0) {
                remove(newConceptAnnotation);
            } else {
                for (Node slotMentionNode :
                        KnowtatorXMLUtil.asList(
                                classElement.getElementsByTagName(OldKnowtatorXMLTags.HAS_SLOT_MENTION))) {
                    Element slotMentionElement = (Element) slotMentionNode;
                    String slotMentionID = slotMentionElement.getAttribute(OldKnowtatorXMLAttributes.ID);
                    Element slotElement = slotToClassIDMap.get(slotMentionID);
                    if (slotElement != null) {
                        annotationToSlotMap.put(newConceptAnnotation, slotElement);
                    }
                }
            }
        }


        GraphSpace oldKnowtatorGraphSpace = new GraphSpace(controller, textSource, "Old Knowtator Relations");
        textSource.getGraphSpaceCollection().add(oldKnowtatorGraphSpace);

        annotationToSlotMap.forEach(
                (annotation, slot) -> {
                    List<Object> vertices = oldKnowtatorGraphSpace.getVerticesForAnnotation(annotation);

                    AnnotationNode source = oldKnowtatorGraphSpace.makeOrGetAnnotationNode(annotation, vertices);

                    String propertyID =
                            ((Element) slot.getElementsByTagName(OldKnowtatorXMLTags.MENTION_SLOT).item(0))
                                    .getAttribute(OldKnowtatorXMLAttributes.ID);
                    for (Node slotMentionValueNode :
                            OldKnowatorUtil.asList(
                                    slot.getElementsByTagName(OldKnowtatorXMLTags.COMPLEX_SLOT_MENTION_VALUE))) {
                        Element slotMentionValueElement = (Element) slotMentionValueNode;
                        String value = slotMentionValueElement.getAttribute(OldKnowtatorXMLAttributes.VALUE);
                        ConceptAnnotation conceptAnnotation1 = textSource.getConceptAnnotationCollection().get(value);

                        List<Object> vertices1 = oldKnowtatorGraphSpace.getVerticesForAnnotation(conceptAnnotation1);


                        AnnotationNode target = oldKnowtatorGraphSpace.makeOrGetAnnotationNode(conceptAnnotation1, vertices1);

                        oldKnowtatorGraphSpace.addTriple(
                                source,
                                target,
                                null,
                                controller.getProfileCollection().getSelection(),
                                null,
                                propertyID,
                                "",
                                "",
                                false);
                    }
                }
        );

        setOWLClassForAnnotations();
    }

    @Override
    public void readFromBratStandoff(
            File file, Map<Character, List<String[]>> annotationCollection, String content) {

        Profile profile = controller.getProfileCollection().getDefaultProfile();

        annotationCollection
                .get(StandoffTags.TEXTBOUNDANNOTATION)
                .forEach(
                        annotation -> {
                            ConceptAnnotation newConceptAnnotation = addAnnotation(annotation[0], null,
                                    annotation[1].split(StandoffTags.textBoundAnnotationTripleDelimiter)[0],
                                    null, profile, "identity");
                            Map<Character, List<String[]>> map = new HashMap<>();
                            List<String[]> list = new ArrayList<>();
                            list.add(annotation);
                            map.put(StandoffTags.TEXTBOUNDANNOTATION, list);
                            newConceptAnnotation.readFromBratStandoff(null, map, content);
                        });

        annotationCollection
                .get(StandoffTags.NORMALIZATION)
                .forEach(
                        normalizaion -> {
                            String[] splitNormalization =
                                    normalizaion[1].split(StandoffTags.relationTripleDelimiter);
                            ConceptAnnotation conceptAnnotation = get(splitNormalization[1]);
                            conceptAnnotation.setOWLClassID(splitNormalization[2]);
                        });

        GraphSpace newGraphSpace = new GraphSpace(controller, textSource, "Brat Relation Graph");
        textSource.getGraphSpaceCollection().add(newGraphSpace);
        newGraphSpace.readFromBratStandoff(null, annotationCollection, null);

        setOWLClassForAnnotations();
    }


    @Override
    public void dispose() {
        super.dispose();
        controller.getOWLModel().removeOntologyChangeListener(this);
    }

    @Override
    public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
        Set<OWLEntity> possiblyAddedEntities = new HashSet<>();
        Set<OWLEntity> possiblyRemovedEntities = new HashSet<>();
        OWLEntityCollector addedCollector = new OWLEntityCollector(possiblyAddedEntities);
        OWLEntityCollector removedCollector = new OWLEntityCollector(possiblyRemovedEntities);

        OWLModel.processOntologyChanges(changes, addedCollector, removedCollector);

    /*
    For now, I will assume that entity removed is the one that existed and the one
    that is added is the new name for it.
     */
        if (!possiblyAddedEntities.isEmpty() && !possiblyRemovedEntities.isEmpty()) {
            OWLClass oldOWLClass = (OWLClass) possiblyRemovedEntities.iterator().next();
            OWLClass newOWLClass = (OWLClass) possiblyAddedEntities.iterator().next();

//            try {
//                log.warn(String.format("Old: %s", controller.getOWLAPIDataExtractor().getOWLEntityRendering(oldOWLClass)));
//                log.warn(String.format("New: %s", controller.getOWLAPIDataExtractor().getOWLEntityRendering(newOWLClass)));
//            } catch (OWLWorkSpaceNotSetException | OWLEntityNullException e) {
//                e.printStackTrace();
//            }
            ConceptAnnotationCollection annotationsForOwlClass = new ConceptAnnotationCollection(controller, textSource);

            String owlClassID = controller.getOWLModel().getOWLEntityRendering(oldOWLClass);

            for (ConceptAnnotation conceptAnnotation : this) {
                if (conceptAnnotation.getOwlClass() == oldOWLClass) {
                    annotationsForOwlClass.add(conceptAnnotation);
                } else if (conceptAnnotation.getOwlClassID().equals(owlClassID)) {
                    conceptAnnotation.setOwlClass(oldOWLClass);
                    annotationsForOwlClass.add(conceptAnnotation);
                }
            }

            for (ConceptAnnotation conceptAnnotation : annotationsForOwlClass) {
                conceptAnnotation.setOwlClass(newOWLClass);
            }
        }
    }

    public void reassignSelectedOWLClassToSelectedAnnotation() {
        OWLEntity selectedOWLEntity = controller.getOWLModel().getSelectedOWLEntity();
        if (selectedOWLEntity instanceof OWLClass) {
            getSelection().setOwlClass((OWLClass) selectedOWLEntity);
            collectionListeners.forEach(listener -> listener.updated(getSelection()));
        }
    }

    public SpanCollection getAllSpanCollection() {
        return allSpanCollection;
    }
}

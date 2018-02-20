/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.view.menus;

import com.mxgraph.model.mxCell;
import edu.ucdenver.ccp.knowtator.model.annotation.Annotation;
import edu.ucdenver.ccp.knowtator.model.annotation.Span;
import edu.ucdenver.ccp.knowtator.view.text.TextPane;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Map;

public class AnnotationPopupMenu extends JPopupMenu {

    private MouseEvent e;
    private TextPane textPane;

    public AnnotationPopupMenu(MouseEvent e, TextPane textPane) {
        this.e = e;
        this.textPane = textPane;
    }



    private JMenuItem addAnnotationCommand() {
        JMenuItem annotateWithCurrentSelectedClass = new JMenuItem("Add annotation");
        annotateWithCurrentSelectedClass.addActionListener(e12 -> textPane.getTextSource().getAnnotationManager()
                .addAnnotation(
                        new Span(textPane.getTextSource(), textPane.getSelectionStart(), textPane.getSelectionEnd())
                )
        );

        return annotateWithCurrentSelectedClass;
    }

    private JMenuItem addSpanToAnnotationCommand() {
        JMenuItem addSpanToAnnotation = new JMenuItem("Add span");
        addSpanToAnnotation.addActionListener(e4 -> textPane.getTextSource().getAnnotationManager()
                .addSpanToAnnotation(
                        textPane.getSelectedAnnotation(),
                        new Span(textPane.getTextSource(), textPane.getSelectionStart(), textPane.getSelectionEnd())
                )
        );

        return addSpanToAnnotation;
    }

    private JMenuItem removeSpanFromAnnotationCommand() {
        JMenuItem removeSpanFromSelectedAnnotation = new JMenuItem(String.format("Delete span from %s", textPane.getSelectedAnnotation().getClassID()));
        removeSpanFromSelectedAnnotation.addActionListener(e5 -> textPane.getTextSource().getAnnotationManager()
                .removeSpanFromAnnotation(
                        textPane.getSelectedAnnotation(),
                        textPane.getSelectedSpan()
                )
        );

        return removeSpanFromSelectedAnnotation;

    }

    private JMenuItem selectAnnotationCommand(Annotation annotation, Span span) {
        JMenuItem selectAnnotationMenuItem = new JMenuItem(String.format("Select %s", annotation.getClassID()));
        selectAnnotationMenuItem.addActionListener(e3 -> textPane.setSelection(span, annotation));

        return  selectAnnotationMenuItem;

    }

    private JMenuItem removeAnnotationCommand() {
        JMenuItem removeAnnotationMenuItem = new JMenuItem(
                String.format("Delete %s", textPane.getSelectedAnnotation().getClassID())
        );
        removeAnnotationMenuItem.addActionListener(e4 -> {
            textPane.getTextSource().getAnnotationManager().removeAnnotation(textPane.getSelectedAnnotation().getID());
            textPane.setSelection(null, null);
        });

        return removeAnnotationMenuItem;
    }

    public void chooseAnnotation(Map<Span, Annotation> spansContainingLocation) {
        // Menu items to select and remove annotations
        spansContainingLocation.forEach((span, annotation) -> add(selectAnnotationCommand(annotation, span)));

        show(e.getComponent(), e.getX(), e.getY());
    }

    private JMenuItem displayAnnotationVertexCommand() {
        JMenuItem menuItem = new JMenuItem(String.format("Add vertex to %s", textPane.getGraphDialog().getGraphViewer().getSelectedGraphComponent().getName()));
        menuItem.addActionListener(e -> {
            textPane.getGraphDialog().setVisible(true);
            if (textPane.getSelectedAnnotation() != null) {
                textPane.getGraphDialog().getGraphViewer().addAnnotationVertex(textPane.getSelectedAnnotation());
            }
        });

        return menuItem;
    }

    private JMenu goToAnnotationInGraphCommand() {
        JMenu jMenu = new JMenu("Graphs");

        textPane.getTextSource().getAnnotationManager().getGraphSpaces()
                .forEach(graphSpace -> {
                    mxCell vertex = graphSpace.containsVertexCorrespondingToAnnotation(textPane.getSelectedAnnotation());
                    if (vertex != null) {
                        JMenuItem menuItem = new JMenuItem(graphSpace.getId());
                        menuItem.addActionListener(e1 -> {
                            textPane.getGraphDialog().setVisible(true);
                            textPane.getGraphDialog().getGraphViewer().getGraphComponent(graphSpace.getId());
                            textPane.getGraphDialog().getGraphViewer().goToVertex(textPane.getSelectedAnnotation());
                        });
                        jMenu.add(menuItem);
                    }
                });

        return jMenu;
    }

    public void showPopUpMenu(int release_offset) {

        Annotation selectedAnnotation = textPane.getSelectedAnnotation();
        Span selectedSpan = textPane.getSelectedSpan();

        if (textPane.getSelectionStart() <= release_offset && release_offset <= textPane.getSelectionEnd() && textPane.getSelectionStart() != textPane.getSelectionEnd()) {
            textPane.select(textPane.getSelectionStart(), textPane.getSelectionEnd());
            add(addAnnotationCommand());
            if (textPane.getSelectedAnnotation() != null) {
                add(addSpanToAnnotationCommand());
            }
        } else if (selectedAnnotation != null  && selectedSpan.getStart() <= release_offset && release_offset <= selectedSpan.getEnd()) {
            add(removeAnnotationCommand());
            if (textPane.getSelectedSpan() != null && textPane.getSelectedAnnotation() != null && textPane.getSelectedAnnotation().getSpans().size() > 1 ) {
                add(removeSpanFromAnnotationCommand());
            }

            if (textPane.getSelectedAnnotation() != null) {
                addSeparator();
                add(displayAnnotationVertexCommand());
                add(goToAnnotationInGraphCommand());
            }
        } else {
            return;
        }

        show(e.getComponent(), e.getX(), e.getY());
    }

}
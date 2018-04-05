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

package edu.ucdenver.ccp.knowtator.model.textsource;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.model.Savable;
import edu.ucdenver.ccp.knowtator.model.annotation.AnnotationManager;
import edu.ucdenver.ccp.knowtator.model.io.knowtator.KnowtatorXMLAttributes;
import edu.ucdenver.ccp.knowtator.model.io.knowtator.KnowtatorXMLTags;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TextSource implements Savable {
    @SuppressWarnings("unused")
    private static Logger log = LogManager.getLogger(TextSource.class);
    private final KnowtatorManager manager;

    public File getSaveFile() {
        return saveFile;
    }

    private final File saveFile;


    private AnnotationManager annotationManager;
    private String docID;
    private File textFile;
    //    private String content;

    public TextSource(KnowtatorManager manager, File saveFile, String docID) {
        this.manager = manager;
        this.saveFile = saveFile;
        this.annotationManager = new AnnotationManager(manager, this);

        if (docID != null) {
            this.docID = docID;
            textFile = new File(manager.getProjectManager().getArticlesLocation(), docID + ".txt");

            if (!textFile.exists()) {
                if (JOptionPane.showConfirmDialog(null, String.format("Could not find file for %s. Choose file location?", docID), "File not found", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select text file for " + docID);

                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        if (JOptionPane.showConfirmDialog(null, String.format("Copy %s to project?", textFile.getName()), "Copy selected file?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            try {
                                textFile = Files.copy(Paths.get(file.toURI()), Paths.get(manager.getProjectManager().getArticlesLocation().toURI().resolve(file.getName()))).toFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            textFile = file;
                        }
                    }

                }
            }

//            try {
//                content = FileUtils.readFileToString(textFile, "UTF-8");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
//        else {
//            this.docID = "Instructions";
//            content = "***Instructions:***" +
//                    "\n" +
//                    "Create a new project: Project -> New Project" +
//                    "\n" +
//                    "Load an existing project: Project -> Load Project";
//        }

    }

    public String getDocID() {
        return docID;
    }

    public File getTextFile() {
        return textFile;
    }

    public AnnotationManager getAnnotationManager() {
        return annotationManager;
    }
    @Override
    public String toString() {
        return String.format("TextSource: docID: %s", docID);
    }
    @Override
    public void writeToKnowtatorXML(Document dom, Element parent) {
        Element textSourceElement = dom.createElement(KnowtatorXMLTags.DOCUMENT);
        parent.appendChild(textSourceElement);
        textSourceElement.setAttribute(KnowtatorXMLAttributes.ID, docID);
        annotationManager.writeToKnowtatorXML(dom, textSourceElement);
    }

    @Override
    public void readFromKnowtatorXML(File file, Element parent, String content) {
        annotationManager.readFromKnowtatorXML(null, parent, getContent());
    }

    public String getContent() {
        while (true) {
            try {
                return FileUtils.readFileToString(textFile, "UTF-8");
            } catch (IOException e) {
                textFile = new File(manager.getProjectManager().getArticlesLocation(), docID + ".txt");
                while (!textFile.exists()) {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        textFile = fileChooser.getSelectedFile();
                    }
                }
            }
        }
    }

    @Override
    public void readFromOldKnowtatorXML(File file, Element parent, String content) {
        annotationManager.readFromOldKnowtatorXML(null, parent, getContent());
    }

    @Override
    public void readFromBratStandoff(File file, Map<Character, List<String[]>> annotationMap, String content) {
        annotationManager.readFromBratStandoff(null, annotationMap, getContent());

    }

    @Override
    public void writeToBratStandoff(Writer writer) throws IOException {
        annotationManager.writeToBratStandoff(writer);
    }

    @Override
    public void readFromGeniaXML(Element parent, String content) {

    }

//    @Override
//    public void convertToUIMA(CAS cas) {
//        CAS textSourceAsCAS = cas.createView(docID);
//        textSourceAsCAS.setDocumentText(getContent());
//        textSourceAsCAS.setDocumentLanguage("en");
//        annotationManager.convertToUIMA(textSourceAsCAS);
//    }

    @Override
    public void writeToGeniaXML(Document dom, Element parent) {

    }
}

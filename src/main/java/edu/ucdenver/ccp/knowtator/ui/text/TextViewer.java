package edu.ucdenver.ccp.knowtator.ui.text;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.actions.ProjectActions;
import edu.ucdenver.ccp.knowtator.annotation.TextSource;
import edu.ucdenver.ccp.knowtator.io.txt.KnowtatorDocumentHandler;
import edu.ucdenver.ccp.knowtator.listeners.TextSourceListener;
import edu.ucdenver.ccp.knowtator.ui.BasicKnowtatorView;
import org.apache.log4j.Logger;
import other.DnDTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextViewer extends DnDTabbedPane implements TextSourceListener {
    public static final Logger log = Logger.getLogger(KnowtatorManager.class);
    private boolean realTabAdded;
    private KnowtatorManager manager;
    private BasicKnowtatorView view;

    public TextViewer(KnowtatorManager manager, BasicKnowtatorView view) {
        super();
        this.manager = manager;
        this.view = view;

        addDefaultTab();

    }

    private void addDefaultTab() {
        add("Untitled", new JScrollPane(new JTextPane()));
        realTabAdded = false;
    }

    private void addNewDocument(TextSource textSource) {
        TextPane textPane = new TextPane(manager, view, textSource);
        textPane.setName(textSource.getDocID());
        if (textSource.getContent() == null) {
            try {
                textPane.read(KnowtatorDocumentHandler.getFileInputStream(textSource.getFileLocation(), false), null);
                textSource.setContent(textPane.getText());
            } catch (IOException | NullPointerException e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle(String.format("Select the location of the text document for %s", textSource.getDocID()));

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        textPane.read(KnowtatorDocumentHandler.getFileInputStream(fileChooser.getSelectedFile().getAbsolutePath(), false), null);
                        textSource.setContent(textPane.getText());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        } else {
            textPane.setText(textSource.getContent());
        }


        addClosableTab(textPane, textSource.getDocID());
    }

    private void addClosableTab(TextPane textPane, String title) {
        JScrollPane sp = new JScrollPane(textPane);

        if (!realTabAdded) this.remove(0);
        addTab(title, sp);

        int index = indexOfTab(title);
        JPanel pnlTab = new JPanel(new GridBagLayout());
        pnlTab.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        JButton btnClose = new JButton("x");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        pnlTab.add(lblTitle, gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        pnlTab.add(btnClose, gbc);

        setTabComponentAt(index, pnlTab);

        TextViewer textViewer = this;
        btnClose.addActionListener(evt -> {
            int result = JOptionPane.showConfirmDialog(null, String.format("Would you like to save changes to %s", title), "Closing document", JOptionPane.YES_NO_CANCEL_OPTION);
            switch (result) {
                case JOptionPane.YES_OPTION: ProjectActions.saveProject(manager);
                case JOptionPane.NO_OPTION:
                    if (getTabCount() == 1) {
                        addDefaultTab();
                    }
                    log.warn(String.format("Closing: %s", title));
                    manager.getTextSourceManager().remove(textPane.getTextSource());
                    textPane.getGraphDialog().setVisible(false);
                    textViewer.remove(sp);
                    break;
            }
        });

        setSelectedComponent(sp);

        realTabAdded = true;
    }

    public TextPane getSelectedTextPane() {
        if (getSelectedComponent() != null) {
            Component c = ((JScrollPane) getSelectedComponent()).getViewport().getView();
            if (c.getClass() == TextPane.class) {
                return (TextPane) ((JScrollPane) getSelectedComponent()).getViewport().getView();
            }
        }
        return null;
    }

    public List<TextPane> getAllTextPanes() {
        List<TextPane> textPaneList = new ArrayList<>();
        for (int i=0; i<getTabCount(); i++) {
            Component c = ((JScrollPane) getComponentAt(i)).getViewport().getView();
            if (c.getClass() == TextPane.class) {
                textPaneList.add((TextPane) c);
            }
        }
        return textPaneList;
    }

    @Override
    public void textSourceAdded(TextSource textSource) {
        addNewDocument(textSource);
        getSelectedTextPane().refreshHighlights();
    }

}

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

package edu.ucdenver.ccp.knowtator.view.menu;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * The main menu dialog for displaying other menu panes
 */
public class MenuDialog extends JDialog {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MenuDialog.class);

	private JPanel contentPane;
	private JList<MenuPane> menuOptionsList;
	private JPanel menuDisplayPane;
	private JSplitPane splitPane;
	private final KnowtatorView view;

	public MenuDialog(Window parent, KnowtatorView view) {
		super(parent);
		this.view = view;
		$$$setupUI$$$();
		setContentPane(contentPane);
		setModal(false);
		setSize(500, 200);
		setTitle("Knowtator Menu");
		try {
			setIconImage(ImageIO.read(getClass().getResource("/ccp_logo.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				splitPane.setDividerLocation(250);
			}
		});

		splitPane.setDividerLocation(250);
		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		for (int i = 0; i < menuOptionsList.getModel().getSize(); i++) {
			MenuPane pane = menuOptionsList.getModel().getElementAt(i);
			menuDisplayPane.add(pane.getContentPane(), pane.toString());
		}
		menuOptionsList.addListSelectionListener(e -> {
			CardLayout cl = (CardLayout) menuDisplayPane.getLayout();
			cl.show(menuDisplayPane, menuOptionsList.getSelectedValue().toString());
			menuOptionsList.getSelectedValue().show();
		});

		menuOptionsList.setSelectedIndex(0);
	}

	@Override
	public void dispose() {
		for (int i = 0; i < menuOptionsList.getModel().getSize(); i++) {
			menuOptionsList.getModel().getElementAt(i).dispose();
		}
		super.dispose();
	}


	private void createUIComponents() {
		menuOptionsList = new JList<>(new MenuPane[]{
				new FilePane(this, view),
				new ImportPane(this, view),
				new ExportPane(view),
				new ProfilePane(view),
				new IAAPane(view),
				new ConsistencyPane()});
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
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
		contentPane.setPreferredSize(new Dimension(700, 500));
		splitPane = new JSplitPane();
		splitPane.setDividerLocation(250);
		splitPane.setEnabled(false);
		contentPane.add(splitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		menuDisplayPane = new JPanel();
		menuDisplayPane.setLayout(new CardLayout(0, 0));
		Font menuDisplayPaneFont = this.$$$getFont$$$("Verdana", Font.PLAIN, 10, menuDisplayPane.getFont());
		if (menuDisplayPaneFont != null) menuDisplayPane.setFont(menuDisplayPaneFont);
		splitPane.setRightComponent(menuDisplayPane);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout(0, 0));
		splitPane.setLeftComponent(panel1);
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setPreferredSize(new Dimension(250, 128));
		panel1.add(scrollPane1, BorderLayout.CENTER);
		menuOptionsList.setBackground(new Color(-1118482));
		menuOptionsList.setFocusCycleRoot(true);
		Font menuOptionsListFont = this.$$$getFont$$$("Verdana", Font.BOLD, 16, menuOptionsList.getFont());
		if (menuOptionsListFont != null) menuOptionsList.setFont(menuOptionsListFont);
		menuOptionsList.setForeground(new Color(-16777216));
		menuOptionsList.setPreferredSize(new Dimension(250, 0));
		scrollPane1.setViewportView(menuOptionsList);
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
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}

}

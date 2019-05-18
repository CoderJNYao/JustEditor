package com.yjn.justeditor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

public class MainFrame {
	public static void main(String[] args) {
		new EditMainFrame("JustEditor");
	}
}

/**
 * ���±���������
 * 
 * @author JustinNeil
 */
class EditMainFrame extends JFrame implements ActionListener, DocumentListener {

	private static final long serialVersionUID = 1L;
	private String title;
	JMenuBar jMenuBar;
	JMenu fileMenu, edit, format, read, help;
	// �ļ��˵���
	JMenuItem newFile, openFile, saveFile, otherSaveTo, pageSet, print, exit;
	// �༭�˵���
	JMenuItem unmake, cut, copy, paste, delete, find, findNext, replace, turnTo, allChoose, dateTime;
	JCheckBoxMenuItem autoNewLine;
	JMenuItem fontStyle;
	JCheckBoxMenuItem stateBar;
	// �����˵���
	JMenuItem forHelp, about;
	JTextArea mainTextArea;
	JPopupMenu popupMenu;
	// �Ҽ������˵���
	JMenuItem popupMenuUndo, popupMenuCut, popupMenuCopy, popupMenuPaste, popupMenuDelete, popupMenuSelectAll;
	JLabel stateLabel;
	FileDialog openDialog, saveDialog, saveToOtherDialog;
	File currentFile;// ��ǰ�ļ�
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Clipboard clipboard = getToolkit().getSystemClipboard();
	protected UndoManager undo = new UndoManager();
	protected UndoableEditListener undoHandler = new UndoHandler();
	String oldValue = "";

	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	public void checkMenuItemEnable() {
		String string = mainTextArea.getSelectedText();
		if (string == null) {
			cut.setEnabled(false);
			popupMenuCut.setEnabled(false);
			copy.setEnabled(false);
			popupMenuCopy.setEnabled(false);
			delete.setEnabled(false);
			popupMenuDelete.setEnabled(false);
		} else {
			cut.setEnabled(true);
			popupMenuCut.setEnabled(true);
			copy.setEnabled(true);
			popupMenuCopy.setEnabled(true);
			delete.setEnabled(true);
			popupMenuDelete.setEnabled(true);
		}
		Transferable contents = clipboard.getContents(this);
		if (contents == null) {
			paste.setEnabled(false);
			popupMenuPaste.setEnabled(false);
		} else {
			paste.setEnabled(true);
			popupMenuPaste.setEnabled(true);
		}
	}

	public EditMainFrame(String title) throws HeadlessException {
		this.title = title;
		setVisible(true);
		setBounds(200, 200, 800, 600);
		iniBar();
		setJMenuBar(jMenuBar);
		mainTextArea = new JTextArea(20, 50);
		JScrollPane scrollPane = new JScrollPane(mainTextArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		mainTextArea.setWrapStyleWord(true);
		mainTextArea.setLineWrap(true);

		mainTextArea.getDocument().addUndoableEditListener(undoHandler);
		mainTextArea.getDocument().addDocumentListener(this);
		iniPopupMenu();
		// �ı��༭��ע���Ҽ��˵��¼�
		mainTextArea.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())// ���ش�����¼��Ƿ�Ϊ��ƽ̨�ĵ����˵������¼�
				{
					popupMenu.show(e.getComponent(), e.getX(), e.getY());// ����������ߵ�����ռ��е�λ��
				}
				checkMenuItemEnable();// ���ü��У����ƣ�ճ����ɾ���ȹ��ܵĿ�����
				mainTextArea.requestFocus();// �༭����ȡ����
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())// ���ش�����¼��Ƿ�Ϊ��ƽ̨�ĵ����˵������¼�
				{
					popupMenu.show(e.getComponent(), e.getX(), e.getY());// ����������ߵ�����ռ��е�λ��
				}
				checkMenuItemEnable();// ���ü��У����ƣ�ճ����ɾ���ȹ��ܵĿ�����
				mainTextArea.requestFocus();// �༭����ȡ����
			}
		});// �ı��༭��ע���Ҽ��˵��¼�����
		stateLabel = new JLabel("��F1��ȡ�ı�����");
		add(stateLabel, BorderLayout.SOUTH);
		checkMenuItemEnable();
		mainTextArea.requestFocus();
		Font font = new Font(mainTextArea.getText(), Font.PLAIN, 20);
		mainTextArea.setFont(font);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitWindowChoose();
			}
		});
		oldValue = mainTextArea.getText();
		validate();
		addAllActionListner();
		turnToLine();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void iniPopupMenu() {
		// �����Ҽ������˵�
		popupMenu = new JPopupMenu();
		popupMenuUndo = new JMenuItem("����(U)");
		popupMenuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		popupMenuCut = new JMenuItem("����(T)");
		popupMenuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		popupMenuCopy = new JMenuItem("����(C)");
		popupMenuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		popupMenuPaste = new JMenuItem("ճ��(P)");
		popupMenuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		popupMenuDelete = new JMenuItem("ɾ��(D)");
		popupMenuSelectAll = new JMenuItem("ȫѡ(A)");
		popupMenuSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		// ���Ҽ��˵���Ӳ˵���ͷָ���
		popupMenu.add(popupMenuUndo);
		popupMenu.addSeparator();
		popupMenu.add(popupMenuCut);
		popupMenu.add(popupMenuCopy);
		popupMenu.add(popupMenuPaste);
		popupMenu.add(popupMenuDelete);
		popupMenu.addSeparator();
		popupMenu.add(popupMenuSelectAll);
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				checkMenuItemEnable();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				checkMenuItemEnable();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				checkMenuItemEnable();
			}
		});
	}

	private void iniBar() {
		Font font = new Font("Dialog", Font.PLAIN, 14);
		setFont(font);
		jMenuBar = new JMenuBar();
		fileMenu = new JMenu("�ļ�(F)");
		fileMenu.setMnemonic('F');
		newFile = new JMenuItem("�½�(N)");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		openFile = new JMenuItem("��(O)");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openDialog = new FileDialog(this, "��", FileDialog.LOAD);
		saveFile = new JMenuItem("����(S)");
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveDialog = new FileDialog(this, "����", FileDialog.SAVE);
		otherSaveTo = new JMenuItem("���Ϊ(A)... ");
		saveToOtherDialog = new FileDialog(this, "���Ϊ", FileDialog.SAVE);
		pageSet = new JMenuItem("ҳ������(U)...");
		print = new JMenuItem("��ӡ(P)");
		print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		exit = new JMenuItem("�˳�(X)");
		fileMenu.add(newFile);
		fileMenu.add(openFile);
		fileMenu.add(saveFile);
		fileMenu.add(otherSaveTo);
		fileMenu.addSeparator();
		fileMenu.add(pageSet);
		fileMenu.add(print);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		unmake = new JMenuItem("����(U)");
		unmake.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		cut = new JMenuItem("����(T)");
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		copy = new JMenuItem("����(C)");
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		paste = new JMenuItem("ճ��(P)");
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		delete = new JMenuItem("ɾ��(L) ");
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		find = new JMenuItem("����(F)...");
		find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		findNext = new JMenuItem("������һ��(N)");
		findNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		replace = new JMenuItem("�滻(R)...");
		replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		turnTo = new JMenuItem("ת��(G)...");
		turnTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		turnTo.addActionListener(this);
		allChoose = new JMenuItem("ȫѡ(A)");
		allChoose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		dateTime = new JMenuItem("ʱ��/����(D)");
		allChoose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		edit = new JMenu("�༭(E)");
		edit.setMnemonic('E');
		edit.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				checkMenuItemEnable();
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				checkMenuItemEnable();
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				checkMenuItemEnable();
			}
		});
		edit.add(unmake);
		edit.addSeparator();
		edit.add(cut);
		edit.add(copy);
		edit.add(paste);
		edit.add(delete);
		edit.addSeparator();
		edit.add(find);
		edit.add(findNext);
		edit.add(replace);
		edit.add(turnTo);
		edit.addSeparator();
		edit.add(allChoose);
		edit.add(dateTime);
		format = new JMenu("��ʽ(O)");
		format.setMnemonic('O');
		autoNewLine = new JCheckBoxMenuItem("�Զ�����(W)");
		autoNewLine.setState(true);
		autoNewLine.addActionListener(this);
		fontStyle = new JMenuItem("����(F)...");
		fontStyle.addActionListener(this);
		format.add(autoNewLine);
		format.add(fontStyle);
		read = new JMenu("�鿴(V)");
		read.setMnemonic('O');
		stateBar = new JCheckBoxMenuItem("״̬��");
		stateBar.addActionListener(this);
		stateBar.setState(true);
		read.add(stateBar);
		help = new JMenu("����(H)");
		help.setMnemonic('H');
		forHelp = new JMenuItem("�鿴����");
		forHelp.addActionListener(this);
		forHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		about = new JMenuItem("����JustEditor");
		about.addActionListener(this);
		help.add(forHelp);
		help.addSeparator();
		help.add(about);
		jMenuBar.add(fileMenu);
		jMenuBar.add(edit);
		jMenuBar.add(format);
		jMenuBar.add(read);
		jMenuBar.add(help);
	}

	private void exitWindowChoose() {
		mainTextArea.requestFocus();
		if (oldValue.equals(mainTextArea.getText())) {
			System.exit(0);
		} else {
			int exitChoose = JOptionPane.showConfirmDialog(this, "�����ļ���δ���棬�Ƿ񱣴棿", "�˳���ʾ",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (exitChoose == JOptionPane.YES_OPTION) {
				saveFile();
				System.exit(0);
			} else if (exitChoose == JOptionPane.NO_OPTION) {
				System.exit(0);
			} else {
				return;
			}
		}
	}

	private void openFile() {
		openDialog.setVisible(true);
		String dirpath = openDialog.getDirectory();// ��ȡ���ļ�·�������浽�ַ����С�
		String fileName = openDialog.getFile();// ��ȡ���ļ����Ʋ����浽�ַ�����
		if (dirpath == null || fileName == null)// �ж�·�����ļ��Ƿ�Ϊ��
			return;
		else
			mainTextArea.setText(null);// �ļ���Ϊ�գ����ԭ���ļ����ݡ�
		currentFile = new File(dirpath, fileName);// �����µ�·��������
		try {
			BufferedReader bufr = new BufferedReader(new FileReader(currentFile));// ���Դ��ļ��ж�����
			String line = null;// �����ַ�����ʼ��Ϊ��
			while ((line = bufr.readLine()) != null) {
				mainTextArea.append(line + "\r\n");// ��ʾÿһ������
			}
			oldValue = mainTextArea.getText();
			bufr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveFile() {
		if (currentFile == null) {
			saveDialog.setVisible(true);// ��ʾ�����ļ��Ի���
			String dirpath = saveDialog.getDirectory();// ��ȡ�����ļ�·�������浽�ַ����С�
			String fileName = saveDialog.getFile();//// ��ȡ�򱣴��ļ����Ʋ����浽�ַ�����
			if (dirpath == null || fileName == null)// �ж�·�����ļ��Ƿ�Ϊ��
				return;
			else
				currentFile = new File(dirpath, fileName);// �ļ���Ϊ�գ��½�һ��·��������
		}
		try {
			BufferedWriter bufw = new BufferedWriter(new FileWriter(currentFile));
			oldValue = mainTextArea.getText();
			bufw.write(oldValue);
			bufw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveToOther() {
		saveDialog.setVisible(true);// ��ʾ�����ļ��Ի���
		String dirpath = saveDialog.getDirectory();// ��ȡ�����ļ�·�������浽�ַ����С�
		String fileName = saveDialog.getFile();//// ��ȡ�򱣴��ļ����Ʋ����浽�ַ�����
		if (dirpath == null || fileName == null) {// �ж�·�����ļ��Ƿ�Ϊ��
			return;// �ղ���
		}
		File otherFile = new File(dirpath, fileName);// �ļ���Ϊ�գ��½�һ��·��������
		try {
			if (!otherFile.exists()) {
				otherFile.createNewFile();
			}
			BufferedWriter bufw = new BufferedWriter(new FileWriter(otherFile));
			String text = mainTextArea.getText();
			bufw.write(text);
			bufw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void newFile() {
		mainTextArea.requestFocus();
		String currentValue = mainTextArea.getText();
		boolean isTextChange = (currentValue.equals(oldValue)) ? false : true;
		if (isTextChange) {
			int saveChoose = JOptionPane.showConfirmDialog(this, "�����ļ���δ���棬�Ƿ񱣴棿", "��ʾ",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (saveChoose == JOptionPane.YES_OPTION) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("���Ϊ");
				int result = fileChooser.showSaveDialog(this);
				if (result == JFileChooser.CANCEL_OPTION) {
					stateLabel.setText("��û��ѡ���κ��ļ�");
					return;
				}
				File saveFile = fileChooser.getSelectedFile();
				if (saveFile == null || saveFile.getName().equals("")) {
					JOptionPane.showMessageDialog(this, "���Ϸ����ļ���", "���Ϸ����ļ���", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						FileWriter fw = new FileWriter(saveFile);
						BufferedWriter bfw = new BufferedWriter(fw);
						bfw.write(mainTextArea.getText(), 0, mainTextArea.getText().length());
						bfw.flush();
						bfw.close();
						currentFile = saveFile;
						oldValue = mainTextArea.getText();
						this.setTitle(saveFile.getName() + " - JustEditor");
						stateLabel.setText("��ǰ���ļ���" + saveFile.getAbsoluteFile());
					} catch (IOException ioException) {
					}
				}
			} else if (saveChoose == JOptionPane.NO_OPTION) {
				mainTextArea.replaceRange("", 0, mainTextArea.getText().length());
				stateLabel.setText(" �½��ļ�");
				this.setTitle("�ޱ��� - JustEditor");
				currentFile = null;
				undo.discardAllEdits(); // �������е�"����"����
				unmake.setEnabled(false);
				oldValue = mainTextArea.getText();
			} else if (saveChoose == JOptionPane.CANCEL_OPTION) {
				return;
			}
		} else {
			mainTextArea.replaceRange("", 0, mainTextArea.getText().length());
			add(stateLabel);
			stateLabel.setText("�½��ļ�");
			this.setTitle("�ޱ��� - ���±�");
			currentFile = null;
			undo.discardAllEdits();
			unmake.setEnabled(false);
			oldValue = mainTextArea.getText();
		}
	}

	private void getDateTime() {
		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		String dateTimeString = simpleDateFormat.format(date);
		mainTextArea.append(dateTimeString);
	}

	// ���ҷ���
	public void find() {
		final JDialog findDialog = new JDialog(this, "����", false);// falseʱ������������ͬʱ���ڼ���״̬(����ģʽ)
		Container con = findDialog.getContentPane();// ���ش˶Ի����contentPane����
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel findContentLabel = new JLabel("��������(N)��");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("������һ��(F)��");
		final JCheckBox matchCheckBox = new JCheckBox("���ִ�Сд(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("����(U)");
		final JRadioButton downButton = new JRadioButton("����(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);
		JButton cancel = new JButton("ȡ��");
		// ȡ����ť�¼�����
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findDialog.dispose();
			}
		});
		// "������һ��"��ť����
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// "���ִ�Сд(C)"��JCheckBox�Ƿ�ѡ��
				int k = 0;
				final String str1, str2, str3, str4, strA, strB;
				str1 = mainTextArea.getText();
				str2 = findText.getText();
				str3 = str1.toUpperCase();
				str4 = str2.toUpperCase();
				if (matchCheckBox.isSelected()) {
					strA = str1;
					strB = str2;
				} else {
					strA = str3;
					strB = str4;
				}
				if (upButton.isSelected()) {
					if (mainTextArea.getSelectedText() == null)
						k = strA.lastIndexOf(strB, mainTextArea.getCaretPosition() - 1);
					else
						k = strA.lastIndexOf(strB, mainTextArea.getCaretPosition() - findText.getText().length() - 1);
					if (k > -1) {
						mainTextArea.setCaretPosition(k);
						mainTextArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "�Ҳ��������ҵ����ݣ�", "����", JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (downButton.isSelected()) {
					if (mainTextArea.getSelectedText() == null)
						k = strA.indexOf(strB, mainTextArea.getCaretPosition() + 1);
					else
						k = strA.indexOf(strB, mainTextArea.getCaretPosition() - findText.getText().length() + 1);
					if (k > -1) {
						mainTextArea.setCaretPosition(k);
						mainTextArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "�Ҳ��������ҵ����ݣ�", "����", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "������һ��"��ť��������
			// ����"����"�Ի���Ľ���
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("����"));
		directionPanel.add(upButton);
		directionPanel.add(downButton);
		panel1.setLayout(new GridLayout(2, 1));
		panel1.add(findNextButton);
		panel1.add(cancel);
		panel2.add(findContentLabel);
		panel2.add(findText);
		panel2.add(panel1);
		panel3.add(matchCheckBox);
		panel3.add(directionPanel);
		con.add(panel2);
		con.add(panel3);
		findDialog.setSize(410, 180);
		findDialog.setResizable(false);// ���ɵ�����С
		findDialog.setLocation(230, 280);
		findDialog.setVisible(true);
	}// ���ҷ�������
		// �滻����

	public void replace() {
		final JDialog replaceDialog = new JDialog(this, "�滻", false);// falseʱ������������ͬʱ���ڼ���״̬(����ģʽ)
		Container con = replaceDialog.getContentPane();// ���ش˶Ի����contentPane����
		con.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel findContentLabel = new JLabel("��������(N)��");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("������һ��(F):");
		JLabel replaceLabel = new JLabel("�滻Ϊ(P)��");
		final JTextField replaceText = new JTextField(15);
		JButton replaceButton = new JButton("�滻(R)");
		JButton replaceAllButton = new JButton("ȫ���滻(A)");
		JButton cancel = new JButton("ȡ��");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceDialog.dispose();
			}
		});
		final JCheckBox matchCheckBox = new JCheckBox("���ִ�Сд(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("����(U)");
		final JRadioButton downButton = new JRadioButton("����(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);
		// "������һ��"��ť����
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // "���ִ�Сд(C)"��JCheckBox�Ƿ�ѡ��
				int k = 0;
				final String str1, str2, str3, str4, strA, strB;
				str1 = mainTextArea.getText();
				str2 = findText.getText();
				str3 = str1.toUpperCase();
				str4 = str2.toUpperCase();
				if (matchCheckBox.isSelected()) {
					strA = str1;
					strB = str2;
				} else {
					strA = str3;
					strB = str4;
				}
				if (upButton.isSelected()) {
					if (mainTextArea.getSelectedText() == null)
						k = strA.lastIndexOf(strB, mainTextArea.getCaretPosition() - 1);
					else
						k = strA.lastIndexOf(strB, mainTextArea.getCaretPosition() - findText.getText().length() - 1);
					if (k > -1) {
						mainTextArea.setCaretPosition(k);
						mainTextArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "�Ҳ��������ҵ����ݣ�", "����", JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (downButton.isSelected()) {
					if (mainTextArea.getSelectedText() == null)
						k = strA.indexOf(strB, mainTextArea.getCaretPosition() + 1);
					else
						k = strA.indexOf(strB, mainTextArea.getCaretPosition() - findText.getText().length() + 1);
					if (k > -1) {
						mainTextArea.setCaretPosition(k);
						mainTextArea.select(k, k + strB.length());
					} else {
						JOptionPane.showMessageDialog(null, "�Ҳ��������ҵ����ݣ�", "����", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "������һ��"��ť��������

		// "�滻"��ť����
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceText.getText().length() == 0 && mainTextArea.getSelectedText() != null)
					mainTextArea.replaceSelection("");
				if (replaceText.getText().length() > 0 && mainTextArea.getSelectedText() != null)
					mainTextArea.replaceSelection(replaceText.getText());
			}
		});// "�滻"��ť��������

		// "ȫ���滻"��ť����
		replaceAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainTextArea.setCaretPosition(0); // �����ŵ��༭����ͷ
				int k = 0, replaceCount = 0;
				if (findText.getText().length() == 0) {
					JOptionPane.showMessageDialog(replaceDialog, "����д��������!", "��ʾ", JOptionPane.WARNING_MESSAGE);
					findText.requestFocus(true);
					return;
				}
				while (k > -1) {
					final String str1, str2, str3, str4, strA, strB;
					str1 = mainTextArea.getText();
					str2 = findText.getText();
					str3 = str1.toUpperCase();
					str4 = str2.toUpperCase();
					if (matchCheckBox.isSelected()) {
						strA = str1;
						strB = str2;
					} else {
						strA = str3;
						strB = str4;
					}
					if (upButton.isSelected()) {
						if (mainTextArea.getSelectedText() == null)
							k = strA.lastIndexOf(strB, mainTextArea.getCaretPosition() - 1);
						else
							k = strA.lastIndexOf(strB,
									mainTextArea.getCaretPosition() - findText.getText().length() - 1);
						if (k > -1) {
							mainTextArea.setCaretPosition(k);
							mainTextArea.select(k, k + strB.length());
						} else {
							if (replaceCount == 0) {
								JOptionPane.showMessageDialog(replaceDialog, "�Ҳ��������ҵ�����!", "���±�",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog, "�ɹ��滻" + replaceCount + "��ƥ������!", "�滻�ɹ�",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					} else if (downButton.isSelected()) {
						if (mainTextArea.getSelectedText() == null)
							k = strA.indexOf(strB, mainTextArea.getCaretPosition() + 1);
						else
							k = strA.indexOf(strB, mainTextArea.getCaretPosition() - findText.getText().length() + 1);
						if (k > -1) {
							mainTextArea.setCaretPosition(k);
							mainTextArea.select(k, k + strB.length());
						} else {
							if (replaceCount == 0) {
								JOptionPane.showMessageDialog(replaceDialog, "�Ҳ��������ҵ�����!", "���±�",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog, "�ɹ��滻" + replaceCount + "��ƥ������!", "�滻�ɹ�",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
					if (replaceText.getText().length() == 0 && mainTextArea.getSelectedText() != null) {
						mainTextArea.replaceSelection("");
						replaceCount++;
					}

					if (replaceText.getText().length() > 0 && mainTextArea.getSelectedText() != null) {
						mainTextArea.replaceSelection(replaceText.getText());
						replaceCount++;
					}
				} // whileѭ������
			}
		});// "�滻ȫ��"��������

		// ����"�滻"�Ի���Ľ���
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("����"));
		// ����directionPanel����ı߿�;
		BorderFactory.createTitledBorder(title);// ����һ���±���߿�ʹ��Ĭ�ϱ߿򣨸��񻯣���Ĭ���ı�λ�ã�λ�ڶ����ϣ���Ĭ�ϵ���
		directionPanel.add(upButton);
		directionPanel.add(downButton);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel panel4 = new JPanel();
		panel4.setLayout(new GridLayout(2, 1));
		panel1.add(findContentLabel);
		panel1.add(findText);
		panel1.add(findNextButton);
		panel4.add(replaceButton);
		panel4.add(replaceAllButton);
		panel2.add(replaceLabel);
		panel2.add(replaceText);
		panel2.add(panel4);
		panel3.add(matchCheckBox);
		panel3.add(directionPanel);
		panel3.add(cancel);
		con.add(panel1);
		con.add(panel2);
		con.add(panel3);
		replaceDialog.setSize(420, 220);
		replaceDialog.setResizable(false);// ���ɵ�����С
		replaceDialog.setLocation(230, 280);
		replaceDialog.setVisible(true);
	}// "ȫ���滻"��ť��������

	// "����"����
	public void font() {
		final JDialog fontDialog = new JDialog(this, "��������", false);
		Container con = fontDialog.getContentPane();
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel fontLabel = new JLabel("����(F)��");
		fontLabel.setPreferredSize(new Dimension(100, 20));// ����һ��Dimension���������ʼ��Ϊָ����Ⱥ͸߶�
		JLabel styleLabel = new JLabel("����(Y)��");
		styleLabel.setPreferredSize(new Dimension(100, 20));
		JLabel sizeLabel = new JLabel("��С(S)��");
		sizeLabel.setPreferredSize(new Dimension(100, 20));
		final JLabel sample = new JLabel("JustinNeil-JustEditor");
		final JTextField fontText = new JTextField(9);
		fontText.setPreferredSize(new Dimension(200, 20));
		final JTextField styleText = new JTextField(8);
		styleText.setPreferredSize(new Dimension(200, 20));
		final int style[] = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };
		final JTextField sizeText = new JTextField(5);
		sizeText.setPreferredSize(new Dimension(200, 20));
		JButton okButton = new JButton("ȷ��");
		JButton cancel = new JButton("ȡ��");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fontDialog.dispose();
			}
		});
		Font currentFont = mainTextArea.getFont();
		fontText.setText(currentFont.getFontName());
		fontText.selectAll();
		if (currentFont.getStyle() == Font.PLAIN)
			styleText.setText("����");
		else if (currentFont.getStyle() == Font.BOLD)
			styleText.setText("����");
		else if (currentFont.getStyle() == Font.ITALIC)
			styleText.setText("б��");
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleText.setText("��б��");
		styleText.selectAll();
		String str = String.valueOf(currentFont.getSize());
		sizeText.setText(str);
		sizeText.selectAll();
		final JList<String> fontList, styleList, sizeList;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String fontName[] = ge.getAvailableFontFamilyNames();
		fontList = new JList<String>(fontName);
		fontList.setFixedCellWidth(86);
		fontList.setFixedCellHeight(20);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String fontStyle[] = { "����", "����", "б��", "��б��" };
		styleList = new JList<String>(fontStyle);
		styleList.setFixedCellWidth(86);
		styleList.setFixedCellHeight(20);
		styleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (currentFont.getStyle() == Font.PLAIN)
			styleList.setSelectedIndex(0);
		else if (currentFont.getStyle() == Font.BOLD)
			styleList.setSelectedIndex(1);
		else if (currentFont.getStyle() == Font.ITALIC)
			styleList.setSelectedIndex(2);
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleList.setSelectedIndex(3);
		final String fontSize[] = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36",
				"48", "72" };
		sizeList = new JList<String>(fontSize);
		sizeList.setFixedCellWidth(43);
		sizeList.setFixedCellHeight(20);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				fontText.setText(fontName[fontList.getSelectedIndex()]);
				fontText.selectAll();
				Font sampleFont1 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont1);
			}
		});
		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				int s = style[styleList.getSelectedIndex()];
				styleText.setText(fontStyle[s]);
				styleText.selectAll();
				Font sampleFont2 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont2);
			}
		});
		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				sizeText.setText(fontSize[sizeList.getSelectedIndex()]);
				sizeText.selectAll();
				Font sampleFont3 = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				sample.setFont(sampleFont3);
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Font okFont = new Font(fontText.getText(), style[styleList.getSelectedIndex()],
						Integer.parseInt(sizeText.getText()));
				mainTextArea.setFont(okFont);
				fontDialog.dispose();
			}
		});
		JPanel samplePanel = new JPanel();
		samplePanel.setBorder(BorderFactory.createTitledBorder("ʾ��"));
		samplePanel.add(sample);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		panel2.add(fontText);
		panel2.add(styleText);
		panel2.add(sizeText);
		panel2.add(okButton);
		panel3.add(new JScrollPane(fontList));// JList��֧��ֱ�ӹ���������Ҫ��JList��ΪJScrollPane���ӿ���ͼ
		panel3.add(new JScrollPane(styleList));
		panel3.add(new JScrollPane(sizeList));
		panel3.add(cancel);
		con.add(panel1);
		con.add(panel2);
		con.add(panel3);
		con.add(samplePanel);
		fontDialog.setSize(350, 340);
		fontDialog.setLocation(200, 200);
		fontDialog.setResizable(false);
		fontDialog.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newFile) {
			newFile();
		} else if (e.getSource() == openFile) {
			openFile();
		} else if (e.getSource() == saveFile) {
			saveFile();
		} else if (e.getSource() == otherSaveTo) {
			saveToOther();
		} else if (e.getSource() == pageSet) {

		} else if (e.getSource() == print) {

		} else if (e.getSource() == exit) {
			System.exit(0);
		} else if (e.getSource() == unmake || e.getSource() == popupMenuUndo) {
			mainTextArea.requestFocus();
			if (undo.canUndo()) {
				try {
					undo.undo();
				} catch (CannotUndoException ex) {
					System.out.println("Unable to undo:" + ex);
				}
			}
			if (!undo.canUndo()) {
				unmake.setEnabled(false);
			}
		} else if (e.getSource() == cut || e.getSource() == popupMenuCut) {
			mainTextArea.requestFocus();
			String text = mainTextArea.getSelectedText();
			StringSelection selection = new StringSelection(text);
			clipboard.setContents(selection, null);
			mainTextArea.replaceRange("", mainTextArea.getSelectionStart(), mainTextArea.getSelectionEnd());
			checkMenuItemEnable();
		} else if (e.getSource() == copy || e.getSource() == popupMenuCopy) {
			mainTextArea.requestFocus();
			String text = mainTextArea.getSelectedText();
			StringSelection selection = new StringSelection(text);
			clipboard.setContents(selection, null);
			checkMenuItemEnable();
		} else if (e.getSource() == paste || e.getSource() == popupMenuPaste) {
			mainTextArea.requestFocus();
			Transferable contents = clipboard.getContents(this);
			if (contents == null) {
				return;
			}
			String text = "";
			try {
				text = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			mainTextArea.replaceRange(text, mainTextArea.getSelectionStart(), mainTextArea.getSelectionEnd());
			checkMenuItemEnable();
		} else if (e.getSource() == delete || e.getSource() == popupMenuDelete) {
			mainTextArea.requestFocus();
			mainTextArea.replaceRange("", mainTextArea.getSelectionStart(), mainTextArea.getSelectionEnd());
			checkMenuItemEnable();
		} else if (e.getSource() == find) {
			find();
		} else if (e.getSource() == findNext) {
			find();
		} else if (e.getSource() == replace) {
			replace();
		} else if (e.getSource() == turnTo) {
			turnToLine();
		} else if (e.getSource() == allChoose || e.getSource() == popupMenuSelectAll) {
			mainTextArea.selectAll();
		} else if (e.getSource() == dateTime) {
			getDateTime();
		} else if (e.getSource() == autoNewLine) {
			if(autoNewLine.getState())  
                mainTextArea.setLineWrap(true);  
            else   
                mainTextArea.setLineWrap(false);  
		} else if (e.getSource() == fontStyle) {
			font();
		} else if (e.getSource() == stateBar) {
			if (stateBar.getState())
				stateLabel.setVisible(true);
			else
				stateLabel.setVisible(false);
		} else if (e.getSource() == forHelp) {
			help();
		} else if (e.getSource() == about) {
			about();
		} else if (e.getSource() == popupMenu) {
			checkMenuItemEnable();
		}

	}

	private void turnToLine() {
		// ȡ��������
		int totalLineCount = mainTextArea.getLineCount();
		if (totalLineCount <= 1) {
			return;
		}
		String title = "��ת���У�(1..." + (totalLineCount-1) + ")";
		String line = JOptionPane.showInputDialog(this, title);
		if (line == null || "".equals(line.trim())) {
			return;
		}
		try {
			int intLine = Integer.parseInt(line);
			if (intLine > totalLineCount) {
				return;
			}
			// JTextArea��ʼ�к���0�����Դ˴�����һ����
			int selectionStart = mainTextArea.getLineStartOffset(intLine - 1);
			int selectionEnd = mainTextArea.getLineEndOffset(intLine - 1);

			// ����ǲ������һ�У�selectionEnd����һ������Ϊ��ʹ�����ѡ������ͬһ��
			if (intLine != totalLineCount) {
				selectionEnd--;
			}

			mainTextArea.requestFocus(); // ��ý���

			mainTextArea.setSelectionStart(selectionStart);
			mainTextArea.setSelectionEnd(selectionEnd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class UndoHandler implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent uee) {
			undo.addEdit(uee.getEdit());
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		unmake.setEnabled(true);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		unmake.setEnabled(true);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		unmake.setEnabled(true);
	}

	private void help() {
		JOptionPane.showMessageDialog(this, "��ô�򵥻���Ҫ�������Լ�������QAQ", "��������", JOptionPane.INFORMATION_MESSAGE);
	}

	private void about() {
		JOptionPane.showMessageDialog(this,
				"**********************************************\n" + " author:JustinNeil \n"
						+ " time:2019-05-13                          \n" + " version:v1.0"
						+ "***********************************************\n",
				"JustEditor", JOptionPane.INFORMATION_MESSAGE);
	}

	private void turnTo(int n) {

	}

	public void addAllActionListner() {
		Class<?> c = this.getClass();
		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			Class<?> type = field.getType();
			if (type == JMenuItem.class) {
				try {
					Method method = type.getMethod("addActionListener", ActionListener.class);
					method.invoke(field.get(this), this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}

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
 * 记事本主窗口类
 * 
 * @author JustinNeil
 */
class EditMainFrame extends JFrame implements ActionListener, DocumentListener {

	private static final long serialVersionUID = 1L;
	private String title;
	JMenuBar jMenuBar;
	JMenu fileMenu, edit, format, read, help;
	// 文件菜单项
	JMenuItem newFile, openFile, saveFile, otherSaveTo, pageSet, print, exit;
	// 编辑菜单项
	JMenuItem unmake, cut, copy, paste, delete, find, findNext, replace, turnTo, allChoose, dateTime;
	JCheckBoxMenuItem autoNewLine;
	JMenuItem fontStyle;
	JCheckBoxMenuItem stateBar;
	// 帮助菜单项
	JMenuItem forHelp, about;
	JTextArea mainTextArea;
	JPopupMenu popupMenu;
	// 右键弹出菜单项
	JMenuItem popupMenuUndo, popupMenuCut, popupMenuCopy, popupMenuPaste, popupMenuDelete, popupMenuSelectAll;
	JLabel stateLabel;
	FileDialog openDialog, saveDialog, saveToOtherDialog;
	File currentFile;// 当前文件
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
		// 文本编辑区注册右键菜单事件
		mainTextArea.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())// 返回此鼠标事件是否为该平台的弹出菜单触发事件
				{
					popupMenu.show(e.getComponent(), e.getX(), e.getY());// 在组件调用者的坐标空间中的位置
				}
				checkMenuItemEnable();// 设置剪切，复制，粘帖，删除等功能的可用性
				mainTextArea.requestFocus();// 编辑区获取焦点
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())// 返回此鼠标事件是否为该平台的弹出菜单触发事件
				{
					popupMenu.show(e.getComponent(), e.getX(), e.getY());// 在组件调用者的坐标空间中的位置
				}
				checkMenuItemEnable();// 设置剪切，复制，粘帖，删除等功能的可用性
				mainTextArea.requestFocus();// 编辑区获取焦点
			}
		});// 文本编辑区注册右键菜单事件结束
		stateLabel = new JLabel("按F1获取文本帮助");
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
		// 创建右键弹出菜单
		popupMenu = new JPopupMenu();
		popupMenuUndo = new JMenuItem("撤销(U)");
		popupMenuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		popupMenuCut = new JMenuItem("剪切(T)");
		popupMenuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		popupMenuCopy = new JMenuItem("复制(C)");
		popupMenuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		popupMenuPaste = new JMenuItem("粘帖(P)");
		popupMenuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		popupMenuDelete = new JMenuItem("删除(D)");
		popupMenuSelectAll = new JMenuItem("全选(A)");
		popupMenuSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		// 向右键菜单添加菜单项和分隔符
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
		fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic('F');
		newFile = new JMenuItem("新建(N)");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		openFile = new JMenuItem("打开(O)");
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openDialog = new FileDialog(this, "打开", FileDialog.LOAD);
		saveFile = new JMenuItem("保存(S)");
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveDialog = new FileDialog(this, "保存", FileDialog.SAVE);
		otherSaveTo = new JMenuItem("另存为(A)... ");
		saveToOtherDialog = new FileDialog(this, "另存为", FileDialog.SAVE);
		pageSet = new JMenuItem("页面设置(U)...");
		print = new JMenuItem("打印(P)");
		print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		exit = new JMenuItem("退出(X)");
		fileMenu.add(newFile);
		fileMenu.add(openFile);
		fileMenu.add(saveFile);
		fileMenu.add(otherSaveTo);
		fileMenu.addSeparator();
		fileMenu.add(pageSet);
		fileMenu.add(print);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		unmake = new JMenuItem("撤销(U)");
		unmake.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		cut = new JMenuItem("剪切(T)");
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		copy = new JMenuItem("复制(C)");
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		paste = new JMenuItem("粘贴(P)");
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		delete = new JMenuItem("删除(L) ");
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		find = new JMenuItem("查找(F)...");
		find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		findNext = new JMenuItem("查找下一个(N)");
		findNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		replace = new JMenuItem("替换(R)...");
		replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		turnTo = new JMenuItem("转到(G)...");
		turnTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		turnTo.addActionListener(this);
		allChoose = new JMenuItem("全选(A)");
		allChoose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		dateTime = new JMenuItem("时间/日期(D)");
		allChoose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		edit = new JMenu("编辑(E)");
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
		format = new JMenu("格式(O)");
		format.setMnemonic('O');
		autoNewLine = new JCheckBoxMenuItem("自动换行(W)");
		autoNewLine.setState(true);
		autoNewLine.addActionListener(this);
		fontStyle = new JMenuItem("字体(F)...");
		fontStyle.addActionListener(this);
		format.add(autoNewLine);
		format.add(fontStyle);
		read = new JMenu("查看(V)");
		read.setMnemonic('O');
		stateBar = new JCheckBoxMenuItem("状态栏");
		stateBar.addActionListener(this);
		stateBar.setState(true);
		read.add(stateBar);
		help = new JMenu("帮助(H)");
		help.setMnemonic('H');
		forHelp = new JMenuItem("查看帮助");
		forHelp.addActionListener(this);
		forHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		about = new JMenuItem("关于JustEditor");
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
			int exitChoose = JOptionPane.showConfirmDialog(this, "您的文件尚未保存，是否保存？", "退出提示",
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
		String dirpath = openDialog.getDirectory();// 获取打开文件路径并保存到字符串中。
		String fileName = openDialog.getFile();// 获取打开文件名称并保存到字符串中
		if (dirpath == null || fileName == null)// 判断路径和文件是否为空
			return;
		else
			mainTextArea.setText(null);// 文件不为空，清空原来文件内容。
		currentFile = new File(dirpath, fileName);// 创建新的路径和名称
		try {
			BufferedReader bufr = new BufferedReader(new FileReader(currentFile));// 尝试从文件中读东西
			String line = null;// 变量字符串初始化为空
			while ((line = bufr.readLine()) != null) {
				mainTextArea.append(line + "\r\n");// 显示每一行内容
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
			saveDialog.setVisible(true);// 显示保存文件对话框
			String dirpath = saveDialog.getDirectory();// 获取保存文件路径并保存到字符串中。
			String fileName = saveDialog.getFile();//// 获取打保存文件名称并保存到字符串中
			if (dirpath == null || fileName == null)// 判断路径和文件是否为空
				return;
			else
				currentFile = new File(dirpath, fileName);// 文件不为空，新建一个路径和名称
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
		saveDialog.setVisible(true);// 显示保存文件对话框
		String dirpath = saveDialog.getDirectory();// 获取保存文件路径并保存到字符串中。
		String fileName = saveDialog.getFile();//// 获取打保存文件名称并保存到字符串中
		if (dirpath == null || fileName == null) {// 判断路径和文件是否为空
			return;// 空操作
		}
		File otherFile = new File(dirpath, fileName);// 文件不为空，新建一个路径和名称
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
			int saveChoose = JOptionPane.showConfirmDialog(this, "您的文件尚未保存，是否保存？", "提示",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (saveChoose == JOptionPane.YES_OPTION) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("另存为");
				int result = fileChooser.showSaveDialog(this);
				if (result == JFileChooser.CANCEL_OPTION) {
					stateLabel.setText("您没有选择任何文件");
					return;
				}
				File saveFile = fileChooser.getSelectedFile();
				if (saveFile == null || saveFile.getName().equals("")) {
					JOptionPane.showMessageDialog(this, "不合法的文件名", "不合法的文件名", JOptionPane.ERROR_MESSAGE);
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
						stateLabel.setText("当前打开文件：" + saveFile.getAbsoluteFile());
					} catch (IOException ioException) {
					}
				}
			} else if (saveChoose == JOptionPane.NO_OPTION) {
				mainTextArea.replaceRange("", 0, mainTextArea.getText().length());
				stateLabel.setText(" 新建文件");
				this.setTitle("无标题 - JustEditor");
				currentFile = null;
				undo.discardAllEdits(); // 撤消所有的"撤消"操作
				unmake.setEnabled(false);
				oldValue = mainTextArea.getText();
			} else if (saveChoose == JOptionPane.CANCEL_OPTION) {
				return;
			}
		} else {
			mainTextArea.replaceRange("", 0, mainTextArea.getText().length());
			add(stateLabel);
			stateLabel.setText("新建文件");
			this.setTitle("无标题 - 记事本");
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

	// 查找方法
	public void find() {
		final JDialog findDialog = new JDialog(this, "查找", false);// false时允许其他窗口同时处于激活状态(即无模式)
		Container con = findDialog.getContentPane();// 返回此对话框的contentPane对象
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel findContentLabel = new JLabel("查找内容(N)：");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("查找下一个(F)：");
		final JCheckBox matchCheckBox = new JCheckBox("区分大小写(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("向上(U)");
		final JRadioButton downButton = new JRadioButton("向下(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);
		JButton cancel = new JButton("取消");
		// 取消按钮事件处理
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findDialog.dispose();
			}
		});
		// "查找下一个"按钮监听
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// "区分大小写(C)"的JCheckBox是否被选中
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
						JOptionPane.showMessageDialog(null, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
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
						JOptionPane.showMessageDialog(null, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "查找下一个"按钮监听结束
			// 创建"查找"对话框的界面
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("方向"));
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
		findDialog.setResizable(false);// 不可调整大小
		findDialog.setLocation(230, 280);
		findDialog.setVisible(true);
	}// 查找方法结束
		// 替换方法

	public void replace() {
		final JDialog replaceDialog = new JDialog(this, "替换", false);// false时允许其他窗口同时处于激活状态(即无模式)
		Container con = replaceDialog.getContentPane();// 返回此对话框的contentPane对象
		con.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel findContentLabel = new JLabel("查找内容(N)：");
		final JTextField findText = new JTextField(15);
		JButton findNextButton = new JButton("查找下一个(F):");
		JLabel replaceLabel = new JLabel("替换为(P)：");
		final JTextField replaceText = new JTextField(15);
		JButton replaceButton = new JButton("替换(R)");
		JButton replaceAllButton = new JButton("全部替换(A)");
		JButton cancel = new JButton("取消");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceDialog.dispose();
			}
		});
		final JCheckBox matchCheckBox = new JCheckBox("区分大小写(C)");
		ButtonGroup bGroup = new ButtonGroup();
		final JRadioButton upButton = new JRadioButton("向上(U)");
		final JRadioButton downButton = new JRadioButton("向下(U)");
		downButton.setSelected(true);
		bGroup.add(upButton);
		bGroup.add(downButton);
		// "查找下一个"按钮监听
		findNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // "区分大小写(C)"的JCheckBox是否被选中
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
						JOptionPane.showMessageDialog(null, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
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
						JOptionPane.showMessageDialog(null, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});// "查找下一个"按钮监听结束

		// "替换"按钮监听
		replaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (replaceText.getText().length() == 0 && mainTextArea.getSelectedText() != null)
					mainTextArea.replaceSelection("");
				if (replaceText.getText().length() > 0 && mainTextArea.getSelectedText() != null)
					mainTextArea.replaceSelection(replaceText.getText());
			}
		});// "替换"按钮监听结束

		// "全部替换"按钮监听
		replaceAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainTextArea.setCaretPosition(0); // 将光标放到编辑区开头
				int k = 0, replaceCount = 0;
				if (findText.getText().length() == 0) {
					JOptionPane.showMessageDialog(replaceDialog, "请填写查找内容!", "提示", JOptionPane.WARNING_MESSAGE);
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
								JOptionPane.showMessageDialog(replaceDialog, "找不到您查找的内容!", "记事本",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog, "成功替换" + replaceCount + "个匹配内容!", "替换成功",
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
								JOptionPane.showMessageDialog(replaceDialog, "找不到您查找的内容!", "记事本",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(replaceDialog, "成功替换" + replaceCount + "个匹配内容!", "替换成功",
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
				} // while循环结束
			}
		});// "替换全部"方法结束

		// 创建"替换"对话框的界面
		JPanel directionPanel = new JPanel();
		directionPanel.setBorder(BorderFactory.createTitledBorder("方向"));
		// 设置directionPanel组件的边框;
		BorderFactory.createTitledBorder(title);// 创建一个新标题边框，使用默认边框（浮雕化）、默认文本位置（位于顶线上）、默认调整
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
		replaceDialog.setResizable(false);// 不可调整大小
		replaceDialog.setLocation(230, 280);
		replaceDialog.setVisible(true);
	}// "全部替换"按钮监听结束

	// "字体"方法
	public void font() {
		final JDialog fontDialog = new JDialog(this, "字体设置", false);
		Container con = fontDialog.getContentPane();
		con.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel fontLabel = new JLabel("字体(F)：");
		fontLabel.setPreferredSize(new Dimension(100, 20));// 构造一个Dimension，并将其初始化为指定宽度和高度
		JLabel styleLabel = new JLabel("字形(Y)：");
		styleLabel.setPreferredSize(new Dimension(100, 20));
		JLabel sizeLabel = new JLabel("大小(S)：");
		sizeLabel.setPreferredSize(new Dimension(100, 20));
		final JLabel sample = new JLabel("JustinNeil-JustEditor");
		final JTextField fontText = new JTextField(9);
		fontText.setPreferredSize(new Dimension(200, 20));
		final JTextField styleText = new JTextField(8);
		styleText.setPreferredSize(new Dimension(200, 20));
		final int style[] = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC };
		final JTextField sizeText = new JTextField(5);
		sizeText.setPreferredSize(new Dimension(200, 20));
		JButton okButton = new JButton("确定");
		JButton cancel = new JButton("取消");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fontDialog.dispose();
			}
		});
		Font currentFont = mainTextArea.getFont();
		fontText.setText(currentFont.getFontName());
		fontText.selectAll();
		if (currentFont.getStyle() == Font.PLAIN)
			styleText.setText("常规");
		else if (currentFont.getStyle() == Font.BOLD)
			styleText.setText("粗体");
		else if (currentFont.getStyle() == Font.ITALIC)
			styleText.setText("斜体");
		else if (currentFont.getStyle() == (Font.BOLD + Font.ITALIC))
			styleText.setText("粗斜体");
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
		final String fontStyle[] = { "常规", "粗体", "斜体", "粗斜体" };
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
		samplePanel.setBorder(BorderFactory.createTitledBorder("示例"));
		samplePanel.add(sample);
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		panel2.add(fontText);
		panel2.add(styleText);
		panel2.add(sizeText);
		panel2.add(okButton);
		panel3.add(new JScrollPane(fontList));// JList不支持直接滚动，所以要让JList作为JScrollPane的视口视图
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
		// 取得总行数
		int totalLineCount = mainTextArea.getLineCount();
		if (totalLineCount <= 1) {
			return;
		}
		String title = "跳转至行：(1..." + (totalLineCount-1) + ")";
		String line = JOptionPane.showInputDialog(this, title);
		if (line == null || "".equals(line.trim())) {
			return;
		}
		try {
			int intLine = Integer.parseInt(line);
			if (intLine > totalLineCount) {
				return;
			}
			// JTextArea起始行号是0，所以此处做减一处理
			int selectionStart = mainTextArea.getLineStartOffset(intLine - 1);
			int selectionEnd = mainTextArea.getLineEndOffset(intLine - 1);

			// 如果是不是最后一行，selectionEnd做减一处理，是为了使光标与选中行在同一行
			if (intLine != totalLineCount) {
				selectionEnd--;
			}

			mainTextArea.requestFocus(); // 获得焦点

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
		JOptionPane.showMessageDialog(this, "这么简单还需要帮助吗，自己看看吧QAQ", "帮助主题", JOptionPane.INFORMATION_MESSAGE);
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

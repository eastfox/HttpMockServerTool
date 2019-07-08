package com.tester.httpServer.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.tester.httpServer.utils.FileUtils;
import com.tester.httpServer.utils.HttpContext;
import com.tester.httpServer.utils.HttpHandler;
import com.tester.httpServer.utils.HttpMockServer;
import com.tester.httpServer.views.AboutDialog;
import com.tester.httpServer.views.HelpDialog;

public class MainInterface extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField textOfPort;
	private JTable urlsTable;
	private JButton startButton;
	private JButton stopButton;
	private JButton rebootButton;
	private JTextArea areaOfConsole;
	private JComboBox<Object> comboBoxOfServerType;
	private JComboBox<Object> comboBoxOfProtocolType;
	private Thread t;

	// 主方法
	public static void main(String[] args) {
		MainInterface mainInterface = new MainInterface();
		mainInterface.setDefaultCloseOperation(3);
		mainInterface.setVisible(true);
	}

	public MainInterface() {
		initFrame();
		initMenuBar();
		initLayout();
	}

	// 绘制窗体
	private void initFrame() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		setResizable(false);
		setSize(800, 600);
		setLocation(screenWidth * 1 / 8, screenHeight * 1 / 8);
		String fileName = FileName();
		setTitle(fileName + "接口测试桩工具(v2.0)");
	}

	// 添加主菜单
	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(Box.createRigidArea(new Dimension(0, 25)));
		setJMenuBar(menuBar);
		// 帮助按钮
		JMenu fileMenu = new JMenu("帮助");
		menuBar.add(fileMenu);
		JMenuItem helpItem = new JMenuItem("帮助");
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpDialog dialog = new HelpDialog(MainInterface.this);
				dialog.setVisible(true);
			}
		});
		fileMenu.add(helpItem);
		// 关于按钮
		JMenu helpMenu = new JMenu("关于");
		menuBar.add(helpMenu);
		JMenuItem aboutItem = new JMenuItem("关于");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog dialog = new AboutDialog(MainInterface.this);
				dialog.setVisible(true);
			}
		});
		helpMenu.add(aboutItem);
	}

	// 初始化端口/服务器端类型/加密协议类型区域
	private void initLayout() {
		JPanel jpanel = new JPanel();
		add(jpanel);
		JPanel portConfigPanel = initPortJPanel();
		JPanel configPanel = initConfigJPanel();
		JPanel consolePanel = initConsoleJPanel();
		jpanel.add(portConfigPanel);
		jpanel.add(configPanel);
		jpanel.add(consolePanel);
	}

	// 绘制端口/服务器端类型/加密协议类型区域
	private JPanel initPortJPanel() {
		JPanel portConfigPanel = new JPanel();
		portConfigPanel.setLayout(new BorderLayout());
		portConfigPanel.setBorder(BorderFactory
				.createTitledBorder("端口设置&服务端类型&加密协议类型"));
		portConfigPanel.setPreferredSize(new Dimension(800, 50));

		JPanel settings = new JPanel();
		// 端口相关
		settings.setLayout(new GridLayout(1, 3));
		this.textOfPort = new JTextField();
		settings.add(this.textOfPort);
		// 服务端类型相关
		// TODO 加上https相关请求
		String[] serverTypeStrings = { "HTTP" };// , "HTTPS" };
		this.comboBoxOfServerType = new JComboBox<Object>(serverTypeStrings);
		settings.add(this.comboBoxOfServerType);
		// 加密协议类型相关
		String[] protocolTypeStrings = { "SSL", "SSLv2", "SSLv3", "TLS",
				"TLSv1", "TLSv1.1", "TLSv1.2" };
		this.comboBoxOfProtocolType = new JComboBox<Object>(protocolTypeStrings);
		settings.add(this.comboBoxOfProtocolType);
		portConfigPanel.add(settings, "North");

		return portConfigPanel;
	}

	// URL和response文件路径设置区域
	private JPanel initConfigJPanel() {
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BorderLayout());
		configPanel.setBorder(BorderFactory.createTitledBorder("请求和响应文件路径设置"));
		configPanel.setPreferredSize(new Dimension(800, 75));
		// 表格绘制区域
		Object[][] urlsAndPaths = null;
		try {
			urlsAndPaths = initConfigTableData();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] columnName = { "请求和响应文件路径" };
		this.urlsTable = new JTable(urlsAndPaths, columnName);
		this.urlsTable.putClientProperty("terminateEditOnFocusLost",
				Boolean.valueOf(true));
		configPanel.add(new JScrollPane(this.urlsTable));
		return configPanel;
	}

	// 控制台区域绘图
	private JPanel initConsoleJPanel() {
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new BorderLayout());
		consolePanel.setBorder(BorderFactory.createTitledBorder("控制台"));
		consolePanel.setPreferredSize(new Dimension(800, 380));
		// 添加按钮
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 3));
		this.startButton = new JButton("启动");
		this.stopButton = new JButton("停止");
		this.rebootButton = new JButton("重新加载");
		buttons.add(this.startButton);
		buttons.add(this.stopButton);
		buttons.add(this.rebootButton);
		consolePanel.add(buttons, "North");
		// 添加输出屏区域属性
		this.areaOfConsole = new JTextArea();
		this.areaOfConsole.setEditable(false);
		this.areaOfConsole.setLineWrap(true);
		consolePanel.add(new JScrollPane(this.areaOfConsole));
		final JPopupMenu clearConsoleMenu = new JPopupMenu();
		JMenuItem itemOfClearConsole = new JMenuItem("清空");
		clearConsoleMenu.add(itemOfClearConsole);
		// 添加控制按钮
		this.startButton.addActionListener(new StartListener());
		this.stopButton.addActionListener(new StopListener());
		this.rebootButton.addActionListener(new RebootListener());
		itemOfClearConsole.addActionListener(new ClearListener());
		// 添加鼠标响应
		this.areaOfConsole.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				triggerEvent(event);
			}

			public void mouseReleased(MouseEvent event) {
				triggerEvent(event);
			}

			private void triggerEvent(MouseEvent event) {
				if (event.isPopupTrigger()) {
					clearConsoleMenu.show(event.getComponent(), event.getX(),
							event.getY());
				}
			}
		});
		this.areaOfConsole.setComponentPopupMenu(clearConsoleMenu);

		return consolePanel;
	}

	HttpContext httpContext = HttpHandler.httpContext;

	// 启动操作
	private void startListener() {
		// 将关闭标签设置为否
		httpContext.setStopMe(false);
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
		// 添加线程
		t = new Thread(new HttpMockServer());
		// System.out.println("新建线程完成:" + df.format(new Date()));
		ArrayList<String> configData = new ArrayList<String>();
		// 设置端口
		httpContext.setPort(Integer.valueOf(this.textOfPort.getText())
				.intValue());
		// new HttpMockServer();
		// TODO https相关改动
		// if (this.comboBoxOfServerType.getSelectedItem().equals("HTTP")) {
		// this.httpMockServer.initServer(Integer.valueOf(
		// this.textOfPort.getText()).intValue());
		// } else {
		// this.httpMockServer.initServer(
		// Integer.valueOf(this.textOfPort.getText()).intValue(),
		// (String) this.comboBoxOfProtocolType.getSelectedItem());
		// }
		// 设置响应读取文件地址
		String file = (String) this.urlsTable.getValueAt(0, 0);
		if (!file.equals("")) {
			// System.out.println("开始读取xlsx:" + df.format(new Date()));
			// HttpHandler httpHandler = new HttpHandler();
			HttpMockServer.httpHandler.readFile(file);
			// System.out.println("xlsx读取完成:" + df.format(new Date()));
			HttpHandler.httpContext.setPort(httpContext.getPort());
			t.start();
			// System.out.println("线程启动完成:" + df.format(new Date()));
			// System.out.println(t.getState());
			httpContext.setAreaOfConsole(this.areaOfConsole);
			configData.add(file);
		}
		if (this.comboBoxOfServerType.getSelectedItem().equals("HTTP")) {
			this.areaOfConsole.append("HTTP Server启动成功，端口号为"
					+ this.textOfPort.getText() + "。\r\n");
		} else {
			this.areaOfConsole.append("HTTPS Server启动成功，端口号为"
					+ this.textOfPort.getText() + "，" + "加密协议为"
					+ (String) this.comboBoxOfProtocolType.getSelectedItem()
					+ "。\r\n");
		}
		if (configData != null) {
			try {
				String configFile = System.getProperty("user.dir") + "\\"
						+ "HttpMockServerTool.config";
				PrintWriter writer;

				writer = new PrintWriter(configFile, "utf-8");

				for (int i = 0; i < configData.size(); i++) {
					String configs = (String) configData.get(i);
					writer.println(configs);
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void stopListener() {
		// TODO 无法正确停止
		httpContext.setStopMe(true);
		t.interrupt();
		this.areaOfConsole.append("接口服务停止。\r\n");
	}

	private void rebootListener() {
		stopListener();
		startListener();
	}

	// 初始化配置文件对象
	private Object[][] initConfigTableData()
			throws UnsupportedEncodingException {
		Object[][] urlsAndPaths = new Object[1][1];
		ArrayList<String> configLines = null;
		String fileName = System.getProperty("user.dir") + "\\"
				+ "HttpMockServerTool.config";
		configLines = FileUtils.readConfigFile(fileName);
		if (configLines != null) {
			int sizeOfTable = configLines.size();
			if (sizeOfTable > 9) {
				sizeOfTable = 9;
			}
			for (int i = 0; i < sizeOfTable; i++) {
				String configLine = (String) configLines.get(i);
				configLine = new String(configLine.getBytes(), "utf-8");
				String[] urlAndPath = configLine.split(",");
				urlsAndPaths[0][0] = urlAndPath[0];
			}
		}
		return urlsAndPaths;
	}

	private class StartListener implements ActionListener {
		private StartListener() {
		}

		public void actionPerformed(ActionEvent e) {
			MainInterface.this.startListener();
		}
	}

	private class StopListener implements ActionListener {
		private StopListener() {
		}

		public void actionPerformed(ActionEvent e) {
			MainInterface.this.stopListener();
		}
	}

	private class RebootListener implements ActionListener {
		private RebootListener() {
		}

		public void actionPerformed(ActionEvent e) {
			MainInterface.this.rebootListener();
		}
	}

	private class ClearListener implements ActionListener {
		private ClearListener() {
		}

		public void actionPerformed(ActionEvent e) {
			MainInterface.this.areaOfConsole.setText("");
		}
	}

	// 获取当前目录下jar包的名称
	private static String FileName() {
		String fileName = "";
		File file = new File("./");
		File[] fileList = file.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			String tmpName = fileList[i].getName();
			if (fileList[i].isFile()) {
				if (tmpName.substring(tmpName.indexOf(".")).equals(".jar")) {
					fileName = tmpName.substring(0, tmpName.indexOf("."))
							+ " - ";
				}
			}
		}
		return fileName;
	}
}

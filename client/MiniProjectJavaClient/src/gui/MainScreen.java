package gui;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
//import javax.swing.event.TreeSelectionEvent;
//import javax.swing.event.TreeSelectionListener;

import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import connection.HttpConnection;
import crypto.EncDecHashHMac;
import partOfDNDController.NodeFile;
import partOfDNDController.SwingWorkerDownload;
import partOfDNDController.SwingWorkerUpload;
import partOfDNDController.UpdatableTableModel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import java.io.File;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Semaphore;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import javax.swing.JSeparator;

import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTable;

/**
 * This class represents the gui's main screen 
 * it consists of 2 trees and upload and download are done by drag and drop from file node
 * into directory node of the tree , overwrite is achieved by dragging onto the overwritten node
 * other actions are performed after clicking on right button of the mouse 
 * while hovering on the requested node of the tree and getting a menu for the actions
 * 
 */
public class MainScreen extends JFrame {

	private JPanel contentPane;
	private JTable tableDownloads;
	private JTable tableUploads;
	private JTree jtreeLocal;
	private DefaultTreeModel modeljtreeLocal;
	private JScrollPane scrollPaneRemote;
	private JTree jtreeRemote;
	private DefaultTreeModel modeljtreeRemote; 
	private String pathTargetRemote;
	private String pathTargetLocal;
	private File dirLocal;
	private JScrollPane scrollPaneLocal;

	private UpdatableTableModel modelDownloads;
	private UpdatableTableModel modelUploads;

	private Semaphore sem;

	private static boolean DEBUG = true;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainScreen frame = new MainScreen("username");
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainScreen(String username) {
		sem = new Semaphore(1);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 430);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.rowHeights = new int[] {0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.weighty = 0.02;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel lblNewLabel = new JLabel(username+" logged in:");
		panel.add(lblNewLabel);

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 1;
		contentPane.add(panel_3, gbc_panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));

		JSeparator separator = new JSeparator();
		panel_3.add(separator, BorderLayout.NORTH);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weighty = 0.5;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		contentPane.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0};
		panel_1.setLayout(gbl_panel_1);

		JPanel panel_12 = new JPanel();
		GridBagConstraints gbc_panel_12 = new GridBagConstraints();
		gbc_panel_12.weightx = 0.22;
		gbc_panel_12.weighty = 0.05;
		gbc_panel_12.insets = new Insets(0, 0, 5, 5);
		gbc_panel_12.fill = GridBagConstraints.BOTH;
		gbc_panel_12.gridx = 0;
		gbc_panel_12.gridy = 0;
		panel_1.add(panel_12, gbc_panel_12);
		GridBagLayout gbl_panel_12 = new GridBagLayout();
		gbl_panel_12.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_panel_12.rowWeights = new double[]{0.0};
		panel_12.setLayout(gbl_panel_12);

		JLabel lblNewLabel_2 = new JLabel("local:");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_2.weightx = 0.1;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel_12.add(lblNewLabel_2, gbc_lblNewLabel_2);

		JPanel panel_14 = new JPanel();
		GridBagConstraints gbc_panel_14 = new GridBagConstraints();
		gbc_panel_14.insets = new Insets(0, 0, 5, 0);
		gbc_panel_14.weightx = 0.4;
		gbc_panel_14.fill = GridBagConstraints.BOTH;
		gbc_panel_14.gridx = 1;
		gbc_panel_14.gridy = 0;
		panel_12.add(panel_14, gbc_panel_14);

		JButton btnLoadDir = new JButton("load dir");

		GridBagConstraints gbc_btnLoadDir = new GridBagConstraints();
		gbc_btnLoadDir.weightx = 0.1;
		gbc_btnLoadDir.insets = new Insets(0, 0, 0, 5);
		gbc_btnLoadDir.gridx = 2;
		gbc_btnLoadDir.gridy = 0;
		panel_12.add(btnLoadDir, gbc_btnLoadDir);

		JPanel panel_13 = new JPanel();
		GridBagConstraints gbc_panel_13 = new GridBagConstraints();
		gbc_panel_13.weightx = 0.45;
		gbc_panel_13.weighty = 0.05;
		gbc_panel_13.insets = new Insets(0, 0, 5, 0);
		gbc_panel_13.fill = GridBagConstraints.BOTH;
		gbc_panel_13.gridx = 2;
		gbc_panel_13.gridy = 0;
		panel_1.add(panel_13, gbc_panel_13);
		GridBagLayout gbl_panel_13 = new GridBagLayout();
		gbl_panel_13.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_13.rowWeights = new double[]{0.0};
		panel_13.setLayout(gbl_panel_13);

		JLabel lblNewLabel_3 = new JLabel("remote:");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_3.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 0;
		panel_13.add(lblNewLabel_3, gbc_lblNewLabel_3);

		JPanel panel_15 = new JPanel();
		GridBagConstraints gbc_panel_15 = new GridBagConstraints();
		gbc_panel_15.weightx = 0.5;
		gbc_panel_15.fill = GridBagConstraints.BOTH;
		gbc_panel_15.gridx = 1;
		gbc_panel_15.gridy = 0;
		panel_13.add(panel_15, gbc_panel_15);

		JPanel panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.weighty = 0.5;
		gbc_panel_4.weightx = 0.22;
		gbc_panel_4.insets = new Insets(0, 0, 0, 5);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		panel_1.add(panel_4, gbc_panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		this.scrollPaneLocal = new JScrollPane();
		panel_4.add(scrollPaneLocal);

		//JTree tree = new JTree();
		this.jtreeLocal = new JTree();
		this.dirLocal = new java.io.File(".");
		load(this.dirLocal);
		this.jtreeLocal.setDragEnabled(true);
		this.jtreeLocal.setDropMode(DropMode.ON_OR_INSERT);

		this.modeljtreeLocal = (DefaultTreeModel)jtreeLocal.getModel();
		this.jtreeLocal.setTransferHandler(new TreeTransferHandlerLocal());

		scrollPaneLocal.setViewportView(this.jtreeLocal);
		// button for loading a selected directory for the loocal tree
		btnLoadDir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("load dir");

				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("choose directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//
				// disable the "All files" option.
				//
				chooser.setAcceptAllFileFilterUsed(false);
				//    
				int returnVal = chooser.showSaveDialog(MainScreen.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
				}else {
					System.out.println("error in choosing directory");
					return;
				}

				load(chooser.getSelectedFile());


			}
		});




		JPanel panel_5 = new JPanel();
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.gridheight = 2;
		gbc_panel_5.weightx = 0.01;
		gbc_panel_5.insets = new Insets(0, 0, 0, 5);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 1;
		gbc_panel_5.gridy = 0;
		panel_1.add(panel_5, gbc_panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		panel_5.add(separator_1, BorderLayout.CENTER);

		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.weighty = 0.5;
		gbc_panel_6.weightx = 0.45;
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 2;
		gbc_panel_6.gridy = 1;
		panel_1.add(panel_6, gbc_panel_6);
		panel_6.setLayout(new BorderLayout(0, 0));

		this.scrollPaneRemote = new JScrollPane();
		panel_6.add(scrollPaneRemote);

		this.jtreeRemote = new JTree();
		//loading the remote files into the remote tree
		loadRemote();

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weighty = 0.3;
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		contentPane.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWeights = new double[]{1.0};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0};
		panel_2.setLayout(gbl_panel_2);

		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.weighty = 0.4;
		gbc_panel_7.insets = new Insets(0, 0, 5, 0);
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.gridx = 0;
		gbc_panel_7.gridy = 0;
		panel_2.add(panel_7, gbc_panel_7);
		GridBagLayout gbl_panel_7 = new GridBagLayout();
		gbl_panel_7.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_7.rowWeights = new double[]{1.0};
		panel_7.setLayout(gbl_panel_7);

		JLabel lblDownloads = new JLabel("Downloads:");
		GridBagConstraints gbc_lblDownloads = new GridBagConstraints();
		gbc_lblDownloads.weightx = 0.05;
		gbc_lblDownloads.insets = new Insets(0, 0, 5, 0);
		gbc_lblDownloads.gridx = 0;
		gbc_lblDownloads.gridy = 0;
		panel_7.add(lblDownloads, gbc_lblDownloads);

		JPanel panel_10 = new JPanel();
		GridBagConstraints gbc_panel_10 = new GridBagConstraints();
		gbc_panel_10.weightx = 0.5;
		gbc_panel_10.fill = GridBagConstraints.BOTH;
		gbc_panel_10.gridx = 1;
		gbc_panel_10.gridy = 0;
		panel_7.add(panel_10, gbc_panel_10);
		panel_10.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_10.add(scrollPane_2);

		this.modelDownloads = new UpdatableTableModel();
		tableDownloads = new JTable();
		tableDownloads.setModel(this.modelDownloads);
		tableDownloads.getColumn("Status").setCellRenderer(new ProgressCellRender());
		scrollPane_2.setViewportView(tableDownloads);

		JPanel panel_8 = new JPanel();
		GridBagConstraints gbc_panel_8 = new GridBagConstraints();
		gbc_panel_8.weighty = 0.4;
		gbc_panel_8.insets = new Insets(0, 0, 5, 0);
		gbc_panel_8.fill = GridBagConstraints.BOTH;
		gbc_panel_8.gridx = 0;
		gbc_panel_8.gridy = 1;
		panel_2.add(panel_8, gbc_panel_8);
		GridBagLayout gbl_panel_8 = new GridBagLayout();
		gbl_panel_8.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_8.rowWeights = new double[]{1.0};
		panel_8.setLayout(gbl_panel_8);

		JLabel lblNewLabel_1 = new JLabel("   Uploads:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.weightx = 0.05;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_8.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JPanel panel_11 = new JPanel();
		GridBagConstraints gbc_panel_11 = new GridBagConstraints();
		gbc_panel_11.weightx = 0.41;
		gbc_panel_11.fill = GridBagConstraints.BOTH;
		gbc_panel_11.gridx = 1;
		gbc_panel_11.gridy = 0;
		panel_8.add(panel_11, gbc_panel_11);
		panel_11.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_3 = new JScrollPane();
		panel_11.add(scrollPane_3);

		this.modelUploads = new UpdatableTableModel();
		tableUploads = new JTable();
		tableUploads.setModel(this.modelUploads);
		tableUploads.getColumn("Status").setCellRenderer(new ProgressCellRender());
		scrollPane_3.setViewportView(tableUploads);

		JPanel panel_9 = new JPanel();
		GridBagConstraints gbc_panel_9 = new GridBagConstraints();
		gbc_panel_9.weighty = 0.05;
		gbc_panel_9.fill = GridBagConstraints.BOTH;
		gbc_panel_9.gridx = 0;
		gbc_panel_9.gridy = 2;
		panel_2.add(panel_9, gbc_panel_9);
		GridBagLayout gbl_panel_9 = new GridBagLayout();
		gbl_panel_9.columnWidths = new int[]{0, 0};
		gbl_panel_9.rowHeights = new int[]{0, 0};
		gbl_panel_9.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_9.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_9.setLayout(gbl_panel_9);

		JButton btnNewButton = new JButton("logout");
		btnNewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("logout");

				HttpConnection hcon = HttpConnection.getInstance();
				String ans = hcon.makeHttpRequest("/logout");
				if(ans.equals("success")) {
					System.out.println("logout succeeded");
					MainScreen.this.setVisible(false);
					new LoginRegister();
				}else {
					System.out.println("logout failed");
					MainScreen.this.setVisible(false);
					new LoginRegister();
				}



			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		panel_9.add(btnNewButton, gbc_btnNewButton);
		//added so when user changes files it will be live on this app
		this.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				load(null);

			}
		});

		this.setVisible(true);

	}

	/**
	 * This function gets a file and convert the data it represents to a tree data that the
	 * local jtree can accept
	 * 
	 * @param node the file for which the jtree is getting its file tree data 
	 * @return the root of the tree to which the jtree is filled with data
	 */
	private DefaultMutableTreeNode scan(File node){
		DefaultMutableTreeNode ret;
		if(node.isDirectory()) {
			ret = new DefaultMutableTreeNode(new NodeFile(node.getName(), node.length(),node.getPath(),"directory",null));
		}else {
			ret = new DefaultMutableTreeNode(new NodeFile(node.getName(), node.length(),node.getPath(),"file",null));
		}
		if(node.isDirectory())
			for (File child: node.listFiles())
				ret.add(scan(child));
		return ret;
	}
	/**
	 * This a helper function for loading a file tree data into a jtree
	 * 
	 * @param dir the directory to be loaded in the jtree
	 */
	public void load(File dir) {
		DefaultMutableTreeNode top; 
		if(dir != null) {
			this.dirLocal = dir;
			top = scan(dir);
		}else {
			top = scan(this.dirLocal);
		}


		MainScreen.this.jtreeLocal = new JTree(top);
		MainScreen.this.jtreeLocal.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		/*
		//Listen for when the selection changes.
		MainScreen.this.jtreeLocal.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						MainScreen.this.jtreeLocal.getLastSelectedPathComponent();

				if (node == null) return;

				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					//BookInfo book = (BookInfo)nodeInfo;
					//displayURL(book.bookURL);
					if (DEBUG) {
						System.out.print("node is a leaf");
					}
				} else {
					System.out.println("node is not leaf"); 
				}
				if (DEBUG) {
					System.out.println(nodeInfo.toString());
					System.out.println(((NodeFile)nodeInfo).path);
				}

			}
		});
		*/
		MainScreen.this.jtreeLocal.setDragEnabled(true);

		MainScreen.this.modeljtreeLocal = (DefaultTreeModel)jtreeLocal.getModel();
		MainScreen.this.jtreeLocal.setTransferHandler(new TreeTransferHandlerLocal());
		MainScreen.this.scrollPaneLocal.setViewportView(MainScreen.this.jtreeLocal); 
		
	}
	/**
	 * This function gets a json tree and convert the data it represents to a tree data that the
	 * remote jtree can accept
	 *  
	 * @param tree the json tree which its data needs to be in the jtree
	 * @param isRoot true iff the tree object is the root of the tree - in the function's 1st call this parameter is true
	 * @return the root of the tree to which the jtree is filled with data
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	private DefaultMutableTreeNode scanRemote(JsonObject tree ,boolean isRoot) throws NumberFormatException, Exception{
		String name;
		if(isRoot) {
			name = tree.get("name").getAsString();
		}else {
			try {
				//filenames needs Decryption in case not root
				name = EncDecHashHMac.getInstance().decryptString(tree.get("name").getAsString());

			}catch (Exception e) {
				return null;
			}
		}
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode(
				
				new NodeFile(				
						name, 
						Long.parseLong(tree.get("size").getAsString()),
						tree.get("path").getAsString(),
						tree.get("type").getAsString(),
						tree.get("name").getAsString()
						));

		if(tree.get("type").getAsString().equals("directory"))
			for (JsonElement child: tree.getAsJsonArray("children")) {
				String nameChild;
				try {
					nameChild = EncDecHashHMac.getInstance().decryptString(child.getAsJsonObject().get("name").getAsString());
					
				}catch (Exception e) {
					return null;
				}
				boolean isChildNotNeedToShow = isRoot &&(nameChild.equals("metafile") || nameChild.equals("tagmetafile"));
				if(!isChildNotNeedToShow) {
					ret.add(scanRemote(child.getAsJsonObject(),false));
				}
			}
		return ret;
	}
	/**
	 * This a helper function for loading the json tree data from server's viewAvailableFiles into a jtree
	 */
	public void loadRemote() {

		HttpConnection hcon = HttpConnection.getInstance();
		String fileTreeString = hcon.makeHttpRequest("/viewAvailableFiles");
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(fileTreeString).getAsJsonObject();

		JsonElement response = o.get("response");
		System.out.println("response is : "+response.getAsString());
		if("success".equals(response.getAsString())) {
			System.out.println("view files request succeeded");
			JsonObject otree = o.getAsJsonObject("data");
			//Dec : (inside the call)
			try {
				DefaultMutableTreeNode top = scanRemote(otree,true);
				MainScreen.this.jtreeRemote = new JTree(top);
			}catch(Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				return;
			}

			this.jtreeRemote.setDragEnabled(true);
			jtreeRemote.setDropMode(DropMode.ON_OR_INSERT);

			this.modeljtreeRemote = (DefaultTreeModel)jtreeRemote.getModel();
			jtreeRemote.setTransferHandler(new TreeTransferHandlerRemote());

			MainScreen.this.jtreeRemote.getSelectionModel().setSelectionMode
			(TreeSelectionModel.SINGLE_TREE_SELECTION);
			

			MainScreen.this.jtreeRemote.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				
				//right click menu for actions such as delete file or directory , create directory , rename
				@Override
				public void mouseClicked(MouseEvent e) {

					if (SwingUtilities.isRightMouseButton(e)) {

						int row = MainScreen.this.jtreeRemote.getClosestRowForLocation(e.getX(), e.getY());
						MainScreen.this.jtreeRemote.setSelectionRow(row);

						TreePath path = MainScreen.this.jtreeRemote.getPathForLocation(e.getX(), e.getY());
						if (path == null)
							return; 
						JTree tree = (JTree)e.getSource();
						tree.setSelectionPath(path);

						DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

						if (node == null)
							//Nothing is selected.  
							return;

						Object nodeInfo = node.getUserObject();
						NodeFile nodefile = (NodeFile) nodeInfo;

						String labelCreate = "create dir";
						String labelDeleteDir = "delete dir";
						String labelDeleteFile = "delete file";
						String labelRename = "rename";
						JMenuItem mItemCreate = new JMenuItem(labelCreate);
						JMenuItem mItemDeleteDir = new JMenuItem(labelDeleteDir);
						JMenuItem mItemDeleteFile = new JMenuItem(labelDeleteFile);
						JMenuItem mItemRename = new JMenuItem(labelRename);

						mItemDeleteFile.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								//before delete doing some validation
								String sub = nodefile.path.substring(nodefile.path.indexOf("/")+1);
								if((sub.indexOf("/")==-1) && 
										(nodefile.filename.equals("metafile") || nodefile.filename.equals("tagmetafile")) ) {

									//show message dialog box saying there are no permissions to change the metafile
									JOptionPane.showMessageDialog(null, "there is no permission to change the metafile or tagmetafile");
									System.out.println("cannot create meta or metatag directory");
									return;
								}
								HttpConnection hcon = HttpConnection.getInstance();
								String pathReq;
								if(nodefile.path.equals("./")){
									System.out.println("no permission to delete root");
									return;
								}else if(nodefile.path.indexOf("./") != -1) {
									pathReq = nodefile.path.substring(nodefile.path.indexOf("/"));
								}else {
									pathReq = "/"+nodefile.path;
								}
								
								//actual delete
								String ansDelete = hcon.makeHttpRequest("/deleteFile", 
										"filePath", 
										pathReq);
								if(ansDelete.equals("failed")) {
									//show message dialog box saying delete file failed
									JOptionPane.showConfirmDialog(null, 
											"Delete operation of file:"+nodefile.filename+" Failed ", "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
									System.out.println("mItemDeleteFile - delete file failed");
									return;
								}
								
								String pathParam;
								if(nodefile.path.indexOf("./") != -1){//case of root already checked above
									pathParam = nodefile.path.substring(nodefile.path.indexOf("/"));
								}else {
									pathParam = "/"+nodefile.path;
								}
								
								pathParam = pathParam.substring(0, pathParam.lastIndexOf("/"));
								pathParam = pathParam +"/";
								//after delete we need to update the metadata
								String ansUpdate = hcon.updateMetaFile(pathParam, 
										null,
										nodefile.encryptedName,
										null,
										null,
										"delete file",
										MainScreen.this.sem);
								if(ansUpdate.equals("success")) {
									System.out.println("update delete success");
									loadRemote();
								}else {
									//error occured and update metafile failed
									throw new IllegalStateException("update delete failed");

								}
							}
						});


						mItemCreate.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {

								String dirName = JOptionPane.showInputDialog("Enter a new directory name");
								//doing some validation
								if(("".equals(dirName)) || (dirName==null)) {
									return;
								}
								String path;

								if((nodefile.path.equals("./")) && (dirName.equals("metafile") || dirName.equals("tagmetafile")) ) {
									//show message dialog box saying there are no permissions to change the metafile
									JOptionPane.showMessageDialog(null, "there is no permission to change the metafile or tagmetafile");
									System.out.println("cannot create meta or metatag directory");
									return;
								}
								//Encrypt directory name
								String encryptedDirName; 
								try {
									
									encryptedDirName = EncDecHashHMac.getInstance().encryptString(dirName);
									
								}catch(Exception ex) {
									ex.printStackTrace();
									return;
								}
								//use saved Enc
								if(!nodefile.isDir()) { //in case user clicked inside a directory on a file
									
									//checking if the given directory name already exist as a file or direcory name
									if(nodefile.path.indexOf("./")!=-1) {
										path = nodefile.path.substring(nodefile.path.indexOf("/"), nodefile.path.lastIndexOf("/")+1);
									}else {
										path = "/"+nodefile.path.substring(0, nodefile.path.lastIndexOf("/")+1);
									}
									DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

									for(int i = 0; i < parent.getChildCount();i++) {
										DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) parent.getChildAt(i); 
										NodeFile nf = (NodeFile)dmt.getUserObject();
										if(nf.filename.equals(dirName)) {

											JOptionPane.showMessageDialog(null, "Name already exist - Can not create directory");
											return;
										}
									}
								//user clicked on a directory node
								}else if( !(nodefile.path.equals("./")) ){//it is not root
									
									//checking if the given directory name already exist as a file or direcory name
									if(nodefile.path.indexOf("./")!=-1) {
										path = nodefile.path.substring(nodefile.path.indexOf("/"))+"/";
									}else {
										path = "/"+nodefile.path+"/";
									}
									for(int i = 0; i < node.getChildCount();i++) {
										DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) node.getChildAt(i); 
										NodeFile nf = (NodeFile)dmt.getUserObject();
										if(nf.filename.equals(dirName)) {

											JOptionPane.showMessageDialog(null, "Name already exist - Can not create directory");
											return;
										}
									}


								}else {
									//checking if the given directory name already exist as a file or direcory name
									path = "/";
									for(int i = 0; i < node.getChildCount();i++) {
										DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) node.getChildAt(i); 
										NodeFile nf = (NodeFile)dmt.getUserObject();
										if(nf.filename.equals(dirName)) {

											JOptionPane.showMessageDialog(null, "Name already exist - Can not create directory");
											return;
										}
									}
								}
								path = path+encryptedDirName;
								HttpConnection hcon = HttpConnection.getInstance();
								//actual creation of the directory on the server
								String ansCreate = hcon.makeHttpRequest("/createDir", "dirPath", path);
								if(ansCreate.equals("failed")) {
									System.out.println("creating dir failed");
									//show Error to user
									JOptionPane.showConfirmDialog(null, 
											"Create directory operation of:"+dirName+" Failed ", "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
									return;
								}
								// updating the metadata after the change
								String ansUpdate = hcon.updateMetaFile(path, 
										null, 
										null, 
										null,
										encryptedDirName, 
										"create dir",
										MainScreen.this.sem);
								if("success".equals(ansUpdate)) {
									System.out.println("create dir succeeded");
									loadRemote();
								}else {
									System.out.println("create dir failed - update failed");
								}
							}
						});


						mItemDeleteDir.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {

								if(node.getChildCount() > 0) {
									//message dialog box - cannot delete non empty directory
									JOptionPane.showMessageDialog(null, "cannot delete non empty directory");
									System.out.println("directory not empty - can not delete");
									return;
								}
								HttpConnection hcon = HttpConnection.getInstance();
								
								String pathReq;
								if(nodefile.path.equals("./")){
									System.out.println("no permission to delete root");
									JOptionPane.showMessageDialog(null, "no permission to delete server's root directory");
									return;
								}else if(nodefile.path.indexOf("./") != -1) {
									pathReq = nodefile.path.substring(nodefile.path.indexOf("/"));
								}else {
									pathReq = "/"+nodefile.path;
								}
								
								//actual delete
								String ansDelete = hcon.makeHttpRequest("/deleteDir", 
										"dirPath", 
										pathReq);
								if(ansDelete.equals("failed")) {
									throw new IllegalStateException("delete dir failed - server delete failed");
								}
								//use saved Enc
								//String path = nodefile.path.substring(nodefile.path.indexOf("/"), nodefile.path.lastIndexOf("/")+1);
								String pathParam;
								if(nodefile.path.indexOf("./") != -1){//case of root already checked above
									pathParam = nodefile.path.substring(nodefile.path.indexOf("/"), nodefile.path.lastIndexOf("/")+1);
								}else {
									pathParam = "/"+nodefile.path;
									pathParam =  pathParam.substring(pathParam.indexOf("/"), pathParam.lastIndexOf("/")+1);
								}
								//updating the metadata after the delete 
								String ansUpdate = hcon.updateMetaFile(
										pathParam, 
										null, 
										null,
										null ,
										nodefile.encryptedName, 
										"delete dir"
										,MainScreen.this.sem);
								if(ansUpdate.equals("failed")) {
									throw new IllegalStateException("delete dir - update meta file failed");
								}
								System.out.println("delete dir succeeded");
								loadRemote();
							}
						});

						mItemRename.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								//validation
								if(nodefile.path.equals("./")) {
									//show message dialog box saying there is no permission from server to change root directory
									JOptionPane.showMessageDialog(null, "there is no permission from server to change root directory");
									return;
								}
								String name = JOptionPane.showInputDialog("Enter a name for rename");
								if( ((name==null) || "".equals(name))) {
									return;
								}
								String sub;
								if(nodefile.path.indexOf("./") != -1) {
									sub = nodefile.path.substring(nodefile.path.indexOf("/")+1);
								}else {
									sub = nodefile.path;
								}
								
								
								if((sub.indexOf("/")==-1) && (name.equals("metafile") || name.equals("tagmetafile")) ) {
									//show message dialog box saying there are no permissions to change the metafile
									JOptionPane.showMessageDialog(null, "there is no permission to change the metafile or tagmetafile");
									System.out.println("cannot rename to meta or metatag");
									return;
								}
								DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

								for(int i = 0; i < parent.getChildCount();i++) {
									DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) parent.getChildAt(i); 
									NodeFile nf = (NodeFile)dmt.getUserObject();
									if(nf.filename.equals(name)) {

										JOptionPane.showMessageDialog(null, "Name already exist - Can not Rename");
										return;
									}
								}
								String newNameEncrypted;
								try {
									newNameEncrypted = EncDecHashHMac.getInstance().encryptString(name);
								}catch(Exception ex){
									ex.printStackTrace();
									return;
								}

								//use saved Enc
								String path;
								if(nodefile.path.indexOf("./") != -1) {
									path = nodefile.path.substring(nodefile.path.indexOf("/"),nodefile.path.lastIndexOf("/")+1);
								}else {
									path = "/"+nodefile.path;
									path = path.substring(path.indexOf("/"),path.lastIndexOf("/")+1);
								}
								
								HttpConnection hcon = HttpConnection.getInstance();
								//actual renaming
								String ansRename = hcon.makeHttpRequest("/renameFile", 
										"oldFilePath", path+nodefile.encryptedName, 
										"newFilePath", path+newNameEncrypted);
								if(ansRename.equals("failed")) {
									System.out.println("rename failed at server");
									return;
								}
								// updating the meta data after the rename took place
								String ansUpdate = hcon.updateMetaFile(path.substring(0, path.lastIndexOf("/")+1), 
										null, 
										null, 
										newNameEncrypted,nodefile.encryptedName, 
										"rename",
										MainScreen.this.sem);
								if(ansUpdate.equals("success")) {
									System.out.println("rename succeeded");
									loadRemote();
								}

							}
						});
						JPopupMenu popup = new JPopupMenu();
						popup.add(mItemCreate);
						if(nodefile.isDir()) {
							popup.add(mItemDeleteDir);
						}else {
							popup.add(mItemDeleteFile);
						}
						popup.add(mItemRename);
						popup.show(tree, e.getX(), e.getY());

						//popupMenu.show(e.getComponent(), e.getX(), e.getY());

					}
				}
			});


			this.scrollPaneRemote.setViewportView(jtreeRemote);

		}else {
			throw new IllegalStateException("Error - view files on server failed");
		}

	}
	
	/**
	 * This inner class is responsible of handling the transfer
	 * of data to the wrapper jtree - jtreeRemote
	 *
	 */
	class TreeTransferHandlerRemote extends TransferHandler{
		
		DataFlavor nodeFlavor;
		DataFlavor[] flavors = new DataFlavor[1];
		public TreeTransferHandlerRemote() {
			try {
				String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" +
						javax.swing.tree.DefaultMutableTreeNode.class.getName() +
						"\"";
				nodeFlavor = new DataFlavor(mimeType);
				flavors[0] = nodeFlavor;
			} catch(ClassNotFoundException e) {
				System.out.println("ClassNotFound: " + e.getMessage());
			}
		}
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}
		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {//when a data completes its transfer to the pther jtree jtreeLocal
			if((action & COPY) == COPY) {
				System.out.println("Remote - export done started");
				JTree tree = (JTree)source;

				DefaultMutableTreeNode node;
				NodeFile nfnode;
				try {
					node = (DefaultMutableTreeNode)( data.getTransferData(nodeFlavor) );
					nfnode = (NodeFile)node.getUserObject();
				} catch (UnsupportedFlavorException | IOException e) {

					e.printStackTrace();
					return;
				}

				//getting the path to be downloaded
				String pathParam;
				if(nfnode.path.equals("./")) {
					pathParam="";
				}else if(nfnode.path.indexOf("./") != -1){
					pathParam = nfnode.path.substring(
							nfnode.path.indexOf("/"),
							nfnode.path.lastIndexOf("/"));
				}else {
					pathParam = "/"+nfnode.path;
					pathParam = pathParam.substring(
							pathParam.indexOf("/"),
							pathParam.lastIndexOf("/"));
				}
				pathParam = pathParam+"/";
				
				SwingWorkerDownload workerdownload = new SwingWorkerDownload(
						MainScreen.this.modelDownloads, 
						nfnode, 
						MainScreen.this.pathTargetLocal, 
						pathParam,
						MainScreen.this,MainScreen.this.sem);
				// the actual download should take place inside here
				// download will run on its own thread thus it can be done in parallel
				workerdownload.execute();

			}
		}
		/**
		 * This function checks if the import to the jtreeRemote is legal
		 */
		public boolean canImport(TransferHandler.TransferSupport support) {

			if (!support.isDrop()) {
				System.out.println("is not drop");
				return false;
			}
			support.setShowDropLocation(true);
			//check if the data of the transferred node is legal
			if(!support.isDataFlavorSupported(nodeFlavor)) {
				System.out.println("flavor not supported");
				return false;
			}
			DefaultMutableTreeNode node = null;
			try {
				Transferable t = support.getTransferable();
				node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);
				if(node ==null) {
					return false;
				}
				//not allow transfer of tree , only a leaf can be transferred
				if(!node.isLeaf()) {
					return false;
				}
			} catch(UnsupportedFlavorException ufe) {
				System.out.println("UnsupportedFlavor: " + ufe.getMessage());
			} catch(java.io.IOException ioe) {
				System.out.println("I/O error: " + ioe.getMessage());
			}

			DefaultTreeModel model = (DefaultTreeModel)jtreeRemote.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
			if(node.getRoot().equals(root)) return false;
			//if(isBelong(root , node)) return false;


			JTree.DropLocation dropLocation =
					(JTree.DropLocation)support.getDropLocation();
			TreePath path = dropLocation.getPath();
			if(path==null) {
				return false;
			}
			DefaultMutableTreeNode parentNode =
					(DefaultMutableTreeNode)path.getLastPathComponent();

			NodeFile nfnode = (NodeFile)parentNode.getUserObject();
			NodeFile nfToTranfer = (NodeFile)(node.getUserObject());
			//can not allow user to overwrite metafile
			// target location of the transfer should be a directory
			if((!nfnode.isDir()) || (nfToTranfer.filename.equals("metafile"))) {
				return false;
			}

			boolean ans;
			if(dropLocation.getPath() != null) {
				ans = true;
			}else {
				//System.out.println("dropLocation.getPath() != null is false");
				ans=false;
			}
			return ans;
		}
		/**
		 * This function gets the data imported into the jtreeRemote
		 */
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				System.out.println("canImport is false");
				return false;
			}else {
				System.out.println("can import is true");
			}
			JTree.DropLocation dropLocation =
					(JTree.DropLocation)support.getDropLocation();
			TreePath path = dropLocation.getPath();
			//Transferable transferable = support.getTransferable();
			//path.getLastPathComponent()

			// Extract transfer data.
			DefaultMutableTreeNode node = null;
			try {
				Transferable t = support.getTransferable();
				node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);
			} catch(UnsupportedFlavorException ufe) {
				System.out.println("UnsupportedFlavor: " + ufe.getMessage());
			} catch(java.io.IOException ioe) {
				System.out.println("I/O error: " + ioe.getMessage());
			}

			int childIndex = dropLocation.getChildIndex();
			if (childIndex == -1) {
				childIndex = MainScreen.this.modeljtreeRemote.getChildCount(path.getLastPathComponent());
			}
			
			DefaultMutableTreeNode newNode = 
					(DefaultMutableTreeNode) node.clone();
			DefaultMutableTreeNode parentNode =
					(DefaultMutableTreeNode)path.getLastPathComponent();
			
			//saving the path for later handle in export done
			NodeFile nfnode = (NodeFile)parentNode.getUserObject();
			MainScreen.this.pathTargetRemote = nfnode.path;
			System.out.println("saved pathTargetRemote : "+MainScreen.this.pathTargetRemote);
			//MainScreen.this.pathTargetRemote =
			
			NodeFile nodeToTransfer = (NodeFile)node.getUserObject();
			for(int i = 0; i < parentNode.getChildCount(); i++) {
				DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) parentNode.getChildAt(i); 
				NodeFile nf = (NodeFile)dmt.getUserObject();
				if(nf.filename.equals(nodeToTransfer.filename)) {
					int input = JOptionPane.showConfirmDialog(null, 
							"would you like to overwrite file : "+nf.filename+" ?","Select an option",
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE);
					if(input == 1) {
						return false;
					}else {
						
						HttpConnection hcon = HttpConnection.getInstance();
						String pathReq;
						if(nf.path.equals("./")){
							System.out.println("no permission to delete root");
							return false;
						}else if(nf.path.indexOf("./") != -1) {
							pathReq = nf.path.substring(nf.path.indexOf("/"));
						}else {
							pathReq = "/"+nf.path;
						}
						
						//delete
						String ansDelete = hcon.makeHttpRequest("/deleteFile", 
								"filePath", 
								pathReq);
						if(ansDelete.equals("failed")) {
							//show message dialog box saying delete file failed
							JOptionPane.showConfirmDialog(null, 
									"Overwrite operation of file:"+nf.filename+" Failed ", "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
							System.out.println("overwrite - delete file failed");
							return false;
						}
						
						String pathParam;
						if(nf.path.indexOf("./") != -1){//case of root already checked above
							pathParam = nf.path.substring(nf.path.indexOf("/"));
						}else {
							pathParam = "/"+nf.path;
						}
						
						pathParam = pathParam.substring(0, pathParam.lastIndexOf("/"));
						pathParam = pathParam +"/";
						//after delete we need to update the metadata
						String ansUpdate = hcon.updateMetaFile(pathParam, 
								null,
								nf.encryptedName,
								null,
								null,
								"delete file",
								MainScreen.this.sem);
						if(ansUpdate.equals("success")) {
							System.out.println("update for delete(overwrite) success");
							//loadRemote();
						}else {
							//error occured and update metafile failed
							throw new IllegalStateException("update delete failed");

						}
						break;
					}
				}
			}


			MainScreen.this.modeljtreeRemote.insertNodeInto(newNode, parentNode, childIndex);
			
			TreePath newPath = path.pathByAddingChild(newNode);
			jtreeRemote.makeVisible(newPath);
			jtreeRemote.scrollRectToVisible(jtreeRemote.getPathBounds(newPath));
			return true;
		}
		/**
		 * creation of the transferable 
		 */
		protected Transferable createTransferable(JComponent c) {
			JTree tree = (JTree)c;
			TreePath path = tree.getSelectionPath();
			if(path != null) {

				DefaultMutableTreeNode node =
						(DefaultMutableTreeNode)path.getLastPathComponent();
				DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node);

				return new NodesTransferable(node);
			}
			return null;
		}
		/**
		 * Wrapper class for DefaultMutableTreeNode needed in order for the tree 
		 * to have support of transferring data of type DefaultMutableTreeNode
		 *
		 */
		class NodesTransferable implements Transferable {
			DefaultMutableTreeNode node;

			public NodesTransferable(DefaultMutableTreeNode node) {
				this.node = node;
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException {
				if(!isDataFlavorSupported(flavor))
					throw new UnsupportedFlavorException(flavor);
				return node;
			}

			public DataFlavor[] getTransferDataFlavors() {
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return nodeFlavor.equals(flavor);
			}
		}
	}
	/**
	 * This inner class is responsible of handling the transfer
	 * of data to the wrapper jtree - jtreeLocal
	 *
	 */
	class TreeTransferHandlerLocal extends TransferHandler{
		DataFlavor nodeFlavor;
		DataFlavor[] flavors = new DataFlavor[1];
		public TreeTransferHandlerLocal() {
			try {
				String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
						";class=\"" +
						javax.swing.tree.DefaultMutableTreeNode.class.getName() +
						"\"";
				nodeFlavor = new DataFlavor(mimeType);
				flavors[0] = nodeFlavor;
			} catch(ClassNotFoundException e) {
				System.out.println("ClassNotFound: " + e.getMessage());
			}
		}
		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}
		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {//when a data completes its transfer to the pther jtree-jtreeRemote
			if((action & COPY) == COPY) {
				System.out.println("Local - export done started");

				//JTree tree = (JTree)source;

				DefaultMutableTreeNode node;
				NodeFile nfnode;
				try {
					node = (DefaultMutableTreeNode)( data.getTransferData(nodeFlavor) );
					nfnode = (NodeFile)node.getUserObject();
				} catch (UnsupportedFlavorException | IOException e) {

					e.printStackTrace();
					return;
				}
				//getting the path for the upload
				String pathParam;
				if(MainScreen.this.pathTargetRemote.equals("./")) {//root path equals "./"
					pathParam="";
				}else if(MainScreen.this.pathTargetRemote.indexOf("./")!=-1){
					pathParam = MainScreen.this.pathTargetRemote.substring(2);
				}else {
					pathParam = MainScreen.this.pathTargetRemote;
				}
				pathParam = pathParam+"/";
				
				SwingWorkerUpload workerupload = new SwingWorkerUpload(
						MainScreen.this.modelUploads, 
						nfnode, 
						//pathTargetLocal, 
						pathParam,
						MainScreen.this,MainScreen.this.sem);
				//actual upload takes place inside here:
				//upload will run on its own thread thus it can be done in parallel
				workerupload.execute();


			}
		}
		/**
		 * This function checks if the import to the jtreeLocal is legal
		 */
		public boolean canImport(TransferHandler.TransferSupport support) {
			try {
				if(support==null) {
					return false; 
				}

				if (!support.isDrop()) {
					System.out.println("is not drop");
					return false;
				}
				support.setShowDropLocation(true);
				//check if the data for the transferred node is legal(supported by the tree)
				if(!support.isDataFlavorSupported(nodeFlavor)) {
					System.out.println("flavor not supported");
					return false;
				}

				DefaultMutableTreeNode node = null;
				try {
					Transferable t = support.getTransferable();
					node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);
					if(node == null) {
						return false;
					}
					//not allow a tree to be transferred , only a leaf
					if(!node.isLeaf()) {
						return false;
					}
					
					NodeFile nfnode = (NodeFile)node.getUserObject();
					if(nfnode.isDir()) {
						return false;
					}
					
				} catch(UnsupportedFlavorException ufe) {
					System.out.println("UnsupportedFlavor: " + ufe.getMessage());
				} catch(java.io.IOException ioe) {
					System.out.println("I/O error: " + ioe.getMessage());
				}


				DefaultTreeModel model = (DefaultTreeModel)jtreeLocal.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
				if(node.getRoot()==null || root==null) {
					return false;
				}
				if(node.getRoot().equals(root)) return false;
				//if(isBelong(root , node)) return false;

				JTree.DropLocation dropLocation =
						(JTree.DropLocation)support.getDropLocation();
				if(dropLocation ==null) {
					return false;
				}
				TreePath path = dropLocation.getPath();
				if(path==null) {
					return false;
				}
				DefaultMutableTreeNode parentNode =
						(DefaultMutableTreeNode)path.getLastPathComponent();
				if(parentNode != null) {
					NodeFile nfnode = (NodeFile)parentNode.getUserObject();
					//location of the target transfer should be a directory
					if(!nfnode.isDir()) {
						return false;
					}
				}
				boolean ans;
				if(dropLocation.getPath() != null) {
					ans = true;
				}else {
					System.out.println("dropLocation.getPath() != null is false");
					ans=false;
				}
				return ans;
			}catch(Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.out.println("Exception caught at canImport() local");
				return false;
			}
		}
		/**
		 * This function gets the data imported into the jtreeLocal
		 */
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				System.out.println("canImport is false");
				return false;
			}else {
				System.out.println("can import is true");
			}
			JTree.DropLocation dropLocation =
					(JTree.DropLocation)support.getDropLocation();
			TreePath path = dropLocation.getPath();
			

			// Extract transfer data.
			DefaultMutableTreeNode node = null;
			try {
				Transferable t = support.getTransferable();
				node = (DefaultMutableTreeNode)t.getTransferData(nodeFlavor);
			} catch(UnsupportedFlavorException ufe) {
				System.out.println("UnsupportedFlavor: " + ufe.getMessage());
			} catch(java.io.IOException ioe) {
				System.out.println("I/O error: " + ioe.getMessage());
			}

			int childIndex = dropLocation.getChildIndex();
			if (childIndex == -1) {
				childIndex = MainScreen.this.modeljtreeLocal.getChildCount(path.getLastPathComponent());
			}

			DefaultMutableTreeNode newNode = 
					(DefaultMutableTreeNode) node.clone();
			DefaultMutableTreeNode parentNode =
					(DefaultMutableTreeNode)path.getLastPathComponent();

			NodeFile nfnode = (NodeFile)parentNode.getUserObject();
			
			MainScreen.this.pathTargetLocal = nfnode.path;
			//checking if the target already have this node name - if it does asking if user wants to overwrite
			NodeFile nodeToTransfer = (NodeFile)node.getUserObject();
			for(int i = 0; i < parentNode.getChildCount(); i++) {
				DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) parentNode.getChildAt(i); 
				NodeFile nf = (NodeFile)dmt.getUserObject();
				if(nf.filename.equals(nodeToTransfer.filename)) {
					int input = JOptionPane.showConfirmDialog(null, 
							"would you like to overwrite file : "+nf.filename+" ?","Select an option",
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE);
					if(input == 1) {
						return false;
					}else {
						break;
					}
				}
			}


			MainScreen.this.modeljtreeLocal.insertNodeInto(newNode, parentNode, childIndex);
			TreePath newPath = path.pathByAddingChild(newNode);
			jtreeLocal.makeVisible(newPath);
			jtreeLocal.scrollRectToVisible(jtreeLocal.getPathBounds(newPath));
			return true;
		}
		/**
		 * creation of the transferable 
		 */
		protected Transferable createTransferable(JComponent c) {
			JTree tree = (JTree)c;
			TreePath path = tree.getSelectionPath();
			if(path != null) {

				DefaultMutableTreeNode node =
						(DefaultMutableTreeNode)path.getLastPathComponent();
				DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node);

				return new NodesTransferable(node);
			}
			return null;
		}
		/**
		 * Wrapper class for DefaultMutableTreeNode needed in order for the tree 
		 * to have support of transferring data of type DefaultMutableTreeNode
		 *
		 */
		class NodesTransferable implements Transferable {
			DefaultMutableTreeNode node;

			public NodesTransferable(DefaultMutableTreeNode node) {
				this.node = node;
			}

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException {
				if(!isDataFlavorSupported(flavor))
					throw new UnsupportedFlavorException(flavor);
				return node;
			}

			public DataFlavor[] getTransferDataFlavors() {
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return nodeFlavor.equals(flavor);
			}
		}
	}
	// class for the uploads downloads tables part
	/**
	 * Progress bar class used inside of the uploads/downloads table
	 * inheritance needed because a component inside a table cell needs rendering
	 *
	 */
	class ProgressCellRender extends JProgressBar implements TableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			int progress = 0;
			if (value instanceof Float) {
				progress = Math.round(((Float) value) * 100f);
			} else if (value instanceof Integer) {
				progress = (int) value;
			}
			setValue(progress);
			return this;
		}
	}

}


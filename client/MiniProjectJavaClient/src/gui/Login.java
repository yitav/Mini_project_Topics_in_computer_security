package gui;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import connection.HttpConnection;
import crypto.EncDecHashHMac;

import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileReader;
import java.io.InputStreamReader;

import javax.crypto.Cipher;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
/**
 * This class represents the gui's Login screen
 * the user should enter here his name and password and on "success" he will
 * get transferred to the MainScreen class screen
 * the above "success" mentioned is received if after checking the metadata received from the server 
 * there was no unauthorized content modification detected such as file deleted or renamed  , or file size changed
 * 
 */
public class Login extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Register frame = new Register();
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
	public Login() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 452, 266);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[] {0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0};
		contentPane.setLayout(gbl_contentPane);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.anchor = GridBagConstraints.SOUTH;
		gbc_panel_1.weighty = 0.25;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		contentPane.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("fill in details:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.weightx = 0.7;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		JPanel panel_5 = new JPanel();
		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
		gbc_panel_5.weightx = 0.15;
		gbc_panel_5.insets = new Insets(0, 0, 5, 5);
		gbc_panel_5.fill = GridBagConstraints.BOTH;
		gbc_panel_5.gridx = 0;
		gbc_panel_5.gridy = 0;
		panel_1.add(panel_5, gbc_panel_5);
		
		JPanel panel_6 = new JPanel();
		GridBagConstraints gbc_panel_6 = new GridBagConstraints();
		gbc_panel_6.weightx = 0.15;
		gbc_panel_6.insets = new Insets(0, 0, 5, 0);
		gbc_panel_6.fill = GridBagConstraints.BOTH;
		gbc_panel_6.gridx = 2;
		gbc_panel_6.gridy = 0;
		panel_1.add(panel_6, gbc_panel_6);
		
		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weighty = 0.25;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		contentPane.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		JLabel lblNewLabel_1 = new JLabel("name:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel_2.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 30);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel_2.add(textField, gbc_textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("password:");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(5, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_2.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.insets = new Insets(0, 0, 0, 30);
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 1;
		panel_2.add(passwordField, gbc_passwordField);
		
		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.weighty = 0.12;
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 2;
		contentPane.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JButton btnNewButton_1 = new JButton("cancel");
		btnNewButton_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("cancel login");
				
				Login.this.setVisible(false);
				new LoginRegister();
			}
		});
		
		
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 0;
		panel_3.add(btnNewButton_1, gbc_btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("    log    ");
		btnNewButton_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("login by user : "+textField.getText()+" | password : "+passwordField.getText());
				String password = passwordField.getText();
				String username = textField.getText();

				try {
					
					String encoded = EncDecHashHMac.getInstance(password).hashSha256(password+"3");
					if (encoded == null) { 
						throw new IllegalStateException("Error - Login failed because hash function failed"); 
					}
					HttpConnection hcon = HttpConnection.getInstance();
					String ans = hcon.makeHttpRequest("/login",
							"username", username, 
							"password", encoded);
					if(ans.equals("success")) {
						System.out.println("login succeeded");
						//get file tree from server viewAvailableFiles
						//get meta file and verify against actual view files from server
						
						String fileTreeString = hcon.makeHttpRequest("/viewAvailableFiles");
						JsonParser parser = new JsonParser();
						JsonObject o = parser.parse(fileTreeString).getAsJsonObject();
						
						JsonObject dataViewFiles = o.get("data").getAsJsonObject();
						
						JsonElement response = o.get("response");
						System.out.println("response is : "+response.getAsString());
						//problem - we need the metafile but we do not know its filename's encryption on the client side
						//          because the client does not save anything so we do not know what name to request
						//          for doing download of the metafile
						//solution - go over the file names from server and decrypt each one and only then compare
						//           to the metafile name , if there is a match metafile is in reach
						if("success".equals(response.getAsString())) {
							JsonObject otree = o.getAsJsonObject("data");
							JsonArray children = otree.getAsJsonArray("children");
							JsonElement je;
							JsonElement jename=null;
							JsonObject jo=null;
							for(int i=0; i < children.size();i++) {
								 je =  children.get(i);
								 jo = je.getAsJsonObject();
								 jename = jo.get("name");
								//Decrypt and compare
								 try {
									 if(/**jename.getAsString().equals("metafile")**/
									EncDecHashHMac.getInstance().decryptString(jename.getAsString()).equals("metafile")) {
										
											break;
									}
								 }catch(Exception ex) {
									 continue;
								 }
							}
							if(jename == null) {
								//error server not safe metafile deleted or renamed
								JOptionPane.showConfirmDialog(null, 
						                "Error - server NOT safe! metafile deleted or renamed",
						                "Error",
						                JOptionPane.DEFAULT_OPTION , 
						                JOptionPane.ERROR_MESSAGE);
								System.out.println("Error - metafile not found");
								return;
							}
							JsonObject joMeta;
							//get tag for metafile
							JsonObject joTagMeta;
							
							//same problem and solution as mentioned above for the metadata for the file tag of the metafile
							JsonElement jeTag;
							JsonElement jenameTag=null;
							JsonObject joTag=null;
							for(int i=0; i < children.size();i++) {
								 jeTag =  children.get(i);
								 joTag = jeTag.getAsJsonObject();
								 jenameTag = joTag.get("name");
								//Encrypt
								 try {
									if(EncDecHashHMac.getInstance().decryptString(jenameTag.getAsString()).equals("tagmetafile")) {
										break;
									}
								}catch(Exception ex) {
									 continue;
								 }
							}
							if(jenameTag == null) {
								//error server not safe tag-metafile deleted
								JOptionPane.showConfirmDialog(null, 
						                "Error - server NOT safe! tag of metafile deleted or renamed",
						                "Error",
						                JOptionPane.DEFAULT_OPTION , 
						                JOptionPane.ERROR_MESSAGE);
								System.out.println("Error - tagmetafile not found");
								return;
							}
							
							
							if((jo!=null) && (joTag!=null)) {	
								joMeta = jo.getAsJsonObject();
								joTagMeta = joTag.getAsJsonObject();
								
							}else {
								System.out.println("could not get meta file data from server's view files");
								throw new IllegalStateException("Error - login failed because not succeeding in getting metafile data from view files");
								//login failed because not succeeding in getting metafile data from view files 
								//return;
							}
							
							//Enc
							String ansForMeta = hcon.makeHttpRequest("/download", "filename", jename.getAsString());
							
							String ansForMetaTag = hcon.makeHttpRequest("/download", "filename", jenameTag.getAsString());
							
							if(ansForMeta.equals("success") && ansForMetaTag.equals("success")){// if there is a metafile and a tag-metafile
								//authenticate meta file decrypt it 
								//compare existing meta file against file tree from server's view files
								//if it does not match show message dialog box and return
								String ansDownload = hcon.makeDownload(jename.getAsString(),
										jename.getAsString() ,
										joMeta.get("size").getAsLong(),
										null,
										null);
								
								String ansDownloadTag = hcon.makeDownload(jenameTag.getAsString(),
										"tagmetafile" ,
										joTagMeta.get("size").getAsLong(),
										null,
										null);
								
								if(ansDownload.equals("error") || ansDownloadTag.equals("error")) {
									//error sizes of metafile and size data from server's view files do not match
									throw new IllegalStateException("Error - actual download of meta data failed");
									//return;
								}
								if(ansDownload.equals("success") && ansDownloadTag.equals("success")) {
									
									File fileTagMeta = new File("tagmetafile");
									FileReader fr = new FileReader(fileTagMeta);
									BufferedReader bufR = new BufferedReader(fr);
									String tagMetafile = bufR.readLine();
									bufR.close();
									
									String computedMetafileTag = EncDecHashHMac.getInstance().createHMac(jename.getAsString());
									if(!tagMetafile.equals(computedMetafileTag)) {
										//show error message server is not safe authentication failed 
										JOptionPane.showConfirmDialog(null, 
								                "Error - server NOT safe! metafile was changed , authentication of metafile failed",
								                "Error",
								                JOptionPane.DEFAULT_OPTION , 
								                JOptionPane.ERROR_MESSAGE);
										System.out.println("Fail - Authentication of metafile failed");
										return;
									}
									EncDecHashHMac decr = EncDecHashHMac.getInstance();
									decr.copy(Cipher.DECRYPT_MODE, jename.getAsString(), "metafile");
									
									/**
									File file = new File(jename.getAsString());
									**/
									
									File file = new File("metafile");
									
									FileInputStream in = new FileInputStream(file);
									BufferedReader buf = new BufferedReader( new InputStreamReader(in) );
									String jsonStringTreeMeta = buf.readLine();
									buf.close();
									JsonParser jsonParser = new JsonParser();
								    JsonObject jsonTreeMetaObject = jsonParser.parse(jsonStringTreeMeta).getAsJsonObject();
									System.out.println("jsonTreeMetaObject :");
									System.out.println(jsonTreeMetaObject.toString());
								    System.out.println("dataViewFiles : ");
								    System.out.println(dataViewFiles.toString());
								    
								    file.delete();
								    new File(jename.getAsString()).delete();
								    
								    fileTagMeta.delete();
								    
								    //recursion function to compare the view files result vs the above meta tree 
								    //taking advantage of the tree structure of both json trees 
								    boolean ansCompare = compareTrees(jsonTreeMetaObject , dataViewFiles,true); 
								    
								    if(ansCompare) {
								    	Login.this.setVisible(false);
								    	System.out.println("login screen success move to next screen");
								    	new MainScreen(username);
										//show message dialog box login succeeded
								    	JOptionPane.showMessageDialog(null, "Login succeeded");
								    }else {
								    	System.out.println("compare trees result files do not match");
								    	return;
								    }
								}else {
									throw new IllegalStateException("Error occured during download");
								}
								
							}else {//error there is no metafile  
								//show message dialog box telling server is not safe
								//throw new IllegalStateException("server is not safe");
								JOptionPane.showConfirmDialog(null, 
						                "Error - server NOT safe! there is no metafile or metafile tag", "Error",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
								return;
							}
							
						}else {
			
							throw new IllegalStateException("view available files failed");
						}
										
					}else {
						throw new IllegalStateException("login on server failed");
					}
					
				}catch(Exception ex) {
					System.out.println(ex.getMessage());
					System.out.println(ex.getStackTrace());
					ex.printStackTrace();
					//show message dialog box exception occured
					JOptionPane.showConfirmDialog(null, 
			                "Login Failed - "+ex.getMessage(), "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
				}
				
				
				
				
			}
		});
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.gridx = 2;
		gbc_btnNewButton_2.gridy = 0;
		panel_3.add(btnNewButton_2, gbc_btnNewButton_2);
		
		JPanel panel_7 = new JPanel();
		GridBagConstraints gbc_panel_7 = new GridBagConstraints();
		gbc_panel_7.weightx = 0.5;
		gbc_panel_7.insets = new Insets(0, 0, 0, 5);
		gbc_panel_7.fill = GridBagConstraints.BOTH;
		gbc_panel_7.gridx = 1;
		gbc_panel_7.gridy = 0;
		panel_3.add(panel_7, gbc_panel_7);
		
		this.setTitle("Login");
		this.setVisible(true);
	}
	private boolean compareTrees(JsonObject tree1/*meta object*/ , JsonObject tree2/*what's actually on server*/,boolean isTop) throws HeadlessException, Exception {
		
		
		String name1 = tree1.get("name").getAsString();
		String name2 = tree2.get("name").getAsString();
		
		String type1 = tree1.get("type").getAsString();
		String type2 = tree2.get("type").getAsString();
		
		if(type1.equals(type2) && type1.equals("directory")) {
			if(name1.equals(name2)) {
				JsonArray jarr1 = tree1.get("children").getAsJsonArray();
				JsonArray jarr2 = tree2.get("children").getAsJsonArray();
				
					
					for(JsonElement je1 : jarr1) {
						
						
						String nameje1 = je1.getAsJsonObject().get("name").getAsString();
						String typeje1 = je1.getAsJsonObject().get("type").getAsString();
						long sizeje1 = je1.getAsJsonObject().get("size").getAsLong();
						
						boolean isEqual = false;
						
						for(JsonElement je2 : jarr2) {
			
							String nameje2 = je2.getAsJsonObject().get("name").getAsString();
							String typeje2 = je2.getAsJsonObject().get("type").getAsString();
							long sizeje2 = je2.getAsJsonObject().get("size").getAsLong();
														
							if( nameje1.equals(nameje2) && typeje1.equals(typeje2) ) {
								isEqual = true;
								if(typeje1.equals("directory")) {
									boolean ansCompare = compareTrees(je1.getAsJsonObject() , je2.getAsJsonObject(),false);
									if(ansCompare==false) return false; 
								}else if(sizeje1 != sizeje2){
									//show message dialog box telling a file size of file nameje1 do not match the one on server
									JOptionPane.showConfirmDialog(null, 
							                "Error - server NOT safe! file size of file:"+
									EncDecHashHMac.getInstance().decryptString(nameje1)+
									" does not equal the one on server", "Error",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
									return false;
								}
							}
						}
						if(isEqual == false) {
							//show message dialog box telling a file nameje1 was not found on server either deleted or renamed
							JOptionPane.showConfirmDialog(null, 
					                "Error - server NOT safe! file:"+
							EncDecHashHMac.getInstance().decryptString(nameje1)
					                +" was deleted or renamed", "Error",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
					if( (!isTop)&&(jarr1.size() == jarr2.size()) || (isTop&&( jarr1.size() == (jarr2.size()-2) ))) {//-2 for metafile and tag-metafile
						
					}else {
						//show message dialog box telling quantity of files do not match
						JOptionPane.showConfirmDialog(null, 
				                "Error - server NOT safe! quantity of files do not match", "Error",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
						return false;
					}
			}
			
		}else {
			//show message dialog box telling types of file local and remote do not match
			JOptionPane.showConfirmDialog(null, 
	                "Error - server NOT safe! types of file local and remote do not match", "Error",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		
		return true;
	}
}

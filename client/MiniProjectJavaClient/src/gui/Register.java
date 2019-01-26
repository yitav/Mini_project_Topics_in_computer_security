package gui;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import connection.HttpConnection;
import crypto.EncDecHashHMac;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
//import java.io.FileReader;
import java.io.OutputStreamWriter;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.security.MessageDigest;
//import java.util.Base64;

import javax.crypto.Cipher;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
/**
 * This class represents the gui's Register screen
 * the user should enter here his name and password and on success he will
 * get transferred to the Login class screen
 *
 */
public class Register extends JFrame {

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
	public Register() {
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
				System.out.println("cancel register");
				
				Register.this.setVisible(false);
				new LoginRegister();
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 0;
		panel_3.add(btnNewButton_1, gbc_btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton("register");
		btnNewButton_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("register after fill in details - username : "+textField.getText() +" | password : "+passwordField.getText());
				
				String password = passwordField.getText();
				String username = textField.getText();
				try {
					//initialization of EncDecHashHMac singleton occurs
					//deriving the 3rd secret from password for server's authentication
					String encoded = EncDecHashHMac.getInstance(password).hashSha256(password+"3");
					if (encoded == null) { 
						throw new IllegalStateException("Error - hash of password for server requests failed");
					}
					HttpConnection hcon = HttpConnection.getInstance();
					String ans = hcon.makeHttpRequest("/register",
							"username", username, 
							"password", encoded);
					if (ans.equals("success")) {
						
						System.out.println("registration at server succeeded creating metafile and tag metafile");
						/**
						File file = new File(
								EncDecHashHMac.getInstance().encryptString("metafile")
								);
						**/
						File file = new File("metafile");
					
						file.createNewFile();
						FileOutputStream out = new FileOutputStream(file);
						BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out));
						
						JsonObject jobj = new JsonObject();
						jobj.addProperty("name", ".");
						jobj.addProperty("size", 0);
						jobj.addProperty("type", "directory");
						jobj.add("children", new JsonArray());
						
						buf.write(jobj.toString());
						
						buf.close();
						String ansLogin = hcon.makeHttpRequest("/login",
								"username", username, 
								"password", encoded);
						
						//encrypt metafile filename and encrypt metafile to temp file
						EncDecHashHMac encr = EncDecHashHMac.getInstance();
						String encryptedfilename = encr.encryptString("metafile");
						encr.copy(Cipher.ENCRYPT_MODE, "metafile", encryptedfilename);
						File filetoUpload = new File(encryptedfilename);
						
						//compute tag for encrypted temp metafile
						String tagMetafile = encr .createHMac(encryptedfilename);
						//encrypt tag-metafile-name
						String encryptedTagMetafileName = encr.encryptString("tagmetafile");
						//open file with tag-metafile-name for tag content and write tag to this file then close file
						File fileMetaTag = new File(encryptedTagMetafileName);
						fileMetaTag.createNewFile();
						FileOutputStream outTag = new FileOutputStream(fileMetaTag);
						BufferedWriter bufTag = new BufferedWriter(new OutputStreamWriter(outTag));
						
						bufTag.write(tagMetafile);
						bufTag.newLine();
						bufTag.close();
						
						
						if(ansLogin.equals("success")) {
							//upload encrypted temp metafile
							String ansUpload = hcon.makeHttpRequest("/upload", "filenamePath" , filetoUpload.getName(), "size", String.valueOf( filetoUpload.length() ) );
							
							//upload tag file for metafile
							String ansUploadTagFile = hcon.makeHttpRequest("/upload", "filenamePath" , fileMetaTag.getName(), "size", String.valueOf( fileMetaTag.length() ) );
							
							if(ansUpload.equals("success") /**&& ansUploadTagFile.equals("success")**/) {
								//Encrypted file
								hcon.makeUpload(filetoUpload,null/**,null**/);
								
								hcon.makeUpload(fileMetaTag,null);//,null
								
								//Encrypted filename
								String ansCheckUpload = hcon.makeHttpRequest( "/isUploadCompleted", "filenamePath", filetoUpload.getName() ) ;
								while( ansCheckUpload.equals("pending") ){
									Thread.sleep(500);
									//Encrypted filename
									ansCheckUpload = hcon.makeHttpRequest( "/isUploadCompleted", "filenamePath", filetoUpload.getName() ) ;
								}
								
								String ansCheckUploadTag = hcon.makeHttpRequest( "/isUploadCompleted", "filenamePath", fileMetaTag.getName() ) ;
								while( ansCheckUploadTag.equals("pending") ){
									Thread.sleep(500);
									ansCheckUploadTag = hcon.makeHttpRequest( "/isUploadCompleted", "filenamePath", fileMetaTag.getName() ) ;
								}
								
								if(ansCheckUpload.equals("success") && ansCheckUploadTag.equals("success")){
									//delete temp metafile
									file.delete();
									filetoUpload.delete();
									
									//delete temp tagfile
									fileMetaTag.delete();
									
									String ansLogout = hcon.makeHttpRequest("/logout");
									if( ansLogout.equals("success") ){
										//show message dialog registration succeeded
										JOptionPane.showMessageDialog(null, "Registration succeeded");
										//(null, "Registration succeeded", "Success", JOptionPane.DEFAULT_OPTION);
										Register.this.setVisible(false);
										new Login();
									}else {
										throw new IllegalStateException("Error - logout failed at server");
									}
									
									
								}else {
									 throw new IllegalStateException("Error - register failed because upload of meta file failed");
								}
							}else {
								 throw new IllegalStateException("Error - registration failed because login for upload meta file failed");
							}
						}else {
							throw new IllegalStateException("Error - login failed : sending meta file could not be done");
						}
						
						
					}else {
						throw new IllegalStateException("Error- registration request at server failed");
					}
				}catch(Exception ex) {
					System.out.println(ex.getMessage());
					System.out.println(ex.getStackTrace());
					//show message dialog exception occured
					JOptionPane.showConfirmDialog(null, 
			                "Registration Failed - "+ex.getMessage(), "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
					return;
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
		
		this.setTitle("Register");
		this.setVisible(true);
	}

}

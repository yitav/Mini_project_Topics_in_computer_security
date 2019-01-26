package partOfDNDController;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import connection.HttpConnection;
import crypto.EncDecHashHMac;
import gui.MainScreen;

public class SwingWorkerUpload extends SwingWorker<NodeFile, NodeFile> {
	private UpdatableTableModel model;
	private NodeFile nfnode;
	//private String pathTargetLocal;
	private String pathParam;
	
	private MainScreen mainscreen;
	private Semaphore sem;
	public SwingWorkerUpload(UpdatableTableModel model ,
			NodeFile nfnode , 
			//String pathTargetLocal,
			String pathParam, MainScreen ms, Semaphore sem){//finalPathParam,this
		this.model = model;
		this.nfnode = nfnode;
		//this.pathTargetLocal = pathTargetLocal;
		this.pathParam = pathParam;
		this.mainscreen = ms;
		this.model.addFile(nfnode);
		this.sem = sem;
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					model.updateStatus(nfnode, (int) evt.getNewValue());//MainScreen.this.modelDownloads
				}
			}
		});
	}
	public String getPathParam() {
		return pathParam;
	}
	/**
	 * This is a wrapper function for setting the progress of the progress bar status
	 * of this upload the worker is responsible for
	 * 
	 * @param progress how much progress got done
	 */
	public void setProgressPublic(int progress) {
		super.setProgress(progress);
	}
	@Override
	protected NodeFile doInBackground() throws Exception {
		try {
			System.out.println("SwingWorkerUpload | doInBackground()");
			HttpConnection hcon = HttpConnection.getInstance();
		
			if(!(nfnode.filename.equals("metafile"))) {
				
				EncDecHashHMac encr = EncDecHashHMac.getInstance();
				String encryptedfilename = encr.encryptString(nfnode.filename);
				//encrypting file before upload
				encr.copy(Cipher.ENCRYPT_MODE, nfnode.path, encryptedfilename);
				
				File encFile = new File(encryptedfilename);
				
				String ansUpload = hcon.makeHttpRequest("/upload",
						"filenamePath", pathParam+encFile.getName(),
						"size", String.valueOf(encFile.length()));
				if(ansUpload.equals("success")) {

					
					
					String ansUp = hcon.makeUpload(encFile,this);
					if(ansUp.equals("success")) {
						//when upload completes in success update meta file
						String ans = hcon.updateMetaFile(pathParam,
								encFile,
								encFile.getName(),
								null,null,
								"upload",
								sem);
						encFile.delete();
						if(ans.equals("success") ) {
							System.out.println("MainScreen - update success");
							this.mainscreen.loadRemote();
						}else  {//if(ans == null)
							throw new IllegalStateException("Error - update metafile failed");
						}

					}else {
						throw new IllegalStateException("actual upload failed");
					}
					

				}else {

					throw new IllegalStateException("upload response from server it is not permitted or filename is illegal");
				}
			}else {
				throw new IllegalStateException("uploading metafile is not permitted");
			}

			return nfnode;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, 
					"Upload Failed | file:"+nfnode.filename+" - "+e.getMessage(), "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}





}

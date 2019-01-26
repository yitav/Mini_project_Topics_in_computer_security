package partOfDNDController;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import connection.HttpConnection;
import crypto.EncDecHashHMac;
import gui.MainScreen;

public class SwingWorkerDownload extends SwingWorker<NodeFile, NodeFile> {

	private UpdatableTableModel model;
	private NodeFile nfnode;
	private String pathTargetLocal;
	private String pathParam;
	private MainScreen mainscreen;
	private Semaphore sem;
	public SwingWorkerDownload(UpdatableTableModel model ,
			NodeFile nfnode , 
			String pathTargetLocal,
			String pathParam,MainScreen ms,Semaphore sem){//MainScreen.this.pathTargetLocal,finalPathParam,this
		this.model = model;
		this.nfnode = nfnode;
		this.pathTargetLocal = pathTargetLocal;
		this.pathParam = pathParam;
		this.mainscreen = ms;
		this.model.addFile(nfnode);
		this.sem = sem;
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("progress")) {
					model.updateStatus(nfnode, (int) evt.getNewValue());//actual updating of the progress bar status
				}
			}
		});
	}
	/**
	 * This function is a wrapper function for the updating of the progress bar status
	 * of this download the worker is responsible for
	 * 
	 * @param progress how much progress got done
	 */
	public void setProgressPublic(int progress) {
		super.setProgress(progress);
	}
	@Override
	protected NodeFile doInBackground() throws Exception {
		try {
			HttpConnection hcon = HttpConnection.getInstance();
			//synchronization is required when multiple downloads can occur in parallel
			//therefore using a Semaphore
			//acquire is before changing path at server and release is 
			//after the server receives the filename path through the socket
			try {
				this.sem.acquire();
			}catch (InterruptedException e1) {
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			}
			String ansChangeDir =  hcon.makeHttpRequest("/changeDirPath", "path", pathParam);
			if(ansChangeDir.equals("success") ) {
				//use saved Enc
				String ansDownload = hcon.makeHttpRequest("/download", "filename", nfnode.encryptedName);
				if(ansDownload.equals("success")) {


					System.out.println("nfnode.size : "+nfnode.size);

					//actual download
					String ansDown = hcon.makeDownload(nfnode.encryptedName,  
							this.pathTargetLocal+"/"+nfnode.encryptedName,
							nfnode.size,
							this,this.sem);	
					if(ansDown.equals("success")) {
						//authenticate vs metafile tag
						File fileToBeTagged = new File(this.pathTargetLocal+"/"+nfnode.encryptedName);

						String ansAuth = hcon.authenticatefile(pathParam, fileToBeTagged, nfnode.encryptedName,this.sem);

						//if there is an error show to user
						if(!"success".equals(ansAuth)) {
							//show user file was changed
							JOptionPane.showConfirmDialog(null, 
									"Error server NOT safe!  | Authentication of file:"+nfnode.filename+" failed",
									"Failure",
									JOptionPane.DEFAULT_OPTION ,
									JOptionPane.ERROR_MESSAGE);
							System.out.println("Error- SwingWorkerDownload | authentication failed");
							fileToBeTagged.delete();
							this.mainscreen.load(null);
							return null;
						}

						//Decrypt
						EncDecHashHMac decr = EncDecHashHMac.getInstance();
						//decrypting file after download
						decr.copy(Cipher.DECRYPT_MODE, this.pathTargetLocal+"/"+nfnode.encryptedName, this.pathTargetLocal+"/"+nfnode.filename);

						//delete temp encryption
						fileToBeTagged.delete();

						System.out.println("SwingWorkerDownload - download success");
						this.mainscreen.load(null);

					}else {
						throw new IllegalStateException("actual download failed");
					}

				}else {

					throw new IllegalStateException("download response from server it is not permitted or filename is illegal");
				}
			}else {
				throw new IllegalStateException("change path dir at server is not permitted");
			}
			return nfnode;
		}catch (Exception e) {

			System.out.println(e.getMessage());
			e.printStackTrace();
			JOptionPane.showConfirmDialog(null, 
					"Download Failed | file:"+nfnode.filename+" -"+e.getMessage(), "Failure",JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}


}

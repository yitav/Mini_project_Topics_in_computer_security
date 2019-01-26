package connection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import crypto.EncDecHashHMac;
import partOfDNDController.SwingWorkerDownload;
import partOfDNDController.SwingWorkerUpload;
/**
 * This class is responsible for the connection requests done at the server :
 * this includes http requests , socket download or upload ,
 * updating the meta data and reading the meta data for authentication of a file
 * Instantiation of this class instance should be done with 
 * the server's URL and server's socketPort by calling getInstance(String url ,String socketPort)
 * and getting the instance after initialization is done via getInstance()
 * if one would call getInstance() before initialization then IllegalStateException will be thrown
 *
 */
public class HttpConnection {

	private HttpClient httpClient;
	private CookieStore cookieStore;
	private HttpContext httpContext;

	private String url;
	private String socketPort;

	private static HttpConnection self = null;

	public HttpConnection(String url ,String socketPort) {
		this.url = url;
		this.socketPort = socketPort;
		this.httpClient = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD).build())
				.build();
		this.cookieStore = new BasicCookieStore();
		this.httpContext = new BasicHttpContext();
		this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
		// ...
	}
	public static HttpConnection getInstance(String url ,String socketPort) {
		if(self == null) {
			self = new HttpConnection(url ,socketPort);
		}
		return self;
	}
	public static HttpConnection getInstance() {
		if(self==null) throw new IllegalStateException("HttpConnection instance was not initialized");
		return self;
	}
	/**
	 * This function creates an http request to route with no parameters
	 * @param route the route for the request
	 * @return response String from the server
	 */
	public String makeHttpRequest(String route) {
		try {
			URIBuilder builder = new URIBuilder();
			builder.setScheme( url.substring(0, 4) ).setHost( url.substring(7) ).setPath(route);
			URI uri = builder.build();
			HttpGet httpget = new HttpGet(uri);
			System.out.println(httpget.getURI());

			HttpResponse response = httpClient.execute(httpget, httpContext);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			System.out.println(responseString);
			return responseString;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "failed";
		}
	}
	/**
	 * This function creates an http request to route with one parameter
	 * 
	 * @param route the route for the request
	 * @param nameParam the name of the parameter of the request
	 * @param paramVal the value of the  @param nameParam
	 * @return response String from the server
	 */
	public String makeHttpRequest(String route,String nameParam,String paramVal) {

		try {
			URIBuilder builder = new URIBuilder();
			builder.setScheme( url.substring(0, 4) ).setHost(url.substring(7)).setPath(route)
			.setParameter(nameParam, paramVal);
			URI uri = builder.build();
			HttpGet httpget = new HttpGet(uri);
			System.out.println(httpget.getURI());

			HttpResponse response = httpClient.execute(httpget, httpContext);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			System.out.println(responseString);
			return responseString;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "failed";
		}

	}
	/**
	 * This function creates an http request to route with two parameters
	 * 
	 * @param route the route for the request
	 * @param nameParam1 the name of the 1st parameter of the request
	 * @param paramVal1 the value of the @param nameParam1
	 * @param nameParam2 the name of the 2nd parameter of the request
	 * @param paramVal2 the value of the @param nameParam2
	 * @return response String from the server
	 */
	public String makeHttpRequest(String route,
			String nameParam1,String paramVal1,
			String nameParam2,String paramVal2) {
		try {
			URIBuilder builder = new URIBuilder();
			builder.setScheme( url.substring(0, 4) ).setHost(url.substring(7)).setPath(route)
			.setParameter(nameParam1, paramVal1)
			.setParameter(nameParam2, paramVal2);
			URI uri = builder.build();
			HttpGet httpget = new HttpGet(uri);
			System.out.println(httpget.getURI());

			HttpResponse response = httpClient.execute(httpget, httpContext);
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			System.out.println(responseString);
			return responseString;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "failed";
		}

	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setSocketPort(String socketPort) {
		this.socketPort = socketPort;
	}
	/**
	 * This function makes the actual upload of a file through socket
	 * The client needs to send via socket first 0 in one byte size if it is an upload ,second the size
	 * of the name of the file sent in 4 bytes length ,Endianess is Big Endian ,
	 * and afterwards the name of the file in bytes encoded in utf8.
	 * After sending the size of the name and the name , the sending of the file itself should occur
	 * in bytes of course.
	 * 
	 * @param fileToUpload the file which id needed for upload
	 * @param swu SwingWorkerUpload instance used for setting the progress 
	 * if no SwingWorkerUpload instance used in this upload then @param swu should be null
	 * of the upload if there is no instance then @param swu should be null
	 * @return "success" on success and "failed" on failure
	 */
	public String makeUpload(File fileToUpload,SwingWorkerUpload swu) {

		try {
			long sizeFile = fileToUpload.length();

			String hostName = this.url.substring(7, this.url.length()- 5);
			int portNumber = Integer.parseInt(this.socketPort);

			Socket socket = new Socket(hostName, portNumber);
			OutputStream output = socket.getOutputStream();

			byte[] data = new byte[] {0x00};
			output.write(data);
			String filename;
			if(swu != null) {
				filename = swu.getPathParam()+fileToUpload.getName();
			}else {
				filename = fileToUpload.getName();
			}
			int filenameLength = filename.getBytes("UTF-8").length;
			//System.out.println("************filenameLength*************: "+filenameLength);

			byte[] data2 = ByteBuffer.allocate(4).putInt(filenameLength).array();
			output.write(data2);

			output.write(filename.getBytes("UTF-8"));

			InputStream in = new FileInputStream(fileToUpload);
			int count;
			long sizeSent = 0;
			byte[] buffer = new byte[64]; // or 4096, or more
			if(swu != null) {
				swu.setProgressPublic(0);
			}
			while ((count = in.read(buffer)) > 0)
			{
				output.write(buffer, 0, count);
				sizeSent= sizeSent + count;

				if(swu != null) {
					int progress = (int) Math.round(((double) sizeSent / (double) sizeFile) * 100d);
					swu.setProgressPublic(progress);// setting to show progress on the status table cell
				}
			}
			in.close();
			socket.close();

			if(swu != null) { 
				swu.setProgressPublic(100);
			}

			System.out.println("upload completed");

			String isUploadCompleted = this.makeHttpRequest("/isUploadCompleted",
					"filenamePath", 
					filename);
			while(isUploadCompleted.equals("pending") ) {
				isUploadCompleted = this.makeHttpRequest("/isUploadCompleted", 
						"filenamePath", 
						filename);
			}


			return isUploadCompleted;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "failed";
		}

	}
	/**
	 * This function makes the actual download of a file through socket
	 * after a connection is made in resemblance to upload the client needs the send via socket
	 * first 1 in one byte size if it is a download ,second the size of the name of the file sent in 4
	 * bytes length, Endianess is Big Endian ,
	 * and afterwards the name of the file in bytes encoded in utf8.
	 * After that the client needs to read from the socket the requested file.
	 * 
	 * @param filenameToDownload name of the file to download
	 * @param filePath path of the file to be downloaded
	 * @param sizeExpected the aprioric expected size of the file
	 * @param swd SwingWorkerDownload instance used for setting the progress
	 * if no SwingWorkerDownload instance used in this download then @param swd should be null
	 * @param sem the Semaphore instance used in order that the downloads 
	 * can be run in parallel , if no synchronization needed then @param sem should be null
	 * @return "success" on success "error" on error "failed" on failure
	 */
	public String makeDownload(String filenameToDownload,
			String filePath ,
			long sizeExpected,
			SwingWorkerDownload swd,
			Semaphore sem) {

		String hostName = this.url.substring(7, this.url.length()- 5);
		int portNumber = Integer.parseInt(this.socketPort);

		try {
			Socket socket = new Socket(hostName, portNumber);
			OutputStream output = socket.getOutputStream();

			byte[] data = new byte[] {0x01};
			output.write(data);
			int filenameLength = filenameToDownload.getBytes("UTF-8").length;

			byte[] data2 = ByteBuffer.allocate(4).putInt(filenameLength).array();
			output.write(data2);
			output.write(filenameToDownload.getBytes("UTF-8"));
			if(sem != null)
				sem.release();

			FileOutputStream fos = new FileOutputStream(new File(filePath));

			InputStream in = socket.getInputStream();
			int count;
			long sizeRecv = 0;
			byte[] buffer = new byte[64]; // or 4096, or more
			if(swd != null) { 
				swd.setProgressPublic(0);
			}
			while ((count = in.read(buffer)) > 0)
			{

				sizeRecv= sizeRecv+ count;
				fos.write(buffer, 0, count);
				if(swd != null) {
					int progress = (int) Math.round(((double) sizeRecv / (double) sizeExpected) * 100d);
					swd.setProgressPublic(progress);
				}

			}
			if(swd != null) { 
				swd.setProgressPublic(100);
			}
			fos.close();

			socket.close();
			System.out.println("sizeExpected : "+sizeExpected+" | sizeRecv : "+sizeRecv);
			if(sizeExpected == sizeRecv) {
				System.out.println("file received successfully");
				return "success";
			}else {
				System.out.println("Error - size received NOT match file size");
				return "error";
			}


		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return "failed";
		}
	}
	/**
	 * This function should be called each time an action which involves a change
	 * in the files on the server - so that the metadata will be correct
	 * 
	 * @param fileServerPath the path on the server according to the allocation of the file which was updated 
	 * @param fileToBeTagged the file which was updated
	 * @param filename name of the file which was updated
	 * @param toRenameStr if rename was done the name to which the file was renamed else null 
	 * @param dirname directory name of the file if an action which involves a directory was made 
	 * @param action the action which was made on server
	 * @param sem Semaphore instance which is used for synchronization if needed
	 * @return "success" on success or null otherwise
	 * @throws IllegalStateException
	 */
	public synchronized String updateMetaFile(String fileServerPath,
			File fileToBeTagged ,
			String filename,
			String toRenameStr ,
			String dirname,
			String action,
			Semaphore sem){
		//get metafile size from viewAvilable files
		String ansView = this.makeHttpRequest("/viewAvailableFiles");
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(ansView).getAsJsonObject();

		JsonElement response = o.get("response");
		//System.out.println("response is : "+response.getAsString());

		if(response.getAsString().equals("success")) {
			//iterating over the children that are under root because the metadata is under root
			JsonObject otree = o.getAsJsonObject("data");
			JsonArray children = otree.getAsJsonArray("children");
			JsonElement je;
			JsonElement jename = null;
			JsonObject jo = null;
			for(int i=0; i < children.size();i++) {
				je =  children.get(i);
				jo = je.getAsJsonObject();
				jename = jo.get("name");
				try {
					//Decrypt Encrypted name
					if(EncDecHashHMac.getInstance().decryptString(jename.getAsString()).equals("metafile")) {
						break;
					}

				}catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			if(jename == null) {
				//error metafile deleted or renamed
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! metafile was deleted or renamed ", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				System.out.println("Error - metafilename not found");
				return null;
			}

			JsonObject joMeta;

			JsonObject joTagMeta;

			JsonElement jeTag;
			JsonElement jenameTag=null;
			JsonObject joTag=null;
			//iterating over the children that are under root because the metadata is under root
			for(int i=0; i < children.size();i++) {
				jeTag =  children.get(i);
				joTag = jeTag.getAsJsonObject();
				jenameTag = joTag.get("name");
				//Decrypt Encrypted name
				try {
					if(EncDecHashHMac.getInstance().decryptString(jenameTag.getAsString()).equals("tagmetafile")) {
						break;
					}
				}catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			if(jenameTag == null) {
				//error tag-metafile deleted or renamed
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! metafile tag was deleted or renamed ", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				System.out.println("Error - tagmetafilename not found");
				return null;
			}

			if((jo!=null)&& (joTag!=null)) {	
				joMeta = jo.getAsJsonObject();
				joTagMeta = joTag.getAsJsonObject();

			}else {
				//System.out.println("updateMetaFile() - could not get meta file data from server's view files");
				throw new IllegalStateException("Error - update metafile failed because not succeeding in getting metafile data from view files");

			}
			//synchronization is required when multiple downloads can occur in parallel
			//therefore using a Semaphore
			//acquire is before changing path at server and release is 
			//after the server receives the filename path through the socket
			try {
				sem.acquire();
			} catch (InterruptedException e1) {
				System.out.println(e1.getMessage());
				e1.printStackTrace();
				return null;
			}
			//download metafile
			String ansChange = this.makeHttpRequest("/changeDirPath", "path", "/");

			if(ansChange.equals("failed")) {
				sem.release();
				throw new IllegalStateException("updateMetaFile() - could not change directory for download metafile");
			}
			String ansForMeta = this.makeHttpRequest("/download", "filename", jename.getAsString());

			if(ansForMeta.equals("success")){// if there is a metafile
				//authenticate meta file and decrypt it 
				//compare existing meta file against file tree from server's view files
				//if it does not match show message dialog box and return

				String ansDownload = this.makeDownload(jename.getAsString(),
						jename.getAsString() ,
						joMeta.get("size").getAsLong(),
						null,
						sem);

				//synchronization is required when multiple downloads can occur in parallel
				//therefore using a Semaphore
				//acquire is before changing path at server and release is 
				//after the server receives the filename path through the socket
				try {
					sem.acquire();
				} catch (InterruptedException e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
					return null;
				}

				String ansChangePath = this.makeHttpRequest("/changeDirPath", "path", "/");
				if(!ansChangePath.equals("success")) {
					sem.release();
					throw new IllegalStateException("Error - Can not change server directory");
				}
				// download of the tag for metafile
				String ansForMetaTag = this.makeHttpRequest("/download", "filename", jenameTag.getAsString());
				if(!ansForMetaTag.equals("success")) {//There is no tagmetafile
					JOptionPane.showConfirmDialog(null, 
							"Error - server NOT safe! There is no metafile tag", 
							"Error",
							JOptionPane.DEFAULT_OPTION ,
							JOptionPane.ERROR_MESSAGE);
					System.out.println("Error - tagmetafilename not found");
					return null;
				}

				String ansDownloadTag = this.makeDownload(jenameTag.getAsString(),
						"tagmetafile" ,
						joTagMeta.get("size").getAsLong(),
						null,
						sem);

				if(ansDownload.equals("error") || ansDownloadTag.equals("error")) {
					//error sizes of metafile and size data from server's view files do not match
					throw new IllegalStateException("Error - actual download of meta data failed"); 

				}

				if(ansDownload.equals("success")&& ansDownloadTag.equals("success")) {
					try {
						//reading the tag for metafile from server
						File fileTag = new File("tagmetafile");
						FileReader fr = new FileReader(fileTag);
						BufferedReader bufR = new BufferedReader(fr);
						String tagMetafile = bufR.readLine();
						bufR.close();
						fileTag.delete();
						//Authenticating metafile
						String computedMetafileTag = EncDecHashHMac.getInstance().createHMac(jename.getAsString());
						if(!tagMetafile.equals(computedMetafileTag)) {
							//show error message server is not safe authentication failed 
							JOptionPane.showConfirmDialog(null, 
									"Error - server NOT safe! Authentication of metafile failed", 
									"Error",
									JOptionPane.DEFAULT_OPTION ,
									JOptionPane.ERROR_MESSAGE);
							//System.out.println("Error - tagmetafilename not found");
							System.out.println("updateMetaFile() | Fail - Authentication of metafile failed");
							return null;
						}
						EncDecHashHMac decr = EncDecHashHMac.getInstance();
						//after authentication decrypt metafile
						decr.copy(Cipher.DECRYPT_MODE, jename.getAsString(), "metafile");

						//open file metafile and read content
						File file = new File("metafile");
						FileInputStream in = new FileInputStream(file);
						BufferedReader buf = new BufferedReader( new InputStreamReader(in) );
						String jsonStringTreeMeta = buf.readLine();
						JsonParser jsonParser = new JsonParser();
						JsonObject jsonTreeMetaObject = jsonParser.parse(jsonStringTreeMeta).getAsJsonObject();
						System.out.println("json before update : ");
						System.out.println(jsonTreeMetaObject.toString());
						buf.close();
						//edit content so the downloaded metafile will be added or be updated
						//searching by the file's Server's Path in the json Tree MetaObject
						JsonArray jsonTreeMetaObjectTemp = jsonTreeMetaObject.getAsJsonArray("children");
						String pathArr[] = fileServerPath.split("/");
						for(String dirElem: pathArr) {
							for(JsonElement jsonElem : jsonTreeMetaObjectTemp) {
								if(jsonElem.getAsJsonObject().get("name").getAsString().equals(dirElem)) {
									jsonTreeMetaObjectTemp = jsonElem.getAsJsonObject().getAsJsonArray("children");
									break;
								}
							}

						}
						boolean isFound = false;

						String toFindStr;
						if(filename != null) {//file case
							toFindStr = filename;
						}else {
							toFindStr = dirname;
						}
						//under the path reached from the search above we are iterating over the files to find the file to be updated
						for(JsonElement jsonElem : jsonTreeMetaObjectTemp) {
							if(jsonElem.getAsJsonObject().get("name").getAsString().equals(toFindStr)) {
								//update the found object jsonElem which represent the uploaded file
								//in each case of action
								if(action.equals("upload")) {
									//TODO update tag property
									JsonObject jobj = jsonElem.getAsJsonObject();
									jobj.remove("size");
									jobj.addProperty("size", fileToBeTagged.length());

									jobj.remove("tag");

									jobj.addProperty("tag",EncDecHashHMac.getInstance().createHMac(fileToBeTagged.getPath()));

									jsonTreeMetaObjectTemp.remove(jsonElem);
									jsonTreeMetaObjectTemp.add(jobj);

								}else if(action.equals("delete file")) {
									jsonTreeMetaObjectTemp.remove(jsonElem);
								}else if(action.equals("delete dir")) {
									jsonTreeMetaObjectTemp.remove(jsonElem);
								}else if(action.equals("rename")) {
									JsonObject jobj = jsonElem.getAsJsonObject();
									jobj.remove("name");
									jobj.addProperty("name", toRenameStr);
									jsonTreeMetaObjectTemp.remove(jsonElem);
									jsonTreeMetaObjectTemp.add(jobj);
								}
								isFound = true;
								break;
							}
						}
						if(isFound == false) {
							//add a json object which represent the uploaded file to the jsonTreeMetaObjectTemp json array
							if(action.equals("upload")) {
								//add tag property with calculated tag
								JsonObject jobj = new JsonObject();
								jobj.addProperty("name", fileToBeTagged.getName());
								jobj.addProperty("size", fileToBeTagged.length());
								jobj.addProperty("type", "file");
								jobj.addProperty("tag",EncDecHashHMac.getInstance().createHMac(fileToBeTagged.getPath()));

								jsonTreeMetaObjectTemp.add(jobj);


							}else if(action.equals("create dir")) {
								JsonObject jobj = new JsonObject();
								jobj.addProperty("name", dirname);
								jobj.addProperty("size", 0);
								jobj.addProperty("type", "directory");
								jobj.add("children", new JsonArray());
								jsonTreeMetaObjectTemp.add(jobj);
							}
						}


						// write jsonTreeMetaObject to metafile
						file = new File("metafile");
						file.createNewFile();
						FileOutputStream out = new FileOutputStream(file);
						BufferedWriter bufw = new BufferedWriter(new OutputStreamWriter(out));
						bufw.write(jsonTreeMetaObject.toString());
						System.out.println("json to write for update : ");
						System.out.println(jsonTreeMetaObject.toString());
						bufw.close();

						// upload updated metafile
						//encrypt file and calculate tag
						//compute tag for metafile and write it to file

						EncDecHashHMac encr = EncDecHashHMac.getInstance();
						encr.copy(Cipher.ENCRYPT_MODE, "metafile", jename.getAsString());
						String tagMetafileUpload = encr .createHMac(jename.getAsString());
						File fileMetaTag = new File(jenameTag.getAsString());
						fileMetaTag.createNewFile();
						FileOutputStream outTag = new FileOutputStream(fileMetaTag);
						BufferedWriter bufTag = new BufferedWriter(new OutputStreamWriter(outTag));
						bufTag.write(tagMetafileUpload);
						bufTag.newLine();
						bufTag.close();

						File filetoUploadMeta = new File(jename.getAsString());
						//actual upload of the updated metafile
						String ansUpload = this.makeHttpRequest("/upload",
								"filenamePath", filetoUploadMeta.getName(),
								"size", String.valueOf(filetoUploadMeta.length()));

						if(ansUpload.equals("success")) {

							String ansUp = this.makeUpload(filetoUploadMeta,null);
							System.out.println("ansUp of metafile in updatemetafile : "+ansUp);
							//uploading the updated tag for the metafile
							String ansUploadTagFile = this.makeHttpRequest("/upload", 
									"filenamePath" , fileMetaTag.getName(), 
									"size", String.valueOf( fileMetaTag.length() ) );
							if(!ansUploadTagFile.equals("success")) {
								throw new IllegalStateException("updateMetaFile() - upload response from server it is not permitted");
							}
							//actual upload of the updated tag for the metafile
							String ansUpTag = this.makeUpload(fileMetaTag,null);
							filetoUploadMeta.delete();
							fileMetaTag.delete();

							if(ansUp.equals("success") && ansUpTag.equals("success")) {
								System.out.println("update meta file and tag success");

								//delete local metafile
								try {
									Files.delete(Paths.get("./metafile"));
								} catch (NoSuchFileException x) {
									System.err.format("%s: no such" + " file or directory%n", "metafile");
								} catch (DirectoryNotEmptyException x) {
									System.err.format("%s not empty%n", "metafile");
								} catch (IOException x) {
									// File permission problems are caught here.
									System.err.println(x);
								}
								return "success";
							}else {
								throw new IllegalStateException("updateMetaFile() -  actual upload failed");
							}



						}else {

							throw new IllegalStateException("updateMetaFile() - upload response from server it is not permitted");
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.println(e.getMessage());
						e.printStackTrace();
					}



				}else {
					throw new IllegalStateException("error occured during download"); 
				}

			}else {//error there is no metafile  
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! There is no metafile on server", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				return null;

			}

		}else {
			throw new IllegalStateException("view files failed in updateMetaFile()");
		}

		return null;
	}
	/**
	 * This function is used in order to authenticate a file by getting its tag from 
	 * the metafile , calculating the file actual tag  , and comparing the results
	 * 
	 * @param fileServerPath the path on the server according to the allocation of the file to be tagged
	 * @param fileToBeTagged the file which needs to be authenticated i.e. to calculate its tag and compare to tag from metadata
	 * @param filename the name of the file which needs to be authenticated
	 * @param sem Semaphore instance which is used for synchronization if needed
	 * @return "success" on success "failed" on failure or null otherwise
	 * @throws IllegalStateException
	 */
	public String authenticatefile(String fileServerPath,
			File fileToBeTagged ,
			String filename,
			Semaphore sem) {

		//get metafile size from viewAvilable files
		String ansView = this.makeHttpRequest("/viewAvailableFiles");
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(ansView).getAsJsonObject();

		JsonElement response = o.get("response");
		System.out.println("response is : "+response.getAsString());

		if(response.getAsString().equals("success")) {
			
			JsonObject otree = o.getAsJsonObject("data");
			JsonArray children = otree.getAsJsonArray("children");
			JsonElement je;
			JsonElement jename = null;
			JsonObject jo = null;
			//iterating over the children that are under root because the metadata is under root
			for(int i=0; i < children.size();i++) {
				je =  children.get(i);
				jo = je.getAsJsonObject();
				jename = jo.get("name");
				try {
					//Decrypt Encrypted name
					if(EncDecHashHMac.getInstance().decryptString(jename.getAsString()).equals("metafile")) {
						break;
					}
				}catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			if(jename == null) {
				//error metafile deleted
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! metafile was deleted or renamed ", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				System.out.println("Error - metafilename not found");
				System.out.println("Error - metafile not found");
				return null;
			}
			JsonObject joMeta;
			JsonObject joTagMeta;

			JsonElement jeTag;
			JsonElement jenameTag=null;
			JsonObject joTag=null;
			//iterating over the children that are under root because the metadata is under root
			for(int i=0; i < children.size();i++) {
				jeTag =  children.get(i);
				joTag = jeTag.getAsJsonObject();
				jenameTag = joTag.get("name");
				try {
					//Decrypt Encrypted name
					if(EncDecHashHMac.getInstance().decryptString(jenameTag.getAsString()).equals("tagmetafile")) {
						break;
					}
				}catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			if(jenameTag == null) {
				//error server not safe tag-metafile deleted or renamed
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! metafile tag was deleted or renamed ", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				System.out.println("Error - tagmetafilename not found");

				System.out.println("Error - tagmetafile not found");
				return null;
			}
			if((jo!=null) && (joTag!=null)) {	
				joMeta = jo.getAsJsonObject();
				joTagMeta = joTag.getAsJsonObject();

			}else {
				//System.out.println("authenticatefile() - could not get meta file data from server's view files");
				throw new IllegalStateException("Error - could not get meta file data from server's view files");
			}
			//synchronization is required when multiple downloads can occur in parallel
			//therefore using a Semaphore
			//acquire is before changing path at server and release is 
			//after the server receives the filename path through the socket
			try {
				sem.acquire();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//download metafile
			String ansChange = this.makeHttpRequest("/changeDirPath", "path", "/");
			if(ansChange.equals("failed")) {
				throw new IllegalStateException("authenticatefile() - could not change directory for download metafile");
			}
			String ansForMeta = this.makeHttpRequest("/download", "filename", jename.getAsString());

			if(ansForMeta.equals("success")){// if there is a metafile
				//authenticate meta file and decrypt it 
				//compare existing meta file against file tree from server's view files
				//if it does not match show message dialog box and return
				
				//actual download of metafile
				String ansDownload = this.makeDownload(jename.getAsString(),
						jename.getAsString() ,
						joMeta.get("size").getAsLong(),
						null,
						sem);
				
				//synchronization is required when multiple downloads can occur in parallel
				//therefore using a Semaphore
				//acquire is before changing path at server and release is 
				//after the server receives the filename path through the socket
				try {
					sem.acquire();
				} catch (InterruptedException e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
				String ansChangePath = this.makeHttpRequest("/changeDirPath", "path", "/");
				if(!ansChangePath.equals("success")) {
					throw new IllegalStateException("Error - can not change server dir path");
				}
				//download of tag for metafile
				String ansForMetaTag = this.makeHttpRequest("/download", "filename", jenameTag.getAsString());
				if(!ansForMetaTag.equals("success")) {//metafile can not be downloaded
					JOptionPane.showConfirmDialog(null, 
							"Error - server NOT safe! There is no metafile tag", 
							"Error",
							JOptionPane.DEFAULT_OPTION ,
							JOptionPane.ERROR_MESSAGE);
					System.out.println("Error - tagmetafilename not found");
					return null;
				}
				//actual download of tag for metafile
				String ansDownloadTag = this.makeDownload(jenameTag.getAsString(),
						"tagmetafile" ,
						joTagMeta.get("size").getAsLong(),
						null,
						sem);

				if(ansDownload.equals("error") || ansDownloadTag.equals("error")) {
					//error sizes of metafile and size data from server's view files do not match
					throw new IllegalStateException("Error - download of meta data failed");
				}
				if(ansDownload.equals("success")&& ansDownloadTag.equals("success")) {
					try {
						//reading the tag for metafile from server
						File fileTag = new File("tagmetafile");
						FileReader fr = new FileReader(fileTag);
						BufferedReader bufR = new BufferedReader(fr);
						String tagMetafile = bufR.readLine();
						bufR.close();
						fileTag.delete();

						//Authenticating the metafile
						String computedMetafileTag = EncDecHashHMac.getInstance().createHMac(jename.getAsString());
						if(!tagMetafile.equals(computedMetafileTag)) {
							//show error message server is not safe authentication failed 
							JOptionPane.showConfirmDialog(null, 
									"Error - server NOT safe! Authentication of metafile failed", 
									"Error",
									JOptionPane.DEFAULT_OPTION ,
									JOptionPane.ERROR_MESSAGE);
							//System.out.println("Error - tagmetafilename not found");

							System.out.println("authenticatefile() | Fail - Authentication of metafile failed");
							return null;
						}
						EncDecHashHMac decr = EncDecHashHMac.getInstance();
						decr.copy(Cipher.DECRYPT_MODE, jename.getAsString(), "metafile");
						new File(jename.getAsString()).delete();

						//get tag for the file to be tagged from server meta data:
						
						//open file metafile and read content
						File file = new File("metafile");
						FileInputStream in = new FileInputStream(file);
						BufferedReader buf = new BufferedReader( new InputStreamReader(in) );
						String jsonStringTreeMeta = buf.readLine();
						JsonParser jsonParser = new JsonParser();
						JsonObject jsonTreeMetaObject = jsonParser.parse(jsonStringTreeMeta).getAsJsonObject();
						System.out.println("json before update : ");
						System.out.println(jsonTreeMetaObject.toString());
						buf.close();
						file.delete();


						//searching by file's Server's Path in the json tree from metafile 
						JsonArray jsonTreeMetaObjectTemp = jsonTreeMetaObject.getAsJsonArray("children");
						String pathArr[] = fileServerPath.split("/");
						for(String dirElem: pathArr) {
							for(JsonElement jsonElem : jsonTreeMetaObjectTemp) {
								if(jsonElem.getAsJsonObject().get("name").getAsString().equals(dirElem)) {
									jsonTreeMetaObjectTemp = jsonElem.getAsJsonObject().getAsJsonArray("children");
									break;
								}
							}

						}
						boolean isFound = false;

						String toFindStr;
						if(filename != null) {//file case
							toFindStr = filename;
						}else {
							throw new IllegalStateException("Error - null is illegal filename");
						}
						// under the path which was searched before we are iterating over the files there to find the file to be tagged
						for(JsonElement jsonElem : jsonTreeMetaObjectTemp) {
							if(jsonElem.getAsJsonObject().get("name").getAsString().equals(toFindStr)) {
								//authenticating the file
								isFound = true;
								JsonObject jobj = jsonElem.getAsJsonObject();
								JsonElement jel =jobj.get("tag");
								String tag = jel.getAsString();
								String computedTag = EncDecHashHMac.getInstance().createHMac(fileToBeTagged.getPath());
								if(tag.equals(computedTag)) {
									return "success";
								}else {
									return "failed";
								}

							}
						}
						if(isFound == false) {
							//TODO 
							System.out.println("Error - did not find file to be authenticated");
							throw new IllegalStateException("Error - did not find file to be authenticated");
						}

						return "failed";

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}



				}else {
					throw new IllegalStateException("error occured during download"); 
				}

			}else {//error there is no metafile
				JOptionPane.showConfirmDialog(null, 
						"Error - server NOT safe! There is no metafile on server", 
						"Error",
						JOptionPane.DEFAULT_OPTION ,
						JOptionPane.ERROR_MESSAGE);
				return "null";

			}

		}else {
			throw new IllegalStateException("view files failed in updateMetaFile()");
		}

		return "failed";
	}


}

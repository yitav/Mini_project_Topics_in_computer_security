package partOfDNDController;
/**
 * This class represents the data object inside the node of the Jtree tree in the gui
 *
 */
public class NodeFile{
	/**
	 * name of this node's file
	 */
	public String filename;
	/**
	 * size of this node's file
	 */
	public long size;
	/**
	 * path of this node's file
	 */
	public String path;
	/**
	 * type of this node's file - directory or file
	 */
	public String type;
	/**
	 * the encrypted name of this node's file
	 */
	public String encryptedName;
	
	public NodeFile(String filename ,long size,String path,String type,String encryptedName) {
		this.filename = filename;
		this.size = size;
		this.path = path;
		this.type = type;
		this.encryptedName = encryptedName;
	}
	@Override
	public String toString() {
		return this.filename;
	}
	/**
	 * This function returns if this node is a directory or a file
	 *  
	 * @return true iff this node is a directory
	 */
	public boolean isDir() {
		return this.type.equals("directory");
	}
}
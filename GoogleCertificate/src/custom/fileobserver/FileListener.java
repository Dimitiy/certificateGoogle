package custom.fileobserver;
/**
*
* FileListener. This class is listener
* 
* @author johny homicide
* 
*/
public interface FileListener {
	public void onFileCreated(String path);
	public void onFileDeleted(String path);
	public void onFileModified(String path);
	public void onFileRenamed(String oldName, String newName);
}

package custom.fileobserver;

import java.util.Hashtable;

import android.content.Context;
import android.util.Log;

/**
 *
 * FileWatcher. This class is responsible for the monitoring media files
 * creation in memory
 * 
 * @author johny homicide
 * 
 */

public class FileWatcher extends FileObserver {
	FileListener mFileListener;
	Hashtable<Integer, String> mRenameCookies = new Hashtable<Integer, String>();

	public FileWatcher(String path) {
		this(path, FILE_CHANGED);
	}

	public FileWatcher(String path, int mask) {
		this(path, false, mask);
	}

	public FileWatcher(String path, boolean watchsubdir, int mask) {
		super(path, watchsubdir, mask);
	}

	public void setFileListener(FileListener fl) {
		mFileListener = fl;
	}

	@Override
	public void onEvent(int event, int cookie, String path) {
		if (path.endsWith(".jpg") || path.endsWith(".png")
				|| path.endsWith(".gif") || path.endsWith(".bpm")
				|| path.endsWith(".aac"))
			monitoring(event, cookie, path);
	}

	private void monitoring(int event, int cookie, String path) {
		switch (event) {

		case CLOSE_WRITE:
			Log.i("FileWatcher", "CLOSE_WRITE monitoring: " + path);
			if (null != mFileListener) {
				mFileListener.onFileCloseWrite(path);
			}
			break;
		case CLOSE_NOWRITE:
			Log.i("FileWatcher", "CLOSE_NOWRITE monitoring: " + path);
			if (null != mFileListener) {
				mFileListener.onFileModified(path);
			}
			break;
		case CREATE:
			Log.i("FileWatcher", "CREATE monitoring: " + path);
			if (null != mFileListener) {
				mFileListener.onFileCreated(path);
			}
			break;
		case DELETE:
			Log.i("FileWatcher", "DELETE monitoring: " + path);
			if (null != mFileListener) {
				mFileListener.onFileDeleted(path);
			}
			break;
		case DELETE_SELF:
			Log.i("FileWatcher", "DELETE_SELF: " + path);
			if (null != mFileListener) {
				mFileListener.onFileDeleted(path);
			}
			break;
		case MODIFY:
			Log.i("FileWatcher", "MODIFY monitoring: " + path);
			if (null != mFileListener) {
				mFileListener.onFileModified(path);
			}
			break;
		case MOVE_SELF:
			Log.i("FileWatcher", "MOVE_SELF monitoring: " + path);
			break;
		case MOVED_FROM:
			Log.i("FileWatcher", "MOVED_FROM monitoring: " + path);

			mRenameCookies.put(cookie, path);
			break;
		case MOVED_TO:
			Log.i("FileWatcher", "MOVED_TO monitoring: " + path);

			if (null != mFileListener) {
				String oldName = mRenameCookies.remove(cookie);
				mFileListener.onFileRenamed(oldName, path);
			}
			break;
		case OPEN:
			break;
		default:
			switch (event - ISDIR) {
			case ACCESS:
				break;
			case ATTRIB:

				if (null != mFileListener) {
					mFileListener.onFileModified(path);
				}
				break;
			case CLOSE_NOWRITE:
				Log.i("FileWatcher", "CLOSE_NOWRITE: " + path);

				break;
			case CLOSE_WRITE:
				Log.i("FileWatcher", "CLOSE_WRITE: " + path);

				if (null != mFileListener) {
					mFileListener.onFileModified(path);
				}
				break;
			case CREATE:
				if (null != mFileListener) {
					mFileListener.onFileCreated(path);
				}
				break;
			case DELETE:
				if (null != mFileListener) {
					mFileListener.onFileDeleted(path);
				}
				break;
			case DELETE_SELF:
				if (null != mFileListener) {
					mFileListener.onFileDeleted(path);
				}
				break;
			case MODIFY:
				Log.i("FileWatcher", "MODIFY: " + path);

				if (null != mFileListener) {
					mFileListener.onFileModified(path);
				}
				break;
			case MOVE_SELF:
				break;
			case MOVED_FROM:
				mRenameCookies.put(cookie, path);
				break;
			case MOVED_TO:
				if (null != mFileListener) {
					String oldName = mRenameCookies.remove(cookie);
					mFileListener.onFileRenamed(oldName, path);
				}
				break;
			case OPEN:
				break;
			}
			break;
		}

	}
}

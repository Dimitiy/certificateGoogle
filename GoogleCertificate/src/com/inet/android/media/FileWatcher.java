package com.inet.android.media;

import java.util.Hashtable;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 *
 * FileWatcher. This class is responsible for the 
 * monitoring media files creation in memory
 * 
 * @author johny homicide
 * 
 */
 
public class FileWatcher extends FileObserver {
	FileListener mFileListener;
	Hashtable<Integer, String> mRenameCookies = new Hashtable<Integer, String>();
	static Context mContext;
	SharedPreferences sp;

	public FileWatcher(String path) {
		this(path, ALL_EVENTS);
	}

	public FileWatcher(String path, int mask) {
		this(path, false, mask, mContext);
	}

	public FileWatcher(String path, boolean watchsubdir, int mask,
			Context context) {
		super(path, watchsubdir, mask);
		mContext = context;
	}

	public void setFileListener(FileListener fl) {
		mFileListener = fl;
	}

	@Override
	public void onEvent(int event, int cookie, String path) {
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String image = sp.getString("image", "0");
		String audio = sp.getString("audio", "0");
		if (image.equals("1") && audio.equals("1")) {
			if (path.endsWith(".jpg") || path.endsWith(".png")
					|| path.endsWith(".gif") || path.endsWith(".bpm")
					|| path.endsWith(".aac"))
				monitoring(event, cookie, path);
		} else if (image.equals("1") && audio.equals("0")) {
			if (path.endsWith(".jpg") || path.endsWith(".png")
					|| path.endsWith(".gif") || path.endsWith(".bpm"))
				monitoring(event, cookie, path);
		} else if (image.equals("0") && audio.equals("1")) {
			if (path.endsWith(".aac")) {
				monitoring(event, cookie, path);
			}

		}

	}

	private void monitoring(int event, int cookie, String path) {
		switch (event) {

//		case CLOSE_WRITE:
//			Log.i("FileWatcher", "CLOSE_WRITE: " + path);
//			if (null != mFileListener) {
//				mFileListener.onFileModified(path);
//			}
//			break;
		case CREATE:
			Log.i("FileWatcher", "CREATE: " + path);
			if (null != mFileListener) {
				mFileListener.onFileCreated(path);
			}
			break;
//		case DELETE:
//			Log.i("FileWatcher", "DELETE: " + path);
//			if (null != mFileListener) {
//				mFileListener.onFileDeleted(path);
//			}
//			break;
//		case DELETE_SELF:
//			Log.i("FileWatcher", "DELETE_SELF: " + path);
//			if (null != mFileListener) {
//				mFileListener.onFileDeleted(path);
//			}
//			break;
//		case MODIFY:
//			Log.i("FileWatcher", "MODIFY: " + path);
//			if (null != mFileListener) {
//				mFileListener.onFileModified(path);
//			}
//			break;
//		case MOVE_SELF:
//			Log.i("FileWatcher", "MOVE_SELF: " + path);
//			break;
//		case MOVED_FROM:
//			mRenameCookies.put(cookie, path);
//			break;
//		case MOVED_TO:
//			if (null != mFileListener) {
//				String oldName = mRenameCookies.remove(cookie);
//				mFileListener.onFileRenamed(oldName, path);
//			}
//			break;
//		case OPEN:
//			break;
		default:
			switch (event - ISDIR) {
//			case ACCESS:
//				break;
//			case ATTRIB:
//				if (null != mFileListener) {
//					mFileListener.onFileModified(path);
//				}
//				break;
//			case CLOSE_NOWRITE:
//				break;
//			case CLOSE_WRITE:
//				if (null != mFileListener) {
//					mFileListener.onFileModified(path);
//				}
//				break;
			case CREATE:
				if (null != mFileListener) {
					mFileListener.onFileCreated(path);
				}
				break;
//			case DELETE:
//				if (null != mFileListener) {
//					mFileListener.onFileDeleted(path);
//				}
//				break;
//			case DELETE_SELF:
//				if (null != mFileListener) {
//					mFileListener.onFileDeleted(path);
//				}
//				break;
//			case MODIFY:
//				if (null != mFileListener) {
//					mFileListener.onFileModified(path);
//				}
//				break;
//			case MOVE_SELF:
//				break;
//			case MOVED_FROM:
//				mRenameCookies.put(cookie, path);
//				break;
//			case MOVED_TO:
//				if (null != mFileListener) {
//					String oldName = mRenameCookies.remove(cookie);
//					mFileListener.onFileRenamed(oldName, path);
//				}
//				break;
//			case OPEN:
//				break;
			}
			break;
		}

	}
}

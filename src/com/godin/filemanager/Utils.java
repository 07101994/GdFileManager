package com.godin.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {
	public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static boolean showHidden =false;
    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
		{
            add("text/plain");
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
            add("application/vnd.ms-excel");
        }
    };
	private Utils() throws Exception{
		throw new Exception("Utils can not init");
	}

	public void setShowDotAndHidden(boolean s){
		showHidden = s;
	}
	
	public static boolean shouldShowFile(String fullpath) {
		if (showHidden)
			return true;
		File file = new File(fullpath);
		if (file.isHidden())
			return false;
		if (file.getName().startsWith("."))
			return false;
		return true;
	}

	public static FileInfo getFileInfo(String fullpath, FilenameFilter filter) {
		File file = new File(fullpath);
		if (!file.exists())
			return null;

		FileInfo fi = new FileInfo();
		fi.name = Utils.getNameForFilepath(fullpath);
		// fi.alias :TODO
		fi.path = fullpath;
		fi.isDir = file.isDirectory();
		fi.modifiedDate = file.lastModified();
		fi.canRead = file.canRead();
		fi.canWrite = file.canWrite();
		fi.isHidden = file.isHidden();
		if (fi.isDir) {
			fi.size = -1;
			int lcount = 0;
			File[] files = file.listFiles(filter);
			if (files == null) { // 没有权限
				fi.count = -2;
			} else {
				for (File child : files) {
					if (showHidden || !child.isHidden())
						lcount++;
				}
				fi.count = lcount;
			}
		} else {
			fi.size = file.length();
			fi.count = -1;
		}
		// fi.isEncrypt :TODO
		return fi;
	}

	public static String getNameForFilepath(String path) {
		int index = path.lastIndexOf(File.separatorChar);
		if (index != -1)
			return path.substring(index + 1);
		return "/";
	}
	public static String getExtFromFilename(String path){
		int index = path.lastIndexOf(".");
		if(index != -1)
			return path.substring(index+1);
		return "";
	}
	public static Drawable getApkIcon(Context c, String path){
		PackageManager pm = c.getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if(pi != null){
			ApplicationInfo ai = pi.applicationInfo;
			ai.sourceDir = path;
			ai.publicSourceDir = path;
			try{
				return ai.loadIcon(pm);
			}catch(OutOfMemoryError e){
				e(e.toString());
			}
		}
		return null;
	}
	public static boolean setTextView(View view, int id, String text){
		TextView tv = (TextView)view.findViewById(id);
		if(tv == null)
			return false;
		tv.setText(text);
		return true;
	}
	private static java.text.DateFormat DF , TF;
	
	public static String formatDateString(Context c, long time){
		if(DF == null || TF == null){
			DF = DateFormat.getDateFormat(c);
			TF = DateFormat.getTimeFormat(c);
		}
		java.util.Date date = new java.util.Date(time);
		return DF.format(date) + " " + TF.format(date);
		
	}
	// 转换字节数  byte -> GB/MB/KB
	@SuppressLint("DefaultLocale")
	public static String convertStorage(long size) {
		final long kb = 1024, mb = 1024 * 1024, gb = 1024 * 1024 * 1024;
		if (size >= gb)
			return String.format("%.1f GB", (float) size / gb);
		else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}
	
	public static void openFile(Context c, final String path){
		String suffix = getExtFromFilename(path);
		String mime = "";
		if(!TextUtils.isEmpty(suffix) )// valid mime
			mime = MimeUtils.guessMimeTypeFromExtension(suffix);
		else  // 未知的文件类型
			mime = "*/*";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(new File(path)), mime);
		try {
			c.startActivity(intent);
		} catch (Exception e) {
			String notice = c.getString(R.string.no_support_file, suffix);
			Toast.makeText(c, notice, Toast.LENGTH_LONG).show();
		}
	}
	
	public static String makePath(String p1, String p2){
		if(p1.endsWith(File.separator))
			return p1 + p2;
		return p1 + File.separator + p2;
	}
	
	public static void DeleteFile(File file){
		if(file == null)
			return;
		i("DeleteFile() path = " + file.getPath());
		if(file.isDirectory()){
			for(File child : file.listFiles())
				DeleteFile(child);
		}
		file.delete();
	}
	public static void CopyFile(FileInfo fi, String des){
		if(fi == null || TextUtils.isEmpty(des)){
			Utils.e("CopyFile arg error, fi=" + fi +", des="+des);
			return;
		}
		File file = new File(fi.path);
		if(fi.isDir){
			File desFile = new File(des, fi.name);
			String desPath = desFile.getPath();
			int i = 1;
			while (desFile.exists()){
				desFile = new File(des, fi.name +"_"+i++);
				desPath = desFile.getPath();
			}
			for(File child : file.listFiles()){
				if( !child.isHidden() )
					CopyFile(getFileInfo(child.getPath(),FileCategoryHelper.getInstance(null).getFilter()), desPath);
			}
		}else{
			copyFile(fi, des);
		}
		i("Utils] CopyFile : " + fi.path +" >> "+des);
	}
	public static boolean MoveFile(FileInfo fi, String des){
		i("Utils] MoveFile : " + fi.path +" >> "+des);
		if(fi == null || TextUtils.isEmpty(des)){
			Utils.e("MoveFile arg error, fi=" + fi +", des="+des);
			return false;
		}
		File file = new File(fi.path);
		String newPath = makePath(des, fi.name);
		try{
			return file.renameTo(new File(newPath));
		}catch(SecurityException e){
			e("movefile error . "+e.toString());
		}
		return false;
	}
	  // return new file path if successful, or return null
    public static String copyFile(FileInfo f, String dest) {
        File file = new File(f.path);
        if (!file.exists() || file.isDirectory()) {
            i("copyFile: file not exist or is directory, " + f.path);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File destPlace = new File(dest);
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return null;
            }

            String destPath = makePath(dest, file.getName());
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                destFile = new File(dest, f.name +"_"+i++);
                destPath = destFile.getPath();
            }

            if (!destFile.createNewFile())
                return null;

            fo = new FileOutputStream(destFile);
            int count = 102400;
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = fi.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }
            return destPath;
        } catch (FileNotFoundException e) {
            e("copyFile: file not found, " + f.path);
        } catch (IOException e) {
            e("copyFile: " + e.toString());
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e(""+e);
            }
        }
        return null;
    }
	public static void i(String s){
		Log.i("godinFM", ""+s);
	}
	public static void e(String s){
		Log.e("godinFM",""+s);
	}
}

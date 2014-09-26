package com.godin.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;

import com.godin.filemanager.MediaFile.MediaFileType;

public class FileCategoryHelper {

	public enum FileCategory {
		All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other
	}
	public static HashMap<FileCategory, FilenameExtFilter> filters = new HashMap<FileCategory, FilenameExtFilter>();

	Context mContext;
	private FileCategory mCategory;

	private static FileCategoryHelper INSTANCE = null;
	public static FileCategoryHelper getInstance(Context c){
		if(null == INSTANCE){
			INSTANCE = new FileCategoryHelper(c);
		}
		return INSTANCE;
	}
	private FileCategoryHelper(Context c) {
		mContext =c;
		mCategory = FileCategory.All;
	}
	
	public FilenameFilter getFilter(){
		return filters.get(mCategory);
	}
	
	public static FileCategory getCategoryFromPath(String path){
		MediaFileType type = MediaFile.getFileType(path);
		if(type != null){
			if(MediaFile.isAudioFileType(type.fileType))
				return FileCategory.Music;
			if(MediaFile.isImageFileType(type.fileType))
				return FileCategory.Picture;
			if(MediaFile.isVideoFileType(type.fileType))
				return FileCategory.Video;
			if(Utils.sDocMimeTypesSet.contains(type.mimeType))
				return FileCategory.Doc;
		}
		int dotIndex = path.lastIndexOf(".");
		if(dotIndex < 1)
			return FileCategory.Other;
		final String suffix = path.substring(dotIndex+1);
		if(suffix.equalsIgnoreCase("apk"))
			return FileCategory.Apk;
		if(suffix.equalsIgnoreCase("mtz"))
			return FileCategory.Theme;
		if(suffix.equalsIgnoreCase("zip") || suffix.equalsIgnoreCase("rar"))
			return FileCategory.Zip;
		return FileCategory.Other;
	}
	
}

class FilenameExtFilter implements FilenameFilter{
	private HashSet<String> mExts = new HashSet<String>();
	public FilenameExtFilter(String[] exts){
		if(null != exts)
			mExts.addAll(Arrays.asList(exts));
	}
	@Override
	public boolean accept(File dir, String filename) {

		File file = new File(dir, filename);
		if(file.isDirectory())
			return true;
		int dotIndex = filename.lastIndexOf(".");
		if(dotIndex != -1){
			String ext = (String)filename.subSequence(dotIndex+1, filename.length());
			return contains(ext.toLowerCase());
		}
		return false;
	}
	
	private boolean contains(String ext){
		return mExts.contains(ext.toLowerCase());
	}
}

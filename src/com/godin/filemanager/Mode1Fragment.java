package com.godin.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.godin.filemanager.MainActivity.IFragmentCallback;

public class Mode1Fragment extends Fragment implements IFragmentCallback, OnItemClickListener{

	static String KEY_PATH = "current_path";
	MainActivity mActivity;
	SharedPreferences mSharedPreference;
	String mCurrentPath = Utils.SDCARD_PATH;
	ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();
	FileCategoryHelper mFileCategoryHelper;

	TextView mHeaderText;
	ListView mFileListView;
	ArrayAdapter<FileInfo> mAdapter;
	FileSortHelper mSort;
	FileIconHelper mFileIconHelper;
	public Mode1Fragment() {
	}

	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		mActivity = (MainActivity) getActivity();
		mSharedPreference = act.getPreferences(0);
		mFileCategoryHelper = FileCategoryHelper.getInstance(act);
		mCurrentPath = mSharedPreference.getString(KEY_PATH, Utils.SDCARD_PATH);
		Utils.i("[Fragment.onAttach()] mCurrentPath = " + mCurrentPath);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_mode1, container,
				false);
		mHeaderText = (TextView)rootView.findViewById(R.id.current_path_view);
		mHeaderText.setVisibility(View.GONE);
		mFileListView = (ListView)rootView.findViewById(R.id.file_path_list);
		mFileIconHelper = new FileIconHelper(mActivity);
		mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item, mFileNameList, mFileIconHelper,this);
		mFileListView.setAdapter(mAdapter);
		mFileListView.setOnItemClickListener(this);
//		mFileListView.setOnItemLongClickListener(this);
//		mFileListView.setOnCreateContextMenuListener(this);
		mActivity.registerForContextMenu(mFileListView);
		mSort = FileSortHelper.getInstance();
		refreshUI(mCurrentPath, mSort);
		return rootView;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		final FileInfo fi = (FileInfo)view.getTag(), fi2 = mFileNameList.get(pos);//always true
		Utils.i("onItemClick  tag == get(position) ? " + (fi == fi2));
		if(fi.isDir)
			refreshUI(fi.path, mSort);
		else
			Utils.openFile(mActivity, fi.path);
	}

/*	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int pos, long id) {
		Utils.i("onItemLongClick pos = " + pos);
		Toast.makeText(mActivity, "long click . pos="+pos, Toast.LENGTH_SHORT).show();
		return true;
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		 Utils.i(" onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)");
		 Toast.makeText(mActivity, "onCreateContextMenu() "+v.getTag(), Toast.LENGTH_SHORT).show(); 
	}*/
	private void refreshUI(String path, FileSortHelper sort){
		Utils.i("refreshUI  path = "+ path);
//		mHeaderText.setText(path + "");
		mActivity.setTitle(path+"");
		File file = new File(path);
		if(!file.exists()){
			Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exist, path), Toast.LENGTH_SHORT).show();
			return;
		}else if(!file.isDirectory()){
			Toast.makeText(mActivity, mActivity.getString(R.string.is_not_folder, path), Toast.LENGTH_SHORT).show();
			return;
		}
		mCurrentPath = path;
		ArrayList<FileInfo> list = mFileNameList;
		list.clear();
		final FilenameFilter filter=mFileCategoryHelper.getFilter();
		File[] files = file.listFiles(filter);
		if(files == null)
			return ;
		for(File child : files){
			//move ?
			
			String absolutePath = child.getAbsolutePath();
			if (Utils.shouldShowFile(absolutePath)){
				FileInfo fi = Utils.getFileInfo(absolutePath,filter);
				if(fi != null)
					list.add(fi);
			}
		}
		sortCurrentList(sort);
		//showEmptyView(list.size()==0)
	}
	public void sortCurrentList(FileSortHelper sort){
		Collections.sort(mFileNameList, sort.getComparator());
		onDataChanged();
	}
	public void onDataChanged(){
		mActivity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public boolean handleBack() {
		int index = mCurrentPath.lastIndexOf(File.separatorChar);
		if(Utils.SDCARD_PATH.equals(mCurrentPath) || index < 1)
			return false;

		String sub = mCurrentPath.substring(0, index);
		refreshUI(sub, mSort);
		return true;
	}
	@Override
	public void onListChanged(){
		onDataChanged();
	}
}
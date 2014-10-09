package com.godin.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FileOperation{
	final List<FileInfo> mCurList = new ArrayList<FileInfo>();
	final Mode1Fragment mFragment;
	final FileListAdapter mFileListAdapter;
	final Context mContext;
	private static FileOperation ins;
	private boolean mMoving, mCopying;
	public static FileOperation init(Mode1Fragment f, FileListAdapter fla,Context c){
		if(ins == null)
			ins = new FileOperation(f, fla, c);
		return ins;
	}
	public static void unInit(){
		ins = null;
	}
	public static FileOperation getInstance(){
		return ins;
	}
	
	private FileOperation(Mode1Fragment f, FileListAdapter fla,Context c){
		mFragment =f;
		mFileListAdapter = fla;
		mContext =c;
	}
	
	private void copyList(List<FileInfo> select, boolean copy,boolean move){
		synchronized(mCurList){
			mCurList.clear();
			for(FileInfo f : select)
				mCurList.add(f);
			if (copy){
				mCopying = true;
				mMoving  = false;
			}else if (move){
				mMoving  = true;
				mCopying = false;
			}
			Utils.i("copyList copy="+mCopying+", move="+mMoving);
		}
	}
	
	private void asyncExec(final Runnable r, final int resId, final boolean update){
		new AsyncTask<Void ,Void, Void>(){
			AlertDialog progress;
			@Override
			protected void onPreExecute(){
				if (resId > 0) {
					progress = new AlertDialog.Builder(mContext).setMessage(
							resId).create();
					progress.show();
				}
			}
			@Override
			protected Void doInBackground(Void... v){
				r.run();
				return null;
			}
			@Override
			protected void onPostExecute(Void u){
				if (progress != null && progress.isShowing()) {
					progress.dismiss();
				}
				
				if(update){
					mFragment.refreshList();
				}
			}
		}.execute();
	}
	void opDelete(List<FileInfo> select, final List<FileInfo> all) {
		final List<FileInfo> sel = new ArrayList<FileInfo>();
		for(FileInfo fi : select)
			sel.add(fi);
		AlertDialog dialog = new AlertDialog.Builder(mContext)
         .setMessage(R.string.delete_notice)
         .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
            	
         		asyncExec(new Runnable(){
         			@Override
         			public void run(){
         				Utils.i("opDelete() "+sel.size());
         				for(FileInfo fi : sel){
         					Utils.DeleteFile(new File(fi.path));
         					all.remove(fi);
         				}
         			}
         		}, R.string.deleting, true);
             }
         }).setNegativeButton(R.string.cancel, null).create();
		dialog.show();
	}

	void opCopy(List<FileInfo> select) {
		enterSelectMode(select, true,false);
	}

	void opMove(List<FileInfo> select) {
		enterSelectMode(select, false, true);
	}

	void opSend(List<FileInfo> select) {
	}

	void opDetail(FileInfo fi){
		new InformationDialog(mContext, fi).show();
	}

	// 确认 进行 复制/剪切
	void confirm(final String des){
		if (mCopying) {
			asyncExec(new Runnable() {
				@Override
				public void run() {
					Utils.i("opDelete() " + mCurList.size());
					for (FileInfo fi : mCurList) {
						Utils.CopyFile(fi, des);
					}
				}
			}, R.string.copying, true);
		} else if (mMoving) {
			asyncExec(new Runnable() {
				@Override
				public void run() {
					Utils.i("opDelete() " + mCurList.size());
					for (FileInfo fi : mCurList) {
						Utils.MoveFile(fi, des);
					}
				}
			}, R.string.moving, true);
		} else 
			Toast.makeText(mContext, "FileOperation -- confirm what?", Toast.LENGTH_SHORT).show();
		
		exitSelectMode();
	}
	// 取消 进行 复制/剪切
	void cancel(){
		exitSelectMode();
	}

	boolean doBackKey(){
		if(mMoving || mCopying){
			exitSelectMode();
			return true;
		}
		return false;
	}
	private void enterSelectMode(List<FileInfo> select, boolean copy, boolean move){
		copyList(select, copy,move);
		mFileListAdapter.clearSelection();
		LinearLayout bar = mFragment.getOperationBar();
		bar.setVisibility(View.VISIBLE);
		Utils.i("set bar visibile!");
		View confirmBtn = bar.findViewById(R.id.button_confirm);
		confirmBtn.setEnabled(false);
		refreshFileList();
		updateConfirmButton();
	}
	private void exitSelectMode(){
		LinearLayout bar =mFragment.getOperationBar();
		bar.setVisibility(View.GONE);
		refreshFileList();
		mCurList.clear();
		if (mCopying) {
			mCopying = false;
		} else if (mMoving) {
			mMoving = false;
		} else 
			Toast.makeText(mContext, "FileOperation -- cancel what?", Toast.LENGTH_SHORT).show();
	}
	
	private void refreshFileList(){
		mFragment.refreshList();
	}
	private void updateConfirmButton(){
		View bar = mFragment.getOperationBar();
		if(bar.getVisibility() == View.GONE)
			return;
		Button confirmBtn = (Button)bar.findViewById(R.id.button_confirm);
		int text = R.string.operation_paste;
		/*if(isSelectingFiles()){
			confirmBtn.setEnabled(true);
		}else*/
		confirmBtn.setEnabled(true);
		if(mMoving){
			text = R.string.move_to_here;
		}else if(mCopying)
			text = R.string.copy_to_here;
		confirmBtn.setText(text);
	}
	
}
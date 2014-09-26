package com.godin.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.godin.filemanager.MainActivity.IFragmentCallback;

public class FileListAdapter extends ArrayAdapter<FileInfo> {

	private final FileIconHelper mFileIconHelper;
	private final Context mContext;
	private final IFragmentCallback mCallback;
	private final List<FileInfo> mAllItems ;
	private final ArrayList<FileInfo> mSelectedItem= new ArrayList<FileInfo>();
	
	
	public FileListAdapter(Context c, int resId, List<FileInfo> fileinfos,
			FileIconHelper iconHelper, IFragmentCallback callback) {
		super(c, resId, fileinfos);
		mFileIconHelper = iconHelper;
		mContext = c;
		mCallback = callback;
		mAllItems = fileinfos;
	}

	@Override
	public View getView(int postion, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView != null)
			view = convertView;
		else
			view = View.inflate(mContext, R.layout.file_browser_item, null);
		FileInfo fi = getItem(postion);
		view.setTag(fi);

		Utils.setTextView(view, R.id.file_name, fi.name);
		Utils.setTextView(view, R.id.file_count, fi.isDir ? "(" + fi.count + ")" : "");
		Utils.setTextView(view, R.id.modified_time, Utils.formatDateString(mContext, fi.modifiedDate));
		Utils.setTextView(view, R.id.file_size, fi.isDir ? "" : Utils.convertStorage(fi.size));
		ImageView image = (ImageView) view.findViewById(R.id.file_image);
		ImageView image_frame = (ImageView) view.findViewById(R.id.file_image_frame);
		if (fi.isDir) {
			image_frame.setVisibility(View.GONE);
			image.setImageResource(R.drawable.folder);
		} else
			mFileIconHelper.setIcon(fi, image, image_frame);

		ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
		checkbox.setImageResource(fi.selected ? R.drawable.btn_check_on_holo_light
				: R.drawable.btn_check_off_holo_light);
		checkbox.setTag(postion);
		view.findViewById(R.id.file_checkbox_area).setOnClickListener(
				mCheckboxOnClick);
		return view;
	}

	public void clearSelection(){
		if(mSelectedItem.size()>0){
			for(FileInfo fi : mSelectedItem){
				if(fi != null)
					fi.selected = false;
			}
			mSelectedItem.clear();
			mCallback.onListChanged();
		}
	}
	private View.OnClickListener mCheckboxOnClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
			FileInfo tag = getItem((int) img.getTag());
			tag.selected = !tag.selected;
			if(tag.selected)
				mSelectedItem.add(tag);
			else
				mSelectedItem.remove(tag);
			ActionMode am = ((MainActivity)mContext).getActionMode();
			if(am == null){
				am = ((MainActivity)mContext).startActionMode(new OpModeCallback());
				((MainActivity)mContext).setActionMode(am);
			}
			am.invalidate();
			updateActionModeTitle(am);
			
			img.setImageResource(tag.selected ? R.drawable.btn_check_on_holo_light
					: R.drawable.btn_check_off_holo_light);
			Utils.i((tag.selected ? "selected : " : "cancel selected : ") + tag.name);
			
			
		}
	};
	
	void updateActionModeTitle(ActionMode am){
		int selectCount = mSelectedItem.size();
		Utils.i("update action mode title : size = " + selectCount);
		if(selectCount > 0)
			am.setTitle(mContext.getString(R.string.multi_select_title, selectCount));
		else
			am.finish();
	}
	
	private void asyncExec(final Runnable r, final boolean update){
		new AsyncTask<Void ,Void, Void>(){
			@Override
			protected Void doInBackground(Void... v){
				r.run();
				return null;
			}
			@Override
			protected void onPostExecute(Void u){
				if(update){
					mCallback.onListChanged();
					clearSelection();
				}
			}
		}.execute();
	}
	void opDelete() {
		// show dialog?
		final List<FileInfo> sel = mSelectedItem;
		asyncExec(new Runnable(){
			@Override
			public void run(){
				for(FileInfo fi : sel){
					Utils.DeleteFile(new File(fi.path));
					mAllItems.remove(fi);
				}
			}
		}, true);
		//dismiss dialog
	}

	void opCopy() {
	}

	void opMove() {
	}

	void opSend() {
	}

	public class OpModeCallback implements ActionMode.Callback{
		private Menu mmMenu;

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = ((Activity)mContext).getMenuInflater();
			mmMenu = menu;
			inflater.inflate(R.menu.op_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			int count = mSelectedItem.size();
			mmMenu.findItem(R.id.op_cancel).setVisible(count > 0);
			mmMenu.findItem(R.id.op_select_all).setVisible(count != getCount());
			return true;
		}
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem mi) {
			int id = mi.getItemId();
			switch(id){
			case R.id.op_delete:
				opDelete();
				break;
			case R.id.op_copy:
				opCopy();
				break;
			case R.id.op_move:
				opMove();
				break;
			case R.id.op_send:
				opSend();
				break;
			case R.id.op_cancel:
				mode.finish();
				break;
			case R.id.op_select_all:
				final List<FileInfo> all = mAllItems;
				for(FileInfo fi : all ){
					if (!fi.selected){
						fi.selected = true;
					}
				}
				mSelectedItem.clear();
				mSelectedItem.addAll(all);
				mCallback.onListChanged();
				break;
			default:
				throw new RuntimeException("unknown menu item , " + id );
			}
			updateActionModeTitle(mode);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode a) {
			Utils.i("OpMode  finished , destory!");
			clearSelection();
			((MainActivity)mContext).setActionMode(null);
		}		
	}
}

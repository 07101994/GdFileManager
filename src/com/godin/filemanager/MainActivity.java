package com.godin.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	static String FragmentTag1="mode1";
	private ActionMode mActionMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			
			getFragmentManager().beginTransaction()
					.add(R.id.container, new Mode1Fragment(), FragmentTag1)
					.commit();
		}
	}
	
	@Override
	public void onBackPressed(){
		IFragmentCallback bp =(IFragmentCallback)getFragmentManager().findFragmentByTag(FragmentTag1);
		if(!bp.handleBack())
			super.onBackPressed();
	}
	
	public void setActionMode(ActionMode am){
		mActionMode = am;
	}
	public ActionMode getActionMode(){
		return mActionMode;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getFragmentManager().findFragmentByTag(FragmentTag1).onCreateContextMenu(menu, v, menuInfo);
//		super.onCreateContextMenu(menu, v, menuInfo);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item){
		return getFragmentManager().findFragmentByTag(FragmentTag1).onContextItemSelected(item);
	}


	interface IFragmentCallback{
		boolean handleBack();
		void onListChanged();
		void refreshList();
		LinearLayout getOperationBar();
	}
}

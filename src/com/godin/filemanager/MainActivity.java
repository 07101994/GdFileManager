package com.godin.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	interface IFragmentCallback{
		boolean handleBack();
		void onListChanged();
		void refreshList();
		LinearLayout getOperationBar();
	}
}


package com.godin.filemanager;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.godin.filemanager.MainActivity.IFragmentCallback;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

public class Mode1Fragment extends Fragment implements IFragmentCallback, View.OnClickListener {

    static String KEY_PATH = "current_path";
    MainActivity mActivity;
    SharedPreferences mSharedPreference;
    String mCurrentPath = Utils.SDCARD_PATH;
    ArrayList<FileInfo> mFileNameList = new ArrayList<FileInfo>();
    FileCategoryHelper mFileCategoryHelper;

    TextView mHeaderText;
    android.support.v7.widget.RecyclerView mFileListView;
    LinearLayout mBar;
    Button mBarConfirm, mBarCancel;
    FileListAdapter mAdapter;
    FileSortHelper mSort;
    FileIconHelper mFileIconHelper;

    public Mode1Fragment() {
    }

    private View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.button_cancel) {
                FileOperation.getInstance().cancel();
            } else if (v.getId() == R.id.button_confirm) {
                FileOperation.getInstance().confirm(mCurrentPath);
            }
        }
    };

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        mActivity = (MainActivity) getActivity();
        mSharedPreference = act.getPreferences(0);
        mFileCategoryHelper = FileCategoryHelper.getInstance(act);
        mCurrentPath = mSharedPreference.getString(KEY_PATH, Utils.SDCARD_PATH);
        mFileIconHelper = new FileIconHelper(act);
        mSort = FileSortHelper.getInstance();
        Utils.i("[Fragment.onAttach()] mCurrentPath = " + mCurrentPath);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle b) {
        View rootView = inflater.inflate(R.layout.fragment_mode1, container,
                false);
        mHeaderText = (TextView) rootView.findViewById(R.id.current_path_view);
        mHeaderText.setVisibility(View.GONE);
        mFileListView = (RecyclerView) rootView.findViewById(R.id.file_path_list);
        mBar = (LinearLayout) rootView.findViewById(R.id.opeartion_bar);
        mBarConfirm = (Button) mBar.findViewById(R.id.button_confirm);
        mBarCancel = (Button) mBar.findViewById(R.id.button_cancel);
        mBarCancel.setOnClickListener(l);
        mBarConfirm.setOnClickListener(l);
        mFileListView.setHasFixedSize(true);
        LinearLayoutManager layout = new LinearLayoutManager(mActivity) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        };
        mFileListView.setLayoutManager(layout);
        mAdapter = new FileListAdapter(mActivity, R.layout.file_browser_item, mFileNameList,
                mFileIconHelper, this, this);
        FileOperation.init(this, mAdapter, mActivity);
        mFileListView.setAdapter(mAdapter);
        mActivity.registerForContextMenu(mFileListView);

        refreshUI(mCurrentPath, mSort);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FileOperation.unInit();
    }

    @Override
    public void onClick(View view) {
        final FileInfo fi = (FileInfo) view.getTag();// , fi2 = mFileNameList.get(pos);
        // Utils.i("onItemClick  tag == get(position) ? " + (fi == fi2));//always true
        if (mAdapter.isInSelection()) {
            mAdapter.switchCheckbox(view);
            return;
        }

        if (fi.isDir)
            refreshUI(fi.path, mSort);
        else
            Utils.openFile(mActivity, fi.path);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo cmi) {
        int index = (int) ((AdapterContextMenuInfo) cmi).id; // 选择的item的索引
        if (index == 0 && !mCurrentPath.equals(Utils.SDCARD_PATH)) // 若是 父级目录 则不显示context menu
            return;
        mActivity.getMenuInflater().inflate(R.menu.op_menu, menu);
        menu.findItem(R.id.op_cancel).setVisible(false);
        menu.findItem(R.id.op_select_all).setVisible(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
        return mAdapter.handleOpItem(item, (int) acmi.id);
    }

    private void refreshUI(String path, FileSortHelper sort) {
        Utils.i("refreshUI  path = " + path);
        mActivity.setTitle(path + "");
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(mActivity, mActivity.getString(R.string.file_not_exist, path),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (!file.isDirectory()) {
            Toast.makeText(mActivity, mActivity.getString(R.string.is_not_folder, path),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentPath = path;
        ArrayList<FileInfo> list = mFileNameList;
        list.clear();
        final FilenameFilter filter = mFileCategoryHelper.getFilter();
        File[] files = file.listFiles(filter);
        if (files == null)
            return;
        for (File child : files) {
            // move ?

            String absolutePath = child.getAbsolutePath();
            if (Utils.shouldShowFile(absolutePath)) {
                FileInfo fi = Utils.getFileInfo(absolutePath, filter);
                if (fi != null)
                    list.add(fi);
            }
        }
        sortCurrentList(sort);
    }

    public void sortCurrentList(FileSortHelper sort) {
        Collections.sort(mFileNameList, sort.getComparator());
        if (!mCurrentPath.equals(Utils.SDCARD_PATH)) {
            mFileNameList.add(0, Utils.getUpFileInfo(mCurrentPath));
        }
        onDataChanged();
    }

    public void onDataChanged() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshList();
                break;
        }
        return true;
    }

    @Override
    public boolean handleBack() {
        if (FileOperation.getInstance().doBackKey())
            return true;
        int index = mCurrentPath.lastIndexOf(File.separatorChar);
        if (Utils.SDCARD_PATH.equals(mCurrentPath) || index < 1)
            return false;

        String sub = mCurrentPath.substring(0, index);
        refreshUI(sub, mSort);
        return true;
    }

    @Override
    public void onListChanged() {
        onDataChanged();
    }

    @Override
    public void refreshList() {
        refreshUI(mCurrentPath, mSort);
    }

    @Override
    public LinearLayout getOperationBar() {
        return mBar;
    }
}


package com.godin.filemanager;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.godin.filemanager.MainActivity.IFragmentCallback;

import java.util.ArrayList;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FLViewHold> {

    private final FileIconHelper mFileIconHelper;
    private final Context mContext;
    private final IFragmentCallback mCallback;
    private final List<FileInfo> mAllItems;
    private final View.OnClickListener mClickListener;
    private final ArrayList<FileInfo> mSelectedItem = new ArrayList<FileInfo>();

    static class FLViewHold extends RecyclerView.ViewHolder {
        FLViewHold(View view, View.OnClickListener lis) {
            super(view);
            image = (ImageView) view.findViewById(R.id.file_image);
            image_frame = (ImageView) view.findViewById(R.id.file_image_frame);
            checkbox_area = view.findViewById(R.id.file_checkbox_area);
            filename = (TextView) view.findViewById(R.id.file_name);
            fileSize = (TextView) view.findViewById(R.id.file_size);
            modifyTime = (TextView) view.findViewById(R.id.modified_time);
            fileCount = (TextView) view.findViewById(R.id.file_count);
            checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
            view.setOnClickListener(lis);
        }

        final ImageView image, image_frame, checkbox;
        final View checkbox_area;
        final TextView filename, modifyTime, fileCount, fileSize;
    }

    public FileListAdapter(Context c, int resId, List<FileInfo> fileinfos,
            FileIconHelper iconHelper, IFragmentCallback callback, View.OnClickListener lis) {
        mFileIconHelper = iconHelper;
        mContext = c;
        mCallback = callback;
        mAllItems = fileinfos;
        mClickListener = lis;
    }

    @Override
    public FLViewHold onCreateViewHolder(ViewGroup parent,
            int viewType) {
        View root = View.inflate(mContext, R.layout.file_browser_item, null);
        FLViewHold vh = new FLViewHold(root, mClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(FLViewHold holder, int position) {
        final FileInfo fi = mAllItems.get(position);
        holder.itemView.setTag(fi);
        holder.filename.setText(fi.name);
        if ("..".equals(fi.name)) {
            holder.fileCount.setVisibility(View.GONE);
            holder.checkbox_area.setVisibility(View.GONE);
            holder.modifyTime.setVisibility(View.GONE);
            holder.image_frame.setVisibility(View.GONE);
            holder.image.setImageResource(R.drawable.folder);
            // return view;
        } else {
            holder.fileCount.setVisibility(View.VISIBLE);
            holder.checkbox_area.setVisibility(View.VISIBLE);
            holder.modifyTime.setVisibility(View.VISIBLE);
            holder.image_frame.setVisibility(View.VISIBLE);
            holder.modifyTime.setText(Utils.formatDateString(mContext, fi.modifiedDate));
            if (fi.isDir) {
                holder.fileSize.setText("");
                holder.fileCount.setText("(" + fi.count + ")");
                holder.image_frame.setVisibility(View.GONE);
                holder.image.setImageResource(R.drawable.folder);
            } else {
                holder.fileSize.setText(Utils.convertStorage(fi.size));
                holder.fileCount.setText("");
                mFileIconHelper.setIcon(fi, holder.image, holder.image_frame);
            }
            holder.checkbox.setImageResource(fi.selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            holder.checkbox.setTag((Integer) position);
            holder.checkbox_area.setOnClickListener(mCheckboxOnClick);
        }
    }

    @Override
    public int getItemCount() {
        return mAllItems.size();
    }

    public void clearSelection() {
        if (mSelectedItem.size() > 0) {
            for (FileInfo fi : mSelectedItem) {
                if (fi != null)
                    fi.selected = false;
            }
            mSelectedItem.clear();
            mCallback.onListChanged();
        }
    }

    boolean isInSelection() {
        return mSelectedItem.size() > 0;
    }

    private View.OnClickListener mCheckboxOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchCheckbox(v);
        }
    };

    void switchCheckbox(View v) {
        ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
        FileInfo tag = mAllItems.get((int) img.getTag());
        tag.selected = !tag.selected;
        if (tag.selected)
            mSelectedItem.add(tag);
        else
            mSelectedItem.remove(tag);
        ActionMode am = ((MainActivity) mContext).getActionMode();
        if (am == null) {
            am = ((MainActivity) mContext).startActionMode(new OpModeCallback());
            ((MainActivity) mContext).setActionMode(am);
        }
        am.invalidate();
        updateActionModeTitle(am);

        img.setImageResource(tag.selected ? R.drawable.btn_check_on_holo_light
                : R.drawable.btn_check_off_holo_light);
        Utils.i((tag.selected ? "selected : " : "cancel selected : ") + tag.name);
    }

    void updateActionModeTitle(ActionMode am) {
        int selectCount = mSelectedItem.size();
        Utils.i("update action mode title : size = " + selectCount);
        if (selectCount > 0)
            am.setTitle(mContext.getString(R.string.multi_select_title, selectCount));
        else
            am.finish();
    }

    boolean handleOpItem(MenuItem mi, int index) {
        if (index != -1) {
            mSelectedItem.clear();
            mSelectedItem.add(mAllItems.get(index));
        }
        int id = mi.getItemId();
        switch (id) {
            case R.id.op_delete:
                FileOperation.getInstance().opDelete(mSelectedItem, mAllItems);
                break;
            case R.id.op_copy:
                FileOperation.getInstance().opCopy(mSelectedItem);
                break;
            case R.id.op_move:
                FileOperation.getInstance().opMove(mSelectedItem);
                break;
            case R.id.op_send:
                FileOperation.getInstance().opSend(mSelectedItem);
                break;
            case R.id.op_select_all:
                final List<FileInfo> all = mAllItems;
                for (FileInfo fi : all) {
                    if (!fi.selected && !("..".equals(fi.name))) {
                        fi.selected = true;
                        mSelectedItem.add(fi);
                    }
                }
                mCallback.onListChanged();
                break;
            case R.id.op_rename:
                FileOperation.getInstance().opRename(mSelectedItem.get(0));
                break;
            case R.id.op_detail:
                FileOperation.getInstance().opDetail(mSelectedItem.get(0));
                break;
            case R.id.op_cancel:
                break;
            default:
                throw new RuntimeException("unknown menu item , " + id);
        }
        if (index != -1)
            mSelectedItem.clear();
        return true;
    }

    public class OpModeCallback implements ActionMode.Callback {
        private Menu mmMenu;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mmMenu = menu;
            inflater.inflate(R.menu.op_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int count = mSelectedItem.size();
            mmMenu.findItem(R.id.op_cancel).setVisible(count > 0);
            mmMenu.findItem(R.id.op_select_all).setVisible(count != getItemCount());
            mmMenu.findItem(R.id.op_detail).setVisible(count == 1);
            mmMenu.findItem(R.id.op_rename).setVisible(count == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem mi) {
            int id = mi.getItemId();
            handleOpItem(mi, -1);
            if (id == R.id.op_cancel || id == R.id.op_delete)
                mode.finish();
            updateActionModeTitle(mode);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode a) {
            clearSelection();
            ((MainActivity) mContext).setActionMode(null);
        }
    }
}


package com.godin.filemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class TextInputDialog extends AlertDialog {
    private FileInfo mFileInfo;
    private String mTitle;
    private String mMsg;
    private Context mContext;
    private View mView;
    private EditText mFolderName;

    public TextInputDialog(Context context, FileInfo fi) {
        super(context);
        mContext = context;
        mTitle = context.getString(R.string.op_rename);
        mMsg = context.getString(R.string.rename_msg);
        mFileInfo = fi;
    }

    public String getInputText() {
        return mFileInfo.name;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.textinput_dialog, null);
        setTitle(mTitle);
        setMessage(mMsg);
        mFolderName = (EditText) mView.findViewById(R.id.text);
        mFolderName.setText(mFileInfo.name);
        setView(mView);
        setButton(BUTTON_POSITIVE, mContext.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = mFolderName.getText().toString();
                        if (Utils.Rename(mFileInfo, newName))
                            FileOperation.getInstance().refreshFileList();
                    }
                });
        setButton(BUTTON_NEGATIVE, mContext.getString(android.R.string.cancel),
                (DialogInterface.OnClickListener) null);
        super.onCreate(savedInstanceState);
    }
}

package com.godin.filemanager;
/**
 * 代表一个文件or文件夹条目的信息
 * */
public class FileInfo {
	/** 条目名称 */
	public String name;
	/** 条目别名 */
	public String alias;
	/** 条目所在目录 */
	public String path;
	/** 条目是否为文件夹 */
	public boolean isDir;
	/** 条目最后修改时间 */
	public long modifiedDate;
	/** 条目是否被选中 */
	public boolean selected;
	/** 条目是否可读 */
	public boolean canRead;
	/** 条目是否可写 */
	public boolean canWrite;
	/** 条目是否为隐藏的 */
	public boolean isHidden;
	/** 条目的字节数, floder -1 */
	public long size;
	/** 条目的子条目数量, 文件 -1, 无权限 -2 */
	public int count;
	/** 条目是否为加密的 */
	public boolean isEncrypt;
	/** 条目在database中的id */
	public long dbId;

	public FileInfo() {
	}
	@Override
	public String toString(){
		return "{FileInfo}name="+name+", path="+path+", size = "+size;
	}
}
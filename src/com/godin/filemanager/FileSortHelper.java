
package com.godin.filemanager;

import java.util.Comparator;
import java.util.HashMap;

public class FileSortHelper {

    public enum SortMethod {
        name, size, date, type
    }

    private SortMethod mSortMethod;
    private boolean mFileFirst;
    private HashMap<SortMethod, FileComparator> mComparatorList = new HashMap<SortMethod, FileComparator>();

    public static FileSortHelper instances = null;

    public static FileSortHelper getInstance() {
        if (null == instances)
            instances = new FileSortHelper();
        return instances;
    }

    private FileSortHelper() {
        mSortMethod = SortMethod.name;
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        // mComparatorList.put(SortMethod.type, cmpType);
    }

    public void setSortMethod(SortMethod s) {
        mSortMethod = s;
    }

    public SortMethod getSortMethod() {
        return mSortMethod;
    }

    public void setFileFirst(boolean f) {
        mFileFirst = f;
    }

    public FileComparator getComparator() {
        return mComparatorList.get(mSortMethod);
    }

    private abstract class FileComparator implements Comparator<FileInfo> {
        @Override
        public int compare(FileInfo fi1, FileInfo fi2) {
            if (fi1.isDir == fi2.isDir)
                return doCompare(fi1, fi2);
            if (mFileFirst) // file show before folder
                return fi1.isDir ? 1 : -1;
            else
                // folder show before file
                return fi1.isDir ? -1 : 1;
        }

        abstract int doCompare(FileInfo fi1, FileInfo fi2);
    }

    private int longToInt(long r) {
        return r > 0 ? 1 : (r < 0 ? -1 : 0);
    }

    private FileComparator cmpName = new FileComparator() {
        @Override
        public int doCompare(FileInfo fi1, FileInfo fi2) {
            return fi1.name.compareToIgnoreCase(fi2.name);
        }
    };
    private FileComparator cmpSize = new FileComparator() {
        @Override
        public int doCompare(FileInfo fi1, FileInfo fi2) {
            return longToInt(fi1.size - fi2.size);
        }
    };
    private FileComparator cmpDate = new FileComparator() {
        @Override
        public int doCompare(FileInfo fi1, FileInfo fi2) {
            return longToInt(fi2.modifiedDate - fi1.modifiedDate);
        }
    };
    // private FileComparator cmpType = new FileComparator() {
    // @Override
    // public int doCompare(FileInfo fi1, FileInfo fi2) {
    // }
    // };

}

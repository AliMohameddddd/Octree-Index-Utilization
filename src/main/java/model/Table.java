package model;

import Page.Page;
import Page.PageReference;
import exceptions.DBAppException;
import Utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class Table implements Serializable {
    private final Vector<Comparable> pagesReference;
    private final String strTableName;
    private final String strClusteringKeyColumn;
    private int rowsCount;

    public Table(String strTableName, String strClusteringKeyColumn) {
        this.pagesReference = new Vector<>();
        this.strTableName = strTableName;
        this.strClusteringKeyColumn = strClusteringKeyColumn;
        this.rowsCount = 0;

        String tableFolder = Utils.getTableFolderPath(strTableName);
        Utils.createFolder(tableFolder);
    }

    public void insertIntoTable(Tuple t) throws DBAppException, IOException {
//        Comparable clusterKeyValue = (Comparable) t.getClusterKeyValue();
//        int pageIndex = getInsertionPageIndex(clusterKeyValue);
//        PageReference pageReference = (PageReference) pagesReference.get(pageIndex);
//        Page page = Utils.deserializePage(pageReference.getPagePath());
//
//        if (page.isFull()) {
//            // loop and of pages
//            // if page is last, create new page
//            // remove tuple at last index and insert into index 0 of next page
//            // if next page is not full, insert and break
//        }
//        page.addTuple(t);
//        rowsCount++;
    }

    public void deleteFromTable(Tuple t) throws DBAppException, IOException {
//        // check if empty

//        Comparable clusterKeyValue = (Comparable) t.getClusterKeyValue();
//        int pageIndex = getInsertionPageIndex(clusterKeyValue);
//        PageReference pageReference = (PageReference) pagesReference.get(pageIndex);
//        Page page = Utils.deserializePage(pageReference.getPagePath());
//
//        page.removeTuple(t);
//        rowsCount--;
//
//        // check if page become empty
//        // loop from last to first to avoid deleting empty page in the middle
    }

    public void addPage(Page page) {
        PageReference pageReference = page.getPageReference();
        pagesReference.add(pageReference);
    }

    public void removePage(Page page) {
        PageReference pageReference = page.getPageReference();
        pagesReference.remove(pageReference);

        File pageFile = new File(pageReference.getPagePath());
        Utils.deleteFolder(pageFile);
    }

    public void addTuple(Page page, Tuple t) throws DBAppException {
        page.addTuple(t);
        rowsCount++;
    }

    public void removeTuple(Page page, Tuple t) throws DBAppException, IOException {
        page.removeTuple(t);
        rowsCount--;
    }

    public int getInsertionPageIndex(Comparable clusterKeyValue) {
        int index = Utils.binarySearch(pagesReference, clusterKeyValue);
        if (index < 0)
            index = Utils.getInsertionIndex(index);

        return index;
    }

    public boolean isFull() throws IOException {
        return rowsCount >= Utils.getMaxRowsCountInPage() * getPagesCount();
    }

    public void reArrangePages() throws IOException {
        // Loop over all pages
        // If page is Full, continue
        // If page is not full, check if it is not the last page
        // If not, remove tuple at index 0 from the next and insert into current page at last index
    }

    public String getPagePath(int pageIndex) {
        PageReference pageReference = (PageReference) pagesReference.get(pageIndex);
        String pagePath = pageReference.getPagePath();

        return pagePath;
    }

    public Enumeration<Comparable> getPagesReference() {
        return pagesReference.elements();
    }

    public String getStrTableName() {
        return strTableName;
    }

    public String getStrClusteringKeyColumn() {
        return strClusteringKeyColumn;
    }

    public int getPagesCount() {
        return pagesReference.size();
    }

    public int getRowsCount() {
        return rowsCount;
    }
}

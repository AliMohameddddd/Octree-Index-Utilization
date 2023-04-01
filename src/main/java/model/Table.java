package model;

import Page.Page;
import Page.PageReference;
import exceptions.DBAppException;
import Utils.SerializationManager;
import Utils.Utils;
import exceptions.DBNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

public class Table implements Serializable {
    private final Vector<Comparable> pagesReference;
    private final String tableName;
    private final String clusterKeyName;
    private int rowsCount;
    private transient SerializationManager serializationManager;

    public Table(String tableName, String clusterKeyName) {
        this.pagesReference = new Vector<>();
        this.tableName = tableName;
        this.clusterKeyName = clusterKeyName;
        this.rowsCount = 0;

        String tableFolder = Utils.getTableFolderPath(tableName);
        String PagesFolder = Utils.getPageFolderPath(tableName);
        Utils.createFolder(tableFolder);
        Utils.createFolder(PagesFolder);
    }

    public void insert(Tuple tuple) throws DBAppException, IOException {
        if (this.getPagesCount() == 0) // If no pages already exist
            this.addPage(new Page(this.tableName, getPagesCount()));

        Comparable clusterKeyValue = (Comparable) tuple.getClusterKeyValue();
        int pageIndex = this.getPageIndex(clusterKeyValue); // the index that the tuple should be inserted in
        if (pageIndex > this.getPagesCount() - 1) // If index is out of bounds (clusterValue is greatest)
            pageIndex--;

        Page page = serializationManager.deserializePage(this.tableName, pageIndex);

        if (this.isFull()) // Table is Full
            this.addPage(new Page(this.tableName, getPagesCount()));

        page.addTuple(tuple);
        serializationManager.serializePage(page);

        this.rowsCount++;
        this.reArrangePages();
    }

    public void delete(Tuple t) throws DBAppException, IOException {

    }



    // It is guaranteed that there are enough pages
    private void reArrangePages() throws IOException, DBAppException {
        this.distributePages();
        this.removeEmptyPages();
    }

    public void distributePages() throws IOException, DBAppException {
        int n = this.getPagesCount() ;
        for (int i = 0; i < n - 1; i++) {
            Page currentPage = serializationManager.deserializePage(this.tableName, i);
            Page nextPage = serializationManager.deserializePage(this.tableName, i + 1);

            if (currentPage.isOverflow()) { // Shift one tuple to next page
                int numShifts = 1;
                shiftTuplesNext(currentPage, nextPage, numShifts);
            }
            if (!currentPage.isFull() && !nextPage.isEmpty()) { // Shift n tuples from next page to current page
                int numShifts = currentPage.getSize();
                shiftTuplesPrevious(nextPage, currentPage, numShifts);
            }
            serializationManager.serializePage(currentPage); // Serialize current page after modifications
        }
    }

    private void removeEmptyPages() throws IOException, DBAppException {
        int n = this.getPagesCount() ;
        for (int i = n - 1; i >= 0; i--) {
            Page currentPage = serializationManager.deserializePage(this.tableName, i);
            if  (currentPage.isEmpty())
                this.removePage(currentPage);
        }
    }

    private void addPage(Page page) throws IOException {
        PageReference pageReference = page.getPageReference();
        pagesReference.add(pageReference);

        serializationManager.serializePage(page);
    }

    private void removePage(Page page) {
        PageReference pageReference = page.getPageReference();
        pagesReference.remove(pageReference);

        File pageFile = new File(pageReference.getPagePath());
        Utils.deleteFolder(pageFile);
    }

    // returns page where this clusterKeyValue is between min and max
    private int getPageIndex(Comparable clusterKeyValue) {
        int index = Utils.binarySearch(pagesReference, clusterKeyValue);
        if (index < 0) // If not found, get page index where it would be the new min
            index = Utils.getInsertionIndex(index);

        return index;
    }

    private void shiftTuplesNext(Page currentPage, Page nextPage, int numShifts) throws DBAppException {
        int n = currentPage.getSize();
        for (int i = 0; i < numShifts && i < n; i++) {
            Object maxClusterKey = currentPage.getMax();
            Tuple tuple = currentPage.removeTuple(maxClusterKey);
            nextPage.addTuple(tuple);
        }
    }
    // difference between previous and next is min, max clusterKey
    private void shiftTuplesPrevious(Page currentPage, Page previousPage, int numShifts) throws DBAppException {
        int n = currentPage.getSize();
        for (int i = 0; i < numShifts && i < n; i++) {
            Object minClusterKey = currentPage.getMin();
            Tuple tuple = currentPage.removeTuple(minClusterKey);
            previousPage.addTuple(tuple);
        }
    }



    public String getPagePath(int pageIndex) {
        PageReference pageReference = (PageReference) pagesReference.get(pageIndex);
        String pagePath = pageReference.getPagePath();

        return pagePath;
    }

//    public Enumeration<Comparable> getPagesReference() {
//        return pagesReference.elements();
//    }

    public PageReference getPageReference(int pageIndex) {
        return (PageReference) pagesReference.get(pageIndex);
    }

    public String getTableName() {
        return tableName;
    }

    public String getClusterKeyName() {
        return clusterKeyName;
    }

    public int getPagesCount() {
        return pagesReference.size();
    }

    public boolean isFull() throws IOException {
        return rowsCount >= Utils.getMaxRowsCountInPage() * getPagesCount();
    }

    public int getRowsCount() {
        return rowsCount;
    }

    public void setSerializationManager(SerializationManager serializationManager) {
        this.serializationManager = serializationManager;
    }
}

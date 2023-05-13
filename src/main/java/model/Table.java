package model;

import exceptions.DBAppException;
import exceptions.DBNotFoundException;
import exceptions.DBQueryException;
import model.Page.Page;
import model.Page.PageReference;
import utils.SerializationManager;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private final Vector<PageReference> pagesReference;
    private final Vector<Index> indices;
    private final String tableName;
    private final String clusterKeyName;
    private int size;

    public Table(String tableName, String clusterKeyName) {
        this.pagesReference = new Vector<>();
        this.indices = new Vector<>();
        this.tableName = tableName;
        this.clusterKeyName = clusterKeyName;
        this.size = 0;

        String pagesFolder = Utils.getPageFolderPath(tableName);
        String IndexFolder = Utils.getIndexFolderPath(tableName);
        Utils.createFolder(pagesFolder);
        Utils.createFolder(IndexFolder);
    }

    public void insertTuple(Tuple tuple) throws DBAppException, IOException {
        if (this.getPagesCount() == 0 || this.isFull()) // If no pages exist OR table is full
            addPage(new Page(this.tableName, getPagesCount()));

        Object clusterKeyValue = tuple.getClusterKeyValue();
        int index = Utils.binarySearch(this.pagesReference, clusterKeyValue);
        int pageIndex = getInsertionPageIndex(index);

        PageReference pageRef = getPageReference(pageIndex);
        Page page = SerializationManager.deserializePage(getTableName(), pageRef);
        page.insertTuple(tuple);

        SerializationManager.serializePage(page);

        this.size++;
        arrangePages();
    }

    public void deleteTuples(Hashtable<String, Object> htblColNameValue) throws DBAppException, IOException {
        Page page;
        for (PageReference pageRef : pagesReference) {
            page = SerializationManager.deserializePage(getTableName(), pageRef);

            Vector<Tuple> toDelete = matchesCriteria(page, htblColNameValue);

            for (Tuple tuple : toDelete) {
                page.deleteTuple(tuple);
                this.size--;
            }

            SerializationManager.serializePage(page);
        }

        arrangePages();
    }

    public void updateTuple(Object clusterKeyValue, Hashtable<String, Object> htblColNameValue) throws DBAppException, IOException {
        int pageIndex = Utils.binarySearch(this.pagesReference, clusterKeyValue);
        if (pageIndex < 0)
            throw new DBNotFoundException("Tuple does not exist");

        PageReference pageRef = getPageReference(pageIndex);
        Page page = SerializationManager.deserializePage(getTableName(), pageRef);

        Tuple tuple = page.findTuple(clusterKeyValue);
        for (String key : htblColNameValue.keySet()) {
            if (key.equals(this.clusterKeyName))
                throw new DBQueryException("Cannot update cluster key value");
            tuple.setColValue(key, htblColNameValue.get(key));
        }

//        page.updateTuple(tuple);

        SerializationManager.serializePage(page);
    }


    public Iterator selectTuples(Map<String, Object> htblColNameValue, String[] compareOperators, String[] strarrOperators) throws DBNotFoundException, IOException {
        List<Tuple> tuples = new Vector<>();
        Page page;
        for (PageReference pageRef : pagesReference) {
            page = SerializationManager.deserializePage(getTableName(), pageRef);
            for (int j = 0; j < page.getSize(); j++) {
                Tuple tuple = page.getTuple(j);

                Boolean[] matches = new Boolean[htblColNameValue.size()];
                Arrays.fill(matches, true);

                Object[] keySet = htblColNameValue.keySet().toArray();
                for (int k = 0; k < htblColNameValue.size(); k++) {
                    String key = (String) keySet[k];
                    if (!compareOperators(tuple.getColValue(key), htblColNameValue.get(key), compareOperators[k]))
                        matches[k] = false;
                }

                if (getBetweenOperators(matches, strarrOperators)) // compare between each two conditions and get final result
                    tuples.add(tuple);
            }
        }
        return tuples.iterator();
    }
    
    public void createIndex(String[] ColNames, Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        this.indices.add(new Index(ColNames, min, max));
        
    }

    private Vector<Tuple> matchesCriteria(Page page, Hashtable<String, Object> htblColNameValue) throws DBAppException {
        Vector<Tuple> toDelete = new Vector<>();
        Tuple tuple;
        for (int j = 0; j < page.getSize(); j++) {
            tuple = page.getTuple(j);

            boolean matches = true;
            for (String key : htblColNameValue.keySet())
                if (!tuple.getColValue(key).equals(htblColNameValue.get(key)))
                    matches = false;

            if (matches)
                toDelete.add(tuple);
        }

        return toDelete;
    }

    // returns page where this clusterKeyValue is between min and max
    private int getInsertionPageIndex(int index) {
        if (index < 0) // If not between any page's min-max, get page index where it would be the new min
            index = Utils.getInsertionIndex(index);
        if (index > getPagesCount() - 1) // If index is out of bounds (clusterKeyValue is greatest)
            index = getPagesCount() - 1;

        return index;
    }

    private void insertIntoIndex() {

    }

    private void removeFromIndex() {

    }

    private void arrangePages() throws IOException, DBAppException {
        distributePages();

        removeEmptyPages();
    }

    // It is guaranteed that there are enough pages to distribute tuples
    private void distributePages(/*int startIndex*/) throws IOException, DBAppException {
        int n = this.getPagesCount();
        for (int i = 0; i < n - 1; i++) {
            PageReference currPageRef = getPageReference(i);
            PageReference nextPageRef = getPageReference(i + 1);

            if (currPageRef.isOverflow()) { // Shift 1 tuple to next page
                int numShifts = 1;
                shiftTuplesTo(currPageRef, nextPageRef, numShifts);
            }
            if (!currPageRef.isFull() && !nextPageRef.isEmpty()) { // Shift tuples from next page to current page to fill space
                int numShifts = currPageRef.getEmptySpace();
                shiftTuplesTo(nextPageRef, currPageRef, numShifts);
            }
        }
    }

    private void removeEmptyPages() throws IOException {
        int n = getPagesCount();
        for (int i = 0; i < n; i++) {
            PageReference currPageRef = getPageReference(i);
            if (currPageRef.isEmpty())
                removePage(currPageRef);
        }
    }

    private void shiftTuplesTo(PageReference fromPageRef, PageReference toPageRef, int numShifts) throws DBAppException, IOException {
        Page fromPage = SerializationManager.deserializePage(this.tableName, fromPageRef);
        Page toPage = SerializationManager.deserializePage(this.tableName, toPageRef);

        Tuple tuple;
        int n = fromPage.getSize();
        for (int i = 0; i < numShifts && i < n; i++) {
            if (toPage.getPageIndex() > fromPage.getPageIndex())
                tuple = fromPage.getMaxTuple();
            else
                tuple = fromPage.getMinTuple();
            // update pageIndex in index
            fromPage.deleteTuple(tuple);
            toPage.insertTuple(tuple);
        }

        SerializationManager.serializePage(fromPage);
        SerializationManager.serializePage(toPage);
    }

    private boolean compareOperators(Object t, Object o, String operator) {
        Comparable t1 = (Comparable) t;

        switch (operator) {
            case ">":
                return t1.compareTo(o) > 0;
            case ">=":
                return t1.compareTo(o) >= 0;
            case "<":
                return t1.compareTo(o) < 0;
            case "<=":
                return t1.compareTo(o) <= 0;
            case "=":
                return t1.compareTo(o) == 0;
            case "!=":
                return t1.compareTo(o) != 0;
            default:
                return false;
        }
    }

    private boolean getBetweenOperators(Boolean[] betweenOperators, String[] operators) {
        Boolean result = betweenOperators[0];
        for (int i = 1; i < operators.length; i++) {
            String operator = operators[i];

            if (operator.equals("AND"))
                result = result && betweenOperators[i];
            else if (operator.equals("OR"))
                result = result || betweenOperators[i];
            else if (operator.equals("XOR"))
                result = result ^ betweenOperators[i];
            else
                return false;
        }

        return result;
    }


    private void addPage(Page page) throws IOException {
        PageReference pageReference = page.getPageReference();
        this.pagesReference.add(pageReference);

        SerializationManager.serializePage(page);
    }

    private void removePage(PageReference pageReference) throws IOException {
        this.pagesReference.remove(pageReference);

        File pageFile = new File(pageReference.getPagePath());
        Utils.deleteFolder(pageFile);
    }


    public String getPagePath(int pageIndex) {
        PageReference pageReference = (PageReference) this.pagesReference.get(pageIndex);
        String pagePath = pageReference.getPagePath();

        return pagePath;
    }

    public PageReference getPageReference(int pageIndex) {
        return this.pagesReference.get(pageIndex);
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getClusterKeyName() {
        return this.clusterKeyName;
    }

    public int getPagesCount() {
        return this.pagesReference.size();
    }

    public int getSize() {
        return this.size;
    }

    public boolean isFull() throws IOException {
        return this.size >= Utils.getMaxRowsCountInPage() * getPagesCount();
    }
}

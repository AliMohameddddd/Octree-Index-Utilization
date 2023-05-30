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

    public void insertTuple(Tuple tuple) throws DBAppException {
        if (this.getPagesCount() == 0 || this.isFull()) // If no pages exist OR table is full
            addPage(new Page(this.tableName, getPagesCount()));

        Object clusterKeyValue = tuple.getClusterKeyValue();
        int index = Utils.binarySearch(this.pagesReference, clusterKeyValue);
        int pageIndex = getInsertionPageIndex(index);

        PageReference pageRef = getPageReference(pageIndex);
        Page page = SerializationManager.deserializePage(getTableName(), pageRef);

        page.insertTuple(tuple);

        this.insertIntoIndices(tuple, pageIndex);

        SerializationManager.serializePage(page);

        this.size++;
        arrangePages();
    }

    public void deleteTuples(Hashtable<String, Object> htblColNameValue) throws DBAppException {
        // Conditions are ANDed together
        String[] logicalOperators = new String[htblColNameValue.size()];
        Arrays.fill(logicalOperators, "AND");

        // compareOperator is always "="
        String[] compareOperators = new String[htblColNameValue.size()];
        Arrays.fill(compareOperators, "=");

        Vector<PageReference> newPagesReference = new Vector<>();
        if (this.canUseIndex(htblColNameValue.keySet().toArray(new String[0]), logicalOperators)) {
            Index index = this.getIndex(htblColNameValue.keySet().toArray(new String[0]));

            HashSet<Integer> pages = index.getPagesIndex(htblColNameValue);
            for (Integer pageIndex : pages)
                newPagesReference.add(this.pagesReference.get(pageIndex));
        } else
            newPagesReference = this.pagesReference;

        Page page;
        for (PageReference pageRef : newPagesReference) {
            page = SerializationManager.deserializePage(getTableName(), pageRef);
            int pageIndex = page.getPageIndex();
            for (int i = 0; i < page.getSize(); i++) {
                Tuple tuple = page.getTuple(i);

                Boolean[] conditionsBool = tuple.AreConditionsSatisfied(htblColNameValue, compareOperators);

                if (tuple.isTermSatisfied(conditionsBool, logicalOperators)) {
                    page.deleteTuple(tuple);
                    this.removeFromIndex(tuple, pageIndex);
                    this.size--;
                }
            }
            SerializationManager.serializePage(page);
        }

        arrangePages();
    }

    public Iterator selectTuples(Hashtable<String, Object> min, Hashtable<String, Object> max, Map<String, Object> htblColNameValue, String[] compareOperators, String[] logicalOperators) throws DBAppException {
        Vector<PageReference> newPagesReference = new Vector<>();
        boolean flag = false;
        for (String compareOperator : compareOperators)
            if (!compareOperator.equals("!=")) {
                flag = true;
                break;
            }

        if (this.canUseIndex(htblColNameValue.keySet().toArray(new String[0]), logicalOperators) && !flag) {
            Index index = this.getIndex(htblColNameValue.keySet().toArray(new String[0]));

            HashSet<Integer> pages = index.getPagesIndex(min, max);
            for (Integer pageIndex : pages)
                newPagesReference.add(this.pagesReference.get(pageIndex));
        } else
            newPagesReference = this.pagesReference;

        List<Tuple> tuples = new Vector<>();
        Page page;
        for (PageReference pageRef : newPagesReference) {
            page = SerializationManager.deserializePage(getTableName(), pageRef);
            for (int j = 0; j < page.getSize(); j++) {
                Tuple tuple = page.getTuple(j);

                Boolean[] conditionsBool = tuple.AreConditionsSatisfied(htblColNameValue, compareOperators);

                if (tuple.isTermSatisfied(conditionsBool, logicalOperators))
                    tuples.add(tuple);
            }
        }
        return tuples.iterator();
    }


    public void updateTuple(Object clusterKeyValue, Hashtable<String, Object> htblColNameValue) throws DBAppException {
        int pageIndex = Utils.binarySearch(this.pagesReference, clusterKeyValue);
        if (pageIndex < 0)
            throw new DBNotFoundException("Tuple does not exist");

        PageReference pageRef = getPageReference(pageIndex);
        Page page = SerializationManager.deserializePage(getTableName(), pageRef);

        Tuple tuple = page.findTuple(clusterKeyValue);
        this.removeFromIndex(tuple, pageIndex);

        for (String key : htblColNameValue.keySet()) {
            if (key.equalsIgnoreCase(this.clusterKeyName))
                throw new DBQueryException("Cannot update cluster key value");
            tuple.setColValue(key, htblColNameValue.get(key));
        }

        this.insertIntoIndices(tuple, pageIndex);

        SerializationManager.serializePage(page);
    }


    public void createIndex(String[] ColNames, Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        Index index = new Index(ColNames, min, max);
        this.indices.add(index);

        // populate
        Page page;
        for (PageReference pageRef : pagesReference) {
            page = SerializationManager.deserializePage(getTableName(), pageRef);
            for (int j = 0; j < page.getSize(); j++) {
                Tuple tuple = page.getTuple(j);
                index.insertTuple(tuple, pageRef.getPageIndex());
            }
        }
    }

    public boolean canUseIndex(String[] colNames, String[] logicOperators) {
        Index index = getIndex(colNames);
        if (index == null) // if no index exists on these columns
            return false;
        if (Arrays.stream(logicOperators).allMatch(operator -> operator.equalsIgnoreCase("AND")))
            return true;

        // indexed columns must be ANDed together and in order from query columns
        ArrayList<String> indexColNames = new ArrayList<>(Arrays.asList(index.getColNames()));
        for (int i = 0; i < colNames.length; i++)
            if (indexColNames.contains(colNames[i].toLowerCase())) {
                indexColNames.remove(colNames[i]);
                for (int j = i + 1; !indexColNames.isEmpty(); j++) {
                    if (!indexColNames.contains(colNames[j].toLowerCase()) || !logicOperators[j - 1].equalsIgnoreCase("AND"))
                        return false;
                    indexColNames.remove(colNames[j]);
                }
            } else
                continue;

        return false;
    }

    private Index getIndex(String[] ColNames) {
        for (Index index : indices) {
            if (index.isIndexOn(ColNames))
                return index;
        }
        return null;
    }

    // returns page where this clusterKeyValue is between min and max
    private int getInsertionPageIndex(int index) {
        if (index < 0) // If not between any page's min-max, get page index where it would be the new min
            index = Utils.getInsertionIndex(index);
        if (index > getPagesCount() - 1) // If index is out of bounds (clusterKeyValue is greatest)
            index = getPagesCount() - 1;

        return index;
    }

    private void insertIntoIndices(Tuple tuple, int pageIndex) throws DBAppException {
        for (int i = 0; i < this.indices.size(); i++)
            indices.get(i).insertTuple(tuple, pageIndex);
    }

    private void removeFromIndex(Tuple tuple, int pageIndex) throws DBAppException {
        for (Index index : this.indices)
            index.deleteTuple(tuple, pageIndex);
    }

    private void updateIndex(Tuple tuple, int oldPageIndex, int newPageIndex) throws DBAppException {
        for (Index index : this.indices)
            index.updateTuplePageIndex(tuple, oldPageIndex, newPageIndex);
    }

    private void arrangePages() throws DBAppException {
        distributePages();

        removeEmptyPages();
    }

    // It is guaranteed that there are enough pages to distribute tuples
    private void distributePages(/*int startIndex*/) throws DBAppException {
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

    private void removeEmptyPages() {
        int n = getPagesCount();
        for (int i = 0; i < n; i++) {
            PageReference currPageRef = getPageReference(i);
            if (currPageRef.isEmpty())
                removePage(currPageRef);
        }
    }

    private void shiftTuplesTo(PageReference fromPageRef, PageReference toPageRef, int numShifts) throws DBAppException {
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

            this.updateIndex(tuple, fromPageRef.getPageIndex(), toPageRef.getPageIndex());
        }

        SerializationManager.serializePage(fromPage);
        SerializationManager.serializePage(toPage);
    }


    private void addPage(Page page) throws DBAppException {
        PageReference pageReference = page.getPageReference();
        this.pagesReference.add(pageReference);

        SerializationManager.serializePage(page);
    }

    private void removePage(PageReference pageReference) {
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

    public boolean isFull() throws DBQueryException {
        try {
            return this.size >= Utils.getMaxRowsCountInPage() * getPagesCount();
        } catch (IOException e) {
            throw new DBQueryException("Error while getting max rows count in page");
        }
    }
}
package Page;

import exceptions.DBAlreadyExistsException;
import exceptions.DBAppException;
import exceptions.DBNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import Utils.Utils;
import model.Tuple;

public class Page implements Serializable {
    private final Vector<Comparable> tuples; // allows only insertion of Tuple, Sorted by clusterKey
    private final String tableName;
    private final int pageIndex;
    private Object min;
    private Object max;
    private PageReference pageReference;

    public Page(String tableName, int pageIndex) {
        this.tableName = tableName;
        this.pageIndex = pageIndex;
        this.min = null;
        this.max = null;
        this.tuples = new Vector<>();

        this.pageReference = new PageReference(tableName, pageIndex);
    }


    public Tuple getTuple(Object clusterKeyValue) throws DBAppException {
        Comparable searchKey = (Comparable) clusterKeyValue; // contains only clusterKeyValue to be used in search
        int index = Utils.binarySearch(tuples, searchKey);

        if (index < 0)
            throw new DBNotFoundException("Tuple does not exist");

        return (Tuple) tuples.get(index);
    }

    public void addTuple(Tuple t) throws DBAppException {
        int index = Utils.binarySearch(tuples, t);
        if (index >= 0)
            throw new DBAlreadyExistsException("Tuple already exists");

        int insertIndex = Utils.getInsertionIndex(index);
        tuples.add(insertIndex, t);

        updateMinMax();
    }

    public void removeTuple(Tuple t) throws IOException {
        int index = Utils.binarySearch(tuples, t);
        if (index < 0)
            return;

        tuples.remove(index);

        updateMinMax();
    }

    public void updateTuple(Tuple t) throws DBAppException {
        int index = Utils.binarySearch(tuples, t);
        if (index < 0)
            throw new DBNotFoundException("Tuple does not exist");

        // No need to sort again, since updateTable will not update clusterKey
        tuples.set(index, t);

        updateMinMax();
    }

    public boolean isPageFull(Page page) throws IOException {
        return page.getSize() >= Utils.getMaxRowsCountInPage();
    }


    // Helper Methods
    public void updateMinMax() {
        int size = getSize();
        min = ((Tuple) tuples.get(0)).getClusterKeyValue();
        max = ((Tuple) tuples.get(size - 1)).getClusterKeyValue();

        pageReference.setMin(min);
        pageReference.setMax(max);
        pageReference.setSize(size);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }

    public int getSize() {
        return tuples.size();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public boolean isFull() throws IOException {
        return getSize() >= Utils.getMaxRowsCountInPage();
    }

    public String getTableName() {
        return tableName;
    }

    public PageReference getPageReference() {
        return pageReference;
    }
}

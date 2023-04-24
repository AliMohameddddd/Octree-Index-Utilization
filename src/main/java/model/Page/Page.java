package model.Page;

import exceptions.DBAlreadyExistsException;
import exceptions.DBAppException;
import exceptions.DBNotFoundException;
import model.Tuple;
import utils.Utils;

import java.util.Vector;

public class Page extends AbstractPage {
    private Vector<Comparable> tuples; // allows only Tuple, sorted by clusterKey
    private PageReference pageReference;

    public Page(String tableName, int pageIndex) {
        super(tableName, pageIndex);
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

    public void insertTuple(Tuple t) throws DBAppException {
        int index = Utils.binarySearch(tuples, t);
        if (index >= 0)
            throw new DBAlreadyExistsException("Tuple already exists");
        int insertionIndex = Utils.getInsertionIndex(index);

        tuples.add(insertionIndex, t);

        this.updateMinMax();
    }

    public Tuple removeTuple(Object clusterKeyValue) throws DBAppException {
        if (clusterKeyValue == null)
            throw new DBNotFoundException("Null clusterKeyValue");

//        Tuple t = Tuple.createInstance(clusterKeyValue); // contains only clusterKeyValue to be used in search
        int index = Utils.binarySearch(tuples, (Comparable) clusterKeyValue);
        if (index < 0)
            throw new DBNotFoundException("Tuple does not exist");

        Tuple t = (Tuple) tuples.get(index);
        tuples.remove(index);

        this.updateMinMax();

        return t;
    }

    public void updateTuple(Tuple t) throws DBAppException {
        int index = Utils.binarySearch(tuples, t);
        if (index < 0)
            throw new DBNotFoundException("Tuple does not exist");

        // No need to sort again, since updateTable will not update clusterKey
        tuples.set(index, t);

        this.updateMinMax();
    }

    // Helper Methods
    public void updateMinMax() {
        this.setSize(tuples.size());
        this.setMin(((Tuple) tuples.get(0)).getClusterKeyValue());
        this.setMax(((Tuple) tuples.get(this.getSize() - 1)).getClusterKeyValue());

        pageReference.setSize(this.getSize());
        pageReference.setMin(this.getMin());
        pageReference.setMax(this.getMax());
    }

    public PageReference getPageReference() {
        return this.pageReference;
    }
}

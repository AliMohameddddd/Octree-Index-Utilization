package model;

import exceptions.DBAppException;
import exceptions.DBDuplicateException;
import exceptions.DBNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import Utils.Utils;

public class Page implements Serializable {
    private final Vector<Tuple> tuples;
    private final int pageIndex;
    private Object min;
    private Object max;
    private int size;

    public Page(int pageIndex) {
        this.pageIndex = pageIndex;
        this.min = null;
        this.max = null;
        this.size = 0;
        this.tuples = new Vector<Tuple>();
    }

//    public void serilaize() throws IOException {
//        String filePath = "src/main/resources/data/pages/" + pageIndex + ".ser";
//        Properties prop = new Properties();
//        FileInputStream configPath = new FileInputStream("src/main/resources/DBApp.config");
//        prop.load(configPath);
//        int pageSize = Integer.parseInt(prop.getProperty("PageSizeinBytes"));
//        SerializationManager.serialize(this, filePath, pageSize);
//    }
//
//    public Page deserialize(int pageIndex) throws IOException {
//        String filePath = "src/main/resources/data/pages/" + pageIndex + ".ser";
//        return (Page) SerializationManager.deserialize(filePath);
//    }


    public Tuple getTuple(Object clusterKeyValue) throws DBAppException {
        Tuple t = Tuple.createInstance(clusterKeyValue); // contains only clusterKeyValue to be used in search
        int index = Utils.binarySearch(tuples, t);
        if (index < 0)
            throw new DBNotFoundException("Tuple does not exist");

        return tuples.get(index);
    }

    public void addTuple(Tuple t) throws DBAppException {
        int index = Utils.binarySearch(tuples, t);
        if (index >= 0)
            throw new DBDuplicateException("Tuple already exists");

        int insertIndex = Utils.getInsertionIndex(tuples, t);
        tuples.add(insertIndex, t);

        size++;
        updateMinMax();
    }

    public void removeTuple(Tuple t) throws IOException {
        int index = Utils.binarySearch(tuples, t);
        if (index < 0)
            return;

        tuples.remove(index);
        size--;

        if (size == 0) {
            min = null;
            max = null;
        }
        else
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


    // Helper Methods
    private void updateMinMax() {
        min = tuples.get(0).getClusterKeyValue();
        max = tuples.get(size - 1).getClusterKeyValue();
    }

    public Vector<Tuple> getTuples() {
        return tuples;
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
        return size;
    }
}

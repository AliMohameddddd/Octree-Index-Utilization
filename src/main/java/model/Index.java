package model;

import exceptions.DBAppException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class Index implements Serializable {
    private String indexName;
    private String[] colNames;
    private Octree root;

    public Index(String[] ColNames, Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        this.indexName = String.join("_", ColNames);
        this.colNames = ColNames;

        this.setRoot(min, max);
    }

    public void insertTuple(Tuple tuple, int pageIndex) throws DBAppException {
        Comparable x = (Comparable) tuple.getColValue(colNames[0]);
        Comparable y = (Comparable) tuple.getColValue(colNames[1]);
        Comparable z = (Comparable) tuple.getColValue(colNames[2]);

        root.insert(x, y, z, pageIndex);
    }

    public void deleteTuple(Tuple tuple, int pageIndex) throws DBAppException {
        Comparable x = (Comparable) tuple.getColValue(colNames[0]);
        Comparable y = (Comparable) tuple.getColValue(colNames[1]);
        Comparable z = (Comparable) tuple.getColValue(colNames[2]);

        root.remove(x, y, z, pageIndex);
    }

    public void updateTuplePageIndex(Tuple tuple, int oldPageIndex, int newPageIndex) {
        Comparable x = (Comparable) tuple.getColValue(colNames[0]);
        Comparable y = (Comparable) tuple.getColValue(colNames[1]);
        Comparable z = (Comparable) tuple.getColValue(colNames[2]);

        root.update(x, y, z, oldPageIndex, newPageIndex);
    }

    public HashSet<Integer> getPagesIndex(Hashtable<String, Object> min, Hashtable<String, Object> max) {
        Comparable x1 = (Comparable) min.get(colNames[0]);
        Comparable y1 = (Comparable) min.get(colNames[1]);
        Comparable z1 = (Comparable) min.get(colNames[2]);
        Comparable x2 = (Comparable) max.get(colNames[0]);
        Comparable y2 = (Comparable) max.get(colNames[1]);
        Comparable z2 = (Comparable) max.get(colNames[2]);

        return root.get(x1, y1, z1, x2, y2, z2);
    }

    public HashSet<Integer> getPagesIndex(Hashtable<String, Object> htblColNameValue) {
        Comparable x = (Comparable) htblColNameValue.get(colNames[0]);
        Comparable y = (Comparable) htblColNameValue.get(colNames[1]);
        Comparable z = (Comparable) htblColNameValue.get(colNames[2]);

        return root.get(x, y, z);
    }

    private void setRoot(Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        Comparable col1Min = (Comparable) min.get(colNames[0]);
        Comparable col1Max = (Comparable) max.get(colNames[0]);
        Comparable col2Min = (Comparable) min.get(colNames[1]);
        Comparable col2Max = (Comparable) max.get(colNames[1]);
        Comparable col3Min = (Comparable) min.get(colNames[2]);
        Comparable col3Max = (Comparable) max.get(colNames[2]);

        root = new Octree(col1Min, col2Min, col3Min, col1Max, col2Max, col3Max);
    }

    public boolean isIndexOn(String[] colNames) {
        List<String> inputColNames = Arrays.asList(colNames);
        if (inputColNames.containsAll(Arrays.asList(this.colNames)))
            return true;
        return false;
    }

    public String getIndexName() {
        return indexName;
    }

    public String[] getColNames() {
        return colNames;
    }
}

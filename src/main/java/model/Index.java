package model;

import exceptions.DBAppException;

import java.io.Serializable;
import java.util.*;

public class Index implements Serializable {
    private String indexName;
    private String[] ColNames;
    private Octree root;

    public Index(String[] ColNames, Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        this.indexName = String.join("_", ColNames);
        this.ColNames = ColNames;

        this.setRoot(min, max);
    }

    public void insertTuple(Tuple tuple, int pageIndex) throws DBAppException {
        Comparable x = (Comparable) tuple.getColValue(ColNames[0]);
        Comparable y = (Comparable) tuple.getColValue(ColNames[1]);
        Comparable z = (Comparable) tuple.getColValue(ColNames[2]);

        root.insert(x, y, z, pageIndex);
    }

    public void deleteTuple(Tuple tuple) throws DBAppException {
        Comparable x = (Comparable) tuple.getColValue(ColNames[0]);
        Comparable y = (Comparable) tuple.getColValue(ColNames[1]);
        Comparable z = (Comparable) tuple.getColValue(ColNames[2]);

        root.remove(x, y, z);
    }

    public void updateTuplePageIndex(Tuple tuple, int oldPageIndex, int newPageIndex) throws DBAppException {
        Comparable x = (Comparable) tuple.getColValue(ColNames[0]);
        Comparable y = (Comparable) tuple.getColValue(ColNames[1]);
        Comparable z = (Comparable) tuple.getColValue(ColNames[2]);

        root.update(x, y, z, oldPageIndex, newPageIndex);
    }

    public Vector<Comparable[]> getPagesIndex(Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        Comparable x1 = (Comparable) min.get(ColNames[0]);
        Comparable y1 = (Comparable) min.get(ColNames[1]);
        Comparable z1 = (Comparable) min.get(ColNames[2]);
        Comparable x2 = (Comparable) max.get(ColNames[0]);
        Comparable y2 = (Comparable) max.get(ColNames[1]);
        Comparable z2 = (Comparable) max.get(ColNames[2]);

        return root.get(x1, y1, z1, x2, y2, z2);
    }

    public HashSet<Integer> getPagesIndex(Hashtable<String, Object> htblColNameValue) {
        Comparable x = (Comparable) htblColNameValue.get(ColNames[0]);
        Comparable y = (Comparable) htblColNameValue.get(ColNames[1]);
        Comparable z = (Comparable) htblColNameValue.get(ColNames[2]);

        return root.get(x, y, z);
    }


    private void setRoot(Hashtable<String, Object> min, Hashtable<String, Object> max) throws DBAppException {
        Comparable col1Min = (Comparable) min.get(ColNames[0]);
        Comparable col1Max = (Comparable) max.get(ColNames[0]);
        Comparable col2Min = (Comparable) min.get(ColNames[1]);
        Comparable col2Max = (Comparable) max.get(ColNames[1]);
        Comparable col3Min = (Comparable) min.get(ColNames[2]);
        Comparable col3Max = (Comparable) max.get(ColNames[2]);

        root = new Octree(col1Min, col1Max, col2Min, col2Max, col3Min, col3Max);
    }

    public String getIndexName() {
        return indexName;
    }
    
    public String[] getColNames() {
        return ColNames;
    }
}

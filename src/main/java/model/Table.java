package model;

import java.io.Serializable;
import java.util.Vector;

public class Table implements Serializable {
    private String strTableName;
    private String strClusteringKeyColumn;
    private int numberOfPages;
    private Vector<Page> pages;

    public Table(String strTableName, String strClusteringKeyColumn) {
        this.strTableName = strTableName;
        this.strClusteringKeyColumn = strClusteringKeyColumn;
        this.pages = new Vector<Page>();
    }

}

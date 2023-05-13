package model.Page;

import exceptions.DBAppException;
import exceptions.DBQueryException;
import utils.Utils;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractPage implements Serializable {
    private String tableName;
    private int pageIndex; // starts from 0
    private Object min;
    private Object max;
    private int size;

    public AbstractPage(String tableName, int pageIndex) {
        this.tableName = tableName;
        this.pageIndex = pageIndex;
        this.size = 0;
    }

    public String getTableName() {
        return tableName;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public String getPagePath() {
        return Utils.getPageFilePath(tableName, pageIndex);
    }

    public Object getMin() {
        return min;
    }

    void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    void setMax(Object max) {
        this.max = max;
    }

    public int getSize() {
        return size;
    }

    void setSize(int size) {
        this.size = size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean isFull() throws DBAppException {
        try {
            return this.size == Utils.getMaxRowsCountInPage();
        } catch (IOException e) {
            throw new DBQueryException("Failed to get max rows count in page");
        }
    }

    public boolean isOverflow() throws DBAppException {
        try {
            return this.size > Utils.getMaxRowsCountInPage();
        } catch (IOException e) {
            throw new DBQueryException("Failed to get max rows count in page");
        }
    }

    public int getEmptySpace() throws DBAppException {
        try {
            return Utils.getMaxRowsCountInPage() - getSize();
        } catch (IOException e) {
            throw new DBQueryException("Failed to get max rows count in page");
        }
    }

}

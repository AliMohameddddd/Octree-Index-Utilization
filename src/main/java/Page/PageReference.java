package Page;

import Utils.Utils;

// implements Comparable to be able to use binarySearch
public class PageReference implements Comparable {
    private String tableName;
    private int pageIndex;
    private Object min;
    private Object max;
    private int size;

    public PageReference(String tableName, int pageIndex) {
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

    //Access modifier is protected to prevent setting min and max from outside the package
    protected void setMin(Object min) {
        this.min = min;
    }

    public Object getMax() {
        return max;
    }

    //Access modifier is protected to prevent setting min and max from outside the package
    protected void setMax(Object max) {
        this.max = max;
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof PageReference) {
            Comparable thisValue = (Comparable) this.getMin();
            Comparable otherValue = (Comparable) ((PageReference) o).getMin();

            return thisValue.compareTo(otherValue);
        }
        else {
            // If o is not a PageReference, it is a clusterKeyValue
            Comparable thisValueMin = (Comparable) this.getMin();
            Comparable thisValueMax = (Comparable) this.getMax();
            Comparable otherValue = (Comparable) o;

            // If otherValue is between min and max
            if (thisValueMin.compareTo(otherValue) <= 0 && thisValueMax.compareTo(otherValue) >= 0)
                return 0;
            else if (thisValueMin.compareTo(otherValue) > 0)
                return 1;
            else
                return -1;
        }

    }

}


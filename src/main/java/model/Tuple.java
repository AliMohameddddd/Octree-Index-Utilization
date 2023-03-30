package model;

import java.util.Hashtable;

public class Tuple implements Comparable<Tuple> {
    private final String clusterKeyName;
    private Hashtable<String, Object> htblColNameValue;

    public Tuple(String clusterKeyName, Hashtable<String, Object> htblColNameValue) {
        this.clusterKeyName = clusterKeyName;
        this.htblColNameValue = htblColNameValue;
    }

    public String getClusterKeyName() {
        return clusterKeyName;
    }

    public Hashtable<String, Object> getHtblColNameValue() {
        return htblColNameValue;
    }

    public Object getClusterKeyValue() {
        return htblColNameValue.get(clusterKeyName);
    }

    @Override
    public int compareTo(Tuple o) {
        Comparable thisValue = (Comparable) this.getClusterKeyValue();
        Comparable otherValue = (Comparable) o.getClusterKeyValue();
        return thisValue.compareTo(otherValue);
    }

    // Based on some column name
    public int compareTo(Tuple o, String colName) {
        Comparable thisValue = (Comparable) this.htblColNameValue.get(colName);
        Comparable otherValue = (Comparable) o.getHtblColNameValue().get(colName);
        return thisValue.compareTo(otherValue);
    }

    // Creates a tuple with clusterKeyValue as needed to help sort based on ClusterKeyColumn using compareTo
    public static Tuple createInstance(Object clusterKeyValue) {
        Hashtable<String, Object> htblColNameValue = new Hashtable<>();
        htblColNameValue.put("whateverKey", clusterKeyValue);

        return new Tuple("whateverKey", htblColNameValue);

    }

}

package model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class Tuple implements Comparable, Serializable {
    private final String clusterKeyName;
    private final Hashtable<String, Object> htblColNameValue;

    public Tuple(String clusterKeyName, Hashtable<String, Object> htblColNameValue) {
        this.clusterKeyName = clusterKeyName;
        this.htblColNameValue = htblColNameValue;
    }

    public Set<String> getColNames() {
        return htblColNameValue.keySet();
    }

    public String getClusterKeyName() {
        return clusterKeyName;
    }

    public Object getColValue(String colName) {
        return htblColNameValue.get(colName);
    }

    public void setColValue(String colName, Object value) {
        htblColNameValue.put(colName, value);
    }

    public Object getClusterKeyValue() {
        return htblColNameValue.get(clusterKeyName);
    }

    public String toString() {
        String s = "";
        for (String colName : htblColNameValue.keySet())
            s += colName + ": " + htblColNameValue.get(colName) + ", ";
        return s;
    }

    public Boolean[] AreConditionsSatisfied(Map<String, Object> htblColNameValue, String[] compareOperators) {
        Boolean[] bool = new Boolean[htblColNameValue.size()];

        String[] keySet = htblColNameValue.keySet().toArray(new String[0]);
        for (int i = 0; i < htblColNameValue.size(); i++) {
            String colName = keySet[i];
            Comparable thisValue = (Comparable) this.getColValue(colName);
            Object otherValue = htblColNameValue.get(colName);
            String operator = compareOperators[i];

            int compare = thisValue.compareTo(otherValue);
            bool[i] = getCompareResult(compare, operator);
        }
        return bool;
    }

    public Boolean isTermSatisfied(Boolean[] conditions, String[] logicalOperators) {
        Boolean result = conditions[0];
        for (int i = 1; i < conditions.length; i++) {
            String operator = logicalOperators[i - 1].toUpperCase();
            switch (operator) {
                case "AND":
                    result = result && conditions[i];
                    break;
                case "OR":
                    result = result || conditions[i];
                    break;
                case "XOR":
                    result = result ^ conditions[i];
                    break;
            }
        }
        return result;
    }


    private boolean getCompareResult(int compare, String operator) {
        switch (operator) {
            case ">":
                return compare > 0;
            case ">=":
                return compare >= 0;
            case "<":
                return compare < 0;
            case "<=":
                return compare <= 0;
            case "=":
                return compare == 0;
            case "!=":
                return compare != 0;
        }
        return false;
    }


    @Override
    public int compareTo(Object o) {
        Comparable thisValue = (Comparable) getClusterKeyValue();
        Object otherValue;

        if (o instanceof Tuple)
            otherValue = ((Tuple) o).getClusterKeyValue();
        else
            // If o is not a tuple, it is a clusterKeyValue
            otherValue = o;

        return thisValue.compareTo(otherValue);
    }

}
import exceptions.*;
import model.Page.Page;
import model.Page.PageReference;
import model.SQLTerm;
import model.Table;
import model.Tuple;
import utils.MetaDataManager;
import utils.SerializationManager;
import utils.Validation;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class DBApp {
    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();
        dbApp.init();

        String strTableName = "Student";
        String strClusteringKeyColumn = "id";

        Hashtable htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("id", "java.lang.Integer");
        htblColNameType.put("name", "java.lang.String");
        htblColNameType.put("gpa", "java.lang.double");
        htblColNameType.put("phone", "java.lang.Integer");
        Hashtable htblColNameMin = new Hashtable<String, String>();
        htblColNameMin.put("id", "0");
        htblColNameMin.put("name", "A");
        htblColNameMin.put("gpa", "0.0");
        htblColNameMin.put("phone", "0");
        Hashtable htblColNameMax = new Hashtable<String, String>();
        htblColNameMax.put("id", "1000000000");
        htblColNameMax.put("name", "Z");
        htblColNameMax.put("gpa", "4.0");
        htblColNameMax.put("phone", "1000000000");


        Hashtable htblColNameValue = new Hashtable<String, Object>();
        htblColNameValue.put("id", 23873);
        htblColNameValue.put("gpa", 0.95);
        htblColNameValue.put("name", "Alaa");
        htblColNameValue.put("phone", 110);

        Hashtable htblColNameValue2 = new Hashtable<String, Object>();
        htblColNameValue2.put("id", 1);
        htblColNameValue2.put("name", "Alaa");
        htblColNameValue2.put("gpa", 1.95);
        htblColNameValue2.put("phone", 110111);

        Hashtable htblColNameValue3 = new Hashtable<String, Object>();
        htblColNameValue3.put("id", 2);
        htblColNameValue3.put("name", "Alaa");
        htblColNameValue3.put("gpa", 1.5);
        htblColNameValue3.put("phone", 110111);


        Hashtable updatedHtblColNameValue = new Hashtable<String, Object>();
        updatedHtblColNameValue.put("gpa", 1.95);

        SQLTerm[] arrSQLTerms;
        arrSQLTerms = new SQLTerm[3];
        arrSQLTerms[0] = new SQLTerm("Student", "name", "=", "Alaa");
        arrSQLTerms[1] = new SQLTerm("Student", "gpa", "=", 1.95);
        arrSQLTerms[2] = new SQLTerm("Student", "phone", "=", 110111);

        String[] strarrOperators = new String[2];
        strarrOperators[0] = "AND";
        strarrOperators[1] = "OR";
        try {
            dbApp.createTable(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);

            dbApp.createIndex(strTableName, new String[]{"gpa", "name", "phone"});

            dbApp.insertIntoTable(strTableName, htblColNameValue);
            dbApp.insertIntoTable(strTableName, htblColNameValue2);
            dbApp.insertIntoTable(strTableName, htblColNameValue3);
//            printTable(strTableName);

            dbApp.deleteFromTable(strTableName, htblColNameValue);
//            printTable(strTableName);

            dbApp.updateTable(strTableName, "2", updatedHtblColNameValue);
//            printTable(strTableName);


            Iterator iterator = dbApp.selectFromTable(arrSQLTerms, strarrOperators);
            printIterator(iterator);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printIterator(Iterator iterator) {
        while (iterator.hasNext())
            System.out.println(iterator.next());
    }

    public static void printTable(String tableName) throws DBAppException {
        Table table = SerializationManager.deserializeTable(tableName);

        System.out.println("Table: " + table.getTableName());
        System.out.println("Cluster Key: " + table.getClusterKeyName());
        System.out.println("Pages Count: " + table.getPagesCount());
        System.out.println("Size: " + table.getSize());
        System.out.println("Pages:-");

        Page page;
        for (int i = 0; i < table.getPagesCount(); i++) {
            PageReference pageRef = table.getPageReference(i);
            page = SerializationManager.deserializePage(table.getTableName(), pageRef);

            System.out.print(page);
        }

    }

    // this does whatever initialization you would like
    // or leave it empty if there is no code you want to
    // execute at application startup
    public void init() throws DBAppException {
        try {
            SerializationManager.createTablesFolder();
            MetaDataManager.createMetaDataFolder();
        } catch (IOException e) {
            throw new DBQueryException("Error creating folders");
        }
    }

    // following method creates one table only
    // strClusteringKeyColumn is the name of the column that will be the primary
    // key and the clustering column as well. The data type of that column will
    // be passed in htblColNameType
    // htblColNameValue will have the column name as key and the data
    // type as value
    // htblColNameMin and htblColNameMax for passing minimum and maximum values
    // for data in the column. Key is the name of the column
    public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType,
                            Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException {

        if (Validation.isTableExists(strTableName))
            throw new DBAlreadyExistsException("Table already exists");
        if (!htblColNameType.containsKey(strClusteringKeyColumn.toLowerCase()))
            throw new DBSchemaException("Clustering key does not exist");
        if (!htblColNameType.keySet().equals(htblColNameMin.keySet()) || !htblColNameType.keySet().equals(htblColNameMax.keySet()))
            throw new DBSchemaException("Columns' names do not match");
        if (!Validation.areAllowedDataTypes(htblColNameType))
            throw new DBSchemaException("Invalid data type");
        if (!Validation.validateMinMax(htblColNameType, htblColNameMin, htblColNameMax))
            throw new DBSchemaException("min, max types do not match schema OR min > max");

        MetaDataManager.createTableMetaData(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);

        Table table = new Table(strTableName, strClusteringKeyColumn);

        SerializationManager.serializeTable(table);
    }

    // following method creates an octree
    // depending on the count of column names passed.
    // If three column names are passed, create an octree.
    // If only one or two column names is passed, throw an Exception.
    public void createIndex(String strTableName, String[] strarrColName) throws DBAppException {
        if (!Validation.isTableExists(strTableName))
            throw new DBNotFoundException("Table do not exist");
        if (strarrColName.length != 3)
            throw new DBQueryException("Invalid number of columns to be indexed");

        Hashtable<String, Hashtable<String, String>> htblColNameMetaData = MetaDataManager.getMetaData(strTableName);
        if (!htblColNameMetaData.containsKey(strarrColName[0].toLowerCase())
                || !htblColNameMetaData.containsKey(strarrColName[1].toLowerCase()) || !htblColNameMetaData.containsKey(strarrColName[2].toLowerCase()))
            throw new DBSchemaException("Column names do not match table schema");

        Table table = SerializationManager.deserializeTable(strTableName);

        String[] colNames = htblColNameMetaData.keySet().toArray(new String[0]);
        Hashtable<String, Object> min = new Hashtable<>();
        Hashtable<String, Object> max = new Hashtable<>();
        for (String colName : colNames) {
            String type = htblColNameMetaData.get(colName).get("ColumnType");
            Object minValue = Validation.getComparable(htblColNameMetaData.get(colName).get("Min"), type);
            Object maxValue = Validation.getComparable(htblColNameMetaData.get(colName).get("Max"), type);

            min.put(colName, minValue);
            max.put(colName, maxValue);
        }

        table.createIndex(strarrColName, min, max);

        MetaDataManager.createIndex(strTableName, strarrColName);

        SerializationManager.serializeTable(table);
    }

    // following method inserts one row only.
    // htblColNameValue must include a value for the primary key
    public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
        if (!Validation.isTableExists(strTableName))
            throw new DBNotFoundException("Table do not exist");

        Hashtable<String, Hashtable<String, String>> htblColNameMetaData = MetaDataManager.getMetaData(strTableName);
        if (!htblColNameValue.keySet().equals(htblColNameMetaData.keySet()))
            throw new DBSchemaException("Column names do not match table schema");
        if (!Validation.validateSchema(htblColNameValue, htblColNameMetaData))
            throw new DBSchemaException("Columns metadata do not match table schema");

        Table table = SerializationManager.deserializeTable(strTableName);

        String clusterKeyName = table.getClusterKeyName();
        Tuple tuple = new Tuple(clusterKeyName, htblColNameValue);

        table.insertTuple(tuple);

        SerializationManager.serializeTable(table);
    }

    // following method updates one row only
    // htblColNameValue holds the key and new value
    // htblColNameValue will not include clustering key as column name
    // strClusteringKeyValue is the value to look for to find the row to update.
    public void updateTable(String strTableName, String strClusteringKeyValue,
                            Hashtable<String, Object> htblColNameValue) throws DBAppException {

        if (!Validation.isTableExists(strTableName))
            throw new DBNotFoundException("Table do not exist");

        Hashtable<String, Hashtable<String, String>> htblColNameMetaData = MetaDataManager.getMetaData(strTableName);
        if (!Validation.validateSchema(htblColNameValue, htblColNameMetaData))
            throw new DBSchemaException("Columns metadata do not match table schema");

        Hashtable<String, String> htblClusteringKeyMetaData = MetaDataManager.getClusteringKeyMetaData(htblColNameMetaData);
        if (htblClusteringKeyMetaData == null || !Validation.isNeededType(strClusteringKeyValue, htblClusteringKeyMetaData.get("ColumnType")))
            throw new DBSchemaException("Clustering type do not match schema");

        Object clusteringKeyValue = Validation.getComparable(strClusteringKeyValue, htblClusteringKeyMetaData.get("ColumnType"));
        if (!Validation.isMidValue(clusteringKeyValue, htblClusteringKeyMetaData.get("Min"), htblClusteringKeyMetaData.get("Max")))
            throw new DBSchemaException("Clustering value is not in the range");

        Table table = SerializationManager.deserializeTable(strTableName);

        table.updateTuple(clusteringKeyValue, htblColNameValue);

        SerializationManager.serializeTable(table);
    }

    // following method could be used to delete one or more rows.
    // htblColNameValue holds the key and value. This will be used in search
    // to identify which rows/tuples to delete.
    // htblColNameValue entries are ANDED together
    public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
        if (!Validation.isTableExists(strTableName))
            throw new DBNotFoundException("Table do not exist");

        Hashtable<String, Hashtable<String, String>> htblColNameMetaData = MetaDataManager.getMetaData(strTableName);
        if (!htblColNameValue.keySet().equals(htblColNameMetaData.keySet()))
            throw new DBSchemaException("Column names do not match table schema");
        if (!Validation.validateSchema(htblColNameValue, htblColNameMetaData))
            throw new DBSchemaException("Columns metadata do not match table schema");

        Table table = SerializationManager.deserializeTable(strTableName);

        table.deleteTuples(htblColNameValue);

        SerializationManager.serializeTable(table);
    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
        if (arrSQLTerms.length == 0)
            throw new DBSchemaException("No SQL terms passed");
        if (arrSQLTerms.length != strarrOperators.length + 1)
            throw new DBSchemaException("SQL terms and operators do not match");
        if (!Validation.areValidConditions(arrSQLTerms))
            throw new DBSchemaException("Invalid Conditions");
        if (!Validation.areValidLogicOperators(strarrOperators))
            throw new DBSchemaException("Invalid logic operators");

        String strTableName = arrSQLTerms[0]._strTableName;
        if (!Validation.isTableExists(arrSQLTerms[0]._strTableName))
            throw new DBNotFoundException("Table do not exist");

        Hashtable<String, Hashtable<String, String>> htblColNameMetaData = MetaDataManager.getMetaData(strTableName);
        LinkedHashMap<String, Object> htblColNameValue = new LinkedHashMap<>();
        String[] compareOperators = new String[arrSQLTerms.length];
        Hashtable<String, Object> htblMin = new Hashtable<>();
        Hashtable<String, Object> htblMax = new Hashtable<>();
        for (int i = 0; i < arrSQLTerms.length; i++) {
            SQLTerm term = arrSQLTerms[i];
            String colName = term._strColumnName;
            String type = htblColNameMetaData.get(colName).get("ColumnType");
            Comparable minDataType = Validation.getComparable(htblColNameMetaData.get(colName).get("Min"), type);
            Comparable maxDataType = Validation.getComparable(htblColNameMetaData.get(colName).get("Max"), type);

            Object min = minDataType;
            Object max = maxDataType;
            if (term._strOperator == "=") {
                min = term._objValue;
                max = term._objValue;
            }
            if (term._strOperator == ">")
                min = Validation.increment((Comparable) term._objValue);
            if (term._strOperator == ">=")
                min = term._objValue;
            if (term._strOperator == "<")
                max = Validation.decrement((Comparable) term._objValue);
            if (term._strOperator == "<=")
                max = term._objValue;


            htblMin.put(colName, min);
            htblMax.put(colName, max);
            htblColNameValue.put(term._strColumnName, term._objValue);
            compareOperators[i] = term._strOperator;
        }

        if (!Validation.validateSchema(htblColNameValue, htblColNameMetaData))
            throw new DBSchemaException("Columns metadata do not match table schema");

        Table table = SerializationManager.deserializeTable(strTableName);

        return table.selectTuples(htblMin, htblMax, htblColNameValue, compareOperators, strarrOperators);
    }

}

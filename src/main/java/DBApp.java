
import exceptions.DBAppException;
import model.MetaDataManager;
import model.SQLTerm;

import java.util.*;
import java.io.*;
import java.io.FileWriter;

public class DBApp {
    private static MetaDataManager metaDataManager;

    // this does whatever initialization you would like
    // or leave it empty if there is no code you want to
    // execute at application startup
    public static void init( ) throws IOException {
        metaDataManager = new MetaDataManager();
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
                            Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax ) throws DBAppException, IOException {
        // Todo: validate schema, check for duplicates, check for invalid data types using metadata

        metaDataManager.createTableMetaData(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);
    }

    // following method inserts one row only.
    // htblColNameValue must include a value for the primary key
    public void insertIntoTable(String strTableName,
                                Hashtable<String, Object> htblColNameValue) throws DBAppException {

        // Todo: validate schema, check for duplicates, check for invalid data types using metadata
        // Todo: check if the table exists

    }

    // following method updates one row only
    // htblColNameValue holds the key and new value
    // htblColNameValue will not include clustering key as column name
    // strClusteringKeyValue is the value to look for to find the row to update.
    public void updateTable(String strTableName, String strClusteringKeyValue,
                            Hashtable<String, Object> htblColNameValue) throws DBAppException {
        // Todo: validate schema, check for duplicates, check for invalid data types using metadata
        // Todo: check if the table exists

        // Todo: Cast strClusteringKeyValue to the correct type based on metadata


        // Table table = new Table(strTableName);
        // String ClusteringKey =table.getClusteringKeyName();
        //htblColNameValue.put(ClusteringKey, strClusteringKeyValue);
    }

    // following method could be used to delete one or more rows.
    // htblColNameValue holds the key and value. This will be used in search
    // to identify which rows/tuples to delete.
    // htblColNameValue enteries are ANDED together
    public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue)
                                    throws DBAppException {

    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
        return null;
    }


//    public static boolean Type(String x) {
//        if(x.equals("java.lang.String")) {
//            return true;
//        }else {
//            return false;
//        }
//    }



    public static void main(String[] args) {
        try {
            init();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String strTableName = "Student";
        DBApp dbApp = new DBApp();

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>( );
        htblColNameType.put("id", "java.lang.Integer");
        htblColNameType.put("name", "java.lang.String");
        htblColNameType.put("gpa", "java.lang.double");
        try {
            dbApp.createTable( strTableName, "id", htblColNameType, htblColNameType,htblColNameType);
        } catch (DBAppException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.print("data entered");
    }



}

package model;

import exceptions.DBAppException;
import exceptions.DBDuplicateException;
import exceptions.DBNotFoundException;

import java.io.*;
import java.util.Hashtable;

public class MetaDataManager {
    private static final String META_DATA_FOLDER = "src/main/resources/metadata/";

    // Delete all metadata files and create a new folder
    public MetaDataManager() throws IOException {
        File metaFile = new File(META_DATA_FOLDER);
        if (metaFile.exists())
            deleteFolder(metaFile);
        metaFile.mkdirs();

    }

    // Helper function to delete a folder
    private void deleteFolder(File f) {
        if (f.isDirectory())
            for (File c : f.listFiles())
                deleteFolder(c);
        if (!f.delete())
            throw new AssertionError("Failed to delete file: " + f);
    }


    public void createTableMetaData(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType,
                                    Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException, IOException {

        String tableMetaDataFile = META_DATA_FOLDER + strTableName + ".csv";
        if (new File(tableMetaDataFile).exists())
            throw new DBDuplicateException("Table MetaData already exists");

        FileWriter writer = new FileWriter(tableMetaDataFile, true);
        writer.write("TableName,ColumnName,ColumnType,ClusteringKey,IndexName,IndexType,Min,Max\n");

        // loop on all columns and write them to the file
        int numCols = htblColNameType.size();
        String[] ColNames = htblColNameType.keySet().toArray(new String[numCols]);
        for (int i = 0; i < numCols; i++) {
            String ColNameType = htblColNameType.get(ColNames[i]);
            String ColNameMin = htblColNameMin.get(ColNames[i]);
            String ColNameMax = htblColNameMax.get(ColNames[i]);

            writer.write(strTableName + "," + ColNames[i] + "," + ColNameType + "," + (ColNames.equals(strClusteringKeyColumn) ? "True" : "False")
                    + "," + "null" + "," + "null" + "," + ColNameMin + "," + ColNameMax + (i != numCols - 1 ? "\n" : ""));
        }
        writer.close();
    }


    // returns hashtable of String key for (column name)
    // and hashtable value that have all data about column (type, isClusteringKey, indexName, indexType, min, max)
    public Hashtable<String, Hashtable<String, String>> getMetaData(String strTableName)
            throws IOException, DBAppException {

        String tableMetaDataFile = META_DATA_FOLDER + strTableName + ".csv";
        if (!(new File(tableMetaDataFile).exists()))
            throw new DBNotFoundException("Table MetaData does not exist");

        // read the csv file and return the data
        FileReader fr = new FileReader(tableMetaDataFile);
        BufferedReader br = new BufferedReader(fr);
        Hashtable<String, Hashtable<String, String>> htblTableMetaData = new Hashtable<>();

        String[] header = br.readLine().split(",");
        while (br.ready()) {
            String[] colMetaData = br.readLine().split(",");

            Hashtable<String, String> htblColMetaData = getHtblColMetaData(header, colMetaData); // Column Name, Value

            String colName = colMetaData[1];
            htblTableMetaData.put(colName, htblColMetaData);
        }
        br.close();

        return htblTableMetaData;
    }


    // Helper function to get the metadata of a column
    private Hashtable<String, String> getHtblColMetaData(String[] header, String[] colMetaData)
            throws IOException, DBAppException {

        Hashtable<String, String> htblColMetaData = new Hashtable<>();
        for (int i = 0; i < header.length; i++)
            htblColMetaData.put(header[i], colMetaData[i]); // Example: htblColMetaData.put("ColumnType", colMetaData[2])

        return htblColMetaData;
    }


}

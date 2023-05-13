package utils;

import exceptions.DBAlreadyExistsException;
import exceptions.DBAppException;
import exceptions.DBNotFoundException;
import exceptions.DBQueryException;

import java.io.*;
import java.util.Hashtable;

public class MetaDataManager {
    private static final String META_DATA_FOLDER = "src/main/resources/metadata/";

    // Delete all metadata files and create a new folder
    public static void createMetaDataFolder() throws IOException {
        File metaFolder = new File(META_DATA_FOLDER);

        if (metaFolder.exists())
            Utils.deleteFolder(metaFolder);

        if (!metaFolder.mkdirs())
            throw new IOException("Failed to create metadata folder");
    }

    public static Hashtable<String, String> getClusteringKeyMetaData(Hashtable<String, Hashtable<String, String>> htblColNameMetaData) {
        for (Hashtable<String, String> htblColMetaData : htblColNameMetaData.values())
            if (htblColMetaData.get("ClusteringKey").equals("True"))
                return htblColMetaData;
        return null;
    }

    public static void createTableMetaData(String strTableName, String strClusteringKeyColumn, Hashtable<String, String> htblColNameType,
                                    Hashtable<String, String> htblColNameMin, Hashtable<String, String> htblColNameMax) throws DBAppException {
        try {
            String tableMetaDataFile = META_DATA_FOLDER + strTableName + ".csv";
            if (new File(tableMetaDataFile).exists())
                throw new DBAlreadyExistsException("Table MetaData already exists");

            FileWriter writer = new FileWriter(tableMetaDataFile, true);
            writer.write("TableName,ColumnName,ColumnType,ClusteringKey,IndexName,IndexType,Min,Max\n");

            // loop on all columns and write their metadata to the file
            int numCols = htblColNameType.size();
            String[] ColNames = htblColNameType.keySet().toArray(new String[numCols]);
            for (int i = 0; i < numCols; i++) {
                String ColNameType = htblColNameType.get(ColNames[i]);
                String ColNameMin = htblColNameMin.get(ColNames[i]);
                String ColNameMax = htblColNameMax.get(ColNames[i]);

                writer.write(strTableName + "," + ColNames[i] + "," + ColNameType + "," + (ColNames[i].equals(strClusteringKeyColumn) ? "True" : "False")
                        + "," + "null" + "," + "null" + "," + ColNameMin + "," + ColNameMax + (i != numCols - 1 ? "\n" : ""));
            }
            writer.close();
            System.out.println("Table MetaData created successfully ar " + tableMetaDataFile);
        } catch (IOException e) {
            throw new DBQueryException("Failed to create Table MetaData");
        }
    }

    // getMetaData
    // returns Hashtable of String key for (column name) and Hashtable value
    // that have all data about column (type, isClusteringKey, indexName, indexType, min, max) and values
    public static Hashtable<String, Hashtable<String, String>> getMetaData(String strTableName)
            throws DBAppException {
        try {
            String tableMetaDataFile = META_DATA_FOLDER + strTableName + ".csv";
            if (!(new File(tableMetaDataFile).exists()))
                throw new DBNotFoundException("Table MetaData does not exist");

            // read the csv file and return the data
            FileReader fr = new FileReader(tableMetaDataFile);
            BufferedReader br = new BufferedReader(fr);
            Hashtable<String, Hashtable<String, String>> htblTableMetaData = new Hashtable<>();

            // read the header and then insert into Hashtable (key: column name, value: Hashtable of column metadata)
            String[] header = br.readLine().split(",");
            while (br.ready()) {
                String[] colMetaData = br.readLine().split(","); // array Column metadata

                Hashtable<String, String> htblColMetaData = new Hashtable<>();
                for (int i = 0; i < header.length; i++)
                    htblColMetaData.put(header[i], colMetaData[i]); // Example: htblColMetaData.put("ColumnType", Double)

                String colName = colMetaData[1];
                htblTableMetaData.put(colName, htblColMetaData); // put (Column Name, Hashtable Column Metadata)
            }
            br.close();

            return htblTableMetaData;
        } catch (IOException e) {
            throw new DBQueryException("Failed to get Table MetaData");
        }
    }

}

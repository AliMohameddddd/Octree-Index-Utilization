package utils;

import exceptions.DBAppException;
import exceptions.DBNotFoundException;
import exceptions.DBQueryException;
import model.Page.Page;
import model.Page.PageReference;
import model.Table;

import java.io.*;

public class SerializationManager {
    private static final String TABLES_DATA_FOLDER = "src/main/resources/Tables/";
    private static final String PAGES_Table_FOLDER = "Pages/";
    private static final String Indexes_TABLE_FOLDER = "Indexes/";

    // Delete all tables files and create a new folder
    public static void createTablesFolder() throws DBAppException {
        File TablesFolder = new File(TABLES_DATA_FOLDER);

        if (TablesFolder.exists())
            Utils.deleteFolder(TablesFolder);

        if (!TablesFolder.mkdirs())
            throw new DBQueryException("Failed to create Tables folder");
    }


    public static void serializeTable(Table table) throws DBAppException {
        String tableName = table.getTableName();
        String tablePath = TABLES_DATA_FOLDER + tableName + "/" + tableName + ".ser";

        serialize(table, tablePath);
    }

    public static Table deserializeTable(String tableName) throws DBAppException {
        String tablePath = TABLES_DATA_FOLDER + tableName + "/" + tableName + ".ser";

        Table table = (Table) deserialize(tablePath);

        return table;
    }

    public static void serializePage(Page page) throws DBAppException {
        String tableName = page.getTableName();
        int pageIndex = page.getPageIndex();
        String PagePath = TABLES_DATA_FOLDER + tableName + "/" + PAGES_Table_FOLDER + pageIndex + ".ser";

        serialize(page, PagePath);
    }

    public static Page deserializePage(String tableName, PageReference pageRef) throws DBAppException {
        int pageIndex = pageRef.getPageIndex();
        String PagePath = TABLES_DATA_FOLDER + tableName + "/" + PAGES_Table_FOLDER + pageIndex + ".ser";

        Page page = (Page) deserialize(PagePath);
        page.setPageReference(pageRef);

        return page;
    }

    // Helper methods
    private static void serialize(Object obj, String filePath) throws DBAppException {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            out.writeObject(obj);

            out.close();
            fileOut.close();
        } catch (IOException e) {
            throw new DBQueryException("Failed to serialize object");
        }
    }

    private static Object deserialize(String filePath) throws DBAppException {
        try {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Object obj;
            try {
                obj = (Object) in.readObject();
            } catch (Exception e) {
                throw new DBNotFoundException("Table not found");
            }

            in.close();
            fileIn.close();

            return obj;
        } catch (IOException e) {
            throw new DBQueryException("Failed to deserialize object");
        }
    }

}

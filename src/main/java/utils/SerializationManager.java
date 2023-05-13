package utils;

import exceptions.DBNotFoundException;
import model.Index;
import model.Page.Page;
import model.Page.PageReference;
import model.Table;

import java.io.*;

public class SerializationManager {
    private static final String TABLES_DATA_FOLDER = "src/main/resources/Tables/";
    private static final String PAGES_Table_FOLDER = "Pages/";
    private static final String Indexes_TABLE_FOLDER = "Indexes/";

    // Delete all tables files and create a new folder
    public static void createTablesFolder() throws IOException {
        File TablesFolder = new File(TABLES_DATA_FOLDER);

        if (TablesFolder.exists())
            Utils.deleteFolder(TablesFolder);

        if (!TablesFolder.mkdirs())
            throw new IOException("Failed to create Tables folder");
    }


    public static void serializeTable(Table table) throws IOException {
        String tableName = table.getTableName();
        String tablePath = TABLES_DATA_FOLDER + tableName + "/" + tableName + ".ser";

        serialize(table, tablePath);
    }

    public static Table deserializeTable(String tableName) throws IOException, DBNotFoundException {
        String tablePath = TABLES_DATA_FOLDER + tableName + "/" + tableName + ".ser";

        Table table = (Table) deserialize(tablePath);

        return table;
    }

    public static void serializePage(Page page) throws IOException {
        String tableName = page.getTableName();
        int pageIndex = page.getPageIndex();
        String PagePath = TABLES_DATA_FOLDER + tableName + "/" + PAGES_Table_FOLDER + pageIndex + ".ser";

        serialize(page, PagePath);
    }

    public static Page deserializePage(String tableName, PageReference pageRef) throws IOException, DBNotFoundException {
        int pageIndex = pageRef.getPageIndex();
        String PagePath = TABLES_DATA_FOLDER + tableName + "/" + PAGES_Table_FOLDER + pageIndex + ".ser";

        Page page = (Page) deserialize(PagePath);
        page.setPageReference(pageRef);

        return page;
    }

    public static void serializeIndex(String tableName, Index index) throws IOException {
        String indexName = index.getIndexName();
        String indexPath = TABLES_DATA_FOLDER + tableName + "/" + Indexes_TABLE_FOLDER + indexName + ".ser";

        serialize(index, indexPath);
    }

    public static Index deserializeIndex(String tableName, String indexName) throws IOException, DBNotFoundException {
        String indexPath = TABLES_DATA_FOLDER + tableName + "/" + Indexes_TABLE_FOLDER + indexName + ".ser";

        Index index = (Index) deserialize(indexPath);

        return index;
    }


    // Helper methods
    private static void serialize(Object obj, String filePath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(filePath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);

        out.writeObject(obj);

        out.close();
        fileOut.close();
    }

    private static Object deserialize(String filePath) throws IOException, DBNotFoundException {
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
    }

}

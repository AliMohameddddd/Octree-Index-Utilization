package Utils;

import model.Tuple;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

public class Utils {

    // returns the index of tuple, if it is in the list; otherwise, (-(insertion point if it were to be) - 1).
    public static int binarySearch(Vector<Tuple> tuples, Tuple t) {
        Comparator<Tuple> c = new Comparator<Tuple>() {
            public int compare(Tuple t1, Tuple t2)
            {
                return t1.compareTo(t2);
            }
        };

        int index = Collections.binarySearch(tuples, t, c);
        return index;
    }

    public static int getInsertionIndex(Vector<Tuple> tuples, Tuple t) {
        int index = binarySearch(tuples, t);
        if (index < 0)
            index = -index - 1;
        return index;
    }

    public static int getMaximumRowsCountInTablePage() throws IOException {
        Properties prop = new Properties();
        FileInputStream configPath = new FileInputStream("src/main/resources/DBApp.config");
        prop.load(configPath);

        return Integer.parseInt(prop.getProperty("MaximumRowsCountInTablePage"));
    }

}

package Utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Validation {

    public static boolean isTableExists(String strTableName) {
        String tableFile = Utils.getTableFilePath(strTableName);
        return new File(tableFile).exists();
    }

    public static boolean areAllowedDataTypes(Hashtable<String, String> htblColNameType) {
        String[] arrAllowedDataTypes = {"java.lang.string", "java.lang.integer", "java.lang.double", "java.util.date"};
        List<String> allowedDataTypes = Arrays.asList(arrAllowedDataTypes);

        for (String strColType : htblColNameType.values())
            if (!allowedDataTypes.contains(strColType.toLowerCase()))
                return false;
        return true;
    }

    public static boolean validColumnMetaData(Hashtable<String, Object> htblColNameValue,
                                              Hashtable<String, Hashtable<String, String>> htblColNameMetaData) {
        for (String key : htblColNameValue.keySet()) {
            Object value = htblColNameValue.get(key);
            if (!htblColNameMetaData.containsKey(key))
                return false;
            Hashtable<String, String> colMetaData = htblColNameMetaData.get(key);
            if (!isNeededType(value, colMetaData.get("ColumnType")))
                return false;
            if (!isValidValue(value, colMetaData.get("Min"), colMetaData.get("Max")))
                return false;
        }
        return true;
    }

    public static boolean validateMinMax(Hashtable<String, String> htblColNameType, Hashtable<String, String> htblColNameMin,
                                         Hashtable<String, String> htblColNameMax) {

        for (String key : htblColNameMin.keySet()) {
            String type = htblColNameType.get(key).toLowerCase();
            String min = htblColNameMin.get(key);
            String max = htblColNameMax.get(key);
            if (!isNeededType(min, type) || !isNeededType(max, type)) // Validate min and max schema
                continue;

            Comparable compMin = htblColNameMin.get(key);
            Comparable compMax = htblColNameMax.get(key);
            if (compMin.compareTo(compMax) <= 0) // Validate min <= max always
                return false;
        }
        return true;
    }



    private static boolean isNeededType(Object obj, String type) {
        if (isString(obj) && type.equals("java.lang.string"))
            return true;
        if (isInteger(obj) && type.equals("java.lang.integer"))
            return true;
        if (isDouble(obj) && type.equals("java.lang.double"))
            return true;
        if (isDate(obj) && type.equals("java.util.date"))
            return true;

        return false;
    }

    private static boolean isNeededType(String obj, String type) {
        if (isString(obj) && type.equals("java.lang.String"))
            return true;
        if (isInteger(obj) && type.equals("java.lang.Integer"))
            return true;
        if (isDouble(obj) && type.equals("java.lang.Double"))
            return true;
        if (isDate(obj) && type.equals("java.util.Date"))
            return true;

        return false;
    }

    private static boolean isValidValue(Object value, String min, String max) {
        if (isString(value))
            return ((String) value).compareTo(min) >= 0 && ((String) value).compareTo(max) <= 0;
        if (isInteger(value))
            return ((Integer) value).compareTo(Integer.parseInt(min)) >= 0 && ((Integer) value).compareTo(Integer.parseInt(max)) <= 0;
        if (isDouble(value))
            return ((Double) value).compareTo(Double.parseDouble(min)) >= 0 && ((Double) value).compareTo(Double.parseDouble(max)) <= 0;
        if (isDate(value)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return ((Date) value).compareTo(format.parse(min)) >= 0 && ((Date) value).compareTo(format.parse(max)) <= 0;
            } catch (ParseException e) {}
        }
        return false;
    }


    private static boolean isString(Object obj) {
        return obj instanceof String;
    }

    private static boolean isInteger(Object obj) {
        return obj instanceof Integer;
    }

    private static boolean isDouble(Object obj) {
        return obj instanceof Double;
    }

    private static boolean isDate(Object obj) {
        return obj instanceof java.util.Date;
}


    private static boolean isString(String str) {
        return str != null && str.length() > 0;
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean isDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.parse(str);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


}

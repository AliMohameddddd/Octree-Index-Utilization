package exceptions;

public abstract class DBAppException extends Exception{
    public DBAppException(String message) {
        super(message);
    }
    public DBAppException() {
        super();
    }
}
package model.exceptions;

public class WeakPasswordException extends Exception {
    public WeakPasswordException(String message) {
        super(message);
    }
}
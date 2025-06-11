package exceptions;

/**
 * This exception is thrown when measures have no notes in them.
 */
public class Empty extends Exception {
    public Empty() {
        super();
    }

    public Empty(String message) {
        super(message);
    }
}

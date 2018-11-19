package place;

/**
 * A custom exception thrown from any of the Place classes if something goes wrong.
 *
 * @author Sean Strout @ RIT CS
 */
public class PlaceException extends Exception {
    /**
     * Convenience constructor to create a new {@link PlaceException}
     * with an error message.
     *
     * @param msg The error message associated with the exception.
     */
    public PlaceException(String msg) {
        super(msg);
    }

    /**
     * Convenience constructor to create a new {@link PlaceException}
     * as a result of some other exception.
     *
     * @param cause The root cause of the exception.
     */
    public PlaceException(Throwable cause) {
        super(cause);
    }

    /**
     * Convenience constructor to create a new {@link PlaceException}
     * as a result of some other exception.
     *
     * @param message The message associated with the exception.
     * @param cause The root cause of the exception.
     */
    public PlaceException(String message, Throwable cause) {
        super(message, cause);
    }
}

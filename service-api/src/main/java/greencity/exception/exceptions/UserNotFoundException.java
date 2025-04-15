package greencity.exception.exceptions;

/**
 * Exception that we get when user trying to find user that does not exist.
 *
 * @author Pavlo Melnyk
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Constructor for UserNotFoundException.
     *
     * @param message - giving message.
     */
    public UserNotFoundException(String message) {
        super(message);
    }
} 
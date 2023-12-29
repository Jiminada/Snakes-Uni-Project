package exceptions;

/**
 * A checked exception wrapper for NullPointerException thrown by an empty
 * buffer in the snake game. Used to cause IDE to prompt for use of try/catch
 * blocks while writing code.
 */
public class EmptyBufferException extends Throwable {
	/**
	 * CONSTRUCTOR for empty buffer exception
	 * 
	 * @param threadName pass the thread name with the throw for display to console
	 */
	public EmptyBufferException(String threadName) {
		super("Attempted to acces empty snake Direction buffer in "+ threadName);
	}
}

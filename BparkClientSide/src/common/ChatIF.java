package common;

/**
 * ChatIF is an interface for displaying messages to a user interface.
 * It is implemented by both client and server UIs that handle output display.
 */
public interface ChatIF {

    /**
     * Displays a message on the implementing UI.
     * @param message The message to be displayed.
     */
    public abstract void display(String message);
}

package client;

/**
 * Enumeration representing the different menu states in the client application.
 * Used to manage transitions and logic flow during user interactions.
 */
public enum MenuState {

    /**
     * The default state when no action is in progress.
     */
    IDLE,

    /**
     * State where the user is prompted to enter an order ID to check its existence.
     */
    CHECK_IF_EXISTS,

    /**
     * State after a valid order ID is confirmed and the user must select which field to update.
     */
    UPDATE_ORDER_SELECT_FIELD,

    /**
     * State where the user inputs a new value for the selected field.
     */
    UPDATE_ORDER_ENTER_VALUE
}
package common;

import java.io.Serializable;

/**
 * A request object sent from the client to the server
 * asking for the current site activity (active parkings + future reservations).
 */
public class GetSiteActivityRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // just a signal request
}

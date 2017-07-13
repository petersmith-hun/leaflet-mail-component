package hu.psprog.leaflet.mail.domain;

/**
 * Mail delivery status.
 *
 * @author Peter Smith
 */
public enum MailDeliveryStatus {

    /**
     * Mail is successfully delivered.
     */
    DELIVERED,

    /**
     * Mail could not be delivered as specified addressee does not exist.
     */
    INVALID_RECIPIENT,

    /**
     * Other communication exception (see application log for details).
     */
    COMMUNICATION_ERROR,

    /**
     * Mail structure is invalid.
     */
    VALIDATION_ERROR,

    /**
     * Unknown error (see application log for details).
     */
    UNKNOWN_ERROR
}

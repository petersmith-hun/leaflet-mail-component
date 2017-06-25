package hu.psprog.leaflet.mail.client.renderer;

import hu.psprog.leaflet.mail.domain.Mail;

/**
 * Mail renderer interface.
 *
 * @author Peter Smith
 */
@FunctionalInterface
public interface MailRenderer {

    /**
     * Renders given {@link Mail} object and returns rendered mail content as String.
     *
     * @param mail {@link Mail} object to render
     * @return rendered Mail object as String
     */
    String renderMail(Mail mail);
}

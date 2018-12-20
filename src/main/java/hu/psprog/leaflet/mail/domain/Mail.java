package hu.psprog.leaflet.mail.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

/**
 * Mail object.
 *
 * @author Peter Smith
 */
public class Mail {

    private String recipient;
    private Map<String, Object> contentMap;

    @NotEmpty
    private String subject;

    @NotEmpty
    private String template;

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplate() {
        return template;
    }

    public Map<String, Object> getContentMap() {
        return contentMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("recipient", recipient)
                .append("subject", subject)
                .append("template", template)
                .append("contentMap", contentMap)
                .toString();
    }

    public static MailBuilder getBuilder() {
        return new MailBuilder();
    }

    /**
     * Builder for {@link Mail} object.
     */
    public static final class MailBuilder {
        private String recipient;
        private String subject;
        private String template;
        private Map<String, Object> contentMap;

        private MailBuilder() {
        }

        /**
         * Adds recipient to the email.
         * Leaving this parameter empty forces the processor to use the default admin notification address as a recipient.
         *
         * @param recipient email address of the recipient
         * @return builder
         */
        public MailBuilder withRecipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * Adds subject parameter to the email.
         *
         * @param subject email subject
         * @return builder
         */
        public MailBuilder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Adds template name.
         * Referenced template must be present on classpath under 'mail' folder.
         *
         * @param template template name
         * @return builder
         */
        public MailBuilder withTemplate(String template) {
            this.template = template;
            return this;
        }

        /**
         * Adds key-value pairs to be included in email's content.
         *
         * @param contentMap key-value pairs
         * @return builder
         */
        public MailBuilder withContentMap(Map<String, Object> contentMap) {
            this.contentMap = contentMap;
            return this;
        }

        public Mail build() {
            Mail mail = new Mail();
            mail.template = this.template;
            mail.subject = this.subject;
            mail.recipient = this.recipient;
            mail.contentMap = this.contentMap;
            return mail;
        }
    }
}

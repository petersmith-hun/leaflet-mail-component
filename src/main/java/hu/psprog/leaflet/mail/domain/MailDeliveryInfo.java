package hu.psprog.leaflet.mail.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * Mail delivery information.
 *
 * @author Peter Smith
 */
public class MailDeliveryInfo {

    private Mail mail;
    private MailDeliveryStatus mailDeliveryStatus;
    private Map<String, String> constraintViolations;

    public Mail getMail() {
        return mail;
    }

    public MailDeliveryStatus getMailDeliveryStatus() {
        return mailDeliveryStatus;
    }

    public Map<String, String> getConstraintViolations() {
        return constraintViolations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof MailDeliveryInfo)) return false;

        MailDeliveryInfo that = (MailDeliveryInfo) o;

        return new EqualsBuilder()
                .append(mail, that.mail)
                .append(mailDeliveryStatus, that.mailDeliveryStatus)
                .append(constraintViolations, that.constraintViolations)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(mail)
                .append(mailDeliveryStatus)
                .append(constraintViolations)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mail", mail)
                .append("mailDeliveryStatus", mailDeliveryStatus)
                .append("constraintViolations", constraintViolations)
                .toString();
    }

    public static MailDeliveryInfoBuilder getBuilder() {
        return new MailDeliveryInfoBuilder();
    }

    /**
     * Builder for {@link MailDeliveryInfo}.
     */
    public static final class MailDeliveryInfoBuilder {
        private Mail mail;
        private MailDeliveryStatus mailDeliveryStatus;
        private Map<String, String> constraintViolations;

        private MailDeliveryInfoBuilder() {
        }

        public MailDeliveryInfoBuilder withMail(Mail mail) {
            this.mail = mail;
            return this;
        }

        public MailDeliveryInfoBuilder withMailDeliveryStatus(MailDeliveryStatus mailDeliveryStatus) {
            this.mailDeliveryStatus = mailDeliveryStatus;
            return this;
        }

        public MailDeliveryInfoBuilder withConstraintViolations(Map<String, String> constraintViolations) {
            this.constraintViolations = constraintViolations;
            return this;
        }

        public MailDeliveryInfo build() {
            MailDeliveryInfo mailDeliveryInfo = new MailDeliveryInfo();
            mailDeliveryInfo.mailDeliveryStatus = this.mailDeliveryStatus;
            mailDeliveryInfo.mail = this.mail;
            mailDeliveryInfo.constraintViolations = this.constraintViolations;
            return mailDeliveryInfo;
        }
    }
}

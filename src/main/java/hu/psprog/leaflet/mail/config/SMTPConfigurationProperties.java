package hu.psprog.leaflet.mail.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;

/**
 * SMTP server configuration properties for mail component.
 * Required properties:
 *  - mail.smtp.host: SMTP host
 *  - mail.smtp.port: SMTP port
 *  - mail.smtp.username: SMTP username
 *  - mail.smtp.password: SMTP password
 *
 * @author Peter Smith
 */
@Component
public class SMTPConfigurationProperties {

    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    public SMTPConfigurationProperties(@NotEmpty @Value("${mail.smtp.host}") String smtpHost,
                                       @NotEmpty @Value("${mail.smtp.port}") int smtpPort,
                                       @NotEmpty @Value("${mail.smtp.username}") String smtpUsername,
                                       @NotEmpty @Value("${mail.smtp.password}") String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("smtpHost", smtpHost)
                .append("smtpPort", smtpPort)
                .append("smtpUsername", smtpUsername)
                .toString();
    }
}

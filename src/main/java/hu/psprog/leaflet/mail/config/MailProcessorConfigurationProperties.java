package hu.psprog.leaflet.mail.config;

import hu.psprog.leaflet.mail.client.renderer.MailRenderer;
import hu.psprog.leaflet.mail.client.renderer.impl.ThymeleafMailRenderer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Mail processor configuration properties.
 * Required properties:
 *  - mail.notification: system notification address
 *  - mail.sender.address: sender (typically a no-reply) address
 *  - mail.sender.name: sender name
 *  - mail.renderer: template renderer engine, defaults to {@link ThymeleafMailRenderer}
 *
 * @author Peter Smith
 */
@Component
public class MailProcessorConfigurationProperties {

    private final String adminNotificationAddress;
    private final String senderAddress;
    private final String senderName;
    private final Class<? extends MailRenderer> renderer;

    public MailProcessorConfigurationProperties(@NotEmpty @Value("${mail.notification}") String adminNotificationAddress,
                                                @NotEmpty @Value("${mail.sender.address}") String senderAddress,
                                                @NotEmpty @Value("${mail.sender.name}") String senderName,
                                                @Value("${mail.renderer}") Class<? extends MailRenderer> renderer) {
        this.adminNotificationAddress = adminNotificationAddress;
        this.senderAddress = senderAddress;
        this.senderName = senderName;
        if (Objects.nonNull(renderer)) {
            this.renderer = renderer;
        } else {
            this.renderer = ThymeleafMailRenderer.class;
        }
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getAdminNotificationAddress() {
        return adminNotificationAddress;
    }

    public Class<? extends MailRenderer> getRenderer() {
        return renderer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("adminNotificationAddress", adminNotificationAddress)
                .append("senderAddress", senderAddress)
                .append("senderName", senderName)
                .append("renderer", renderer)
                .toString();
    }
}

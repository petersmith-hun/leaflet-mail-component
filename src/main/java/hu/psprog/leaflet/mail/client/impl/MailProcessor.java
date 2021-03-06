package hu.psprog.leaflet.mail.client.impl;

import hu.psprog.leaflet.mail.client.renderer.MailRenderer;
import hu.psprog.leaflet.mail.config.MailProcessorConfigurationProperties;
import hu.psprog.leaflet.mail.domain.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Processes and sends mails.
 *
 * @author Peter Smith
 */
@Component
class MailProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailProcessor.class);

    private static final String NO_MAIL_RENDERER_PROVIDED = "No mail renderer provided.";
    private static final String SELECTED_MAIL_RENDERER_NOT_AVAILABLE = "Selected mail renderer [%s] not available.";

    private List<MailRenderer> availableMailRendererList;
    private MailRenderer mailRenderer;
    private MailProcessorConfigurationProperties mailProcessorConfigurationProperties;
    private JavaMailSender javaMailSender;
    private Address sender;

    @Autowired
    public MailProcessor(List<MailRenderer> mailRendererList, MailProcessorConfigurationProperties mailProcessorConfigurationProperties, JavaMailSender javaMailSender) {
        this.mailProcessorConfigurationProperties = mailProcessorConfigurationProperties;
        this.javaMailSender = javaMailSender;
        this.availableMailRendererList = mailRendererList;
    }

    @PostConstruct
    public void initialize() {
        selectMailRenderer(availableMailRendererList);
        prepareSender();
    }

    /**
     * Processes given {@link Mail} object by transforming it into a {@link MimeMessage}.
     * Implementation uses {@link JavaMailSender} to prepare and send the mail.
     *
     * @param mail {@link Mail} object
     * @throws MessagingException if an error occurred during sending the mail
     */
    public void process(Mail mail) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom(sender);
        prepareMessage(mail, message);

        javaMailSender.send(message);
    }

    private void prepareSender() {
        try {
            sender = new InternetAddress(mailProcessorConfigurationProperties.getSenderAddress(), mailProcessorConfigurationProperties.getSenderName());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not prepare sender with mail configuration {}.", mailProcessorConfigurationProperties, e);
            throw new IllegalArgumentException("Could not prepare sender - invalid mail configuration provided.");
        }
    }

    private void prepareMessage(Mail mail, MimeMessage message) throws MessagingException {
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message);
        mimeMessageHelper.setTo(getRecipient(mail));
        mimeMessageHelper.setSubject(mail.getSubject());
        mimeMessageHelper.setText(mailRenderer.renderMail(mail), true);

        if (Objects.nonNull(mail.getReplyTo())) {
            mimeMessageHelper.setReplyTo(mail.getReplyTo());
        }
    }

    private String getRecipient(Mail mail) {
        return Optional.ofNullable(mail.getRecipient())
                .orElse(mailProcessorConfigurationProperties.getAdminNotificationAddress());
    }

    private void selectMailRenderer(List<MailRenderer> availableMailRendererList) {

        Assert.notEmpty(availableMailRendererList, NO_MAIL_RENDERER_PROVIDED);

        mailRenderer = availableMailRendererList.stream()
                .filter(renderer -> renderer.getClass().isAssignableFrom(mailProcessorConfigurationProperties.getRenderer()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(SELECTED_MAIL_RENDERER_NOT_AVAILABLE, mailProcessorConfigurationProperties.getRenderer().getName())));
    }
}

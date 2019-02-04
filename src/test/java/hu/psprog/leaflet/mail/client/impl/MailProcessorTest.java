package hu.psprog.leaflet.mail.client.impl;

import hu.psprog.leaflet.mail.client.renderer.MailRenderer;
import hu.psprog.leaflet.mail.client.renderer.impl.ThymeleafMailRenderer;
import hu.psprog.leaflet.mail.config.MailProcessorConfigurationProperties;
import hu.psprog.leaflet.mail.domain.Mail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.ReflectionUtils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link MailProcessor}.
 *
 * @author Peter Smith
 */
@RunWith(MockitoJUnitRunner.class)
public class MailProcessorTest {

    private static final String DEFAULT_RECIPIENT = "default@dev.lflt";
    private static final String EXACT_RECIPIENT = "test@dev.lflt";
    private static final String RENDERED_MAIL_CONTENT = "Rendered mail";
    private static final String SENDER_ADDRESS = "sender-address@local.dev";
    private static final String SENDER_NAME = "Test Sender";
    private static final String FIELD_MAIL_RENDERER = "mailRenderer";
    private static final String FIELD_SENDER = "sender";
    private static final InternetAddress FROM_ADDRESS = prepareSender();
    private static final String REPLY_TO_ADDRESS = "test@dev.local";
    private static final String SUBJECT = "Test";
    private static final String CONTENT_TYPE_HTML = "text/html";

    @Mock
    private MailRenderer mailRenderer;

    @Mock
    private MailProcessorConfigurationProperties mailProcessorConfigurationProperties;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailProcessor mailProcessor;

    private Mail mail;
    private ThymeleafMailRenderer thymeleafMailRenderer;

    @Before
    public void setup() {
        mailRenderer = mock(MailRenderer.class);
        thymeleafMailRenderer = new ThymeleafMailRenderer(null);
    }

    @Test
    public void shouldSelectMailRendererAndPrepareSender() throws NoSuchFieldException {

        // given
        doReturn(ThymeleafMailRenderer.class).when(mailProcessorConfigurationProperties).getRenderer();
        given(mailProcessorConfigurationProperties.getSenderAddress()).willReturn(SENDER_ADDRESS);
        given(mailProcessorConfigurationProperties.getSenderName()).willReturn(SENDER_NAME);
        MailProcessor mailProcessorToInit = new MailProcessor(Collections.singletonList(thymeleafMailRenderer), mailProcessorConfigurationProperties, javaMailSender);

        // when
        mailProcessorToInit.initialize();

        // then
        assertThat(getMailRendererField(mailProcessorToInit), equalTo(thymeleafMailRenderer));
        assertThat(getSenderField(mailProcessorToInit), equalTo(FROM_ADDRESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSpecifiedMailRendererIsNotAvailable() {

        // given
        doReturn(MailRenderer.class).when(mailProcessorConfigurationProperties).getRenderer();
        MailProcessor mailProcessorToInit = new MailProcessor(Collections.singletonList(thymeleafMailRenderer), mailProcessorConfigurationProperties, javaMailSender);

        // when
        mailProcessorToInit.initialize();

        // then
        // expected exception
    }

    @Test
    public void shouldProcessMailWithGivenRecipient() throws NoSuchFieldException, MessagingException {

        // given
        prepareMail(true);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        given(mailRenderer.renderMail(mail)).willReturn(RENDERED_MAIL_CONTENT);
        prepareMailRendererField();
        prepareSenderField();

        // when
        mailProcessor.process(mail);

        // then
        verify(mailRenderer).renderMail(mail);
        verify(mailProcessorConfigurationProperties).getAdminNotificationAddress();
        verify(mimeMessage).setRecipient(Message.RecipientType.TO, getAddressToCheck(EXACT_RECIPIENT));
        verify(mimeMessage).setFrom(FROM_ADDRESS);
        verify(javaMailSender).send(mimeMessage);
        verify(mimeMessage).setSubject(SUBJECT);
        verify(mimeMessage).setContent(RENDERED_MAIL_CONTENT, CONTENT_TYPE_HTML);
        verifyNoMoreInteractions(mimeMessage);
    }

    @Test
    public void shouldProcessMailWithDefaultRecipient() throws NoSuchFieldException, MessagingException {

        // given
        prepareMail(false);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        given(mailRenderer.renderMail(mail)).willReturn(RENDERED_MAIL_CONTENT);
        given(mailProcessorConfigurationProperties.getAdminNotificationAddress()).willReturn(DEFAULT_RECIPIENT);
        prepareMailRendererField();
        prepareSenderField();

        // when
        mailProcessor.process(mail);

        // then
        verify(mailRenderer).renderMail(mail);
        verify(mailProcessorConfigurationProperties).getAdminNotificationAddress();
        verify(mimeMessage).setRecipient(Message.RecipientType.TO, getAddressToCheck(DEFAULT_RECIPIENT));
        verify(mimeMessage).setFrom(FROM_ADDRESS);
        verify(javaMailSender).send(mimeMessage);
        verify(mimeMessage).setSubject(SUBJECT);
        verify(mimeMessage).setContent(RENDERED_MAIL_CONTENT, CONTENT_TYPE_HTML);
        verifyNoMoreInteractions(mimeMessage);
    }

    @Test
    public void shouldProcessMailWithReplyToAddress() throws NoSuchFieldException, MessagingException {

        // given
        prepareMail(true, true);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        given(mailRenderer.renderMail(mail)).willReturn(RENDERED_MAIL_CONTENT);
        prepareMailRendererField();
        prepareSenderField();

        // when
        mailProcessor.process(mail);

        // then
        verify(mailRenderer).renderMail(mail);
        verify(mailProcessorConfigurationProperties).getAdminNotificationAddress();
        verify(mimeMessage).setRecipient(Message.RecipientType.TO, getAddressToCheck(EXACT_RECIPIENT));
        verify(mimeMessage).setFrom(FROM_ADDRESS);
        verify(javaMailSender).send(mimeMessage);
        verify(mimeMessage).setReplyTo(new Address[] {getAddressToCheck(REPLY_TO_ADDRESS)});
        verify(mimeMessage).setSubject(SUBJECT);
        verify(mimeMessage).setContent(RENDERED_MAIL_CONTENT, CONTENT_TYPE_HTML);
        verifyNoMoreInteractions(mimeMessage);
    }

    private static InternetAddress prepareSender() {

        InternetAddress address = null;
        try {
            address = new InternetAddress(SENDER_ADDRESS, SENDER_NAME);
        } catch (UnsupportedEncodingException e) {
            fail("Failed to prepare sender address.");
        }

        return address;
    }

    private void prepareMail(boolean exactRecipient) {
        prepareMail(exactRecipient, false);
    }

    private void prepareMail(boolean exactRecipient, boolean withReplyTo) {
        mail = Mail.getBuilder()
                .withRecipient(exactRecipient
                        ? EXACT_RECIPIENT
                        : null)
                .withSubject(SUBJECT)
                .withReplyTo(withReplyTo
                        ? REPLY_TO_ADDRESS
                        : null)
                .build();
    }

    private Address getAddressToCheck(String recipient) throws AddressException {
        return InternetAddress.parse(recipient)[0];
    }

    private MailRenderer getMailRendererField(MailProcessor mailProcessor) throws NoSuchFieldException {
        return (MailRenderer) ReflectionUtils.getField(accessField(FIELD_MAIL_RENDERER), mailProcessor);
    }

    private InternetAddress getSenderField(MailProcessor mailProcessor) throws NoSuchFieldException {
        return (InternetAddress) ReflectionUtils.getField(accessField(FIELD_SENDER), mailProcessor);
    }

    private void prepareMailRendererField() throws NoSuchFieldException {
        ReflectionUtils.setField(accessField(FIELD_MAIL_RENDERER), mailProcessor, mailRenderer);
    }

    private void prepareSenderField() throws NoSuchFieldException {
        ReflectionUtils.setField(accessField(FIELD_SENDER), mailProcessor, FROM_ADDRESS);
    }

    private Field accessField(String fieldName) throws NoSuchFieldException {

        Field field = MailProcessor.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return field;
    }
}

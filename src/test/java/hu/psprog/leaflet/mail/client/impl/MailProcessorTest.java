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
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.ReflectionUtils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    public void setup() throws MessagingException {
        mailRenderer = mock(MailRenderer.class);
        thymeleafMailRenderer = new ThymeleafMailRenderer(null);
    }

    @Test
    public void shouldSelectMailRenderer() throws MessagingException, NoSuchFieldException {

        // given
        doReturn(ThymeleafMailRenderer.class).when(mailProcessorConfigurationProperties).getRenderer();
        MailProcessor mailProcessorToInit = new MailProcessor(Collections.singletonList(thymeleafMailRenderer), mailProcessorConfigurationProperties, javaMailSender);

        // when
        mailProcessorToInit.initialize();

        // then
        assertThat(getMailRendererField(mailProcessorToInit), equalTo(thymeleafMailRenderer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfSpecifiedMailRendererIsNotAvailable() throws MessagingException, NoSuchFieldException {

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
        setMailRendererField(mailProcessor, mailRenderer);

        // when
        mailProcessor.process(mail);

        // then
        verify(mailProcessorConfigurationProperties).getSenderAddress();
        verify(mailRenderer).renderMail(mail);
        verify(mailProcessorConfigurationProperties).getAdminNotificationAddress();
        verify(mimeMessage).setRecipient(Message.RecipientType.TO, getAddressToCheck(EXACT_RECIPIENT));
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    public void shouldProcessMailWithDefaultRecipient() throws NoSuchFieldException, MessagingException {

        // given
        prepareMail(false);
        given(javaMailSender.createMimeMessage()).willReturn(mimeMessage);
        given(mailRenderer.renderMail(mail)).willReturn(RENDERED_MAIL_CONTENT);
        given(mailProcessorConfigurationProperties.getAdminNotificationAddress()).willReturn(DEFAULT_RECIPIENT);
        setMailRendererField(mailProcessor, mailRenderer);

        // when
        mailProcessor.process(mail);

        // then
        verify(mailProcessorConfigurationProperties).getSenderAddress();
        verify(mailRenderer).renderMail(mail);
        verify(mailProcessorConfigurationProperties).getAdminNotificationAddress();
        verify(mimeMessage).setRecipient(Message.RecipientType.TO, getAddressToCheck(DEFAULT_RECIPIENT));
        verify(javaMailSender).send(mimeMessage);
    }

    private void prepareMail(boolean exactRecipient) {
        mail = Mail.getBuilder()
                .withRecipient(exactRecipient
                        ? EXACT_RECIPIENT
                        : null)
                .withSubject("Test")
                .build();
    }

    private Address getAddressToCheck(String recipient) throws AddressException {
        return InternetAddress.parse(recipient)[0];
    }

    private MailRenderer getMailRendererField(MailProcessor mailProcessor) throws NoSuchFieldException {
        return (MailRenderer) ReflectionUtils.getField(accessMailRendererField(), mailProcessor);
    }

    private void setMailRendererField(MailProcessor mailProcessor, MailRenderer mailRenderer) throws NoSuchFieldException {
        ReflectionUtils.setField(accessMailRendererField(), mailProcessor, mailRenderer);
    }

    private Field accessMailRendererField() throws NoSuchFieldException {

        Field field = MailProcessor.class.getDeclaredField("mailRenderer");
        field.setAccessible(true);

        return field;
    }
}

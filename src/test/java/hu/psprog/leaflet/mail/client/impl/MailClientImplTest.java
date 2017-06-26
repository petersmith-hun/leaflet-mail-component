package hu.psprog.leaflet.mail.client.impl;

import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import hu.psprog.leaflet.mail.domain.MailDeliveryStatus;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Unit tests for {@link MailClientImpl}.
 *
 * @author Peter Smith
 */
@RunWith(JUnitParamsRunner.class)
public class MailClientImplTest {

    private static final String MOCKED_VIOLATION = "Mocked Violation";
    private static final String VIOLATING_FIELD = "violatingField";

    @Mock
    private MailProcessor mailProcessor;

    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<Mail> violation;

    @Mock
    private Path path;

    @InjectMocks
    private MailClientImpl mailClient;

    private Mail mail;

    @Before
    public void setup() throws MessagingException {
        MockitoAnnotations.initMocks(this);
        mail = Mail.getBuilder().build();
        doNothing().when(mailProcessor).process(mail);
    }

    @Test
    public void shouldSendMail() throws MessagingException {

        // given
        given(validator.validate(mail)).willReturn(Collections.emptySet());

        // when
        MailDeliveryInfo result = mailClient.sendMail(mail).blockingFirst();

        // then
        assertThat(result, notNullValue());
        assertThat(result.getMail(), equalTo(mail));
        assertThat(result.getMailDeliveryStatus(), equalTo(MailDeliveryStatus.DELIVERED));
        verify(validator).validate(mail);
        verify(mailProcessor).process(mail);
    }

    @Test
    public void shouldNotSendEmailBecauseOfValidationError() {

        // given
        given(validator.validate(mail)).willReturn(prepareConstraintViolations());
        given(violation.getMessage()).willReturn(MOCKED_VIOLATION);
        given(violation.getPropertyPath()).willReturn(path);
        given(path.toString()).willReturn(VIOLATING_FIELD);

        // when
        MailDeliveryInfo result = mailClient.sendMail(mail).blockingFirst();

        // then
        assertThat(result, notNullValue());
        assertThat(result.getMail(), equalTo(mail));
        assertThat(result.getMailDeliveryStatus(), equalTo(MailDeliveryStatus.VALIDATION_ERROR));
        assertThat(result.getConstraintViolations().size(), equalTo(1));
        assertThat(result.getConstraintViolations().get(VIOLATING_FIELD), equalTo(MOCKED_VIOLATION));
        verify(validator).validate(mail);
        verifyZeroInteractions(mailProcessor);
    }

    @Test
    @Parameters(source = MailStatusProvider.class)
    public void shouldHandleSpecificExceptions(Class<Exception> exception, MailDeliveryStatus expectedStatus) throws MessagingException {

        // given
        given(validator.validate(mail)).willReturn(Collections.emptySet());
        doThrow(exception).when(mailProcessor).process(mail);

        // when
        MailDeliveryInfo result = mailClient.sendMail(mail).blockingFirst();

        // then
        assertThat(result, notNullValue());
        assertThat(result.getMail(), equalTo(mail));
        assertThat(result.getMailDeliveryStatus(), equalTo(expectedStatus));
        verify(validator).validate(mail);
    }

    private Set<ConstraintViolation<Mail>> prepareConstraintViolations() {

        Set<ConstraintViolation<Mail>> constraintViolations = new HashSet<>();
        constraintViolations.add(violation);

        return  constraintViolations;
    }

    public static class MailStatusProvider {

        public static Object[] provideParameters() {
            return new Object[] {
                    new Object[] {SendFailedException.class, MailDeliveryStatus.INVALID_RECIPIENT},
                    new Object[] {MessagingException.class, MailDeliveryStatus.COMMUNICATION_ERROR},
                    new Object[] {RuntimeException.class, MailDeliveryStatus.UNKNOWN_ERROR}
            };
        }
    }
}

package hu.psprog.leaflet.mail.client.impl;

import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import hu.psprog.leaflet.mail.domain.MailDeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link MailClientImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
public class MailClientImplTest {

    private static final String MOCKED_VIOLATION = "Mocked Violation";
    private static final String VIOLATING_FIELD = "violatingField";

    @Mock(lenient = true)
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

    @BeforeEach
    public void setup() throws MessagingException {
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
        verifyNoInteractions(mailProcessor);
    }

    @ParameterizedTest
    @MethodSource("mailStatusDataProvider")
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

    private static Stream<Arguments> mailStatusDataProvider() {
        return Stream.of(
                Arguments.of(SendFailedException.class, MailDeliveryStatus.INVALID_RECIPIENT),
                Arguments.of(MessagingException.class, MailDeliveryStatus.COMMUNICATION_ERROR),
                Arguments.of(RuntimeException.class, MailDeliveryStatus.UNKNOWN_ERROR)
        );
    }
}

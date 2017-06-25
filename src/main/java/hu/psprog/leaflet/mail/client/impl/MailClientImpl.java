package hu.psprog.leaflet.mail.client.impl;

import hu.psprog.leaflet.mail.client.MailClient;
import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import hu.psprog.leaflet.mail.domain.MailDeliveryStatus;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link MailClient}.
 *
 * @author Peter Smith
 */
@Service
class MailClientImpl implements MailClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailClientImpl.class);

    private MailProcessor mailProcessor;
    private Validator validator;

    @Autowired
    public MailClientImpl(MailProcessor mailProcessor, Validator validator) {
        this.mailProcessor = mailProcessor;
        this.validator = validator;
    }

    @Override
    public Observable<MailDeliveryInfo> sendMail(Mail mail) {

        return Observable.create(emitter -> {
            MailDeliveryInfo.MailDeliveryInfoBuilder mailDeliveryInfo = MailDeliveryInfo.getBuilder()
                    .withMail(mail);

            Set<ConstraintViolation<Mail>> validationResult = validator.validate(mail);

            if (validationResult.isEmpty()) {
                try {
                    mailProcessor.process(mail);
                    mailDeliveryInfo.withMailDeliveryStatus(MailDeliveryStatus.DELIVERED);
                } catch (SendFailedException e) {
                    LOGGER.error("Invalid recipient", e);
                    mailDeliveryInfo.withMailDeliveryStatus(MailDeliveryStatus.INVALID_RECIPIENT);
                } catch (MessagingException e) {
                    LOGGER.error("Failed to send message", e);
                    mailDeliveryInfo.withMailDeliveryStatus(MailDeliveryStatus.COMMUNICATION_ERROR);
                } catch (Exception e) {
                    LOGGER.error("Unknown exception occurred while processing mail", e);
                    mailDeliveryInfo.withMailDeliveryStatus(MailDeliveryStatus.UNKNOWN_ERROR);
                }
            } else {
                LOGGER.error("Invalid mail structure.");
                mailDeliveryInfo.withConstraintViolations(prepareConstraintViolations(validationResult));
                mailDeliveryInfo.withMailDeliveryStatus(MailDeliveryStatus.VALIDATION_ERROR);
            }

            emitter.onNext(mailDeliveryInfo.build());
        });
    }

    private Map<String, String> prepareConstraintViolations(Set<ConstraintViolation<Mail>> validationResult) {
        return validationResult.stream()
                .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }
}

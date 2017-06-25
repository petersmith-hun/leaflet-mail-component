package hu.psprog.leaflet.mail.client.renderer.impl;

import hu.psprog.leaflet.mail.client.renderer.MailRenderer;
import hu.psprog.leaflet.mail.domain.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * {@link MailRenderer} implementation capable of Thymeleaf-template based mail rendering.
 *
 * @author Peter Smith
 */
@Component
public class ThymeleafMailRenderer implements MailRenderer {

    private TemplateEngine templateEngine;

    @Autowired
    public ThymeleafMailRenderer(@Qualifier("emailTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public String renderMail(Mail mail) {

        Context context = new Context();
        context.setVariables(mail.getContentMap());

        return templateEngine.process(mail.getTemplate(), context);
    }
}

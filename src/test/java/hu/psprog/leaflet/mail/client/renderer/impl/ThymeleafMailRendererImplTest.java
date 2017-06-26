package hu.psprog.leaflet.mail.client.renderer.impl;

import hu.psprog.leaflet.mail.domain.Mail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ThymeleafMailRenderer}.
 *
 * @author Peter Smith
 */
@RunWith(MockitoJUnitRunner.class)
public class ThymeleafMailRendererImplTest {

    private static final String EMAIL_TEMPLATE = "test_template";

    @Mock
    private ITemplateEngine templateEngine;

    @InjectMocks
    private ThymeleafMailRenderer thymeleafMailRenderer;

    @Test
    public void shouldRenderMail() {

        // given
        Mail mail = prepareMail();

        // when
        thymeleafMailRenderer.renderMail(mail);

        // then
        verify(templateEngine).process(eq(EMAIL_TEMPLATE), any(Context.class));
    }

    private Mail prepareMail() {
        return Mail.getBuilder()
                .withTemplate(EMAIL_TEMPLATE)
                .withContentMap(prepareContentMap())
                .build();
    }

    private Map<String, Object> prepareContentMap() {

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("firstKey", "firstValue");
        contentMap.put("secondKey", "secondValue");

        return contentMap;
    }
}

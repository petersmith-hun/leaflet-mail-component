package hu.psprog.leaflet.mail.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import static hu.psprog.leaflet.mail.config.MailComponentConfig.COMPONENT_SCAN_PATH;

/**
 * Mail module configuration.
 *
 * @author Peter Smith
 */
@Configuration
@ComponentScan(COMPONENT_SCAN_PATH)
public class MailComponentConfig {

    static final String COMPONENT_SCAN_PATH = "hu.psprog.leaflet.mail";

    private static final String TEMPLATE_RESOLVER_PREFIX = "/mail/";
    private static final String TEMPLATE_RESOLVER_SUFFIX = ".html";
    private static final String CHARACTER_ENCODING = "UTF-8";

    @Bean
    @Autowired
    public JavaMailSender javaMailSender(SMTPConfigurationProperties smtpConfigurationProperties) {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(smtpConfigurationProperties.getSmtpHost());
        javaMailSender.setPort(smtpConfigurationProperties.getSmtpPort());
        javaMailSender.setUsername(smtpConfigurationProperties.getSmtpUsername());
        javaMailSender.setPassword(smtpConfigurationProperties.getSmtpPassword());
        javaMailSender.setDefaultEncoding(CHARACTER_ENCODING);

        return javaMailSender;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    @Autowired
    public TemplateEngine emailTemplateEngine(MessageSource messageSource) {

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlEmailTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(messageSource);

        return templateEngine;
    }

    private ITemplateResolver htmlEmailTemplateResolver() {

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(1);
        templateResolver.setPrefix(TEMPLATE_RESOLVER_PREFIX);
        templateResolver.setSuffix(TEMPLATE_RESOLVER_SUFFIX);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(CHARACTER_ENCODING);
        templateResolver.setCacheable(false);

        return templateResolver;
    }
}

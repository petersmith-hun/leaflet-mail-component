open module leaflet.component.mail {
    requires java.annotation;
    requires java.compiler;
    requires jakarta.mail;
    requires java.validation;
    requires io.reactivex.rxjava2;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.context.support;
    requires spring.core;
    requires thymeleaf;
    requires thymeleaf.spring5;

    exports hu.psprog.leaflet.mail.client;
    exports hu.psprog.leaflet.mail.config;
    exports hu.psprog.leaflet.mail.domain;
}
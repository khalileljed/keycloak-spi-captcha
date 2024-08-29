package org.example;

import org.example.services.CaptchaService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CaptchaResourceProviderFactory implements RealmResourceProviderFactory {
    public static final String PROVIDER_ID = "keycloak-captcha";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new CaptchaResourceProvider(new CaptchaService());
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}

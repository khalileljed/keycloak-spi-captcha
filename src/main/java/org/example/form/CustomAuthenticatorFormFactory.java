package org.example.form;

import org.example.services.CaptchaService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;


public class CustomAuthenticatorFormFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "custom-authenticator-form";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Custom Username Password Form with Captcha";
    }

    @Override
    public String getReferenceCategory() {
        return "custom-authentication";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void init(Config.Scope config) {
        // Initialization code if needed
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Post-initialization code if needed
    }

    @Override
    public void close() {
        // Cleanup code if needed
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CaptchaUsernamePasswordForm(new CaptchaService());
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty maxFailedAttempts = new ProviderConfigProperty();
        maxFailedAttempts.setName("MAX_FAILED_ATTEMPTS");
        maxFailedAttempts.setLabel("Max Failed Attempts");
        maxFailedAttempts.setType(ProviderConfigProperty.STRING_TYPE);
        maxFailedAttempts.setHelpText("A custom parameter for the authenticator.");
        return List.of(maxFailedAttempts);
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{AuthenticationExecutionModel.Requirement.REQUIRED}; // or Requirement.REQUIRED, if needed
    }

    @Override
    public String getHelpText() {
        return "Custom authenticator for username/password login with captcha support.";
    }
}
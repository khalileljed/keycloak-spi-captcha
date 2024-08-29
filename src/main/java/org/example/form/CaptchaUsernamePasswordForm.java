package org.example.form;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.services.CaptchaService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;


/**
 * @author EL JED khalil
 */
public class CaptchaUsernamePasswordForm extends UsernamePasswordForm {
    private static final String ATTEMPTS_KEY = "failedAttempts";
    private static final String CAPTCHA_REQUIRED_KEY = "captchaRequired";
    private static final String MAX_FAILED_ATTEMPTS = "MAX_FAILED_ATTEMPTS";
    @Inject
    public CaptchaService captchaService;
    public static AuthenticationSessionModel authenticationSessionModel;

    public CaptchaUsernamePasswordForm(CaptchaService captchaService)
    {
        this.captchaService = captchaService;
    }

    @Override
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        boolean captchaRequired = getCaptchaRequired(context);
        if (captchaRequired && !validateCaptcha(context, formData)) {
            return false;
        }
        return validateUserAndPassword(context, formData);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }
        setCaptchaRequired(context, false);
        resetFailedAttempts(context);
        context.form().setAttribute(CAPTCHA_REQUIRED_KEY, false);
        authenticationSessionModel = context.getAuthenticationSession();
        context.success();
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        if (error != null) {
            incrementFailedAttempts(context);
            if (getFailedAttempts(context) >= Integer.parseInt(getMaxFailedAttempts(context))) {
                setCaptchaRequired(context, true);
                form.setAttribute(CAPTCHA_REQUIRED_KEY, true);
                authenticationSessionModel = context.getAuthenticationSession();
            }
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        return createLoginForm(form);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        if (config != null) {
            String customParam = config.getConfig().get(MAX_FAILED_ATTEMPTS);
            try {
                if (customParam != null) {
                    setMaxFailedAttempts(context, Integer.parseInt(customParam));
                } else {
                    setMaxFailedAttempts(context, 0);
                    log.error("MAX_FAILED_ATTEMPTS is not configured. Please set it in the Keycloak dashboard.");
                }
            } catch (NumberFormatException e) {
                setMaxFailedAttempts(context, 0);
                log.error("Invalid value for MAX_FAILED_ATTEMPTS. It should be an integer. Please correct it in the Keycloak dashboard.");
            }
        } else {
            setMaxFailedAttempts(context, 0);
            log.error("Authenticator configuration is missing. Please configure the MAX_FAILED_ATTEMPTS field in the Keycloak dashboard.");
        }
        if (Integer.parseInt(getMaxFailedAttempts(context)) == 0) {
            setCaptchaRequired(context, true);
            context.form().setExecution(context.getExecution().getId()).setAttribute(CAPTCHA_REQUIRED_KEY, true);
            authenticationSessionModel = context.getAuthenticationSession();
        }
        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getSession());

        if (context.getUser() != null) {
            LoginFormsProvider form = context.form();
            form.setAttribute(LoginFormsProvider.USERNAME_HIDDEN, true);
            form.setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true);
            context.getAuthenticationSession().setAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
            if (loginHint != null || rememberMeUsername != null) {
                if (loginHint != null) {
                    formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
                } else {
                    formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
        }
        boolean captchaRequired = getCaptchaRequired(context);
        context.form().setAttribute(CAPTCHA_REQUIRED_KEY, captchaRequired);
        authenticationSessionModel = context.getAuthenticationSession();
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    protected boolean validateCaptcha(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        String captchaToken = formData.getFirst("captcha_token");
        String captchaText = formData.getFirst("captcha_text");
        if (captchaToken == null || captchaText == null || !isValid(context,captchaToken, captchaText)) {
            context.getEvent().error("invalid_captcha");
            boolean captchaRequired = getCaptchaRequired(context);
            context.form().setAttribute(CAPTCHA_REQUIRED_KEY, captchaRequired);
            authenticationSessionModel = context.getAuthenticationSession();
            Response challenge = context.form().setError("Invalid captcha").createLoginUsernamePassword();
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return false;
        }
        return true;
    }

    public boolean isValid(AuthenticationFlowContext context,String captchaToken, String captchaText) {
        authenticationSessionModel = context.getAuthenticationSession();
        return captchaService.getCaptchaTextByToken(captchaToken).equals(captchaText);
    }

    private void incrementFailedAttempts(AuthenticationFlowContext context) {
        int failedAttempts = getFailedAttempts(context) + 1;
        context.getAuthenticationSession().setAuthNote(ATTEMPTS_KEY, String.valueOf(failedAttempts));
    }

    private int getFailedAttempts(AuthenticationFlowContext context) {
        String attemptsStr = context.getAuthenticationSession().getAuthNote(ATTEMPTS_KEY);
        return attemptsStr == null ? 0 : Integer.parseInt(attemptsStr);
    }

    private void resetFailedAttempts(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(ATTEMPTS_KEY);
        context.getAuthenticationSession().removeAuthNote(CAPTCHA_REQUIRED_KEY);
    }

    private void setCaptchaRequired(AuthenticationFlowContext context, boolean required) {
        context.getAuthenticationSession().setAuthNote(CAPTCHA_REQUIRED_KEY, String.valueOf(required));
    }

    private boolean getCaptchaRequired(AuthenticationFlowContext context) {
        String captchaRequiredStr = context.getAuthenticationSession().getAuthNote(CAPTCHA_REQUIRED_KEY);
        return Boolean.parseBoolean(captchaRequiredStr);
    }

    private void setMaxFailedAttempts(AuthenticationFlowContext context, int maxFailedAttempts) {
        context.getAuthenticationSession().setAuthNote(MAX_FAILED_ATTEMPTS, String.valueOf(maxFailedAttempts));
    }

    private String getMaxFailedAttempts(AuthenticationFlowContext context) {
        return context.getAuthenticationSession().getAuthNote(MAX_FAILED_ATTEMPTS);
    }
}

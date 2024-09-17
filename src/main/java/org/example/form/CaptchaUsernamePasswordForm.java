package org.example.form;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.entity.CaptchaEntity;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * @author EL JED khalil
 */
public class CaptchaUsernamePasswordForm extends UsernamePasswordForm {
    private static final String ATTEMPTS_KEY = "failedAttempts";
    private static final String CAPTCHA_REQUIRED_KEY = "captchaRequired";
    private static final String CAPTCHA_IMAGE = "captchaImage";
    private static final String CAPTCHA_AUDIO = "captchaAudio";
    private static final String CAPTCHA_TOKEN = "captchaToken";
    private static final String CAPTCHA_REGENERATION = "X-Captcha-Regen";
    private static final String MAX_FAILED_ATTEMPTS = "MAX_FAILED_ATTEMPTS";
    public static final String AUDIO_FOLDER = "audio/";
    public static final String WAV_EXTENSION = ".wav";
    public static final String SERIF_FONT_FILEPATH = "fonts/DejaVuSans.ttf";

    public CaptchaUsernamePasswordForm()
    {
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
        String captchaRegen = context.getSession().getContext().getRequestHeaders().getHeaderString(CAPTCHA_REGENERATION);
        if(captchaRegen != null && captchaRegen.equals("true"))
        {
            CaptchaEntity captcha = generateCaptcha(context);
            context.form().setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
            context.form().setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
            context.form().setAttribute(CAPTCHA_TOKEN, captcha.getToken());
            context.form().setAttribute(CAPTCHA_REQUIRED_KEY, true);
            Response challengeResponse = challenge(context, formData);
            context.challenge(challengeResponse);
            return;
        }
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
                CaptchaEntity captcha = generateCaptcha(context);
                form.setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
                form.setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
                form.setAttribute(CAPTCHA_TOKEN, captcha.getToken());
                form.setAttribute(CAPTCHA_REQUIRED_KEY, true);
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
        boolean captchaRequired = getCaptchaRequired(context);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String captchaRegen = context.getSession().getContext().getRequestHeaders().getHeaderString(CAPTCHA_REGENERATION);
        if(captchaRequired && captchaRegen != null && captchaRegen.equals("true")) {
            CaptchaEntity captcha = generateCaptcha(context);
            context.form().setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
            context.form().setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
            context.form().setAttribute(CAPTCHA_TOKEN, captcha.getToken());
            context.form().setAttribute(CAPTCHA_REQUIRED_KEY, true);
            Response challengeResponse = challenge(context, formData);
            context.challenge(challengeResponse);
            return;
        }
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
            CaptchaEntity captcha = generateCaptcha(context);
            context.form().setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
            context.form().setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
            context.form().setAttribute(CAPTCHA_TOKEN, captcha.getToken());
            context.form().setAttribute(CAPTCHA_REQUIRED_KEY, true);
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
        CaptchaEntity captcha = generateCaptcha(context);
        context.form().setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
        context.form().setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
        context.form().setAttribute(CAPTCHA_TOKEN, captcha.getToken());
        context.form().setAttribute(CAPTCHA_REQUIRED_KEY, captchaRequired);
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    protected boolean validateCaptcha(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        String captchaToken = formData.getFirst("captcha_token");
        String captchaText = formData.getFirst("captcha_text");
        if (captchaToken == null || captchaText == null || !isValid(context,captchaToken, captchaText)) {
            context.getEvent().error("invalid_captcha");
            boolean captchaRequired = getCaptchaRequired(context);
            if(captchaRequired) {
                CaptchaEntity captcha = generateCaptcha(context);
                context.form().setExecution(context.getExecution().getId()).setAttribute(CAPTCHA_IMAGE, captcha.getCaptchaImage());
                context.form().setExecution(context.getExecution().getId()).setAttribute(CAPTCHA_AUDIO, captcha.getCaptchaAudio());
                context.form().setExecution(context.getExecution().getId()).setAttribute(CAPTCHA_TOKEN, captcha.getToken());
            }
            context.form().setAttribute(CAPTCHA_REQUIRED_KEY, captchaRequired);
            Response challenge = context.form().setError("Invalid captcha").createLoginUsernamePassword();
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return false;
        }
        return true;
    }

    public boolean isValid(AuthenticationFlowContext context,String captchaToken, String captchaText) {
        return getCaptchaTextByToken(context,captchaToken).equals(captchaText);
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
    public void setCaptchaTextByToken(AuthenticationFlowContext context, String token, String captchaText) {
        context.getAuthenticationSession().setAuthNote(token, captchaText);
    }

    public String getCaptchaTextByToken(AuthenticationFlowContext context, String token) {
        return context.getAuthenticationSession().getAuthNote(token);
    }
    public static String convertWordToAudio(String word) {
        AudioFormat audioFormat = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for (int i = 0; i < word.length(); i++) {
                String fileName = AUDIO_FOLDER + word.charAt(i) + WAV_EXTENSION;
                try (InputStream soundFile = CaptchaUsernamePasswordForm.class.getClassLoader().getResourceAsStream(fileName)) {
                    assert soundFile != null;
                    try (BufferedInputStream bufferedStream = new BufferedInputStream(soundFile)) {
                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);

                        if (audioFormat == null) {
                            audioFormat = audioInputStream.getFormat();
                        }

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }

                        audioInputStream.close();
                    }
                }
            }
            byte[] concatenatedAudioData = byteArrayOutputStream.toByteArray();
            // Write the concatenated audio data to the output file
            if (audioFormat != null) {
                AudioInputStream concatenatedAudioInputStream =
                        new AudioInputStream(
                                new ByteArrayInputStream(concatenatedAudioData),
                                audioFormat,
                                concatenatedAudioData.length / audioFormat.getFrameSize());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                AudioSystem.write(concatenatedAudioInputStream, AudioFileFormat.Type.WAVE, baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static String generateImage(String text) {
        int w = 180, h = 40;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);

        try {
            InputStream fontFileStream = CaptchaUsernamePasswordForm.class.getClassLoader().getResourceAsStream(SERIF_FONT_FILEPATH);
            g.setFont(Font.createFont(Font.TRUETYPE_FONT, fontFileStream).deriveFont(26f));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        g.setColor(Color.blue);
        int start = 10;
        byte[] bytes = text.getBytes();

        Random random = new Random();
        for (int i = 0; i < bytes.length; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawString(new String(new byte[]{bytes[i]}), start + (i * 20), (int) (Math.random() * 20 + 20));
        }
        g.setColor(Color.white);
        for (int i = 0; i < 8; i++) {
            g.drawOval((int) (Math.random() * 160), (int) (Math.random() * 10), 30, 30);
        }
        g.dispose();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bout);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bout.toByteArray());
    }
    public CaptchaEntity generateCaptcha(AuthenticationFlowContext context) {
        String captchaText = new StringTokenizer(UUID.randomUUID().toString(), "-").nextToken();
        String token = UUID.randomUUID().toString();
        setCaptchaTextByToken(context,token,captchaText);
        return CaptchaEntity.builder()
                .captchaImage(generateImage(captchaText))
                .token(token)
                .text(captchaText)
                .captchaAudio(
                        convertWordToAudio(captchaText)).build();
    }
}

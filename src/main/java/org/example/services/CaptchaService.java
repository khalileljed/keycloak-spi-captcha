package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.form.CaptchaUsernamePasswordForm;

@ApplicationScoped
public class CaptchaService {
    public void setCaptchaTextByToken(String token, String captchaText) {
            CaptchaUsernamePasswordForm.authenticationSessionModel.setAuthNote(token, captchaText);
    }

    public String getCaptchaTextByToken(String token) {
            return CaptchaUsernamePasswordForm.authenticationSessionModel.getAuthNote(token);
    }
}


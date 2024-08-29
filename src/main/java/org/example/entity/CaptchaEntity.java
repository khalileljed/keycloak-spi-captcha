package org.example.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CaptchaEntity {
    private byte[] captchaImage;
    private String captchaAudio;
    private String text;
    private String token;
}

package org.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CaptchaEntity;
import org.example.services.CaptchaService;
import org.keycloak.services.resource.RealmResourceProvider;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class CaptchaResourceProvider implements RealmResourceProvider {
    public static final String AUDIO_FOLDER = "audio/";
    public static final String WAV_EXTENSION = ".wav";
    public static final String SERIF_FONT_FILEPATH = "fonts/DejaVuSans.ttf";
    @Inject
    public CaptchaService captchaService;
    public CaptchaResourceProvider(CaptchaService captchaService)
    {
        this.captchaService = captchaService;
    }
    public static String convertWordToAudio(String word) {
        AudioFormat audioFormat = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            for (int i = 0; i < word.length(); i++) {
                String fileName = AUDIO_FOLDER + word.charAt(i) + WAV_EXTENSION;
                try (InputStream soundFile = CaptchaResourceProvider.class.getClassLoader().getResourceAsStream(fileName)) {
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

    public static byte[] generateImage(String text) {
        int w = 180, h = 40;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);

        try {
            InputStream fontFileStream = CaptchaResourceProvider.class.getClassLoader().getResourceAsStream(SERIF_FONT_FILEPATH);
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
        return bout.toByteArray();
    }

    @GET
    @Path("regenerateCaptcha/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public CaptchaEntity regenerateCaptcha(@PathParam("token") String captchaToken) {
        String captchaText = new StringTokenizer(UUID.randomUUID().toString(), "-").nextToken();
        captchaService.setCaptchaTextByToken(captchaToken,captchaText);

                return CaptchaEntity.builder()
                        .captchaImage(generateImage(captchaText))
                        .token(captchaToken)
                        .captchaAudio(
                                convertWordToAudio(captchaText)).build();
    }

    @GET
    @Path("generateCaptcha")
    @Produces(MediaType.APPLICATION_JSON)
    public CaptchaEntity generateCaptcha() {
        String captchaText = new StringTokenizer(UUID.randomUUID().toString(), "-").nextToken();
        String token = UUID.randomUUID().toString();
        captchaService.setCaptchaTextByToken(token,captchaText);
        return CaptchaEntity.builder().captchaImage(generateImage(captchaText))
                .token(token)
                .captchaAudio(
                        convertWordToAudio(captchaText)).build();
    }

    @Override
    public void close() {
        // Clean up resources if necessary
    }

    @Override
    public Object getResource() {
        return this; // Return the current instance which implements MyCustomResourceProvider
    }
}
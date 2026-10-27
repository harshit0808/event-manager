package com.example.event_manager.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendRegistrationConfirmation(String toEmail, String eventTitle, byte[] qrCodeImage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(toEmail);
            helper.setSubject("Registration Confirmed: " + eventTitle);

            // Escape the title: it is user-supplied and goes into an HTML email.
            String htmlContent = "<h1>Registration Confirmed!</h1>" +
                    "<p>You are officially registered for <b>" + HtmlUtils.htmlEscape(eventTitle) + "</b>.</p>" +
                    "<p>Please show the attached QR code at the event entrance.</p>" +
                    "<p>Thank you!</p>" +
                    "<img src='cid:registrationQR'>";

            helper.setText(htmlContent, true);
            helper.addInline("registrationQR", new ByteArrayResource(qrCodeImage), "image/png");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Email problems must never break a registration; just log them.
            log.error("Failed to send registration email to {}: {}", toEmail, e.getMessage());
        }
    }
}

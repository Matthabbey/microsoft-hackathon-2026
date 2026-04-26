package com.ai.hackathon.telecom.operations.platform.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName = (emailTemplate != null)
                ? emailTemplate.getName()
                : "confirm-email";

        // Create a new MIME message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

        // Prepare the email template context
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        // Set email parameters
        helper.setFrom("jamesugwuodoke@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        // Generate the email content using Thymeleaf template
        String content = templateEngine.process(templateName, context);
        helper.setText(content, true);

        // Send the email
        try {
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", to);
        } catch (MailSendException e) {
            log.error("Failed to send email to {} due to: {}", to, e.getMessage());
            throw e;
        }
    }}
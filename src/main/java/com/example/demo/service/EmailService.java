package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public void sendEmail(
            String to,
            String subject,
            String message) {

        if (!mailEnabled) {
            System.out.println(
                "Email disabled. Would send to: " + to
            );
            return;
        }

        if (to == null || to.isBlank()) {
            return;
        }

        try {
            SimpleMailMessage mail =
                    new SimpleMailMessage();

            mail.setFrom(senderEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            System.out.println(
                "Email sent successfully to: " + to
            );

        } catch (Exception e) {

            System.out.println(
                "Email sending failed: "
                + e.getMessage()
            );
        }
    }

    public void sendRegistrationConfirmation(
            String studentName,
            String studentEmail,
            String eventTitle,
            String eventDate,
            String venue) {

        String subject =
                "Registration Confirmed - "
                + eventTitle;

        String message =
                "Hello " + studentName + ",\n\n"
                + "Your registration for the following "
                + "college event has been confirmed.\n\n"
                + "Event: " + eventTitle + "\n"
                + "Date: " + eventDate + "\n"
                + "Venue: " + venue + "\n\n"
                + "Please be present at the venue "
                + "on time.\n\n"
                + "Regards,\n"
                + "Smart College Event Management Portal";

        sendEmail(
                studentEmail,
                subject,
                message
        );
    }
}
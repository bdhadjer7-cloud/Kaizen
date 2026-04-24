package service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private final Map<String, String> smtpConfig = new LinkedHashMap<>();
    private final String senderEmail;
    private final String senderPassword;


    public EmailService(String senderEmail, String senderPassword) {
        this.senderEmail    = senderEmail;
        this.senderPassword = senderPassword;


        smtpConfig.put("mail.smtp.auth",              "true");
        smtpConfig.put("mail.smtp.starttls.enable",   "true");
        smtpConfig.put("mail.smtp.host",              "smtp.gmail.com");
        smtpConfig.put("mail.smtp.port",              "587");
        smtpConfig.put("mail.smtp.ssl.trust",         "smtp.gmail.com");
        smtpConfig.put("mail.smtp.connectiontimeout", "5000");
        smtpConfig.put("mail.smtp.timeout",           "5000");
    }


    private Session buildSession() {
        Properties props = new Properties();
        props.putAll(smtpConfig);                    // Map → Properties

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
    }

    public void sendTeacherVerification(String toEmail, String name, String code)
            throws MessagingException {
        sendEmail(
                toEmail,
                "Kaizen — Confirm your Teacher account",
                buildTeacherVerificationEmail(name, code)
        );
        LOG.info("[EMAIL] Teacher verification sent to: " + toEmail);
    }


    public void sendPasswordReset(String toEmail, String name, String code)
            throws MessagingException {
        sendEmail(
                toEmail,
                "Kaizen — Password Reset Code",
                buildPasswordResetEmail(name, code)
        );
        LOG.info("[EMAIL] Password reset sent to: " + toEmail);
    }


    public void sendDeadlineReminder(String toEmail, String name,
                                     String courseName, String deadline)
            throws MessagingException {
        sendEmail(
                toEmail,
                "Kaizen — Deadline tomorrow: " + courseName,
                buildDeadlineEmail(name, courseName, deadline)
        );
        LOG.info("[EMAIL] Deadline reminder sent to: " + toEmail);
    }

    public <T extends String> void sendToMany(List<T> recipients,
                                              String subject,
                                              String htmlBody) {
        Map<String, String> results = new LinkedHashMap<>();
        for (T email : recipients) {
            try {
                sendEmail(email, subject, htmlBody);
                results.put(email, "SENT");
            } catch (MessagingException e) {
                results.put(email, "FAILED: " + e.getMessage());
                LOG.warning("[EMAIL] Failed to send to: " + email);
            }
        }
        LOG.info("[EMAIL] Bulk send results: " + results);
    }

    public void sendEmail(String toEmail, String subject, String htmlBody)
            throws MessagingException {
        Session session = buildSession();

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(senderEmail, "Kaizen App", "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(senderEmail));
        }
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlBody, "text/html; charset=utf-8");
        message.setSentDate(new Date());

        Transport.send(message);
    }


    private String buildTeacherVerificationEmail(String name, String code) {
        return "<!DOCTYPE html><html><body style='"
                + "font-family:Arial,sans-serif;"
                + "background:#EDF1F6;"
                + "padding:30px;margin:0'>"

                + "<div style='"
                + "max-width:520px;margin:0 auto;"
                + "background:white;"
                + "border-radius:16px;"
                + "overflow:hidden;"
                + "border:1px solid rgba(8,31,92,0.08)'>"


                + "<div style='"
                + "background:#081F5C;"
                + "padding:32px 28px;"
                + "text-align:center'>"
                + "<h1 style='color:white;font-size:28px;margin:0;letter-spacing:-0.5px'>Kaizen 改善</h1>"
                + "<p style='color:#7096D1;margin:6px 0 0;font-size:14px'>"
                + "Collaborative Study Platform</p>"
                + "</div>"


                + "<div style='padding:32px 28px'>"
                + "<h2 style='color:#081F5C;margin:0 0 12px'>Hello, " + name + " 👋</h2>"
                + "<p style='color:#444;line-height:1.7;margin:0 0 20px'>"
                + "You requested a <strong>Teacher account</strong> on Kaizen. "
                + "Enter the verification code below to confirm your role.</p>"

                // Code box
                + "<div style='"
                + "background:#EDF1F6;"
                + "border-radius:12px;"
                + "padding:24px;"
                + "text-align:center;"
                + "margin:0 0 20px'>"
                + "<p style='color:#7096D1;font-size:13px;margin:0 0 10px;"
                + "text-transform:uppercase;letter-spacing:1px'>Verification Code</p>"
                + "<h1 style='"
                + "color:#081F5C;"
                + "font-size:48px;"
                + "letter-spacing:14px;"
                + "margin:0;"
                + "font-family:monospace'>" + code + "</h1>"
                + "<p style='color:#7096D1;font-size:12px;margin:10px 0 0'>"
                + "⏱ Expires in 15 minutes</p>"
                + "</div>"


                + "<div style='"
                + "background:#FFF9F0;"
                + "border-left:3px solid #334EAC;"
                + "border-radius:0 8px 8px 0;"
                + "padding:12px 16px'>"
                + "<p style='color:#444;font-size:13px;margin:0'>"
                + "If you didn't request this, ignore this email. "
                + "Your account will remain as <strong>Student</strong>.</p>"
                + "</div>"
                + "</div>"

                // Footer
                + "<div style='"
                + "background:#EDF1F6;"
                + "padding:16px 28px;"
                + "text-align:center'>"
                + "<p style='color:#7096D1;font-size:12px;margin:0'>"
                + "Kaizen App · University Study Platform</p>"
                + "</div>"

                + "</div></body></html>";
    }

    private String buildPasswordResetEmail(String name, String code) {
        return "<!DOCTYPE html><html><body style='"
                + "font-family:Arial,sans-serif;background:#EDF1F6;padding:30px'>"
                + "<div style='max-width:520px;margin:0 auto;background:white;"
                + "border-radius:16px;overflow:hidden'>"
                + "<div style='background:#081F5C;padding:32px;text-align:center'>"
                + "<h1 style='color:white;margin:0'>Kaizen 改善</h1></div>"
                + "<div style='padding:32px'>"
                + "<h2 style='color:#081F5C'>Password Reset, " + name + " 🔐</h2>"
                + "<p style='color:#444;line-height:1.7'>"
                + "Enter this code in the app to reset your password.</p>"
                + "<div style='background:#EDF1F6;border-radius:12px;"
                + "padding:24px;text-align:center;margin:20px 0'>"
                + "<h1 style='color:#334EAC;font-size:48px;"
                + "letter-spacing:14px;margin:0;font-family:monospace'>" + code + "</h1>"
                + "<p style='color:#7096D1;font-size:12px;margin:10px 0 0'>"
                + "Expires in 15 minutes</p>"
                + "</div>"
                + "<p style='color:#888;font-size:12px'>"
                + "If you didn't request this, your account is safe. Ignore this email.</p>"
                + "</div></div></body></html>";
    }

    private String buildDeadlineEmail(String name, String course, String deadline) {
        return "<!DOCTYPE html><html><body style='"
                + "font-family:Arial,sans-serif;background:#EDF1F6;padding:30px'>"
                + "<div style='max-width:520px;margin:0 auto;background:white;"
                + "border-radius:16px;overflow:hidden'>"
                + "<div style='background:#081F5C;padding:32px;text-align:center'>"
                + "<h1 style='color:white;margin:0'>Kaizen 改善</h1></div>"
                + "<div style='padding:32px'>"
                + "<h2 style='color:#081F5C'>Don't forget, " + name + " ⏰</h2>"
                + "<p style='color:#444;line-height:1.7'>"
                + "You have a deadline coming up tomorrow!</p>"
                + "<div style='background:#FFF0E0;"
                + "border-left:4px solid #334EAC;"
                + "border-radius:0 10px 10px 0;"
                + "padding:16px 20px;margin:20px 0'>"
                + "<strong style='color:#081F5C;font-size:15px'>" + course + "</strong>"
                + "<p style='color:#444;margin:6px 0 0;font-size:13px'>"
                + "📅 Due: " + deadline + "</p>"
                + "</div>"
                + "<a href='#' style='display:inline-block;background:#334EAC;"
                + "color:white;padding:12px 24px;border-radius:8px;"
                + "text-decoration:none;font-weight:bold'>Open Kaizen App</a>"
                + "</div></div></body></html>";
    }

    public Map<String, String> getSmtpConfig() {
        return Collections.unmodifiableMap(smtpConfig);
    }

    public String getSenderEmail() { return senderEmail; }
}
package com.example.beskar.data;

import com.example.beskar.Beskar;
import com.example.beskar.util.Logger;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmailService {
    private static SendEmailService instance = null;

    private final String username = "INSERT_USERNAME";
    private final String password = "INSERT_PASSWORD";

    Properties prop;
    Session session;

    private SendEmailService() {
        prop = new Properties();
        prop.put("mail.smtp.host", "smtp.office365.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    public static synchronized SendEmailService getInstance() {
        if (instance == null) {
            instance = new SendEmailService();
        }
        return instance;
    }

    public void sendEmail() {
        String toEmail = Beskar.getPrefs().getString("beskar_email", "nil");
        boolean sendReport = Beskar.getPrefs().getBoolean("beskar_send_report", false);
        if (toEmail.equals("nil") || !sendReport) {
            Logger.debug("Email service not scheduled. toEmail: " +
                    toEmail + ", sendReport: " + (sendReport ? "true" : "false"));
            return;
        }
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Testing Email TLS");
            message.setText("Hello world!");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

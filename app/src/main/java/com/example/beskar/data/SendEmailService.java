package com.example.beskar.data;

import com.example.beskar.Beskar;
import com.example.beskar.util.Logger;

import java.util.List;
import java.util.Locale;
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
        // Check that email report is scheduled
        String toEmail = Beskar.getPrefs().getString("beskar_email", "nil");
        boolean sendReport = Beskar.getPrefs().getBoolean("beskar_send_report", false);

        if (toEmail.equals("nil") || !sendReport) {
            Logger.debug("Email service not scheduled. toEmail: " +
                    toEmail + ", sendReport: " + (sendReport ? "true" : "false"));
            return;
        }

        try {
            // Get data synchronously from database
            AppDatabase db = AppDatabase.getDatabase(Beskar.getInstance());
            List<DateTimeInteractions> allConfigChanges =
                    db.interactionsDao().getAllWithInteractionFrom7dAgoSynchronous(Interactions.CONFIG_CHANGE);
            List<DateTimeInteractions> allSwitchedOff =
                    db.interactionsDao().getAllWithInteractionFrom7dAgoSynchronous(Interactions.SWITCHED_OFF);
            List<DateTimeLocalResolve> allBlockedSites =
                    db.localResolveDao().getAllWithResolutionFrom7dAgoSynchronous(LocalResolve.ONE_RES);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Your accountability partner's weekly report from Brick");
            message.setText(generateMessage(allConfigChanges, allSwitchedOff, allBlockedSites));
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateMessage(List<DateTimeInteractions> allSwitchedOff,
                                   List<DateTimeInteractions> allConfigChanges,
                                   List<DateTimeLocalResolve> allBlockedSites) {
        String summary = String.format(Locale.getDefault(),
                "Your accountability partner's weekly usage statistics:\n" +
                "- Total number of times the VPN was switched off: %1$d\n" +
                "- Total number of times a configuration change was made: %2$d\n" +
                "- Total blocked adult sites: %3$d\n" +
                "\n", allSwitchedOff.size(), allConfigChanges.size(), allBlockedSites.size());

        String switchedOffHeader = "Here are all the times the VPN was switched off:\n";

        String configChangeHeader = "Here are all the times a configuration change was made:\n";

        String blockedSitesHeader = "Here are all the blocked adult sites with attempted " +
                "access:\n";

        return new StringBuilder()
                .append(summary)
                .append(switchedOffHeader)
                .append(generateInteractionsTable(allSwitchedOff))
                .append(configChangeHeader)
                .append(generateInteractionsTable(allConfigChanges))
                .append(blockedSitesHeader)
                .append(generateLocalResolvesTable(allBlockedSites))
                .toString();
    }

    private String generateInteractionsTable(List<DateTimeInteractions> interactions) {
        StringBuilder sb = new StringBuilder();
        for (DateTimeInteractions item : interactions) {
            sb.append(generateRow(item.getDateTime(), item.getDescription()));
        }
        return sb.append("\n").toString();
    }

    private String generateLocalResolvesTable(List<DateTimeLocalResolve> localResolves) {
        StringBuilder sb = new StringBuilder();
        for (DateTimeLocalResolve item : localResolves) {
            sb.append(generateRow(item.getDatetime(), item.getDomain()));
        }
        return sb.append("\n").toString();
    }

    private String generateRow(String timestamp, String detail) {
        return new StringBuilder()
                .append("- At ")
                .append(timestamp)
                .append(": ")
                .append(detail)
                .append("\n")
                .toString();
    }
}

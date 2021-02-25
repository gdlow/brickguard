package com.example.beskar.data;

import android.content.Context;

import com.example.beskar.Beskar;
import com.example.beskar.R;
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

    private String EMAIL_U;
    private String EMAIL_P;

    Properties prop;
    Session session;

    private SendEmailService(Context context) {
        EMAIL_U = context.getString(R.string.email_u);
        EMAIL_P = context.getString(R.string.email_p);

        prop = new Properties();
        prop.put("mail.smtp.host", "smtp.office365.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_U, EMAIL_P);
                    }
                });
    }

    public static synchronized SendEmailService getInstance(Context context) {
        if (instance == null) {
            instance = new SendEmailService(context);
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
            long currentStreakDays = Beskar.getPrefs().getLong("beskar_current_time_delta", 0);
            long longestStreakDays = Beskar.getPrefs().getLong("beskar_longest_time_delta", 0);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_U));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Your accountability partner's weekly report from Brick");
            message.setContent(generateMessage(currentStreakDays, longestStreakDays,
                    allBlockedSites, allSwitchedOff, allConfigChanges)
                    , "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateMessage(long currentStreakDays, long longestStreakDays,
                                   List<DateTimeLocalResolve> allBlockedSites,
                                   List<DateTimeInteractions> allSwitchedOff,
                                   List<DateTimeInteractions> allConfigChanges) {
        String emailHeader = "<h2>Your accountability partner's weekly usage statistics</h2>\n";
        String summary = String.format(Locale.getDefault(),
                "<h4>Summary:</h4>\n" +
                "  <ul>\n" +
                "    <li>Current streak: %1$d days\n</li>" +
                "    <li>Longest streak: %2$d days\n</li>" +
                "    <li>Total blocked adult sites: %3$d\n</li>" +
                "    <li>Total number of times the VPN was switched off: %4$d</li>\n" +
                "    <li>Total number of times a configuration change was made: %5$d</li>\n" +
                "  </ul>\n",
                currentStreakDays, longestStreakDays, allBlockedSites.size(), allSwitchedOff.size(),
                allConfigChanges.size());

        String blockedSitesHeader = "<h4>Here are all the blocked adult sites with attempted " +
                "access:</h4>\n";

        String switchedOffHeader = "<h4>Here are all the times the VPN was switched off:</h4>\n";

        String configChangeHeader = "<h4>Here are all the times a configuration change was made:</h4>\n";

        return new StringBuilder()
                .append(emailHeader)
                .append(summary)
                .append(blockedSitesHeader)
                .append(generateLocalResolvesTable(allBlockedSites))
                .append(switchedOffHeader)
                .append(generateInteractionsTable(allSwitchedOff))
                .append(configChangeHeader)
                .append(generateInteractionsTable(allConfigChanges))
                .toString();
    }

    private String generateInteractionsTable(List<DateTimeInteractions> interactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <ul>\n");
        for (DateTimeInteractions item : interactions) {
            sb.append(generateRow(item.getDateTime(), item.getDescription()));
        }
        return sb.append("  </ul>\n").toString();
    }

    private String generateLocalResolvesTable(List<DateTimeLocalResolve> localResolves) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <ul>\n");
        for (DateTimeLocalResolve item : localResolves) {
            sb.append(generateRow(item.getDatetime(), item.getDomain()));
        }
        return sb.append("  </ul>\n").toString();
    }

    private String generateRow(String timestamp, String detail) {
        return new StringBuilder()
                .append("    <li>At ")
                .append(timestamp)
                .append(": ")
                .append(detail)
                .append("</li>\n")
                .toString();
    }
}

package com.gdlow.brickguard.data;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.util.Logger;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TrackOpens;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.MessageResult;
import com.mailjet.client.transactional.response.SendEmailsResponse;

import java.util.List;
import java.util.Locale;

public class SendEmailService {

    private static SendEmailService instance = null;

    // Metadata
    private static final String FROM_NAME = "BrickGuard Admin";
    private static final String TO_NAME = "Accountability Partner";
    private static final String CUSTOM_ID = "weekly_report";

    private SendEmailService() {}

    public static synchronized SendEmailService getInstance() {
        if (instance == null) {
            instance = new SendEmailService();
        }
        return instance;
    }

    public void sendEmail(String apiKey, String apiSecret, String senderEmail) {
        // Check that email report is scheduled
        String toEmail = BrickGuard.getPrefs().getString("brickguard_email", "nil");
        boolean sendReport = BrickGuard.getPrefs().getBoolean("brickguard_send_report", false);

        if (toEmail.equals("nil") || !sendReport) {
            Logger.debug("Email service not scheduled. toEmail: " +
                    toEmail + ", sendReport: " + (sendReport ? "true" : "false"));
            return;
        }

        // Get data synchronously from database
        AppDatabase db = AppDatabase.getDatabase(BrickGuard.getInstance());
        List<DateTimeInteractions> allConfigChanges =
                db.interactionsDao().getAllWithInteractionFrom7dAgoSynchronous(Interactions.CONFIG_CHANGE);
        List<DateTimeInteractions> allSwitchedOff =
                db.interactionsDao().getAllWithInteractionFrom7dAgoSynchronous(Interactions.SWITCHED_OFF);
        List<DateTimeLocalResolve> allBlockedSites =
                db.localResolveDao().getAllWithResolutionFrom7dAgoSynchronous(LocalResolve.ONE_RES);
        long currentStreakDays = BrickGuard.getPrefs().getLong("brickguard_current_time_delta", 0);
        long longestStreakDays = BrickGuard.getPrefs().getLong("brickguard_longest_time_delta", 0);

        // Send email via API
        String htmlContent = generateMessage(currentStreakDays, longestStreakDays,
                allBlockedSites, allSwitchedOff, allConfigChanges);
        String subject = "Your accountability partner's weekly report from BrickGuard";
        sendEmailHelperApi(apiKey, apiSecret, senderEmail, toEmail, subject, htmlContent);
    }

    private void sendEmailHelperApi(String apiKey, String apiSecret, String senderEmail,
                                    String toEmail, String subject, String htmlContent) {
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecret)
                .build();

        MailjetClient client = new MailjetClient(options);
        TransactionalEmail message = TransactionalEmail
                .builder()
                .from(new SendContact(senderEmail, FROM_NAME))
                .to(new SendContact(toEmail, TO_NAME))
                .htmlPart(htmlContent)
                .subject(subject)
                .trackOpens(TrackOpens.ENABLED)
                .customID(CUSTOM_ID)
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message)
                .build();

        try {
            SendEmailsResponse response = request.sendWith(client);
            MessageResult messageResult = response.getMessages()[0];
            Logger.debug("Mailjet response status: " + messageResult.getStatus());
        } catch (MailjetException e) {
            Logger.error("Mailjet exception error: " + e.getMessage());
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
                "    <li>Total blocked adult and custom sites: %3$d\n</li>" +
                "    <li>Total number of times the VPN service was deactivated: %4$d</li>\n" +
                "    <li>Total number of times a configuration change was made: %5$d</li>\n" +
                "  </ul>\n",
                currentStreakDays, longestStreakDays, allBlockedSites.size(), allSwitchedOff.size(),
                allConfigChanges.size());

        String blockedSitesHeader = "<h4>Here are all the blocked adult and custom sites with " +
                "attempted access:</h4>\n";

        String switchedOffHeader = "<h4>Here are all the times the VPN service was deactivated:</h4>\n";

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

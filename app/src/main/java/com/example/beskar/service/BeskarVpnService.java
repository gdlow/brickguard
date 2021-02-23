package com.example.beskar.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.BackoffPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.beskar.Beskar;
import com.example.beskar.MainActivity;
import com.example.beskar.R;
import com.example.beskar.data.StreakWorker;
import com.example.beskar.provider.Provider;
import com.example.beskar.provider.ProviderPicker;
import com.example.beskar.receiver.StatusBarBroadcastReceiver;
import com.example.beskar.server.AbstractDnsServer;
import com.example.beskar.server.DnsServer;
import com.example.beskar.server.DnsServerHelper;
import com.example.beskar.util.DnsServersDetector;
import com.example.beskar.util.Logger;
import com.example.beskar.util.RuleResolver;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class BeskarVpnService extends VpnService implements Runnable {
    public static final String ACTION_ACTIVATE = "com.example.beskar.service.BeskarVpnService.ACTION_ACTIVATE";
    public static final String ACTION_DEACTIVATE = "com.example.beskar.service.BeskarVpnService.ACTION_DEACTIVATE";

    private static final int NOTIFICATION_ACTIVATED = 1;

    private static final String TAG = "BeskarVpnService";
    private static final String CHANNEL_ID = "Beskar_channel_1";
    private static final String CHANNEL_NAME = "Beskar_channel";

    public static AbstractDnsServer primaryServer;
    public static AbstractDnsServer secondaryServer;
    private static InetAddress aliasPrimary;
    private static InetAddress aliasSecondary;

    private NotificationCompat.Builder notification = null;
    private Provider provider;
    private ParcelFileDescriptor descriptor;
    private Thread mThread = null;
    public HashMap<String, AbstractDnsServer> dnsServers;
    private WorkManager mWorkManager;
    private static boolean activated = false;
    private static BroadcastReceiver receiver;

    public static boolean isActivated() {
        return activated;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Beskar.getPrefs().getBoolean("settings_use_system_dns", false)) {
            registerReceiver(receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateUpstreamToSystemDNS(context);
                }
            }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        mWorkManager = WorkManager.getInstance(getApplication());
    }

    public static void updateUpstreamToSystemDNS(Context context) {
        // Only trigger if service is on
        if (!isActivated()) return;

        String[] servers = DnsServersDetector.getServers(context);
        if (servers != null) {
            if (servers.length >= 2 && (aliasPrimary == null || !aliasPrimary.getHostAddress().equals(servers[0])) &&
                    (aliasSecondary == null || !aliasSecondary.getHostAddress().equals(servers[0])) &&
                    (aliasPrimary == null || !aliasPrimary.getHostAddress().equals(servers[1])) &&
                    (aliasSecondary == null || !aliasSecondary.getHostAddress().equals(servers[1]))) {
                primaryServer.setAddress(servers[0]);
                primaryServer.setPort(DnsServer.DNS_SERVER_DEFAULT_PORT);
                secondaryServer.setAddress(servers[1]);
                secondaryServer.setPort(DnsServer.DNS_SERVER_DEFAULT_PORT);
            } else if ((aliasPrimary == null || !aliasPrimary.getHostAddress().equals(servers[0])) &&
                    (aliasSecondary == null || !aliasSecondary.getHostAddress().equals(servers[0]))) {
                primaryServer.setAddress(servers[0]);
                primaryServer.setPort(DnsServer.DNS_SERVER_DEFAULT_PORT);
                secondaryServer.setAddress(servers[0]);
                secondaryServer.setPort(DnsServer.DNS_SERVER_DEFAULT_PORT);
            } else {
                StringBuilder buf = new StringBuilder();
                for (String server : servers) {
                    buf.append(server).append(" ");
                }
                Logger.error("Invalid upstream DNS " + buf);
                updateUpstreamToGoogleDns();
            }
            Logger.info("Upstream DNS updated to system default: " + primaryServer.getAddress() + " " + secondaryServer.getAddress());
        } else {
            Logger.error("Cannot obtain upstream DNS server!");
            updateUpstreamToGoogleDns();
        }
    }

    private static void updateUpstreamToGoogleDns() {
        primaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getGoogle()).clone();
        secondaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getGoogle()).clone();
    }

    private void schedulePeriodicUpdateStreak() {
        if (mWorkManager == null) {
            Logger.error("mWorkManager is not initialized. Streak worker will not run.");
            return;
        }

        // Define periodic sync work
        PeriodicWorkRequest periodicSyncDataWork =
                new PeriodicWorkRequest.Builder(StreakWorker.class, 1, TimeUnit.DAYS)
                        .addTag(StreakWorker.TAG_UPDATE_STREAK)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                PeriodicWorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .build();

        // Enqueue periodic work
        mWorkManager.enqueue(periodicSyncDataWork);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // Start periodic worker
            schedulePeriodicUpdateStreak();
            switch (intent.getAction()) {
                case ACTION_ACTIVATE:
                    activated = true;

                    // Build notification
                    NotificationManager manager =
                            (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                                CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                        manager.createNotificationChannel(channel);
                        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
                    } else {
                        builder = new NotificationCompat.Builder(this);
                    }

                    Intent deactivateIntent =
                            new Intent(StatusBarBroadcastReceiver.STATUS_BAR_BTN_DEACTIVATE_CLICK_ACTION);
                    deactivateIntent.setClass(this, StatusBarBroadcastReceiver.class);
                    Intent settingsIntent =
                            new Intent(StatusBarBroadcastReceiver.STATUS_BAR_BTN_SETTINGS_CLICK_ACTION);
                    settingsIntent.setClass(this, StatusBarBroadcastReceiver.class);
                    PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                            new Intent(this, MainActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setWhen(0)
                            .setContentTitle(getResources().getString(R.string.notice_activated))
                            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                            .setSmallIcon(R.drawable.ic_brick_toolbar)
                            .setColor(getResources().getColor(R.color.black_a80)) //backward
                            // compatibility
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setTicker(getResources().getString(R.string.notice_activated))
                            .setContentIntent(pIntent)
                            .addAction(R.drawable.ic_clear,
                                    getResources().getString(R.string.notification_text_deactivate),
                                    PendingIntent.getBroadcast(this, 0,
                                            deactivateIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .addAction(R.drawable.ic_settings,
                                    getResources().getString(R.string.action_settings),
                                    PendingIntent.getBroadcast(this, 0,
                                            settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
                    this.notification = builder;
                    startForeground(NOTIFICATION_ACTIVATED, notification.build());

                    Beskar.initRuleResolver();
                    startThread();
                    Beskar.updateShortcut(getApplicationContext());

                    if (MainActivity.getInstance() != null) {
                        MainActivity.getInstance().startActivity(
                                new Intent(getApplicationContext(), MainActivity.class)
                                        .putExtra(MainActivity.LAUNCH_ACTION,
                                                MainActivity.LAUNCH_ACTION_SERVICE_DONE));
                    }

                    return START_STICKY;
                case ACTION_DEACTIVATE:
                    Beskar.getPrefs().edit().putLong("beskar_current_time_delta", 0).apply();
                    stopThread();
                    return START_NOT_STICKY;
            }
        }
        return START_NOT_STICKY;
    }

    private void startThread() {
        if (this.mThread == null) {
            this.mThread = new Thread(this, "BeskarVpn");
            this.mThread.start();
        }
    }

    @Override
    public void onDestroy() {
        stopThread();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void stopThread() {
        Log.d(TAG, "stopThread");
        activated = false;
        boolean shouldRefresh = false;
        try {
            if (this.descriptor != null) {
                this.descriptor.close();
                this.descriptor = null;
            }
            if (mThread != null) {
                shouldRefresh = true;
                if (provider != null) {
                    provider.shutdown();
                    mThread.interrupt();
                    provider.stop();
                } else {
                    mThread.interrupt();
                }
                mThread = null;
            }
            if (notification != null) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NOTIFICATION_ACTIVATED);
                notification = null;
            }
            dnsServers = null;
        } catch (Exception e) {
            Logger.logException(e);
        }
        stopSelf();

        if (shouldRefresh) {
            RuleResolver.clear();
            DnsServerHelper.clearCache();
            Logger.info("Beskar VPN service has stopped");
        }

        if (shouldRefresh && MainActivity.getInstance() != null) {
            MainActivity.getInstance().startActivity(new Intent(getApplicationContext(), MainActivity.class)
                    .putExtra(MainActivity.LAUNCH_ACTION, MainActivity.LAUNCH_ACTION_SERVICE_DONE));
        } else if (shouldRefresh) {
            Beskar.updateShortcut(getApplicationContext());
        }
    }

    @Override
    public void onRevoke() {
        stopThread();
    }

    private InetAddress addDnsServer(Builder builder, String format, byte[] ipv6Template, AbstractDnsServer addr)
            throws UnknownHostException {
        int size = dnsServers.size();
        size++;
        if (addr.getAddress().contains("/")) {//https uri
            String alias = String.format(format, size + 1);
            dnsServers.put(alias, addr);
            builder.addRoute(alias, 32);
            return InetAddress.getByName(alias);
        }
        InetAddress address = InetAddress.getByName(addr.getAddress());
        if (address instanceof Inet6Address && ipv6Template == null) {
            Log.i(TAG, "addDnsServer: Ignoring DNS server " + address);
        } else if (address instanceof Inet4Address) {
            String alias = String.format(format, size + 1);
            addr.setHostAddress(address.getHostAddress());
            dnsServers.put(alias, addr);
            builder.addRoute(alias, 32);
            return InetAddress.getByName(alias);
        } else if (address instanceof Inet6Address) {
            ipv6Template[ipv6Template.length - 1] = (byte) (size + 1);
            InetAddress i6addr = Inet6Address.getByAddress(ipv6Template);
            addr.setHostAddress(address.getHostAddress());
            dnsServers.put(i6addr.getHostAddress(), addr);
            return i6addr;
        }
        return null;
    }

    @Override
    public void run() {
        try {
            DnsServerHelper.buildCache();
            Builder builder = new Builder()
                    .setSession("Beskar")
                    .setConfigureIntent(PendingIntent.getActivity(this, 0,
                            new Intent(this, MainActivity.class).putExtra(MainActivity.LAUNCH_FRAGMENT, MainActivity.FRAGMENT_SETTINGS),
                            PendingIntent.FLAG_ONE_SHOT));

            String format = null;

            for (String prefix : new String[]{"10.0.0", "192.0.2", "198.51.100", "203.0.113", "192.168.50"}) {
                try {
                    builder.addAddress(prefix + ".1", 24);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                format = prefix + ".%d";
                break;
            }

            // turn on advanced - turns on local VPN tunnel by default
            byte[] ipv6Template = new byte[]{32, 1, 13, (byte) (184 & 0xFF), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            try {
                InetAddress addr = Inet6Address.getByAddress(ipv6Template);
                Log.d(TAG, "configure: Adding IPv6 address" + addr);
                builder.addAddress(addr, 120);
            } catch (Exception e) {
                Logger.logException(e);

                ipv6Template = null;
            }

            dnsServers = new HashMap<>();
            aliasPrimary = addDnsServer(builder, format, ipv6Template, primaryServer);
            aliasSecondary = addDnsServer(builder, format, ipv6Template, secondaryServer);

            Logger.info("Beskar VPN service is listening on " + primaryServer.getAddress() + " as " + aliasPrimary.getHostAddress());
            Logger.info("Beskar VPN service is listening on " + secondaryServer.getAddress() + " as " + aliasSecondary.getHostAddress());
            builder.addDnsServer(aliasPrimary).addDnsServer(aliasSecondary);

            builder.setBlocking(true);
            builder.allowFamily(OsConstants.AF_INET);
            builder.allowFamily(OsConstants.AF_INET6);

            descriptor = builder.establish();
            Logger.info("Beskar VPN service is started");

            provider = ProviderPicker.getProvider(descriptor, this);
            provider.start();
            provider.process();

        } catch (Exception e) {
            Logger.logException(e);
        } finally {
            stopThread();
        }
    }

    public static class VpnNetworkException extends Exception {
        public VpnNetworkException(String s) {
            super(s);
        }

        public VpnNetworkException(String s, Throwable t) {
            super(s, t);
        }
    }
}

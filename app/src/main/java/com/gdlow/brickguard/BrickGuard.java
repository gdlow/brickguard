package com.gdlow.brickguard;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;

import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.gdlow.brickguard.data.EmailReportWorker;
import com.gdlow.brickguard.data.Interactions;
import com.gdlow.brickguard.data.InteractionsRepository;
import com.gdlow.brickguard.data.LocalResolve;
import com.gdlow.brickguard.data.LocalResolveRepository;
import com.gdlow.brickguard.data.StreakWorker;
import com.gdlow.brickguard.server.AbstractDnsServer;
import com.gdlow.brickguard.server.DnsServer;
import com.gdlow.brickguard.server.DnsServerHelper;
import com.gdlow.brickguard.service.BrickGuardVpnService;
import com.gdlow.brickguard.util.Logger;
import com.gdlow.brickguard.util.PreferencesModel;
import com.gdlow.brickguard.util.Rule;
import com.gdlow.brickguard.util.RuleResolver;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BrickGuard extends Application {

    private static final String SHORTCUT_ID_ACTIVATE = "shortcut_activate";

    public static final List<DnsServer> DNS_SERVERS = new ArrayList<DnsServer>() {{
        add(new DnsServer("8.8.8.8", R.string.filter_none));
        add(new DnsServer("185.228.168.9", R.string.filter_low));
        add(new DnsServer("185.228.168.10", R.string.filter_medium));
        add(new DnsServer("185.228.168.168", R.string.filter_high));
        add(new DnsServer("208.67.222.123", R.string.filter_backup_primary));
        add(new DnsServer("208.67.220.123", R.string.filter_backup_secondary));
    }};

    public static List<Rule> dnsmasqRules = new ArrayList<Rule>() {{
        add(new Rule("chadmayfield/porn-top1m", "chadmayfield.dnsmasq",
                "https://raw.githubusercontent.com/chadmayfield/my-pihole-blocklists/master/lists" +
                        "/pi_blocklist_porn_top1m.list"));

        add(new Rule("notracking/hosts-blacklists", "notracking.dnsmasq",
                "https://raw.githubusercontent.com/notracking/hosts-blocklists/master/dnsmasq" +
                        "/dnsmasq.blacklist.txt"));
    }};

    public static HashMap<String, Boolean> customDomains = new HashMap<>();

    public static String rulePath;
    public static String logPath;

    private static BrickGuard instance;
    private SharedPreferences prefs;
    private Thread mResolver;

    // Stuff required for rule syncing
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // Repository required for interacting with db
    private InteractionsRepository mInteractionsRepository;
    private LocalResolveRepository mLocalResolveRepository;

    // Work manager required for scheduling periodic tasks
    private WorkManager mWorkManager;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        Logger.init();

        // Start resolver thread server
        mResolver = new Thread(new RuleResolver());
        mResolver.start();

        // Initalize repositories
        mInteractionsRepository = new InteractionsRepository(this);
        mLocalResolveRepository = new LocalResolveRepository(this);

        // Initialize all required data and preferences
        initData();

        // Schedule work
        mWorkManager = WorkManager.getInstance(this);
        scheduleSendEmailWork();
        scheduleUpdateStreakWork();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        dnsmasqRules.clear();
        dnsmasqRules = null;
        customDomains.clear();
        customDomains = null;

        instance = null;
        prefs = null;

        RuleResolver.shutdown();
        mResolver.interrupt();
        RuleResolver.clear();
        mResolver = null;

        Logger.shutdown();
    }

    private void initData() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getExternalFilesDir(null) != null) {
            rulePath = getExternalFilesDir(null).getPath() + "/rules/";
            logPath = getExternalFilesDir(null).getPath() + "/logs/";

            initDirectory(rulePath);
            initDirectory(logPath);
        }

        // Load preferences
        initPreferences();
    }

    private void initDirectory(String dir) {
        File directory = new File(dir);
        if (!directory.isDirectory()) {
            Logger.warning(dir + " is not a directory. Delete result: " + directory.delete());
        }
        if (!directory.exists()) {
            Logger.debug(dir + " does not exist. Create result: " + directory.mkdirs());
        }
    }

    private void initPreferences() {
        initUpstreamServers();
        initDnsmasqRules();
        initCustomDomains();
        commitChanges();
        Logger.debug("Loaded preferences from file.");
    }

    private void initUpstreamServers() {
        int sliderIdx = prefs.getInt("home_slider_index", 2);
        if (sliderIdx > 0) {
            prefs.edit().putBoolean("settings_use_system_dns", false).apply();
            prefs.edit().putString("primary_server", String.valueOf(sliderIdx)).apply();
            updateUpstreamServers();
        } else {
            prefs.edit().putBoolean("settings_use_system_dns", true).apply();
            BrickGuardVpnService.updateUpstreamToSystemDNS(getApplicationContext());
        }
    }

    private void initDnsmasqRules() {
        selectRule(dnsmasqRules.get(0), prefs.getBoolean("home_adult_switch_checked", true));
        selectRule(dnsmasqRules.get(1), prefs.getBoolean("home_ads_switch_checked", true));
    }

    private void initCustomDomains() {
        for (String domain : PreferencesModel.getCurrChipMap().keySet()) {
            selectCustomDomain(domain, true);
        }
    }

    public static void selectRule(Rule rule, boolean isUsing) {
        // Always download rules by default. Disable downloading rules in-flight.
        if (!rule.getDownloaded()) {
            instance.ruleSync(rule);
        }
        rule.setUsing(isUsing);
    }

    public static void selectCustomDomain(String domain, boolean isUsing) {
        customDomains.put(domain, isUsing);
    }

    // This should only be called once all changes are applied.
    public static void commitChanges() {
        RuleResolver.setPending();
    }

    public static Intent getServiceIntent(Context context) {
        return new Intent(context, BrickGuardVpnService.class);
    }

    public static boolean switchService() {
        if (BrickGuardVpnService.isActivated()) {
            deactivateService(instance);
            return false;
        } else {
            prepareAndActivateService(instance);
            return true;
        }
    }

    public static boolean prepareAndActivateService(Context context) {
        Intent intent = VpnService.prepare(context);
        if (intent != null) {
            return false;
        } else {
            activateService(context);
            return true;
        }
    }

    // Global entrypoint for all activations (boot + manual).
    // Client should call prepareAndActivateService over this method.
    public static void activateService(Context context) {
        // Start service in foreground if API >= 26
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            Logger.info("Starting VPN service in foreground.");
            context.startForegroundService(BrickGuard.getServiceIntent(context).setAction(BrickGuardVpnService.ACTION_ACTIVATE));
        } else {
            Logger.info("Starting VPN service in background.");
            context.startService(BrickGuard.getServiceIntent(context).setAction(BrickGuardVpnService.ACTION_ACTIVATE));
        }
    }

    public static void updateUpstreamServers() {
        BrickGuardVpnService.primaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getPrimary()).clone();
        BrickGuardVpnService.secondaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getSecondary()).clone();
        Logger.info("Upstream DNS set to: " + BrickGuardVpnService.primaryServer.getAddress() + " " + BrickGuardVpnService.secondaryServer.getAddress());
    }

    public static void insertLocalResolve(String dnsQueryName, String response) {
        if (instance == null || instance.mLocalResolveRepository == null) return;
        long timestamp = System.currentTimeMillis() / 1000L;
        instance.mLocalResolveRepository.insert(new LocalResolve(timestamp, dnsQueryName, response));
        Logger.debug("Inserted local resolve { " + dnsQueryName + ": " + response + " } " +
                "into database.");
    }

    public static void insertInteraction(String interaction, String description) {
        if (instance == null || instance.mInteractionsRepository == null) return;
        long timestamp = System.currentTimeMillis() / 1000L;
        instance.mInteractionsRepository.insert(new Interactions(timestamp, interaction, description));
        Logger.debug("Inserted: " + interaction + " interaction into database.");
    }

    // Global entrypoint for all manual deactivations
    public static void deactivateService(Context context) {
        context.startService(getServiceIntent(context).setAction(BrickGuardVpnService.ACTION_DEACTIVATE));
        context.stopService(getServiceIntent(context));
        BrickGuard.getPrefs().edit().putLong("brickguard_start_time_marker", 0).apply();
        BrickGuard.getPrefs().edit().putLong("brickguard_current_time_delta", 0).apply();
        insertInteraction(Interactions.SWITCHED_OFF, "VPN service deactivated");
    }

    public static void updateShortcut(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Logger.info("Updating shortcut");
            boolean activate = BrickGuardVpnService.isActivated();
            String notice = activate ? context.getString(R.string.button_text_deactivate) : context.getString(R.string.button_text_activate);
            ShortcutInfo info = new ShortcutInfo.Builder(context, BrickGuard.SHORTCUT_ID_ACTIVATE)
                    .setLongLabel(notice)
                    .setShortLabel(notice)
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_vpn_key))
                    .setIntent(new Intent(context, LockActivity.class).setAction(Intent.ACTION_VIEW)
                            .putExtra(LockActivity.LOCK_SCREEN_ACTION, LockActivity.LOCK_SCREEN_ACTION_AUTHENTICATE)
                            .putExtra(MainActivity.LAUNCH_ACTION, activate ? MainActivity.LAUNCH_ACTION_DEACTIVATE : MainActivity.LAUNCH_ACTION_ACTIVATE))
                    .build();
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(SHORTCUT_SERVICE);
            shortcutManager.addDynamicShortcuts(Collections.singletonList(info));
        }
    }

    private void scheduleSendEmailWork() {
        if (mWorkManager == null) {
            Logger.error("mWorkManager is not initialized. Worker will not run.");
            return;
        }

        Logger.debug("Enqueuing email work request...");
        // Create network constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Define periodic sync work
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(EmailReportWorker.class, 7, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .setInitialDelay(4, TimeUnit.HOURS)
                        .build();

        // Enqueue periodic work
        if (!isWorkScheduled(EmailReportWorker.TAG_EMAIL_REPORT)) {
            mWorkManager.enqueueUniquePeriodicWork(EmailReportWorker.TAG_EMAIL_REPORT,
                    ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        }
    }

    private void scheduleUpdateStreakWork() {
        if (mWorkManager == null) {
            Logger.error("mWorkManager is not initialized. Worker will not run.");
            return;
        }

        Logger.debug("Enqueuing streak work request...");
        // Define periodic sync work
        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(StreakWorker.class, 1, TimeUnit.HOURS)
                        .build();

        // Enqueue periodic work
        if (!isWorkScheduled(StreakWorker.TAG_UPDATE_STREAK)) {
            mWorkManager.enqueueUniquePeriodicWork(StreakWorker.TAG_UPDATE_STREAK,
                    ExistingPeriodicWorkPolicy.REPLACE, workRequest);
        }
    }

    private boolean isWorkScheduled(String tag) {
        ListenableFuture<List<WorkInfo>> statuses = mWorkManager.getWorkInfosByTag(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void openUri(String uri) {
        try {
            instance.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    private void ruleSync(Rule rule) {
        String ruleFilename = rule.getFileName();
        String ruleDownloadUrl = rule.getDownloadUrl();

        Thread ruleSyncThread = new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(ruleDownloadUrl).get().build();
                Response response = HTTP_CLIENT.newCall(request).execute();
                Logger.info("Downloaded " + ruleDownloadUrl);
                if (response.isSuccessful()) {
                    File file = new File(BrickGuard.rulePath + ruleFilename);
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(response.body().bytes());
                    stream.close();
                    Logger.info("DNSMasq rule: " + ruleFilename + " has been downloaded successfully.");
                }
            } catch (Exception e) {
                Logger.logException(e);
            } finally {
                Thread.currentThread().interrupt();
            }
        });
        ruleSyncThread.start();
    }

    public static int getColor(Context context, int colorResId) {
        TypedValue typedValue = new TypedValue();
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[] {colorResId});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    public static SharedPreferences getPrefs() {
        return getInstance().prefs;
    }

    public static BrickGuard getInstance() {
        return instance;
    }
}

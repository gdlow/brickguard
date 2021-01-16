package com.example.beskar;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;

import androidx.preference.PreferenceManager;

import com.example.beskar.server.AbstractDnsServer;
import com.example.beskar.server.DnsServer;
import com.example.beskar.server.DnsServerHelper;
import com.example.beskar.service.BeskarVpnService;
import com.example.beskar.util.Configurations;
import com.example.beskar.util.Logger;
import com.example.beskar.util.Rule;
import com.example.beskar.util.RuleResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Beskar extends Application {

    private static final String SHORTCUT_ID_ACTIVATE = "shortcut_activate";

    public static final List<DnsServer> DNS_SERVERS = new ArrayList<DnsServer>() {{
        add(new DnsServer("8.8.8.8", R.string.filter_none));
        add(new DnsServer("185.228.168.9", R.string.filter_low));
        add(new DnsServer("185.228.168.10", R.string.filter_medium));
        add(new DnsServer("185.228.168.168", R.string.filter_high));
        add(new DnsServer("208.67.222.123", R.string.filter_backup_primary));
        add(new DnsServer("208.67.220.123", R.string.filter_backup_secondary));
    }};

    public static final ArrayList<Rule> RULES = new ArrayList<Rule>() {{
        add(new Rule("chadmayfield/porn-top1m", "chadmayfield.dnsmasq", Rule.TYPE_DNSMASQ,
                "https://raw.githubusercontent.com/chadmayfield/my-pihole-blocklists/master/lists/pi_blocklist_porn_top1m.list",
                false));

        add(new Rule("notracking/hosts-blacklists", "notracking.dnsmasq", Rule.TYPE_DNSMASQ,
                "https://raw.githubusercontent.com/notracking/hosts-blocklists/master/dnsmasq/dnsmasq.blacklist.txt",
                false));
    }};

    public static final String[] DEFAULT_TEST_DOMAINS = {
            "google.com",
            "twitter.com",
            "youtube.com",
            "facebook.com",
            "wikipedia.org"
    };

    public static HashMap<String, String> customDomains = new HashMap<>();

    public static Configurations configurations;
    public static String rulePath;
    public static String logPath;
    private static String configPath;

    private static Beskar instance;
    private SharedPreferences prefs;
    private Thread mResolver;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        Logger.init();
        mResolver = new Thread(new RuleResolver());
        mResolver.start();
        initData();
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

    private void initData() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getExternalFilesDir(null) != null) {
            rulePath = getExternalFilesDir(null).getPath() + "/rules/";
            logPath = getExternalFilesDir(null).getPath() + "/logs/";
            configPath = getExternalFilesDir(null).getPath() + "/config.json";

            initDirectory(rulePath);
            initDirectory(logPath);
        }

        if (configPath != null) {
            configurations = Configurations.load(new File(configPath));
        } else {
            configurations = new Configurations();
        }
    }

    public static <T> T parseJson(Class<T> beanClass, JsonReader reader) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(reader, beanClass);
    }

    public static void initRuleResolver() {
        ArrayList<String> pendingLoad = new ArrayList<>();
        ArrayList<Rule> usingRules = configurations.getUsingRules();
        if (usingRules != null && usingRules.size() > 0) {
            for (Rule rule : usingRules) {
                if (rule.isUsing()) {
                    pendingLoad.add(rulePath + rule.getFileName());
                }
            }
            if (pendingLoad.size() > 0) {
                String[] arr = new String[pendingLoad.size()];
                pendingLoad.toArray(arr);
                switch (usingRules.get(0).getType()) {
                    case Rule.TYPE_HOSTS:
                        RuleResolver.startLoadHosts(arr);
                        break;
                    case Rule.TYPE_DNSMASQ:
                        RuleResolver.startLoadDnsmasq(arr);
                        break;
                }
            } else {
                RuleResolver.clear();
            }
        } else {
            RuleResolver.clear();
        }

        if (!customDomains.isEmpty()) {
            RuleResolver.startLoadCustom();
        }
    }

    public static void addCustomDomain(String key) {
        customDomains.put(key, "0.0.0.0");
        RuleResolver.addCustom(key);
    }

    public static void removeCustomDomain(String key) {
        customDomains.remove(key);
        RuleResolver.removeCustom(key);
    }

    public static void setRulesChanged() {
        if (BeskarVpnService.isActivated()) {
            initRuleResolver();
        }
    }

    public static SharedPreferences getPrefs() {
        return getInstance().prefs;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        instance = null;
        prefs = null;
        RuleResolver.shutdown();
        mResolver.interrupt();
        RuleResolver.clear();
        mResolver = null;
        Logger.shutdown();
    }

    public static Intent getServiceIntent(Context context) {
        return new Intent(context, BeskarVpnService.class);
    }

    public static boolean switchService() {
        if (BeskarVpnService.isActivated()) {
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

    public static void activateService(Context context) {
        activateService(context, false);
    }

    public static void activateService(Context context, boolean forceForeground) {
        updateUpstreamServers();
        if ((getInstance().prefs.getBoolean("settings_foreground", false) || forceForeground)
                && Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            Logger.info("Starting foreground service");
            context.startForegroundService(Beskar.getServiceIntent(context).setAction(BeskarVpnService.ACTION_ACTIVATE));
        } else {
            Logger.info("Starting background service");
            context.startService(Beskar.getServiceIntent(context).setAction(BeskarVpnService.ACTION_ACTIVATE));
        }
    }

    public static void updateUpstreamServers() {
        BeskarVpnService.primaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getPrimary()).clone();
        BeskarVpnService.secondaryServer =
                (AbstractDnsServer) DnsServerHelper.getServerById(DnsServerHelper.getSecondary()).clone();
        Logger.info("Upstream DNS set to: " + BeskarVpnService.primaryServer.getAddress() + " " + BeskarVpnService.secondaryServer.getAddress());
    }

    public static void deactivateService(Context context) {
        context.startService(getServiceIntent(context).setAction(BeskarVpnService.ACTION_DEACTIVATE));
        context.stopService(getServiceIntent(context));
    }

    public static void updateShortcut(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Logger.info("Updating shortcut");
            boolean activate = BeskarVpnService.isActivated();
            String notice = activate ? context.getString(R.string.button_text_deactivate) : context.getString(R.string.button_text_activate);
            ShortcutInfo info = new ShortcutInfo.Builder(context, Beskar.SHORTCUT_ID_ACTIVATE)
                    .setLongLabel(notice)
                    .setShortLabel(notice)
                    .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                    .setIntent(new Intent(context, MainActivity.class).setAction(Intent.ACTION_VIEW)
                            .putExtra(MainActivity.LAUNCH_ACTION, activate ? MainActivity.LAUNCH_ACTION_DEACTIVATE : MainActivity.LAUNCH_ACTION_ACTIVATE))
                    .build();
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(SHORTCUT_SERVICE);
            shortcutManager.addDynamicShortcuts(Collections.singletonList(info));
        }
    }

    public static void donate() {
        openUri("https://qr.alipay.com/FKX04751EZDP0SQ0BOT137");
    }

    public static void openUri(String uri) {
        try {
            instance.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public static Beskar getInstance() {
        return instance;
    }
}

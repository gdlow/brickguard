package com.gdlow.brickguard.util;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.data.LocalResolve;

import org.minidns.record.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RuleResolver implements Runnable {

    public static final int STATUS_LOADED = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_NOT_LOADED = 2;
    public static final int STATUS_PENDING_LOAD = 3;

    public static final int MODE_DNSMASQ = 1;
    public static final int MODE_CUSTOM = 2;

    private static int status = STATUS_NOT_LOADED;
    private static int mode = MODE_DNSMASQ;
    private static String[] dnsmasqFiles;
    private static HashMap<String, String> rulesA = new HashMap<>();
    private static HashMap<String, String> rulesAAAA = new HashMap<>();
    private static boolean shutdown = false;

    public RuleResolver() {
        status = STATUS_NOT_LOADED;
        dnsmasqFiles = new String[0];
        shutdown = false;
    }

    public static void shutdown() {
        shutdown = true;
    }

    public static void startLoadDnsmasq(String[] loadPath) {
        dnsmasqFiles = loadPath;
        mode = MODE_DNSMASQ;
        status = STATUS_PENDING_LOAD;
    }

    public static void startLoadCustom() {
        mode = MODE_CUSTOM;
        status = STATUS_PENDING_LOAD;
    }

    public static void removeCustom(String key) {
        // Remove key
        rulesA.remove(key);
        Logger.info("Loaded " + rulesA.size() + " total rules");
    }

    public static void addCustom(String key) {
        // Add key
        rulesA.put(key, LocalResolve.NULL_RES);
        Logger.info("Loaded " + rulesA.size() + " total rules");
    }

    public static void clear() {
        rulesA = new HashMap<>();
        rulesAAAA = new HashMap<>();
    }

    public static String resolve(String hostname, Record.TYPE type) {
        HashMap<String, String> rules;
        if (type == Record.TYPE.A) {
            rules = rulesA;
        } else if (type == Record.TYPE.AAAA) {
            rules = rulesAAAA;
        } else {
            return null;
        }
        if (rules.size() == 0) {
            return null;
        }
        if (rules.containsKey(hostname)) {
            return rules.get(hostname);
        }
        if (mode == MODE_DNSMASQ) {
            String[] pieces = hostname.split("\\.");
            StringBuilder builder;
            for (int i = 1; i < pieces.length; i++) {
                builder = new StringBuilder();
                for (int j = i; j < pieces.length; j++) {
                    builder.append(pieces[j]);
                    if (j < pieces.length - 1) {
                        builder.append(".");
                    }
                }
                if (rules.containsKey(builder.toString())) {
                    return rules.get(builder.toString());
                }
            }
        }
        return null;
    }

    private void load() {
        try {
            status = STATUS_LOADING;
            rulesA = new HashMap<>();
            rulesAAAA = new HashMap<>();
            if (mode == MODE_DNSMASQ) {
                for (String dnsmasqFile : dnsmasqFiles) {
                    File file = new File(dnsmasqFile);
                    if (file.canRead()) {
                        Logger.info("Loading DNSMasq configuration from " + file.toString());
                        FileInputStream stream = new FileInputStream(file);
                        BufferedReader dataIO = new BufferedReader(new InputStreamReader(stream));
                        String strLine;
                        String[] data;
                        int count = 0;
                        while ((strLine = dataIO.readLine()) != null) {
                            if (!strLine.equals("") && !strLine.startsWith("#")) {
                                data = strLine.split("/");

                                // valid dnsmasq configurations
                                if (data.length == 3 && data[0].equals("address=")) {
                                    if (data[1].startsWith(".")) {
                                        data[1] = data[1].substring(1, data[1].length());
                                    }
                                    if (strLine.contains(":")) {//IPv6
                                        rulesAAAA.put(data[1], data[2]);
                                    } else if (strLine.contains(".")) {//IPv4
                                        // ad blacklist goes down this code path
                                        data[2] = data[2].equals("#") ? LocalResolve.NULL_RES : data[2];
                                        rulesA.put(data[1], data[2]);
                                    }
                                    count++;
                                }

                                // only websites listed - equivalent to blacklisting
                                // porn blacklist goes down this code path
                                else if (data.length == 1) {
                                    if (strLine.contains(".")) {//IPv4
                                        rulesA.put(data[0], LocalResolve.ONE_RES);
                                    }
                                    count++;
                                }
                            }
                        }

                        dataIO.close();
                        stream.close();

                        Logger.info("Loaded " + count + " rules");
                    }
                }
            } else if (mode == MODE_CUSTOM) {
                rulesA.putAll(BrickGuard.customDomains);
                Logger.info("Loaded " + BrickGuard.customDomains.size() + " custom rules");
                Logger.info("Loaded " + rulesA.size() + " total rules");
            }
            status = STATUS_LOADED;
        } catch (Exception e) {
            Logger.logException(e);

            status = STATUS_NOT_LOADED;
        }
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                if (status == STATUS_PENDING_LOAD) {
                    load();
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            Logger.logException(e);
        }
    }
}

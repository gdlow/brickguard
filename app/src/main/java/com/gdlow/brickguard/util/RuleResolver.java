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

    private static int status = STATUS_NOT_LOADED;
    private static HashMap<String, String> rulesA = new HashMap<>();
    private static HashMap<String, String> rulesAAAA = new HashMap<>();
    private static boolean shutdown = false;

    public RuleResolver() {
        status = STATUS_NOT_LOADED;
        shutdown = false;
    }

    public static void shutdown() {
        shutdown = true;
    }

    // This should only be called once all changes are applied.
    public static void setPending() {
        status = STATUS_PENDING_LOAD;
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

        // Resolve DNS configuration
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
        return null;
    }

    private void loadDnsmasqFile(String dnsmasqFile, boolean toAdd) {
        File file = new File(dnsmasqFile);
        if (file.canRead()) {
            try {
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
                        // ad blacklist goes down this code path
                        if (data.length == 3 && data[0].equals("address=")) {
                            if (data[1].startsWith(".")) {
                                data[1] = data[1].substring(1);
                            }
                            if (strLine.contains(":")) {//IPv6
                                putOrRemove(rulesAAAA, data[1], data[2], toAdd);
                                count++;
                            } else if (strLine.contains(".")) {//IPv4
                                data[2] = data[2].equals("#") ? LocalResolve.NULL_RES : data[2];
                                putOrRemove(rulesA, data[1], data[2], toAdd);
                                count++;
                            }
                        }

                        // only websites listed - equivalent to blacklisting
                        // porn blacklist goes down this code path
                        else if (data.length == 1) {
                            if (strLine.contains(".")) {//IPv4
                                putOrRemove(rulesA, data[0], LocalResolve.ONE_RES, toAdd);
                                count++;
                            }
                        }
                    }
                }
                Logger.info((toAdd ? "Added " : "Removed ") + count + " DNSMasq rules from " + file.toString());
                dataIO.close();
                stream.close();
            } catch (Exception e) {
                Logger.logException(e);
            }
        }
    }

    private void putOrRemove(HashMap<String, String> rules, String key, String value, boolean toAdd) {
        if (toAdd) {
            rules.put(key, value);
        } else {
            rules.remove(key);
        }
    }

    private void load() {
        try {
            status = STATUS_LOADING;

            // Load diffs for DNS rules
            for (Rule rule : BrickGuard.dnsmasqRules) {
                String dnsMasqFile = BrickGuard.rulePath + rule.getFileName();
                loadDnsmasqFile(dnsMasqFile, rule.isUsing());
            }

            // Load diffs for custom domains
            int addCount = 0; int removeCount = 0;
            for (String domain : BrickGuard.customDomains.keySet()) {
                boolean toAdd = BrickGuard.customDomains.get(domain);
                if (toAdd) {
                    addCount++;
                } else {
                    removeCount++;
                }
                // custom domains go down this code path
                putOrRemove(rulesA, domain, LocalResolve.ONE_RES, toAdd);
            }

            Logger.info("Added " + addCount + " and removed " + removeCount + " custom domains");
            Logger.info("Loaded " + (rulesA.size() + rulesAAAA.size()) + " total rules");

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

package com.gdlow.brickguard.server;

import android.content.Context;
import android.net.Uri;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.util.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DnsServerHelper {

    public static final String HTTPS_SUFFIX = "https://";

    public static HashMap<String, List<InetAddress>> domainCache = new HashMap<>();

    public static void clearCache() {
        domainCache = new HashMap<>();
    }

    public static void buildCache() {
        domainCache = new HashMap<>();
        if (!BrickGuard.getPrefs().getBoolean("settings_dont_build_cache", false)) {
            buildDomainCache(getServerById(getPrimary()).getAddress());
            buildDomainCache(getServerById(getSecondary()).getAddress());
        }
    }

    private static void buildDomainCache(String addr) {
        addr = HTTPS_SUFFIX + addr;
        String host = Uri.parse(addr).getHost();
        try {
            domainCache.put(host, Arrays.asList(InetAddress.getAllByName(host)));
        } catch (Exception e) {
            Logger.logException(e);
        }
    }

    public static int getPosition(String id) {
        int intId = Integer.parseInt(id);
        if (intId < BrickGuard.DNS_SERVERS.size()) {
            return intId;
        }

        return 0;
    }

    public static String getPrimary() {
        return String.valueOf(DnsServerHelper.checkServerId(Integer.parseInt(BrickGuard.getPrefs().getString("primary_server", "2"))));
    }

    public static String getSecondary() {
        return String.valueOf(DnsServerHelper.checkServerId(Integer.parseInt(BrickGuard.getPrefs().getString("secondary_server", "4"))));
    }

    public static String getGoogle() {
        return String.valueOf(0);
    }

    private static int checkServerId(int id) {
        if (id < BrickGuard.DNS_SERVERS.size()) {
            return id;
        }

        return 0;
    }

    public static AbstractDnsServer getServerById(String id) {
        for (DnsServer server : BrickGuard.DNS_SERVERS) {
            if (server.getId().equals(id)) {
                return server;
            }
        }

        return BrickGuard.DNS_SERVERS.get(0);
    }

    public static String[] getIds() {
        ArrayList<String> servers = new ArrayList<>(BrickGuard.DNS_SERVERS.size());
        for (DnsServer server : BrickGuard.DNS_SERVERS) {
            servers.add(server.getId());
        }
        String[] stringServers = new String[BrickGuard.DNS_SERVERS.size()];
        return servers.toArray(stringServers);
    }

    public static String[] getNames(Context context) {
        ArrayList<String> servers = new ArrayList<>(BrickGuard.DNS_SERVERS.size());
        for (DnsServer server : BrickGuard.DNS_SERVERS) {
            servers.add(server.getStringDescription(context));
        }
        String[] stringServers = new String[BrickGuard.DNS_SERVERS.size()];
        return servers.toArray(stringServers);
    }

    public static ArrayList<AbstractDnsServer> getAllServers() {
        ArrayList<AbstractDnsServer> servers = new ArrayList<>(BrickGuard.DNS_SERVERS.size());
        servers.addAll(BrickGuard.DNS_SERVERS);
        return servers;
    }

    public static String getDescription(String id, Context context) {
        for (DnsServer server : BrickGuard.DNS_SERVERS) {
            if (server.getId().equals(id)) {
                return server.getStringDescription(context);
            }
        }
        return BrickGuard.DNS_SERVERS.get(0).getStringDescription(context);
    }
}

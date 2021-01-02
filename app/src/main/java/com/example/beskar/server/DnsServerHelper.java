package com.example.beskar.server;

import android.content.Context;
import android.net.Uri;

import com.example.beskar.Beskar;
import com.example.beskar.provider.ProviderPicker;
import com.example.beskar.util.Logger;

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
        if (!Beskar.getPrefs().getBoolean("settings_dont_build_cache", false)) {
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
        if (intId < Beskar.DNS_SERVERS.size()) {
            return intId;
        }

        return 0;
    }

    public static String getPrimary() {
        return String.valueOf(DnsServerHelper.checkServerId(Integer.parseInt(Beskar.getPrefs().getString("primary_server", "0"))));
    }

    public static String getSecondary() {
        return String.valueOf(DnsServerHelper.checkServerId(Integer.parseInt(Beskar.getPrefs().getString("secondary_server", "1"))));
    }

    private static int checkServerId(int id) {
        if (id < Beskar.DNS_SERVERS.size()) {
            return id;
        }

        return 0;
    }

    public static AbstractDnsServer getServerById(String id) {
        for (DnsServer server : Beskar.DNS_SERVERS) {
            if (server.getId().equals(id)) {
                return server;
            }
        }

        return Beskar.DNS_SERVERS.get(0);
    }

    public static String[] getIds() {
        ArrayList<String> servers = new ArrayList<>(Beskar.DNS_SERVERS.size());
        for (DnsServer server : Beskar.DNS_SERVERS) {
            servers.add(server.getId());
        }
        String[] stringServers = new String[Beskar.DNS_SERVERS.size()];
        return servers.toArray(stringServers);
    }

    public static String[] getNames(Context context) {
        ArrayList<String> servers = new ArrayList<>(Beskar.DNS_SERVERS.size());
        for (DnsServer server : Beskar.DNS_SERVERS) {
            servers.add(server.getStringDescription(context));
        }
        String[] stringServers = new String[Beskar.DNS_SERVERS.size()];
        return servers.toArray(stringServers);
    }

    public static ArrayList<AbstractDnsServer> getAllServers() {
        ArrayList<AbstractDnsServer> servers = new ArrayList<>(Beskar.DNS_SERVERS.size());
        servers.addAll(Beskar.DNS_SERVERS);
        return servers;
    }

    public static String getDescription(String id, Context context) {
        for (DnsServer server : Beskar.DNS_SERVERS) {
            if (server.getId().equals(id)) {
                return server.getStringDescription(context);
            }
        }
        return Beskar.DNS_SERVERS.get(0).getStringDescription(context);
    }
}

package com.example.beskar.server;

import android.content.Context;

import com.example.beskar.Beskar;

public class DnsServer extends AbstractDnsServer {

    private static int totalId = 0;

    private String id;
    private int description;

    public DnsServer(String address, int description, int port) {
        super(address, port);
        this.id = String.valueOf(totalId++);
        this.description = description;
    }

    public DnsServer(String address, int description) {
        this(address, description, DNS_SERVER_DEFAULT_PORT);
    }

    public DnsServer(String address) {
        this(address, 0);
    }

    public String getId() {
        return id;
    }

    public String getStringDescription(Context context) {
        return context.getResources().getString(description);
    }

    @Override
    public String getName() {
        return getStringDescription(Beskar.getInstance());
    }
}

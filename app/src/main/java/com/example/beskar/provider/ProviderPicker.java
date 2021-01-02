package com.example.beskar.provider;

import android.os.ParcelFileDescriptor;

import com.example.beskar.Beskar;
import com.example.beskar.service.BeskarVpnService;

public abstract class ProviderPicker {
    public static final int DNS_QUERY_METHOD_UDP = 0;

    public static Provider getProvider(ParcelFileDescriptor descriptor, BeskarVpnService service) {
        switch (getDnsQueryMethod()) {
            case DNS_QUERY_METHOD_UDP:
                return new UdpProvider(descriptor, service);
            default: // should not go here
                break;
        }
        return new UdpProvider(descriptor, service);
    }

    public static int getDnsQueryMethod() {
        return Integer.parseInt(Beskar.getPrefs().getString("settings_dns_query_method", "0"));
    }
}

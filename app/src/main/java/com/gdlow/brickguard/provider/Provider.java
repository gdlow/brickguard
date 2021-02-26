package com.gdlow.brickguard.provider;

import android.os.ParcelFileDescriptor;
import android.system.Os;

import com.gdlow.brickguard.BrickGuard;
import com.gdlow.brickguard.data.LocalResolve;
import com.gdlow.brickguard.data.LocalResolveRepository;
import com.gdlow.brickguard.service.BrickGuardVpnService;
import com.gdlow.brickguard.util.Logger;
import com.gdlow.brickguard.util.RuleResolver;

import org.minidns.dnsmessage.DnsMessage;
import org.minidns.record.A;
import org.minidns.record.AAAA;
import org.minidns.record.Record;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Provider {
    protected ParcelFileDescriptor descriptor;
    protected BrickGuardVpnService service;
    protected boolean running = false;
    protected static long dnsQueryTimes = 0;

    protected FileDescriptor mBlockFd = null;
    protected FileDescriptor mInterruptFd = null;
    protected LocalResolveRepository mRepository;
    protected final Queue<byte[]> deviceWrites = new LinkedList<>();

    Provider(ParcelFileDescriptor descriptor, BrickGuardVpnService service) {
        this.descriptor = descriptor;
        this.service = service;
        dnsQueryTimes = 0;
        mRepository = new LocalResolveRepository(service.getApplication());
    }

    public final long getDnsQueryTimes() {
        return dnsQueryTimes;
    }

    public abstract void process();

    public final void start() {
        running = true;
    }

    public final void shutdown() {
        running = false;
    }

    public void stop() {
        try {
            if (mInterruptFd != null) {
                Os.close(mInterruptFd);
            }
            if (mBlockFd != null) {
                Os.close(mBlockFd);
            }
            if (this.descriptor != null) {
                this.descriptor.close();
                this.descriptor = null;
            }
        } catch (Exception ignored) {
        }
    }

    protected void queueDeviceWrite(IpPacket ipOutPacket) {
        dnsQueryTimes++;
        deviceWrites.add(ipOutPacket.getRawData());
    }

    public boolean resolve(IpPacket parsedPacket, DnsMessage dnsMsg) {
        String dnsQueryName = dnsMsg.getQuestion().name.toString();

        try {
            String response = RuleResolver.resolve(dnsQueryName, dnsMsg.getQuestion().type);
            if (response != null && dnsMsg.getQuestion().type == Record.TYPE.A) {
                long timestamp = System.currentTimeMillis() / 1000L;
                mRepository.insert(new LocalResolve(timestamp, dnsQueryName, response));
                Logger.info("Provider: Resolved " + dnsQueryName + "  Local resolver response: " + response);
                Logger.debug("Inserted local resolve { " + dnsQueryName + ": " + response + " } " +
                        "into database.");
                DnsMessage.Builder builder = dnsMsg.asBuilder()
                        .setQrFlag(true)
                        .addAnswer(new Record<>(dnsQueryName, Record.TYPE.A, 1, 64,
                                new A(Inet4Address.getByName(response).getAddress())));
                handleDnsResponse(parsedPacket, builder.build().toArray());
                return true;
            } else if (response != null && dnsMsg.getQuestion().type == Record.TYPE.AAAA) {
                Logger.info("Provider: Resolved " + dnsQueryName + "  Local resolver response: " + response);
                DnsMessage.Builder builder = dnsMsg.asBuilder()
                        .setQrFlag(true)
                        .addAnswer(new Record<>(dnsQueryName, Record.TYPE.AAAA, 1, 64,
                                new AAAA(Inet6Address.getByName(response).getAddress())));
                handleDnsResponse(parsedPacket, builder.build().toArray());
                return true;
            }
        } catch (Exception e) {
            Logger.logException(e);
        }
        return false;
    }

    /**
     * Handles a responsePayload from an upstream DNS server
     *
     * @param requestPacket   The original request packet
     * @param responsePayload The payload of the response
     */
    void handleDnsResponse(IpPacket requestPacket, byte[] responsePayload) {
        if (BrickGuard.getPrefs().getBoolean("settings_debug_output", false)) {
            try {
                Logger.debug("DnsResponse: " + new DnsMessage(responsePayload).toString());
            } catch (IOException e) {
                Logger.logException(e);
            }
        }
        UdpPacket udpOutPacket = (UdpPacket) requestPacket.getPayload();
        UdpPacket.Builder payLoadBuilder = new UdpPacket.Builder(udpOutPacket)
                .srcPort(udpOutPacket.getHeader().getDstPort())
                .dstPort(udpOutPacket.getHeader().getSrcPort())
                .srcAddr(requestPacket.getHeader().getDstAddr())
                .dstAddr(requestPacket.getHeader().getSrcAddr())
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true)
                .payloadBuilder(
                        new UnknownPacket.Builder()
                                .rawData(responsePayload)
                );


        IpPacket ipOutPacket;
        if (requestPacket instanceof IpV4Packet) {
            ipOutPacket = new IpV4Packet.Builder((IpV4Packet) requestPacket)
                    .srcAddr((Inet4Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet4Address) requestPacket.getHeader().getSrcAddr())
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();

        } else {
            ipOutPacket = new IpV6Packet.Builder((IpV6Packet) requestPacket)
                    .srcAddr((Inet6Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet6Address) requestPacket.getHeader().getSrcAddr())
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();
        }

        queueDeviceWrite(ipOutPacket);
    }

    protected void writeToDevice(FileOutputStream outFd) throws BrickGuardVpnService.VpnNetworkException {
        try {
            outFd.write(deviceWrites.poll());
        } catch (IOException e) {
            throw new BrickGuardVpnService.VpnNetworkException("Outgoing VPN output stream closed");
        }
    }

    protected void readPacketFromDevice(FileInputStream inputStream, byte[] packet) throws BrickGuardVpnService.VpnNetworkException {
        // Read the outgoing packet from the input stream.
        int length;
        try {
            length = inputStream.read(packet);
        } catch (IOException e) {
            throw new BrickGuardVpnService.VpnNetworkException("Cannot read from device", e);
        }
        if (length == 0) {
            return;
        }
        final byte[] readPacket = Arrays.copyOfRange(packet, 0, length);
        handleDnsRequest(readPacket);
    }

    protected abstract void handleDnsRequest(byte[] packetData) throws BrickGuardVpnService.VpnNetworkException;
}

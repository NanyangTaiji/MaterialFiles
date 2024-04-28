package me.zhanghai.android.files.storage;

import java.net.InetAddress;

public class LanSmbServer implements Comparable<LanSmbServer> {
    private String host;
    private InetAddress address;

    public LanSmbServer(String host, InetAddress address) {
        this.host = host;
        this.address = address;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public int compareTo(LanSmbServer other) {
        int addressComparison = this.address.getHostAddress().compareTo(other.address.getHostAddress());
        if (addressComparison != 0) {
            return addressComparison;
        }
        return this.host.compareTo(other.host);
    }
}


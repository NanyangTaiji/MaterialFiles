package me.zhanghai.android.files.storage;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;

import jcifs.NameServiceClient;
import jcifs.NetbiosAddress;
import me.zhanghai.android.files.util.Failure;
import me.zhanghai.android.files.util.Loading;
import me.zhanghai.android.files.util.Stateful;
import me.zhanghai.android.files.util.Success;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LanSmbServerListLiveData extends MutableLiveData<Stateful<List<LanSmbServer>>> {

    private Future<?> loadFuture;

    public LanSmbServerListLiveData() {
        loadValue();
    }

    public void loadValue() {
        close();
        setValue(new Loading<>(null));  // No previous value to pass
        loadFuture = ((ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR).submit(() -> {
            try {
                Set<LanSmbServer> newServerSet = new HashSet<>();
                ExecutorService fixedThreadPool = Executors.newFixedThreadPool(60);
                List<LanSmbServer> serverList = new ArrayList<>();
                for (int i = 0; i < 60; i++) {
                    fixedThreadPool.execute(() -> getServersByComputerBrowserService(newServerSet, serverList));
                    fixedThreadPool.execute(() -> getServersByScanningSubnet(newServerSet, serverList));
                }

                fixedThreadPool.shutdown();
                try {
                    fixedThreadPool.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<LanSmbServer> newServers = new ArrayList<>(serverList);
                newServers.retainAll(newServerSet);
                postValue(new Success<>(newServers));
            } catch (Exception e) {
                postValue(new Failure<>(null, e));  // No previous value to pass
            }
        });
    }


    private void getServersByComputerBrowserService(Set<LanSmbServer> newServerSet, List<LanSmbServer> serverList) {
        try {
            SmbFile lan = new SmbFile("smb://");
            SmbFile[] domains = lan.listFiles();
            NameServiceClient nameServiceClient = SingletonContext.getInstance().getNameServiceClient();
            for (SmbFile domain : domains) {
                SmbFile[] servers = domain.listFiles();
                for (SmbFile server : servers) {
                    String host = server.getName().substring(0, server.getName().length() - 1);
                    InetAddress address = nameServiceClient.getByName(host).toInetAddress();
                    LanSmbServer lanSmbServer = new LanSmbServer(host, address);
                    synchronized (newServerSet) {
                        newServerSet.add(lanSmbServer);
                    }
                    synchronized (serverList) {
                        serverList.add(lanSmbServer);
                    }
                }
            }
        } catch (SmbException | UnknownHostException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getServersByScanningSubnet(Set<LanSmbServer> newServerSet, List<LanSmbServer> serverList) {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (!(localAddress instanceof Inet4Address) || !((Inet4Address) localAddress).isSiteLocalAddress()) {
                return;
            }
            NameServiceClient nameServiceClient = SingletonContext.getInstance().getNameServiceClient();
            for (InetAddress address : getSubnetAddresses((Inet4Address) localAddress)) {
                NetbiosAddress[] nbtAddresses = nameServiceClient.getNbtAllByAddress(address.getHostAddress());
                String host = nbtAddresses.length > 0 ? nbtAddresses[0].getHostName() : null;
                if (host != null) {
                    LanSmbServer lanSmbServer = new LanSmbServer(host, address);
                    synchronized (newServerSet) {
                        newServerSet.add(lanSmbServer);
                    }
                    synchronized (serverList) {
                        serverList.add(lanSmbServer);
                    }
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private List<Inet4Address> getSubnetAddresses(Inet4Address localAddress) {
        List<Inet4Address> addresses = new ArrayList<>();
        byte[] addressBytes = localAddress.getAddress();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 3; j++) {
                int lastBit = 100 * j + i;
                if (lastBit <= 255) {
                    addressBytes[3] = (byte) lastBit;
                    try {
                        addresses.add((Inet4Address) InetAddress.getByAddress(addressBytes));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return addresses;
    }

    @Override
    public void onInactive() {
        super.onInactive();
        close();
    }

    public void close() {
        if (loadFuture != null) {
            loadFuture.cancel(true);
            loadFuture = null;
        }
    }

}


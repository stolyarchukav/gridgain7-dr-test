package com.ringcentral.platform.tpg;

import com.ringcentral.platform.tpg.model.token.AuthToken;
import com.ringcentral.platform.tpg.replication.ConflictResolver;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicWriteOrderMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.marshaller.jdk.JdkMarshaller;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.gridgain.grid.cache.conflict.CacheConflictMode;
import org.gridgain.grid.cache.dr.CacheDrSenderConfiguration;
import org.gridgain.grid.configuration.DrReceiverConfiguration;
import org.gridgain.grid.configuration.DrSenderConfiguration;
import org.gridgain.grid.configuration.GridGainCacheConfiguration;
import org.gridgain.grid.configuration.GridGainConfiguration;
import org.gridgain.grid.dr.DrSenderConnectionConfiguration;
import org.gridgain.grid.dr.store.fs.DrSenderFsStore;

public class DrTest {
    public static void main(String[] args) throws InterruptedException {
        Ignite ignite1 = start(30, 29);

        Thread.sleep(10000);

        Ignite ignite2 = start(29, 30);

        System.out.println("DC 1: " + ignite1.cluster().nodes());
        System.out.println("DC 2: " + ignite2.cluster().nodes());

        IgniteCache<Integer, Integer> cache1 = ignite1.cache("tokens");
        IgniteCache<Integer, Integer> cache2 = ignite2.cache("tokens");

//        for (int i = 0; i < 100; i++)
//            cache1.put(i, i);

        Thread.sleep(6000);

        System.out.println("Size 1: " + cache1.size());
        System.out.println("Size 2: " + cache2.size());

//        for (int i = 0; i < 100; i++)
//            cache1.remove(i);

        Thread.sleep(6000);

        System.out.println("Size 1: " + cache1.size());
        System.out.println("Size 2: " + cache2.size());

//        ignite1.close();
//
//        cache1 = start(1, 2).cache("tokens");
//
//        Thread.sleep(6000);
//
//        System.out.println("Size 1: " + cache1.size());
//        System.out.println("Size 2: " + cache2.size());
    }

    private static Ignite start(int loc, int rmt) {
        GridGainConfiguration ggCfg = new GridGainConfiguration();

        ggCfg.setDataCenterId((byte)loc);

        DrSenderConfiguration senderCfg = new DrSenderConfiguration();

        senderCfg.setCacheNames("tokens");

        DrSenderFsStore store = new DrSenderFsStore();
        store.setDirectoryPath("C:\\test-store" + loc + "\\");

        senderCfg.setStore(store);

        DrSenderConnectionConfiguration senderConnCfg = new DrSenderConnectionConfiguration();

        senderConnCfg.setDataCenterId((byte)rmt);
        senderConnCfg.setLocalOutboundHost("127.0.0.1");
        senderConnCfg.setReceiverAddresses("127.0.0.1:" + (49000 + rmt));
//        senderConnCfg.setAwaitAcknowledge(true);

        senderCfg.setConnectionConfiguration(senderConnCfg);

        ggCfg.setDrSenderConfiguration(senderCfg);

        DrReceiverConfiguration receiverCfg = new DrReceiverConfiguration();

        receiverCfg.setLocalInboundHost("127.0.0.1");
        receiverCfg.setLocalInboundPort(49000 + loc);
//        receiverCfg.setPerNodeBufferSize(1);

        ggCfg.setDrReceiverConfiguration(receiverCfg);

        GridGainCacheConfiguration ggCacheCfg = new GridGainCacheConfiguration();

        CacheDrSenderConfiguration cacheSenderCfg = new CacheDrSenderConfiguration();

        cacheSenderCfg.setBatchSendSize(512);
        cacheSenderCfg.setMaxBatches(8);

        ggCacheCfg.setDrSenderConfiguration(cacheSenderCfg);
        ggCacheCfg.setDrReceiverEnabled(true);
        ggCacheCfg.setConflictResolverMode(CacheConflictMode.AUTO);
        ggCacheCfg.setConflictResolver(new ConflictResolver());

        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("tokens");
        cacheConfiguration.setCacheMode(CacheMode.PARTITIONED);
        cacheConfiguration.setPluginConfigurations(ggCacheCfg);
        cacheConfiguration.setIndexedTypes(String.class, AuthToken.class);
        cacheConfiguration.setBackups(1);
        cacheConfiguration.setAtomicWriteOrderMode(CacheAtomicWriteOrderMode.CLOCK);
        cacheConfiguration.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        IgniteConfiguration cfg = new IgniteConfiguration().
            setGridName("ignite-" + loc).
            setPluginConfigurations(ggCfg).
            setCacheConfiguration(cacheConfiguration).
            setMarshaller(new JdkMarshaller()).
            setDiscoverySpi(new TcpDiscoverySpi().
                setIpFinder(new TcpDiscoveryVmIpFinder(true)));

        return Ignition.start(cfg);
    }
}


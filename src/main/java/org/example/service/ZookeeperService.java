package org.example.service;

import com.typesafe.config.Config;

import java.util.concurrent.TimeUnit;

public class ZookeeperService {

    private final ZkCurator zkCurator;

    public ZkCurator getZkCurator () {
        return zkCurator;
    }

    public ZookeeperService(Config zkConfig) {
        String zkServers = zkConfig.getString("brokers");
        zkCurator = new ZkCurator.Builder(zkServers)
                .setCnxnTimeoutMs((int) zkConfig.getDuration("connTimeoutMs", TimeUnit.MILLISECONDS))
                .setRetryTimes(zkConfig.getInt("retryTimes"))
                .setSessionTimeoutMs((int) zkConfig.getDuration(
                        "sessionTimeoutMs",
                        TimeUnit.MILLISECONDS
                ))
                .setSleepMsBetweenRetries((int) zkConfig.getDuration(
                        "sleepMsBetweenRetries",
                        TimeUnit.MILLISECONDS
                ))
                .build();
        zkCurator.start();
    }
}

package org.example.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

public class ZkCurator {

    private CuratorFramework curator;

    public ZkCurator(Builder builder) {
        curator = CuratorFrameworkFactory.newClient(
                builder.zkServers,
                builder.sessionTimeoutMs,
                builder.cnxnTimeoutMs,
                new RetryNTimes(builder.retryTimes, builder.sleepMsBetweenRetries)
        );
    }
    public CuratorFramework getCurator() {
        return curator;
    }

    public void start() {
        curator.start();
    }

    public static class Builder {
        private final String zkServers;

        private int cnxnTimeoutMs = 10 * 1000; // 10s

        private int retryTimes = 6 * 60 * 24;  // 1d

        private int sessionTimeoutMs = 10 * 1000; // 10s

        private int sleepMsBetweenRetries = 1000; // 1s

        public Builder(String zkServers) {
            this.zkServers = zkServers;
        }

        public Builder setCnxnTimeoutMs(int cnxnTimeoutMs) {
            if (cnxnTimeoutMs > 0) {
                this.cnxnTimeoutMs = cnxnTimeoutMs;
            }
            return this;
        }

        public Builder setRetryTimes(int retryTimes) {
            if (retryTimes > 0) {
                this.retryTimes = retryTimes;
            }
            return this;
        }

        public Builder setSessionTimeoutMs(int sessionTimeoutMs) {
            if (sessionTimeoutMs > 0) {
                this.sessionTimeoutMs = sessionTimeoutMs;
            }
            return this;
        }

        public Builder setSleepMsBetweenRetries(int sleepMsBetweenRetries) {
            if (sleepMsBetweenRetries > 0) {
                this.sleepMsBetweenRetries = sleepMsBetweenRetries;
            }
            return this;
        }

        public ZkCurator build() {
            return new ZkCurator(this);
        }
    }
}

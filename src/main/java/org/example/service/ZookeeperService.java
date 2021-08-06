package org.example.service;

import com.typesafe.config.Config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.example.zk.ZkCurator;

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

    // 创建节点
    public String CreatePath(String path, CreateMode mode, String value) {
        String result = null;
        boolean flag = checkExist(path);
        try {
            if (!flag) {
                result = zkCurator.getCurator().create().withMode(mode).forPath(path, value.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 判断节点是否存在
    public boolean checkExist(String path) {
        Stat stat = null;
        try {
            stat =zkCurator.getCurator().checkExists().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Objects.nonNull(stat);
    }

    // 节点set V
    public void setData(String path, String value) throws Exception {
        boolean b = checkExist(path);
        if (!b) {
            throw new RuntimeException("节点不存在！");
        }
        zkCurator.getCurator().setData().forPath(path, value.getBytes());

    }

    // 获取节点数据
    public byte[] getDataBytes(String path) throws Exception {
      return zkCurator.getCurator().getData().forPath(path);
    }

    // 创建本节点监听
    public void watch(String path) throws Exception {
       final NodeCache nodeCache = new NodeCache(zkCurator.getCurator(), path, false);
        nodeCache.getListenable().addListener(()->{
            dealListenerData(nodeCache.getCurrentData());
        });
       nodeCache.start();

    }

    // 处理变化数据
    public void dealListenerData(ChildData data){};

    //创建子节点监控
    public void watchChildData(String path) throws Exception {
        PathChildrenCache childrenCache = new PathChildrenCache(zkCurator.getCurator(), path, true);
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework,
                PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                Type type = pathChildrenCacheEvent.getType();
                switch (type) {
                    case CHILD_ADDED:
                    case CHILD_REMOVED:
                    case CHILD_UPDATED:
                        dealListenerData(pathChildrenCacheEvent.getData());
                        break;
                    default:
                        break;
                }

            }
        };
        childrenCache.getListenable().addListener(listener);
        childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
    }
}

package org.example.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.jrf.base.fiber.FiberPool;
import com.jrf.base.fiber.FuturePool;
import com.jrf.base.fiber.FuturePoolConfig;
import com.jrf.base.fiber.ThreadPool;
import com.typesafe.config.Config;
import org.example.service.ZookeeperService;

public class ConfigModule extends AbstractModule {
    private static final String CONFIG_FUTURE_POOL_TYPE = "type";
    private static final String CONFIG_FUTURE_POOL_CORE_SIZE = "core";
    private static final String CONFIG_FUTURE_POOL_MAX_SIZE = "max";
    private final Config config;
    private final ZookeeperService resourceZookeeperService;

    /**
     * 图提交执行线程池.
     */
    private FuturePool graphFuturePool;
    private FuturePool rpcFuturePool;

    public ConfigModule(Config config) {
        this.config = config;
        Config zkConfig = config.getConfig("zk");
        resourceZookeeperService = new ZookeeperService(zkConfig);
        graphFuturePool = createFuturePool(config.getConfig("bootstrap.futurePool.graph"));
        rpcFuturePool = createFuturePool(config.getConfig("bootstrap.futurePool.rpc"));
    }

    @Override
    protected void configure() {
        bind(Config.class).annotatedWith(Names.named("systemConfig")).toInstance(config);
        bind(ZookeeperService.class).annotatedWith(Names.named("zookeeperService")).toInstance(resourceZookeeperService);
        bind(FuturePool.class).annotatedWith(Names.named("graph"))
            .toInstance(graphFuturePool);
        bind(FuturePool.class).annotatedWith(Names.named("rpc"))
            .toInstance(rpcFuturePool);
    }

    private FuturePool createFuturePool(Config config) {
        FuturePoolType futurePoolType = FuturePoolType.valueOf(
            config.getString(CONFIG_FUTURE_POOL_TYPE));
        int coreSize = config.getInt(CONFIG_FUTURE_POOL_CORE_SIZE);
        int maxSize = config.getInt(CONFIG_FUTURE_POOL_MAX_SIZE);
        FuturePoolConfig futurePoolConfig = new FuturePoolConfig(coreSize, maxSize);

        switch (futurePoolType) {
            case THREAD_POOL:
                return new ThreadPool(futurePoolConfig);
            case FIBER_POOL:
                return new FiberPool(futurePoolConfig);
            default:
                return null;
        }
    }

    /**
     * 线程池类型.
     */
    enum FuturePoolType {
        THREAD_POOL,
        FIBER_POOL
    }
}

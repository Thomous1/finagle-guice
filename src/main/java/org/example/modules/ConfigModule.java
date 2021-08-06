package org.example.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import org.example.service.ZookeeperService;

public class ConfigModule extends AbstractModule {
    private final Config config;
    private final ZookeeperService resourceZookeeperService;
    public ConfigModule(Config config) {
        this.config = config;
        Config zkConfig = config.getConfig("zk");
        resourceZookeeperService = new ZookeeperService(zkConfig);
    }

    @Override
    protected void configure() {
        bind(Config.class).annotatedWith(Names.named("systemConfig")).toInstance(config);
        bind(ZookeeperService.class).annotatedWith(Names.named("zookeeperService")).toInstance(resourceZookeeperService);
    }
}

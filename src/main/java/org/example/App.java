package org.example;

import com.github.racc.tscg.TypesafeConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jd.rec.jrf.SystemConfig;
import com.twitter.finagle.Http;
import com.twitter.finagle.ListeningServer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.HttpMuxer;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.TimeoutException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.log4j.PropertyConfigurator;
import org.example.handler.TestHanlder;
import org.example.modules.ConfigModule;
import org.example.modules.TestModule;
import org.example.service.TestService;
import org.example.service.ZkService;


import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String GUICE_SCAN_PACKAGE_NAME_PREFIX = "org.example";

    public static void main( String[] args ) throws TimeoutException, InterruptedException {
        PropertyConfigurator.configure(App.class.getResourceAsStream("/log4j.properties"));
        Config config = ConfigFactory.parseFile(new File("config/application.conf")).resolve();
        final SystemConfig systemConfig = new SystemConfig(config);
        Injector injector = Guice.createInjector(
                new TestModule(),
                new ConfigModule(config),
            TypesafeConfigModule.fromConfigWithPackage(systemConfig.config,
                GUICE_SCAN_PACKAGE_NAME_PREFIX)
        );
        TestService testService = injector.getInstance(TestService.class);
        ZkService zkService = injector.getInstance(ZkService.class);
        Service<Request, Response> service =
                new HttpMuxer().withHandler(
                   "/test", testService
                ).withHandler(
                   "/zkTest", zkService
                );

        int port = config.getConfig("server").getInt("port");
        ListeningServer server = Http.server()
                .withCompressionLevel(2)
                .serve(new InetSocketAddress(port), service);
        testService.start();
        Await.ready(server);
        logger.info("Server is started at port : " + port);
    }
}

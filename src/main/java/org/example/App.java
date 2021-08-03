package org.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.twitter.finagle.Http;
import com.twitter.finagle.ListeningServer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.HttpMuxer;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.TimeoutException;
import org.example.handler.TestHanlder;
import org.example.modules.TestModule;
import org.example.service.TestService;

import java.net.InetSocketAddress;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) throws TimeoutException, InterruptedException {
        Injector injector = Guice.createInjector(
                new TestModule()
        );
        TestService testService = injector.getInstance(TestService.class);
        Service<Request, Response> service = new TestHanlder().andThen(
                new HttpMuxer().withHandler(
                   "/test", testService
                )
        );
        ListeningServer server = Http.server()
                .withCompressionLevel(2)
                .serve(new InetSocketAddress(8080), service);
        Await.ready(server);
    }
}

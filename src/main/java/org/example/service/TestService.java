package org.example.service;

import co.paralleluniverse.strands.SuspendableCallable;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jd.broadway.grpc.BroadwayReply;
import com.jd.broadway.grpc.BroadwayRequest;
import com.jd.rec.jrf.Graph;
import com.jd.rec.jrf.GraphClosure;
import com.jd.rec.jrf.GraphData;
import com.jd.rec.jrf.GraphExecutor;
import com.jrf.base.fiber.FuturePool;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.http.Response.Ok;
import com.twitter.util.Future;
import com.twitter.util.Promise;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.example.graph.GraphPool;
import org.example.graph.item.PooledGraph;
import org.example.inter.TestInter;

import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestService extends Service<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);
    private static final String START = "testAResult";
    private TestInter testInter;

    private static final String RESULT = "testBResult";
    @Inject
    private GraphPool graphPool;
    /**
     * 图执行器.
     */
    @Inject
    private GraphExecutor graphExecutor;

    /**
     * 图提交执行线程池.
     */
    @Inject
    @Named("graph")
    private FuturePool graphFuturePool;

    public void start() {
        try {
            graphPool.start();
        } catch (Exception e) {
            LOGGER.error("odin service start error: ", e);
        }
    }

    @Inject
    TestService(@Named("testInter") TestInter testInter) {
        this.testInter = testInter;
    }

    @Override
    public Future<Response> apply(Request request) {
        LOGGER.error("request is {}", request);
        // testInter.test();
        Promise<Response> promise = new Promise<>();
                // 获取图对象
                PooledGraph pooledGraph = graphPool.get(request.getParam("handler"));
                if (pooledGraph == null) {
                    return null;
                }
                String result = "null";
                Graph graph = pooledGraph.getGraph();
                GraphData<String> startProcessor = graph.findData(START);
                if (startProcessor != null) {
                    startProcessor.ready(false);
                }
                GraphData<String> target = graph.findData(RESULT);
                GraphClosure graphClosure = graph.run(Arrays.asList(target));
                graphClosure.getGraphClosureContext().setExecutor(graphExecutor);
                result = target.getData();
                Response response = new Ok();
                final byte[] resByte;
                resByte = result.getBytes(Charset.forName("utf-8"));
                response.withOutputStream(output -> {
                    try {
                        if (resByte == null || resByte.length == 0) {
                            throw new RuntimeException("Invalid bytearray");
                        }
                        output.write(resByte);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return output;
                });


        promise.setValue(response);
        return promise;
    }
}

package org.example.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Promise;
import org.example.inter.TestInter;

import java.nio.charset.Charset;

public class TestService extends Service<Request, Response> {
    private TestInter testInter;

    @Inject
    TestService(@Named("testInter") TestInter testInter) {
        this.testInter = testInter;
    }

    @Override
    public Future<Response> apply(Request request) {

        testInter.test();

        Promise<Response> promise = new Promise<>();
        Response response = new Response.Ok();
        String proxyRes = "hello world---------;";
        final byte[] resByte;
        resByte = proxyRes.getBytes(Charset.forName("utf-8"));
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

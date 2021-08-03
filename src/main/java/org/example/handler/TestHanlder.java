package org.example.handler;

import com.twitter.finagle.Service;
import com.twitter.finagle.SimpleFilter;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;

import java.nio.charset.Charset;

public class TestHanlder extends SimpleFilter<Request, Response> {

    @Override
    public Future<Response> apply(Request request, Service<Request, Response> service) {
        return service.apply(request).map(resp->{
            byte[] bytes = resp.content().copiedByteArray();
            String str = new String(bytes);
            str += "my demo";
            final byte[] resBytes = str.getBytes(Charset.forName("utf-8"));
            Response response = new Response.Ok();
            response.withOutputStream(output -> {
                try {
                    if (resBytes == null || resBytes.length == 0) {
                        throw new RuntimeException("Invalid bytearray");
                    }
                    output.write(resBytes);
                } catch (Exception e) {
                    response.statusCode(500);
                }
                return output;
            });
            return response;
        });
    }
}

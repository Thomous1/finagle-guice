package org.example.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Promise;

import java.nio.charset.Charset;

public class ZkService extends Service<Request, Response> {


    private ZookeeperService zookeeperService;

    @Inject
    ZkService(@Named("zookeeperService") ZookeeperService zookeeperService) {
        this.zookeeperService = zookeeperService;
    }

    @Override
    public Future<Response> apply(Request request) {
        if (request.params() == null) {
            throw new RuntimeException("this request is a bad request, case dont have any request params");
        }
        String path = request.getParam("path");
        Response response = new Response.Ok();
        Promise<Response> promise = new Promise<>();
        try {
            String s = zookeeperService.getZkCurator().getCurator().create().forPath(path, "this is test zk demo".getBytes(Charset.forName("UTF-8")));
            byte[] pathBytes = s.getBytes(Charset.forName("UTF-8"));
            response.withOutputStream(output -> {
                try {
                    if (pathBytes == null || pathBytes.length == 0) {
                        throw new RuntimeException("Invalid bytearray");
                    }
                    output.write(pathBytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return output;
            });
            byte[] bytes = zookeeperService.getZkCurator().getCurator().getData().forPath(path);
            response.headerMap().add("test", new String(bytes, Charset.forName("UTF-8")));
            promise.setValue(response);
        } catch (Exception e) {
            promise.setValue(new Response.Ok());
            return promise;
        }
        return promise;
    }
}

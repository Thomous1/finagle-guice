package org.example.handler;

import com.twitter.finagle.Service;
import com.twitter.finagle.SimpleFilter;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;

import java.nio.charset.Charset;

/*
* 序列化懒得写公公方法了  哈哈哈哈哈哈哈哈哈隔
* */
public class TestHanlder extends SimpleFilter<Request, Response> {

    @Override
    public Future<Response> apply(Request request, Service<Request, Response> service) {
        return service.apply(request).map(resp->{
            byte[] bytes = resp.content().copiedByteArray();
            String str = new String(bytes);
            str += "-----my demo";
            if (resp.headerMap().contains("test")){
                // response headerMap get(K) 返回optional 容器
                str+= "-----" + resp.headerMap().get("test").get();
                resp.headerMap().remove("test");
            }
            final byte[] resBytes = str.getBytes(Charset.forName("utf-8"));
            resp.withOutputStream(output -> {
                try {
                    if (resBytes == null || resBytes.length == 0) {
                        throw new RuntimeException("Invalid bytearray");
                    }
                    output.write(resBytes);
                } catch (Exception e) {
                    resp.statusCode(500);
                }
                return output;
            });
            return resp;
        });
    }
}

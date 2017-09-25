package server.tools;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {
    private RequestParser() {
    }

    public static Map<String, String> getParam(FullHttpRequest req) throws IOException {
        HttpMethod method = req.method();
        Map<String, String> requestParams = new HashMap<>();

        if (HttpMethod.GET == method) {
            QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> parame = decoder.parameters();
            for (String key : parame.keySet()) {
                List<String> value = parame.get(key);
                if (value != null && value.size() > 0) {
                    requestParams.put(key, value.get(0));
                }
            }
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(req);
            decoder.offer(req);
            List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
            for (InterfaceHttpData parm : parmList) {
                Attribute data = (Attribute) parm;
                requestParams.put(data.getName(), data.getValue());
            }
        } else {
            throw new IOException("MethodNotSupportedException");
        }
        return requestParams;
    }
}

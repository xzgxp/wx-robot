package me.robin.wx.robot.frame.cookie;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuanlubin on 2017/4/19.
 */
public class CookieInterceptor implements Interceptor {

    private CookieHandler cookieHandler = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        URI uri = request.url().uri();
        List<String> cookies = cookieHandler.get(uri, new HashMap<>()).get("Cookie");

        Response networkResponse;
        if (null != cookies && !cookies.isEmpty()) {
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.header("Cookie", StringUtils.join(cookies,"; "));
            networkResponse = chain.proceed(requestBuilder.build());
            networkResponse = networkResponse.newBuilder().request(request).build();
        } else {
            networkResponse = chain.proceed(request);
        }
        Headers headers = networkResponse.headers();
        if (headers.size() > 0) {
            Map<String, List<String>> headersMap = new HashMap<>();
            for (String name : headers.names()) {
                List<String> values = headers.values(name);
                headersMap.put(name, values);
            }
            cookieHandler.put(uri, headersMap);
        }
        return networkResponse;
    }
}

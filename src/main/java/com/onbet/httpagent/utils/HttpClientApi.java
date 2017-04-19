package com.onbet.httpagent.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xsTao on 2016/8/22.
 */
@SuppressWarnings("Duplicates")
public class HttpClientApi  {
    public static final Logger LOG = LoggerFactory.getLogger(HttpClientApi.class);
    public  Map<String, String> getDefaultHeader(Map<String,String> headers){
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        header.put("Accept-Language", "en-us,en;q=0.5");
        header.put("Connection", "keep-alive");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        if(null != headers && !headers.isEmpty()){
            header.putAll(headers);
        }
        return header;
    }
    public String post(HttpParam param) {
        try {
            HttpClientBuilder builder=httpClientBuilder;
            String url=param.getUrl();
            if(isHttps(url)){
                Pattern ptn = Pattern.compile("^https://[A-Za-z0-9.:\\-]+");
                Matcher matcher = ptn.matcher(url);
                if (!matcher.find()) {
                    throw new RuntimeException("url[" + url + "]不是合法的https地址");
                }
                String domain = matcher.group();
                builder = getBuilder(domain);
            }
            if(StringUtils.isBlank(param.getContent())){
                return  post(builder,param.getUrl(),param.getParams(),
                        getDefaultHeader(param.getHeader()),param.getCharset());
            }
            return  post(builder,param.getUrl(),param.getContent(),
                    getDefaultHeader(param.getHeader()),param.getCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String get(HttpParam param) {
        try {
            HttpClientBuilder builder=httpClientBuilder;
            String url=param.getUrl();
            if(isHttps(url)){
                Pattern ptn = Pattern.compile("^https://[A-Za-z0-9.:\\-]+");
                Matcher matcher = ptn.matcher(url);
                if (!matcher.find()) {
                    throw new RuntimeException("url[" + url + "]不是合法的https地址");
                }
                String domain = matcher.group();
                builder = getBuilder(domain);
            }
            return get(builder, param.getUrl(), param.getParams(),
                    getDefaultHeader(param.getHeader())
                    , param.getCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public String exec(HttpParam param) {
        if(StringUtils.equalsIgnoreCase(param.getMethod(),"GET")){
            return get(param);
        }
        return post(param);
    }



    ///


    private BasicCookieStore cookieStore = new BasicCookieStore();
    private Map<String, String> header;
    public static final int TIMEOUT=5000;


//    public static void main(String[] args) {
//        HttpClientApi api=new HttpClientApi();
//        String 		url = "https://183.230.40.46:8043/bossagent/auth/ln/normalRequestHandler";
//        HttpParam param=new HttpParam();
//        param.setUrl("https://www.baidu.com");
//		String resp = api.post(param);
//		System.out.println(resp);
//    }


    private static RequestConfig requestConfig;
    //所有http请求共用一个builder
    private static HttpClientBuilder httpClientBuilder;

    //每个https域名占用一个builder
    private static Map<String, HttpClientBuilder> httpsBuilderMap = new ConcurrentHashMap<String, HttpClientBuilder>();

    static {
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(TIMEOUT);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
        httpClientBuilder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
    }
    private synchronized static HttpClientBuilder getBuilder(String domain) {
        HttpClientBuilder builder = httpsBuilderMap.get(domain);
        if (builder == null) {
            builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
            SSLConnectionSocketFactory sslCSFactory = createSSLConnSocketFactory();
            builder.setSSLSocketFactory(sslCSFactory);
            httpsBuilderMap.put(domain, builder);
        }
        return builder;
    }
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (GeneralSecurityException e) {
            LOG.error("https构造SSLConnectionSocketFactory出错", e);
        }
        return sslsf;
    }
    private String post(HttpClientBuilder builder,String url, Map<String, Object> params, Map<String, String> header, String charset)
            throws IOException {
        CloseableHttpClient client = builder.build();
        try {
            HttpPost httppost = new HttpPost(url);
            // 参数
            if (params != null) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
                httppost.setEntity(entity);
            }

            // 设置头文件
            if (header == null) {
                header = this.header;
            }
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httppost.addHeader(entry.getKey(), entry.getValue());
            }
            CloseableHttpResponse response = client.execute(httppost);
            try {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (status >= 200 && status < 300) {
                    if (StringUtils.isNotBlank(charset)) {
                        return entity != null ? EntityUtils.toString(entity, charset) : null;
                    } else {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status + " " + EntityUtils.toString(entity));
                }
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }
    }
    private  String post(HttpClientBuilder builder, String url,  String content,   Map<String, String> header,String charset) {
        CloseableHttpClient client = builder.build();
        HttpPost method = new HttpPost(url);
        if (null != header && !header.isEmpty()) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                method.addHeader(entry.getKey(), entry.getValue());
            }
        }
        String result = null;
        HttpResponse httpResponse = null;
        try {
            if (content != null) {
                method.setEntity(new StringEntity(content, charset));
            }

            httpResponse = client.execute(method);

            // 请求发送成功，并得到响应
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                try {
                    // 读取服务器返回过来的字符串数据
                    result = EntityUtils.toString(httpResponse.getEntity(), charset);
                } catch (Exception e) {
                    LOG.error("读取httpApi返回字符串数据失败:" + url, e);
                }
            } else {
                LOG.error("httpApi接口返回响应失败：response=" + httpResponse.getStatusLine());
            }
        } catch (IOException e) {
            LOG.error("httpApi：" + e.getMessage() + "[" + url + "]");
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOG.error("post请求提交boss失败：", e);
            throw new RuntimeException(e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
            }
        }
        return result;

    }

    private String get(HttpClientBuilder builder,String url, Map<String, Object> params, Map<String, String> header, String charset) throws IOException,
            URISyntaxException {
        // 设置cookies
        CloseableHttpClient httpclient =builder.build();
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    uriBuilder.setParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            // 设置头文件
            if (header == null) {
                header = this.header;
            }
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }

            CloseableHttpResponse response = httpclient.execute(httpGet);
            try {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (status >= 200 && status < 300) {
                    if (StringUtils.isNotBlank(charset)) {
                        return entity != null ? EntityUtils.toString(entity, charset) : null;
                    } else {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status + " " + EntityUtils.toString(entity));
                }
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
    public boolean isHttps(String url) {

        return url.startsWith("https://");
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }
}

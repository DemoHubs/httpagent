package com.onbet.httpagent.agent;

import com.onbet.httpagent.utils.HttpClientApi;
import com.onbet.httpagent.utils.HttpParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by xsTao on 2017/4/19.
 */
@RestController
@RequestMapping
public class HttpAgentController {
    private HttpClientApi api=new HttpClientApi();
    public static final String WHITESPACE = " ";


    @RequestMapping("/http2")
    public String httpAgent2(String url, String paramStr, Map<String, Object> param, String charset) {
        if(StringUtils.isEmpty(url)){
            return "";
        }
        if (StringUtils.contains(url, WHITESPACE)) {
            url = url.replaceAll(WHITESPACE, "%20");
        }
        if(StringUtils.isEmpty(charset)){
            charset="utf-8";
        }
        if (StringUtils.isNotBlank(paramStr)) {
            paramStr = paramStr.replaceAll(",", "&");
        }
        String fillUrl = StringUtils.endsWith(url, "?") ? url.concat(paramStr) : url.concat("?").concat(paramStr);
        System.out.println(fillUrl);
        HttpParam para = new HttpParam(fillUrl);
        if (param != null && param.size() > 0) {
            para.setParams(param);
           para.setMethod("POST");
            para.setCharset(charset);
        } else {
            para.setMethod("GET");
            para.setCharset(charset);
        }

        return  api.exec(para);
    }

    @RequestMapping("/http")
    public String httpAgent(String url, Map<String, Object> param, String charset) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }
        if (StringUtils.contains(url, WHITESPACE)) {
            url = url.replaceAll(WHITESPACE, "%20");
        }
        if (StringUtils.isEmpty(charset)) {
            charset = "utf-8";
        }
        System.out.println(url);
        HttpParam para = new HttpParam(url);
        if (param != null && param.size() > 0) {
            para.setParams(param);
            para.setMethod("POST");
            para.setCharset(charset);
        } else {
            para.setMethod("GET");
            para.setCharset(charset);
        }

        return api.exec(para);
    }
}

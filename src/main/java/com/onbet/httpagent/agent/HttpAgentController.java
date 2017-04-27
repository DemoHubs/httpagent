package com.onbet.httpagent.agent;

import com.onbet.httpagent.utils.HttpClientApi;
import com.onbet.httpagent.utils.HttpParam;
import org.springframework.util.StringUtils;
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
    @RequestMapping("/http")
    public String httpAgent(String url,Map<String,Object> param,String charset){
        if(StringUtils.isEmpty(url)){
            return "";
        }
        if(StringUtils.isEmpty(charset)){
            charset="utf-8";
        }
        HttpParam para=new HttpParam(url);
        if (param != null && param.size() > 0) {
            para.setParams(param);
           para.setMethod("POST");
            para.setCharset(charset);
        }

        return  api.exec(para);
    }
}

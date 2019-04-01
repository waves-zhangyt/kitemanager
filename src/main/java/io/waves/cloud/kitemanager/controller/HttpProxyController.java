/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.util.ConstUtil;
import io.waves.cloud.kitemanager.util.JSONUtil;
import io.waves.cloud.kitemanager.util.StringUtil;
import io.waves.cloud.kitemanager.websocket.Cmd;
import io.waves.cloud.kitemanager.websocket.CmdResult;
import io.waves.cloud.kitemanager.websocket.CmdResultSyncer;
import io.waves.cloud.kitemanager.websocket.KiteWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 *  内置 http proxy 接口
 * @author ytzhang0828@qq.com
 */
@RestController
public class HttpProxyController {

    private static Logger logger = LoggerFactory.getLogger(HttpProxyController.class);

    /** https schema 代理接口 */
    @RequestMapping("httpsproxy/{clientId}/{hostname}/{port}/**")
    public void httpsproxy(HttpServletRequest request, HttpServletResponse response,
                          @PathVariable("clientId") String clientId,
                          @PathVariable("hostname") String hostname, @PathVariable("port") Integer port) {
        forward(request, response, clientId, "https", hostname, port);
    }

    /** http schema 代理接口 */
    @RequestMapping("httpproxy/{clientId}/{hostname}/{port}/**")
    public void httpproxy(HttpServletRequest request, HttpServletResponse response,
                          @PathVariable("clientId") String clientId,
                          @PathVariable("hostname") String hostname, @PathVariable("port") Integer port) {
        forward(request, response, clientId, "http", hostname, port);
    }

    /**
     * 代理请求
     * @param request
     * @param response
     * @param clientId kiteagent id
     * @param schema http or https
     * @param hostname proxied host
     * @param port proxied port
     */
    public void forward(HttpServletRequest request, HttpServletResponse response,
                          String clientId,
                          String schema, String hostname, Integer port) {

        logger.debug("host{}, 端口{}", hostname, port);

        String uri = request.getRequestURI();
        String portStr = String.valueOf(port);
        String realUri = uri.substring(uri.indexOf("/" + portStr) + portStr.length() + 1);

        //请求url
        String requestUrl = schema +  "://" + hostname + ":" + port + realUri;
        //请求方法
        String method = request.getMethod();

        //请求头信息
        Map<String, String> headers = new HashMap<>();
        Enumeration enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            // 拒绝缓存
            if (isCacheAbourHeader(key)) continue;
            // Host需要重写
            if (key.equals("Host")) continue;

            headers.put(key, request.getHeader(key));
        }
        headers.put("Host", hostname + ":" + port);
        //一般浏览器会接受 gzip, deflate, br ，下面被注释掉的方式是只接受不经过压缩的内容来适应我的传输数据协议
        //后面把对于压缩过的按照二进制数据base64化处理了，所以这个方法也用不着了
        //headers.put("Accept-Encoding", "deflate");

        //请求body
        String body = null;
        String bodyType = null;
        byte[] bodyData = null;
        if (method.equals("POST") || method.equals("PUT")) {
            String contentType = request.getHeader("Content-Type");

            //把servlet层转换的form转换回原始值
            StringBuilder stringBuilder = new StringBuilder();
            if ("application/x-www-form-urlencoded".equals(contentType)) {
                bodyData = convertFormDataBackToBytes(request);
            }
            //直接抽取原始值
            else {
                bodyData = extractRequestBody(request);
            }

            //直接以utf8字符串格式传输
            if (isTextContent(contentType)) {
                bodyType = "UTF-8";
                body = new String(bodyData);
            }
            //以base64编码传输
            else {
                bodyType = "BASE64";
                body = Base64.getEncoder().encodeToString(bodyData);
            }
        }

        //timeout 涉及到服务端和客户端分别的策略，暂时使用默认值 todo
        //组建proxy.http命令body格式参数
        Map<String, Object> cmdBody = new LinkedHashMap<>();
        cmdBody.put("method", method);
        cmdBody.put("url", requestUrl);
        cmdBody.put("headers", headers);
        cmdBody.put("body", body);
        cmdBody.put("bodyType", bodyType);

        //发送命令并接收返回结果
        String jobId = StringUtil.uuid();
        CmdResultSyncer.addJob(jobId);
        CmdResult cmdResult = null;
        try {
            Cmd cmd = new Cmd(ConstUtil.proxyHttp, jobId, JSONUtil.encodeJSONString(cmdBody));
            KiteWebSocketEndpoint.sendCmd(clientId, cmd);

            long timeout = CmdResultSyncer.DEFAULT_TIMEOUT; //和客户端默认超时时间一样
            try {
                cmdResult = CmdResultSyncer.getJobResult(jobId, timeout + 3);
            } catch (Exception e) {
                logger.error("等待同步命令执行结果出现异常", e);
            }
            if (cmdResult == null) {
                throw new RuntimeException("命令超时");
            }
        } catch (Exception e) {
            sendErrorMessage(response, 400, e.getMessage());
            return;
        } finally {
            CmdResultSyncer.clearJob(jobId);
        }

        //有错误信息返回，直接返回给调用方
        if (!StringUtil.isEmpty(cmdResult.getStderr())) {
            sendErrorMessage(response, 401, cmdResult.getStderr());
            return;
        }

        //获取返回结果
        Map<String, Object> resultMap = (Map<String, Object>) JSONUtil.decodeJSONString(cmdResult.getStdout(), Map.class);

        //设置返回结果状态码
        Integer responseCode = (Integer) resultMap.get("responseCode");
        if (responseCode != null) {
            response.setStatus(responseCode);
        }

        //设置返回结果头信息
        Map<String, Object> resultHeaders = JSONUtil.decodeJSONString((String)resultMap.get("headers"));
        for (Map.Entry<String,Object> entry : resultHeaders.entrySet()) {
            if (entry.getValue() != null) {
                List<String> values = (List<String>) entry.getValue();
                if (!values.isEmpty()) response.setHeader(entry.getKey(), values.get(0)); //TODO 暂时只传递第一个数值
            }
        }

        //设置返回结果body
        try (OutputStream outputStream = response.getOutputStream()) {
            String contentType = (String) resultMap.get("contentType");
            String bodyStr = (String) resultMap.get("body");
            //contentEncoding 判断被压缩的按二进制内容处理
            String contentEncoding = response.getHeader("Content-Encoding");
            if (!StringUtil.isEmpty(contentType) &&
                    !"gzip".equals(contentEncoding) &&
                    (contentType.contains("json") || contentType.contains("xml") || contentType.contains("text") || contentType.contains("xhtml"))) {
                outputStream.write(bodyStr.getBytes("UTF-8"));
            }
            else {
                byte[] bodyBytes = Base64.getDecoder().decode(bodyStr);
                outputStream.write(bodyBytes);
            }
            outputStream.flush();
        } catch (IOException e) {
            logger.error("会写返回结果出错");
        }

    }

    /** 发送错误消息 */
    public void sendErrorMessage(HttpServletResponse response, int code, String text) {
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.setStatus(code);
            response.getWriter().println(text);
        } catch (IOException e) {
            logger.error("发送出错消息失败", e);
        }
    }

    /** 内容是否可以按文本类型对待（request） */
    public boolean isTextContent(String contentType) {
        return !StringUtil.isEmpty(contentType) && (
                contentType.contains("json") || contentType.contains("xml") || contentType.contains("text")
                        || contentType.contains("xhtml"));
    }

    /** 是否是缓存相关的头信息 */
    public boolean isCacheAbourHeader(String key) {
        return key.equals("Cache-Control") || key.equals("If-Modified-Since")
                || key.equals("If-None-Match");
    }

    /** 把servlet框架转化的form数据转换回去 */
    public byte[] convertFormDataBackToBytes(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            stringBuilder.append(key);
            stringBuilder.append("=");
            for (String val : values) {
                stringBuilder.append(StringUtil.encodeUrlWithUtf8(val));
                stringBuilder.append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return StringUtil.getUtf8Byte(stringBuilder.toString());
    }

    /** 抽取request body数据 */
    public byte[] extractRequestBody(HttpServletRequest request) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            InputStream inputStream = request.getInputStream();
            byte buff[] = new byte[1024];
            int read;
            while ((read = inputStream.read(buff)) > 0) {
                baos.write(buff, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try { baos.close(); } catch (IOException e) {}
        }
    }

}

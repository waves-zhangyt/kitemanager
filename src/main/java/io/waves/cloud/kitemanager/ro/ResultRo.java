/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.ro;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用命令返回结果对象
 * @param <T>
 */
@JsonInclude(value= JsonInclude.Include.NON_NULL)
public class ResultRo<T> {

    private static Logger logger = LoggerFactory.getLogger(ResultRo.class);

    private Integer code;
    private String msg;

    private T data;

    public ResultRo() { }

    public ResultRo(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public interface Processor {
        Object process();
    }

    public static ResultRo error(int code, String msg) {
        return new ResultRo(code, msg);
    }

    public static ResultRo process(Processor processor) {
        Object data = null;
        ResultRo resultRo = new ResultRo();

        try {
            data  = processor.process();
        } catch (Exception e) {
            logger.warn(processor.getClass().getCanonicalName() + " 运行出错 500", e.getMessage());
            logger.debug(processor.getClass().getCanonicalName() + " 运行出错 500", e);
            resultRo.code = 500;
            resultRo.msg = e.getMessage();
            return resultRo;
        }


        resultRo.code = 200;
        resultRo.msg = "ok";
        resultRo.data = data;

        return resultRo;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

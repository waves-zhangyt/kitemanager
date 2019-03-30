/**
 *  Created By ytzhang0828@qq.com
 *  Use of this source code is governed by a Apache-2.0 LICENSE
 */
package io.waves.cloud.kitemanager.framework;

import com.alibaba.fastjson.JSON;
import io.waves.cloud.kitemanager.ro.ResultRo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * controller helper
 * @author zhangyt
 */
@Aspect
@Component
public class ControllerHelper {

    private static Logger logger = LoggerFactory.getLogger(ControllerHelper.class);

    @Around("execution( * io.waves.cloud.*.controller..*.*(..))")
    public Object handleControllerMethod(ProceedingJoinPoint pjp) {

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        Class<?> clazz =  method.getReturnType();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String uri = request.getRequestURI();

        if(requestMapping == null || clazz != ResultRo.class) {
            long start = System.currentTimeMillis();
            Object[] args = pjp.getArgs();
            List<Object> argsList = Arrays.asList(args);
            try {
                Object obj = pjp.proceed(args);
                logger.debug("请求 {}, 执行 {}, 参数 {}, 用时 {}ms, 返回结果: {}", uri, pjp.getSignature(), argsList,
                        System.currentTimeMillis() - start, obj);
                return obj;
            } catch(Throwable throwable) {
                logger.debug("请求 {}, 执行 {}, 参数 {}, 用时 {}ms, 出现异常", uri, pjp.getSignature(), argsList,
                        System.currentTimeMillis() - start);
                throw new RuntimeException(throwable);
            }
        }

        Object returnObj = null;
        long start = System.currentTimeMillis();
        Object[] args = pjp.getArgs();
        List<Object> argsList = Arrays.asList(args);
        try {
            returnObj =  pjp.proceed(args);
            logger.debug("请求 {}, 参数 {} 用时 {}ms，返回结果: {}", uri, argsList, System.currentTimeMillis() - start,
                    JSON.toJSONString(returnObj));
        } catch (Throwable throwable) {
            logger.debug("请求 {}, 参数 {} 用时 {}ms, 出现异常", uri, argsList, System.currentTimeMillis() - start);
            returnObj = handlerException(pjp, throwable);
        }

        return returnObj;
    }

    /**
     * 异常处理架
     */
    private ResultRo<?> handlerException(ProceedingJoinPoint pjp, Throwable e) {
        ResultRo<?> resultRo = new ResultRo<>();
        logger.error(e.getMessage(), e);
        resultRo.setCode(500);
        resultRo.setMsg("error");

        return resultRo;
    }

}

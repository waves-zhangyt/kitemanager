package io.waves.cloud.kitemanager.controller;

import io.waves.cloud.kitemanager.ro.ResultRo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * uni exception handler
 * the main target is to override the default 400 error handler the springboot framework do
 */
@ControllerAdvice
public class UniExceptionHandler {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResultRo handleException(Exception e){
        return new ResultRo(450, e.getMessage());
    }

}

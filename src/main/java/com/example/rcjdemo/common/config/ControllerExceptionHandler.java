package com.example.rcjdemo.common.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/***
 * @description 异常信息拦截类，如果程序运行过程中抛出异常，会将异常拦截下来，并返回前端一个包含异常信息的Json数据
 */

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultVO handleServiceException(
            Exception e) {

        return ResultVO.getException(e);
    }
    //.......
}

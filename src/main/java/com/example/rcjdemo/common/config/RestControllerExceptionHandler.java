package com.example.rcjdemo.common.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/***
 * @description 异常信息拦截类，如果程序运行过程中抛出异常，会将异常拦截下来，并返回前端一个包含异常信息的Json数据
 */

@RestControllerAdvice
public class RestControllerExceptionHandler {

	  @ExceptionHandler(Exception.class)
	  public ResultVO handleServiceException(
			  RuntimeException e){
		  e.printStackTrace();
		  //封装错误信息
		  return ResultVO.getException(e);
	  }
	  //.......
}

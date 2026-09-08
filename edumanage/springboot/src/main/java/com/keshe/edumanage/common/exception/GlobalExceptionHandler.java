package com.keshe.edumanage.common.exception;
import com.keshe.edumanage.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(
            BusinessException e
    ){
        return Result.error(
                e.getCode(),
                e.getMessage()
        );
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(
            Exception e
    ){
        e.printStackTrace();

        return Result.error(
                500,
                "服务器异常"
        );
    }
}

package com.keshe.edumanage.common.exception;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.common.result.ResultCode;
import org.springframework.security.access.AccessDeniedException;
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
     * 处理权限不足（@PreAuthorize 校验失败时抛出 AccessDeniedException）
     * <p>必须单独处理：否则会被下面的 Exception 兜底吞成「服务器异常 500」，
     * 前端既看不到 403 也看不到真实原因（端到端联调时实际踩到过）。</p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(
            AccessDeniedException e
    ){
        return Result.error(
                ResultCode.FORBIDDEN,
                "无权限执行该操作"
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

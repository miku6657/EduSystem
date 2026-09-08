package com.keshe.edumanage.common.result;

/**
 * 统一返回状态码
 */
public interface ResultCode {
    /**
     * 成功
     */
    Integer SUCCESS = 200;

    /**
     * 请求失败
     */
    Integer ERROR = 500;

    /**
     * 参数错误
     */
    Integer PARAM_ERROR = 400;

    /**
     * 未认证
     */
    Integer UNAUTHORIZED = 401;

    /**
     * 无权限
     */
    Integer FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    Integer NOT_FOUND = 404;
}

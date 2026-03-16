package com.example.demo.recommendation.exception;

/**
 * 推荐异常基类
 * 所有推荐系统相关异常的父类
 */
public class RecommendationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errorCode;

    /**
     * 默认构造器
     */
    public RecommendationException() {
        super();
    }

    /**
     * 带消息的构造器
     * @param message 异常消息
     */
    public RecommendationException(String message) {
        super(message);
    }

    /**
     * 带消息和原因的构造器
     * @param message 异常消息
     * @param cause 异常原因
     */
    public RecommendationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带错误码和消息的构造器
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public RecommendationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带错误码、消息和原因的构造器
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 异常原因
     */
    public RecommendationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误码
     * @param errorCode 错误码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}

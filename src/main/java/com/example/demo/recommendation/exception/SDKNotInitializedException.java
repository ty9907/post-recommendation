package com.example.demo.recommendation.exception;

/**
 * SDK未初始化异常
 * 当SDK未初始化或已关闭时调用相关方法抛出此异常
 */
public class SDKNotInitializedException extends RecommendationException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_ERROR_CODE = "SDK_001";
    private static final String DEFAULT_MESSAGE = "SDK未初始化，请先调用initialize方法进行初始化";

    /**
     * 默认构造器
     */
    public SDKNotInitializedException() {
        super(DEFAULT_ERROR_CODE, DEFAULT_MESSAGE);
    }

    /**
     * 带消息的构造器
     * @param message 异常消息
     */
    public SDKNotInitializedException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 带消息和原因的构造器
     * @param message 异常消息
     * @param cause 异常原因
     */
    public SDKNotInitializedException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }
}

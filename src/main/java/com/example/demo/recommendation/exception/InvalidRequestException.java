package com.example.demo.recommendation.exception;

/**
 * 无效请求异常
 * 当推荐请求参数无效时抛出此异常
 */
public class InvalidRequestException extends RecommendationException {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_ERROR_CODE = "SDK_002";

    /**
     * 默认构造器
     */
    public InvalidRequestException() {
        super(DEFAULT_ERROR_CODE, "无效的推荐请求");
    }

    /**
     * 带消息的构造器
     * @param message 异常消息
     */
    public InvalidRequestException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 带消息和原因的构造器
     * @param message 异常消息
     * @param cause 异常原因
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 带字段名的构造器
     * @param fieldName 无效字段名
     * @param reason 无效原因
     * @return 异常实例
     */
    public static InvalidRequestException forField(String fieldName, String reason) {
        return new InvalidRequestException(
            String.format("请求参数'%s'无效: %s", fieldName, reason)
        );
    }

    /**
     * 空值异常
     * @param fieldName 空值字段名
     * @return 异常实例
     */
    public static InvalidRequestException nullField(String fieldName) {
        return new InvalidRequestException(
            String.format("请求参数'%s'不能为空", fieldName)
        );
    }
}

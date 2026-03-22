package com.example.demo.duplicate.async;

import com.example.demo.duplicate.model.DuplicateCheckReport;

/**
 * 异步检测回调。
 */
public interface DetectionResultCallback {

    void onComplete(DuplicateCheckReport report);

    default void onError(Exception exception) {
    }
}

package org.snailclient.activity.bean;

/**
 * Created by wubo1 on 2017/8/17.
 */

public enum TestType {
    PERFORMANCE(1, "性能测试"), COMPATIBILITY(2, "兼容性测试"), STABILITY(3, "稳定性测试");

    private int key;
    private String value;

    TestType(int mKey, String mValue) {
        this.key = mKey;
        this.value = mValue;
    }
}

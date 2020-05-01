package com.lagou.work.pojo;

public enum SqlCommandType {
    UNKNOWN,
    INSERT,
    UPDATE,
    DELETE,
    SELECT;

    private SqlCommandType() {
    }
}

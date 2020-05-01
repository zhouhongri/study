package com.lagou.work.io;

import java.io.InputStream;

public class Resources {

    // 根据配置文件路径，将配置文件解析字节流存储在内存中
    public static InputStream getResourceAsStream(String path) {
        InputStream resourceAsStream = Resources.class.getClassLoader().getResourceAsStream(path);
        return resourceAsStream;
    }
}

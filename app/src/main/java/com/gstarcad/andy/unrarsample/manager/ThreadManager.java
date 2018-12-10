package com.gstarcad.andy.unrarsample.manager;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Andy.R
 */
public class ThreadManager {

    /**
     * 核心线程数，除非allowCoreThreadTimeOut被设置为true，否则它闲着也不会死
     */
    private int corePoolSize = 5;
    /**
     * 最大线程数，活动线程数量超过它，后续任务就会排队
     */
    private int maximumPoolSize = 100;
    /**
     * 超时时长，作用于非核心线程（allowCoreThreadTimeOut被设置为true时也会同时作用于核心线程），闲置超时便被回收
     */
    private long keepAliveTime = 1;
    /**
     * 枚举类型，设置keepAliveTime的单位，有TimeUnit.MILLISECONDS（ms）、TimeUnit. SECONDS（s）等
     */
    private TimeUnit timeUnit = TimeUnit.HOURS;

    private ThreadPoolExecutor threadPoolExecutor;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("demo-pool-%d").build();

    private static final ThreadManager ourInstance = new ThreadManager();

    public static ThreadManager getInstance() {
        return ourInstance;
    }

    private ThreadManager() {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
                timeUnit, new LinkedBlockingDeque<Runnable>(), threadFactory);
    }

    public void start(Runnable runnable) {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.execute(runnable);
        }
    }

    public void stop() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }
}

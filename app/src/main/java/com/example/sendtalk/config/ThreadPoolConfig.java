package com.example.sendtalk.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:Wallace
 * @Description:
 * @Date:2021/4/12 21:32
 * @Modified By:
 */
public class ThreadPoolConfig {
    /**运行时CPU核心数*/
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**核心线程数量*/
    public static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    /**线程池最大容纳线程数
     * 对于IO密集型应用，此处为 ：运行时CPU心数*2+1
     * 对于cpu密集型应用，此处为 ：运行时CPU心数*+1
     */
    public static final int MAXIMUM_POOL_SIZE = CPU_COUNT + 1;
    /**线程空闲后的存活时长*/
    public static final int KEEP_ALIVE_TIME = 30;
    /**任务过多后，存储任务的一个阻塞队列*/
    public static final BlockingQueue<Runnable> WORK_QUEUE = new SynchronousQueue<>();
    /**线程的创建工厂*/
    public static ThreadFactory threadFactory = new ThreadFactory() {
        public final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "MyAsyncTask #" + mCount.getAndIncrement());
        }
    };
    /**线程池任务满载后采取的任务拒绝策略*/
    public static RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
}

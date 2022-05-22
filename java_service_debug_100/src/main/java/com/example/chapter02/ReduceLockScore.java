package com.example.chapter02;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 加锁要考虑锁的粒度和场景问题
 */
@Slf4j
public class ReduceLockScore {

    private final List<Integer> data = new ArrayList<>();

    // 不涉及共享资源的慢方法
    private void slow() {
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 错误的加锁方法
    public void wrong() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            // 加锁粒度太粗了
            synchronized (this) {
                slow();
                data.add(i);
            }
        });
        log.info("wrong took:{}", System.currentTimeMillis() - begin);
    }

    // 正确的加锁方法
    public void right() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(i -> {
            slow();
            // 只对List加锁
            synchronized (this) {
                data.add(i);
            }
        });
        log.info("right took:{}", System.currentTimeMillis() - begin);
    }

    @Test
    public void testAll() {
        /*
         * 00:43:28.880 [main] INFO com.example.chapter02.ReduceLockScore - wrong took:2326
         * 00:43:29.044 [main] INFO com.example.chapter02.ReduceLockScore - right took:160
         */
        new ReduceLockScore().wrong();
        new ReduceLockScore().right();
    }

}

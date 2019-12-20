package com.elastic.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-20 오전 10:16
 **/
@Slf4j
@Component
@Aspect
public class LogAspect {

    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object proceed = joinPoint.proceed();

        stopWatch.stop();
        log.info("##############################");
        log.info("### Execution Time : {} ms ###",stopWatch.getTotalTimeMillis());
        log.info("##############################");

        return proceed;
    }
}

package com.yimusi.common.log;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Web 层日志切面
 * 用于记录 Controller 层的请求参数、响应结果和执行耗时
 *
 * 配合 MDCFilter 使用，MDCFilter 负责记录基础请求信息(IP, URL, Method)，
 * 本切面负责记录业务处理细节(Class, Method, Args, Result)。
 */
@Aspect
@Component
@Slf4j
public class WebLogAspect {

    // 定义切点：扫描 controller 包及其子包下的所有 public 方法
    @Pointcut("execution(public * com.yimusi.controller..*.*(..))")
    public void webLog() {}

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 打印请求参数日志
        // 注意：如果参数包含敏感信息（如密码），建议在此处进行脱敏处理
        log.info("Request Process: {} | Args: {}", classMethod, Arrays.toString(args));

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            // 异常日志，记录耗时和错误信息
            log.error("Request Failed: {} | Duration: {}ms | Error: {}", classMethod, duration, e.getMessage());
            throw e;
        }

        long duration = System.currentTimeMillis() - startTime;

        // 打印响应日志
        // 注意：如果响应体过大，建议截断或不打印 Result
        log.info("Request Success: {} | Duration: {}ms | Result: {}", classMethod, duration, result);

        return result;
    }
}

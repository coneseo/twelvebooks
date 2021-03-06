package com.twelvebooks.twelvebook.aop;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.twelvebooks.twelvebook.util.RestProperties;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@Component // 1
@Aspect // 2
@Slf4j
@PropertySource( value = "/rest.properties", ignoreResourceNotFound = true )
public class RequestLoggingAspect {

    @Value("${slackApi.key}")
    private String accessKey;

    private final SlackMessage slackMessage = new SlackMessage();

    //paramMap을 스트링으로 변환시켜주는 메서드
    //stream().map()를 사용하여 변환시켜준다.
    private String paramMapToString(Map<String, String[]> paramMap) {
        return paramMap.entrySet().stream()
                .map(entry -> String.format("%s -> (%s)",
                        entry.getKey(), Joiner.on(",").join(entry.getValue())))
                .collect(Collectors.joining(", "));
    }

    //포인트컷 설정
    @Pointcut("within(com.twelvebooks.twelvebook.controller.api..*)") // 3
    public void onRequest() {}

    //api메서드들이 실행이 되고 나서 무슨 요청이 들어왔는지 log를 보여준다.
    @After("execution(* com.twelvebooks.twelvebook.controller.api.*.*(..))") // 4
    public Object doLogging(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = // 5
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        //파라미터로 받은 값들을 map에 저장
        Map<String, String[]> paramMap = request.getParameterMap();
        String params = "";
        //파라미터가 비어있지 않다면
        if (paramMap.isEmpty() == false) {
            params = " [" + paramMapToString(paramMap) + "]";
        }

        long start = System.currentTimeMillis();
        try {
            return joinPoint.getArgs();
        } finally {
            long end = System.currentTimeMillis();
            log.info("Request: {} {}{} < {} ({}ms)", request.getMethod(), request.getRequestURI(),
                    params, request.getRemoteHost(), end - start);

//            SlackApi slackApi = new SlackApi(""+accessKey+"");
//            slackMessage.setUsername("twelvebooksError");
//            slackMessage.setChannel("#general");
//            slackMessage.setText(" url={"+request.getRequestURL()+"}, errorMessage={"+request.getMethod()+"}");
//            slackApi.call(slackMessage);
        }
    }
    @AfterThrowing (pointcut = "execution(* com.twelvebooks.twelvebook.controller.api.*.*(..))", throwing = "ex")
    public void logAfterThrowingAllMethods(Exception ex) throws Throwable
    {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        log.error("Exception Occured at {} with {}", request.getRequestURI(), ex);
        SlackApi slackApi = new SlackApi(""+accessKey+"");
        slackMessage.setUsername("twelvebooksError");
        slackMessage.setChannel("#general");
        slackMessage.setText(" url={"+request.getRequestURL()+"}, errorMessage={"+ex+"}");
        slackApi.call(slackMessage);
    }

}


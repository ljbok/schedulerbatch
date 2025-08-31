package com.study.schedulerbatch.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    
    /**
     * scheduler 를 이용한 API 호출 시점 + 50초 뒤 log 기록하는 메소드
     * @param
     * @return logObject
     */

// @Scheduled 옵션들 정리 *****************************************************************************
// [ 바로 실행되게끔 설정하는 경우 (서버 시작 시 바로 실행) ] => 무한 실행 위험!! - 사용 X - 정리만!!
// 1. fixedRate = 0 : 서버 시작 시 바로 실행 후, 이후 주기에 맞게 실행
// @Scheduled(fixedRate = 0) // 바로 실행되고 이후 주기마다 실행

// 2. cron 표현식에서 "0"으로 시작하는 경우
//    - 예: "0 * * * * ?" : 매분 0초에 실행되며, 서버 시작 후 첫 실행도 바로 일어남
// @Scheduled(cron = "0 * * * * ?") // 매분 0초에 실행

// 3. initialDelay = 0, fixedDelay = 0 : 서버 시작 후 즉시 실행 =
// @Scheduled(initialDelay = 0, fixedDelay = 0) // 서버 시작 후 즉시 실행
// [ 주기적 실행 옵션 경우 ]
// 1. fixedRate: 호출 시 첫 실행은 바로 발생하고, 이전 작업의 "시작 시간" 기준 - 일정 시간 간격으로 반복 실행
// @Scheduled(fixedRate = 1000) // 이전 작업 시작 후 1초 마다 반복 실행

// 2. fixedDelay: 호출 시 첫 실행은 바로 발생하고, 이전 작업의 "종료 시간" 기준, 지정된 시간 간격으로 반복 실행
// @Scheduled(fixedDelay = 1000) // 이전 작업 완료 후 1초 마다 반복 실행

// 3. initialDelay: 지정된 시간 후에 첫 번째 실행, 이후에는 fixedRate 또는 fixedDelay에 따라 실행
//  @Scheduled(initialDelay = 5000, fixedRate = 1000) // 5초 후 첫 실행, 그 후 매 1초마다 실행

// 4. cron: cron 표현식을 사용한 복잡한 스케줄링 (초단위까지 조정 가능)
//  @Scheduled(cron = "0 0/1 * * * ?") // 매 1분마다 실행
// ***************************************************************************************************

    // api 호출 후 바로 실행  + 1분 마다 다시 실행
    @Scheduled(fixedDelay = 1000*60)
    @Async
    // public void setSchedulerLog(Map<String,Object> logObject) {
    public void setSchedulerLog() {
        LocalDateTime apiTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = apiTime.format(formatter);
        String date = formattedTime;
        String content = "@Schduled 어노테이션이 붙은 메소드에는 파라미터를 사용할 수 없다고 한다 ㅠㅠ";

        Map<String,Object> logObject = new HashMap<>();
        logObject.put("date",date);
        logObject.put("content",content);

        log.info("[setSchedulerLog]");
        log.info(
                "date : " + date
                + " , content : " + content
                + " , log object : " + logObject
        );
    }

    /**
     * @Async + taskscheduler 를 이용한 API request Body 타겟 시간 + 1분 뒤 log 기록하는 메소드
     * @param logObject
     * @return logObject
     */
    public void setTaskSchedulerLog(Map<String,Object> logObject) {

        String dateStr = (String) logObject.get("date"); // 입력받은 시간
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        LocalDateTime targetDate = dateTime.plusMinutes(1); // 입력받은 시간 + 1분 계산
        String content = (String) logObject.get("content");

        // 1분 후 시간을 Instant로 변환 (TaskScheduler는 Instant 객체를 사용)
        // Instant는 절대적인 시점을 나타내므로, LocalDateTime을 Instant로 변환하여 사용
        Instant scheduledTime = targetDate.atZone(ZoneId.systemDefault()).toInstant();

        log.info("[setTaskSchedulerLog]");
        log.info("setTaskSchedulerLog - scheduledTime : " + targetDate);

        // 정해진 시간에 한 번만 실행시키는 경우
        taskScheduler.schedule(() -> {
            asyncTaskSchedulerLog(content);
        }, scheduledTime);

        // api 호출 시 첫 실행시키고, 1분마다 반복실행 시키는 경우
        /*
        taskScheduler.scheduleAtFixedRate(() -> {
             asyncTaskSchedulerLog(content);
        }, 1000 * 60);
        */

        // 첫 실행자체를 정해진 시간에 실행시키고 그 이후부터 1분 마다 반복실행 시키는 경우
        /*
        taskScheduler.schedule(() -> {
            asyncTaskSchedulerLog(content);
        }, Date.from(firstExecutionTime.atZone(scheduledTime, 1000 * 60)); // 매개변수 (실행로직, Date.from(첫 실행 시작 시점, 이후 반복 주기))
        */
    }

    @Async
    public void asyncTaskSchedulerLog(String content) {
        log.info(
                "setTaskSchedulerLog - runTime : " + LocalDateTime.now()
                 + " , content : " + content
        );
    }
}

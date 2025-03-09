package com.study.schedulerbatch.scheduler.controller;

import com.study.schedulerbatch.scheduler.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/scheduler/log")
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping("/set")
    public ResponseEntity<?> setSchedulerLog(@RequestBody Map<String,Object> logObject) {
        try {
            // API 호출 시간 기록
            LocalDateTime apiTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = apiTime.format(formatter);
            log.info("[API-REQUEST-TIME (setTaskSchedulerLog)] ▶▶▶▶▶ " + formattedTime);

            // @Async + @Scheduled
            // @Scheduled 어노테이션이 붙은 메소드에는 파라미터를 사용할 수 없다고 한다ㅠㅜ
            // @Scheduled 어노테이션이 붙은 메소드는
            // schedulerService.setSchedulerLog();

            // @Async + taskScheduler
            schedulerService.setTaskSchedulerLog(logObject);

            return new ResponseEntity<Map<String,?>>(logObject, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

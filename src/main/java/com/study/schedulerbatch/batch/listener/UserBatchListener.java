package com.study.schedulerbatch.batch.listener;

import com.study.schedulerbatch.domain.entity.User1;
import com.study.schedulerbatch.domain.entity.User2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserBatchListener implements
        JobExecutionListener,
        StepExecutionListener,
        ItemReadListener<User1>,
        ItemProcessListener<User1, User2>,
        ItemWriteListener<User2>,
        SkipListener<User1, User2> {

    private static final String USER_1TO2_MIGRATION = "user1To2MigrationStep";

    // 🔹 StepExecutionListener
    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        log.info("[STEP START] stepName={}", stepName);

        if (USER_1TO2_MIGRATION.equals(stepName)) {
            log.info("[beforeStep] 아직 데이터 접근 전 단계입니다.");
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();
        long skipCount = stepExecution.getSkipCount();

        if (USER_1TO2_MIGRATION.equals(stepName)) {
            log.info("[STEP END] stepName={}", stepName);
            log.info("읽은 USER1 수: {}", readCount);
            log.info("마이그레이션된 USER2 수: {}", writeCount);
            log.info("스킵된 수: {}", skipCount);
        }

        return ExitStatus.COMPLETED;
    }

    // 🔹 JobExecutionListener
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[JOB START] jobName={}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("[JOB END] status={}", jobExecution.getStatus());
    }

    // 🔹 ItemReadListener
    @Override
    public void beforeRead() {
        log.info("[READ START]");
    }

    @Override
    public void afterRead(User1 item) {
        log.info("[READ END] ID={}, DATA={}", item.getId(), item);
    }

    @Override
    public void onReadError(Exception ex) {
        log.error("[READ ERROR] {}", ex.getMessage());
    }

    // 🔹 ItemProcessListener
    @Override
    public void beforeProcess(User1 item) {
        log.info("[PROCESS START] USERNAME={}", item.getUsername());
    }

    @Override
    public void afterProcess(User1 item, User2 result) {
        if (result != null) {
            log.info("[PROCESS END] USERNAME={} → {}", item.getUsername(), result.getUsername());
        } else {
            log.info("[PROCESS SKIPPED] USERNAME={} → result=null", item.getUsername());
        }
    }

    @Override
    public void onProcessError(User1 item, Exception e) {
        log.error("[PROCESS ERROR] USERNAME={}, reason={}", item.getUsername(), e.getMessage());
    }

    // 🔹 ItemWriteListener
    @Override
    public void beforeWrite(Chunk<? extends User2> items) {
        log.info("[WRITE START] 대상 수={}건, 리스트={}", items.size(), items);
    }

    @Override
    public void afterWrite(Chunk<? extends User2> items) {
        log.info("[WRITE END] 마이그레이션 완료: {}건", items.size());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends User2> items) {
        log.error("[WRITE ERROR] reason={}, 대상 수={}", exception.getMessage(), items.size());
    }

    // 🔹 SkipListener
    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("[SKIP READ] reason={}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(User1 item, Throwable t) {
        log.warn("[SKIP PROCESS] ID={}, USERNAME={}, reason={}", item.getId(), item.getUsername(), t.getMessage());
    }

    @Override
    public void onSkipInWrite(User2 item, Throwable t) {
        log.warn("[SKIP WRITE] ID={}, USERNAME={}, reason={}", item.getId(), item.getUsername(), t.getMessage());
    }
}

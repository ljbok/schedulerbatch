package com.study.schedulerbatch.batch.runner;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBatchRunner {
    private final JobLauncher jobLauncher;
    private final Job userMigrationJob;

    @PostConstruct
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터
                    .toJobParameters();

            jobLauncher.run(userMigrationJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

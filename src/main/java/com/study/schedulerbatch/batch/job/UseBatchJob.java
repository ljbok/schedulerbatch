package com.study.schedulerbatch.batch.job;

import com.study.schedulerbatch.batch.step.UserBatchStep;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseBatchJob {

    private final JobRepository jobRepository;
    private final UserBatchStep userBatchStep; // Step을 정의한 클래스

    @Bean
    public Job userMigrationJob() {
        return new JobBuilder("userMigrationJob", jobRepository)
                .start(userBatchStep.user1To2MigrationStep(jobRepository, userBatchStep.getTransactionManager()))
                .build();
    }
}

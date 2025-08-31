package com.study.schedulerbatch.batch.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    
}

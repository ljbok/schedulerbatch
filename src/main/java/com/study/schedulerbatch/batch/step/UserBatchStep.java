package com.study.schedulerbatch.batch.step;

import com.study.schedulerbatch.batch.listener.UserBatchListener;
import com.study.schedulerbatch.domain.entity.User1;
import com.study.schedulerbatch.domain.entity.User2;
import com.study.schedulerbatch.domain.repository.User1Repository;
import com.study.schedulerbatch.domain.repository.User2Repository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserBatchStep {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final User1Repository user1Repository;
    private final User2Repository user2Repository;
    private final UserBatchListener userBatchListener;

    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    // reader, processor, writer 정의 //////////////////////////////////////////////////////////////////////
    // role : 데이터 읽어오기
    private JpaPagingItemReader<User1> user1ListReader () {
        return new JpaPagingItemReaderBuilder<User1>()
                .name("userPrintReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User1 u ORDER BY u.id ASC")// 여기서 주의 : SQL 문법 X , JPQL 문법 O
                .pageSize(10)
                .build();
    }

    // role : 데이터 변환하기
    private ItemProcessor<User1, User2> user1to2MigrationProcessor () {
        return user1 -> {
            // 마이그레이션 할 데이터가 이미 User2 에 존재한다면 migration 대상에서 제외한다.
            Optional<User2> exist = user2Repository.findById(user1.getId());
            if (exist.isPresent()) {
                log.info("이미 값이 있어서 배치 로직 중단 !! ");
                return null;
            }

            String migratedAt = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            return User2.builder()
                    .id(user1.getId())
                    .age(user1.getAge())
                    .username(user1.getUsername())
                    .email(user1.getEmail())
                    .password(user1.getPassword())
                    .memo(migratedAt)
                    .build();

        };
    }

    // role : 데이터 실제로 저장시키기
    private ItemWriter<User2> user2ItemWriter () {
        return user2 -> {
            user2Repository.saveAll(user2);
        };
    }

    // 위에서 정의한 reader, processor, writer 를 step 으로 정의하는 부분 ////////////////////////////////////////////
    @Bean
    public Step user1To2MigrationStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        log.info("user1To2MigrationStep 스텝 실행 !!!! ");
        return new StepBuilder("user1To2MigrationStep", jobRepository)
                .<User1, User2>chunk(10, transactionManager)
                .reader(user1ListReader())
                .processor(user1to2MigrationProcessor())
                .writer(user2ItemWriter())
                .faultTolerant()
                .retry(TransientDataAccessException.class) // 일시적인 DB 접근 오류 발생 시 재시도
                .retryLimit(3) // 청크당 3 회가 아니라 한 step 기준
                .skip(NullPointerException.class) // null point exception 발생 시 스킵
                .skipLimit(10) // 청크당 10 회가 아니라 한 step 기준
                // .listener(userBatchListener) // 하나의 공통 리스터 클래스에 implements 여러개 해서 썼더니 프레임워크가 어느 인터페이스로 가지고 와야 하는지 몰라서 컴파일 에러남
                .listener((ItemReadListener<? super User1>) userBatchListener)
                .listener((ItemProcessListener<? super User1, ? super User2>) userBatchListener)
                .listener((ItemWriteListener<? super User2>) userBatchListener)
                .listener((SkipListener<? super User1, ? super User2>) userBatchListener)
                .listener((StepExecutionListener) userBatchListener)
                .build();
    }
}

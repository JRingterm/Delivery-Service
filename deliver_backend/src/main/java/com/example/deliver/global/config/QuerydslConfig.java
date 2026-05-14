
package com.example.deliver.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig { //Querydsl을 위한 JPAQueryFactory를 Bean으로 등록하는 파일.

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() { //Querydsl 쿼리를 작성하기 위해 필요한 JPAQueryFactory
        return new JPAQueryFactory(entityManager);
    }
}
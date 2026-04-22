package com.example.deliver.domain.store.entity;

import com.example.deliver.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) //필요할 때만 연관 객체를 DB에서 가져오게 하기위해 LAZY 설정. (불필요한 조회 줄이기)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder
    public Store(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }
}
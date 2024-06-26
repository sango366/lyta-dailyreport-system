package com.techacademy.entity;

import java.time.LocalDateTime;
import java.time.LocalDate;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Report {

    // ID
    @Id
    @NotEmpty
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    // 日付
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    // タイトル
    @Column(length = 100, nullable = false)
    @NotEmpty
    @Length(max = 100)
    private String title;

    // 内容
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    @NotEmpty
    private String content;

    // 社員番号
    @Column(name = "employee_code", length = 10, nullable = false)
    @NotEmpty
    @Length(max = 10)
    private String employeeCode;

    // 削除フラグ
    @Column(name = "delete_flg", columnDefinition = "TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
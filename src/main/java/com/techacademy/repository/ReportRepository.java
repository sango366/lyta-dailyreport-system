package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    List<Report> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate);

    List<Report> findByEmployeeCodeAndReportDateAndIdNot(String employeeCode, LocalDate reportDate, Integer id);

}
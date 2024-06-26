package com.techacademy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;

public interface ReportRepository extends JpaRepository<Employee, String> {
}
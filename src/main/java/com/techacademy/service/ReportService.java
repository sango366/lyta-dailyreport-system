package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
//    private final PasswordEncoder passwordEncoder;
//
    @Autowired
//    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
//        this.passwordEncoder = passwordEncoder;
    }


    // 日報一覧表示処理
    public List<Employee> findAll() {
        return reportRepository.findAll();
    }
}

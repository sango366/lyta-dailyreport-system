package com.techacademy.service;

import java.time.LocalDate;
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
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

//ログインしているユーザー情報を取得する用
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    //同じユーザーで同一日に日報がないかチェック
    public boolean existsByEmployeeCodeAndReportDate(String employeeCode, LocalDate reportDate) {
        return reportRepository.existsByEmployeeCodeAndReportDate(employeeCode, reportDate);
    }

    //同じユーザーで同一日に他の日報がないかチェック
    public List<Report> findByEmployeeCodeAndReportDateAndId(String employeeCode, LocalDate reportDate, Integer id) {
        return reportRepository.findByEmployeeCodeAndReportDateAndIdNot(employeeCode, reportDate, id);
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 1件を検索
    public Report findByReport(Integer id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    // 現在のユーザーの社員番号を取得
    public String getCurrentEmployeeCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    //日報の従業員を取得
    public List<Report> findByEmployeeCode(String employeeCode) {
        return reportRepository.findByEmployeeCode(employeeCode);
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(Integer id) {

        Report report = findByReport(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds renew(Report report, Integer id) {

        Report report_tmp = findByReport(id);

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(report_tmp.getCreatedAt());
        report.setEmployeeCode(report_tmp.getEmployeeCode());
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

}

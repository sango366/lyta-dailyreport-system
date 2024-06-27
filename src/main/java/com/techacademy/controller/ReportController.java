package com.techacademy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private final EmployeeService employeeService;

    @Autowired
    public ReportController(ReportService reportService, EmployeeService employeeService) {
        this.reportService = reportService;
        this.employeeService = employeeService;
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, Model model) {
        String employeeCode = reportService.getCurrentEmployeeCode();

        report.setEmployeeCode(employeeCode); // ログインユーザーの社員番号をセット

        Employee employee = employeeService.findByCode(employeeCode);
        if (employee != null) {
            model.addAttribute("employeeName", employee.getName());
        }

        model.addAttribute("report", report);
        return "reports/new";
    }

 // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model) {

        // 入力チェック
        if (res.hasErrors()) {
            return create(report, model); // 入力エラーがある場合、createメソッドにreportを渡して戻る
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportService.save(report); // レポートを保存

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report, model); // エラーがある場合、createメソッドにreportを渡して戻る
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(report, model); // エラーがある場合、createメソッドにreportを渡して戻る
        }

        return "redirect:/reports"; // 成功したらリダイレクト
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model) {
        // 日報リストを取得
        List<Report> reportList = reportService.findAll();

        // 日報と従業員情報を紐づけたマップを用意
        Map<Report, Employee> reportEmployeeMap = new HashMap<>();

        for (Report report : reportList) {
            // 日報に関連する従業員を取得
            Employee employee = employeeService.findByCode(report.getEmployeeCode());
            // 日報と従業員情報をマップに追加
            reportEmployeeMap.put(report, employee);
        }

        // マップのエントリセットをリストに変換
        List<Map.Entry<Report, Employee>> reportEmployeeList = new ArrayList<>(reportEmployeeMap.entrySet());

        // モデルにリストサイズ、日報と従業員のリストを追加
        model.addAttribute("listSize", reportEmployeeList.size());
        model.addAttribute("reportEmployeeList", reportEmployeeList);

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}")
    public String detail(@PathVariable Integer id, Model model) {

        // IDを使ってReportを取得
        Report report = reportService.findByReport(id);

        // ReportからemployeeCodeを取得し、Employeeを取得
        Employee employee = employeeService.findByCode(report.getEmployeeCode());

        // ReportとEmployeeをモデルに追加
        model.addAttribute("report", report);
        model.addAttribute("employee", employee);

        return "reports/detail";

    }

 // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable Integer id, Model model) {
        // 削除処理を実行し、結果を取得
        ErrorKinds result = reportService.delete(id);

        if (ErrorMessage.contains(result)) {
            // エラーメッセージが含まれている場合、エラーをモデルに追加し、詳細画面にリダイレクト
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findByReport(id));
            return detail(id, model);
        }

        // 成功した場合は一覧画面にリダイレクト
        return "redirect:/reports";
    }

    // 従業員更新画面
    @GetMapping("/{id}/update")
    public String update(@PathVariable Integer id, Model model) {
        if(id == null) {
            return "/reports/update";
        }

        // IDを使ってReportを取得
        Report report = reportService.findByReport(id);

        // ReportからemployeeCodeを取得し、Employeeを取得
        Employee employee = employeeService.findByCode(report.getEmployeeCode());

        model.addAttribute("report", reportService.findByReport(id));
        model.addAttribute("employee", employee);
        // User更新画面に遷移
        return "reports/update";
    }


}

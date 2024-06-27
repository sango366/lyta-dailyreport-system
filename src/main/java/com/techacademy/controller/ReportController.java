package com.techacademy.controller;

import java.util.ArrayList;
import java.util.List;

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

        List<Report> reportList = reportService.findAll();

        List<Employee> employees = new ArrayList<>();

        for (Report report : reportList) {
            Employee employee = employeeService.findByCode(report.getEmployeeCode());
            employees.add(employee);
        }

        model.addAttribute("listSize", reportList.size());
        model.addAttribute("reportList", reportList);
        model.addAttribute("employees", employees);

        return "reports/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable Integer id, Model model) {

        model.addAttribute("report", reportService.findByReport(id));
        return "reports/detail";
    }

}

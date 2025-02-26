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
import org.springframework.security.core.userdetails.UserDetails;

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

        // 現在のログインユーザーを取得
        String currentUser = reportService.getCurrentEmployeeCode();

        // 指定された日付と現在のログインユーザーの日報データが存在するか確認
        boolean reportExists = reportService.existsByEmployeeCodeAndReportDate(currentUser, report.getReportDate());

        //Trueだった場合
        if (reportExists) {
            model.addAttribute("errorMessage", "既に登録されている日付です。");
            return create(report, model); // エラーメッセージを設定して戻る
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

        String employeeCode = reportService.getCurrentEmployeeCode();
        // ログインユーザーの社員番号をセット
        Employee currentUser = employeeService.findByCode(employeeCode);

        // 日報リストを宣言
        List<Report> reportList;

        // ログインユーザーが一般ユーザーの場合、自分の日報のみを取得
        if (currentUser.getRole() == Employee.Role.GENERAL) {
            //repositoryで定義しているので、以下のようなクエリが発行される
            //SELECT * FROM reports WHERE employee_code = ? 結果これだけで希望のデータが取れる
            reportList = reportService.findByEmployeeCode(currentUser.getCode());
        } else {
            // 管理者の場合は全ての日報を取得
            reportList = reportService.findAll();
        }

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

    // 日報更新画面
    @GetMapping("/{id}/update")
    public String update(@PathVariable Integer id, Model model, Report report) {
        if(id == null){
            // バリデーションチェックに引っかかった場合は、idがnull。postMappingから遷移
            Employee employee = employeeService.findByCode(report.getEmployeeCode());
            //postMappingのメソッドから持ってきたreportインスタンスをそのままセット
            model.addAttribute("report", report);
            model.addAttribute("employee", employee);

          }else{
            // idがnull以外⇒詳細画面から遷移。idを使ってReportを取得
              report = reportService.findByReport(id);

              // ReportからemployeeCodeを取得し、Employeeを取得
              Employee employee = employeeService.findByCode(report.getEmployeeCode());

              model.addAttribute("report", reportService.findByReport(id));
              model.addAttribute("employee", employee);
          }
        // User更新画面に遷移
        return "reports/update";
    }

    /** 日報更新処理 @PostMapping画面でもらってきたデータを受け取って処理をする*/
    @PostMapping("/{id}/update")
    public String postReport(@PathVariable Integer id, @Validated Report report, BindingResult res, Model model) {

        // 現在のログインユーザーを取得
        String currentUser = reportService.getCurrentEmployeeCode();

        if(res.hasErrors()) {
            model.addAttribute("report", report);
            return update(null, model, report);
        }

        // 現在のログインユーザー以外の日報データで、指定ID以外の日付の重複をチェック Notをつけると除外される
        List<Report> otherReports = reportService.findByEmployeeCodeAndReportDateAndId(currentUser, report.getReportDate(), id);

        // 他の日報データで同じ日付が存在する場合
        if (!otherReports.isEmpty()) {
            model.addAttribute("errorMessage", "既に登録されている日付です。");
            return update(null, model, report); // エラーメッセージを設定して更新画面に戻る
        }


        ErrorKinds result = reportService.renew(report, id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return update(null, model, report);
        }

        return "redirect:/reports";
    }


}

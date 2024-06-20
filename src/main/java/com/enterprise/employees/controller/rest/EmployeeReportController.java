package com.enterprise.employees.controller.rest;

import com.enterprise.employees.service.EmployeeReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class EmployeeReportController {

    @Autowired
    private EmployeeReportService employeeReportService;

    @GetMapping("/employees/pdf")
    public ResponseEntity<byte[]> getEmployeePdfReport() {
        try {
            byte[] report = employeeReportService.generateEmployeeReport();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "employee_report.pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}

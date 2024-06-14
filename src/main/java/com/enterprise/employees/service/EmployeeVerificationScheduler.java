//package com.enterprise.employees.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EmployeeVerificationScheduler {
//    @Autowired
//    private EmployeeService employeeService;
//
//    @Scheduled(fixedRate = 300000) // Run every 5 minutes
//    public void deleteUnverifiedEmployees() {
//        employeeService.deleteUnverifiedEmployees();
//    }
//}

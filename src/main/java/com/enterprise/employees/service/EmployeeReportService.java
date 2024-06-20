package com.enterprise.employees.service;
import net.sf.jasperreports.engine.*;

import com.enterprise.employees.model.Employee;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.Task;
import com.enterprise.employees.repository.EmployeesRepository;
import com.enterprise.employees.repository.ProjectRepository;
import com.enterprise.employees.repository.TaskRepository;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeReportService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeReportService.class);


    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;


    public   byte[] generateEmployeeReport() throws JRException {

        List<Map<String, Object>> reportData = new ArrayList<>();
        List<Map<String, Object>> reportSubData = new ArrayList<>();


        long totalEmployees = employeesRepository.findAll().size();
        long employeesInProjectsCount = employeesRepository.findEmployeesInProjects().size();
        long employeesInTasksCount = employeesRepository.findEmployeesInTasks().size();
        long remainingEmployees = totalEmployees - employeesInProjectsCount - employeesInTasksCount;


        List<Employee> employeesInProjects = employeesRepository.findEmployeesInProjects();
        List<Employee> employeesInTasks = employeesRepository.findEmployeesInTasks();
        List<Employee> allEmployees = employeesRepository.findAll();

        for (Employee employee : allEmployees) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", employee.getFullName());

            List<Project> projects = projectRepository.findByEmployeesContaining(employee);
            String projectNames = projects.stream().map(Project::getName).collect(Collectors.joining(", "));
            data.put("projects", projectNames.isEmpty() ? "0" : projectNames);

            List<Task> tasks = taskRepository.findByEmployeesContaining(employee);
            String taskNames = tasks.stream().map(Task::getName).collect(Collectors.joining(", "));
            data.put("tasks", taskNames.isEmpty() ? "0" : taskNames);

            reportSubData.add(data);
        }

        logger.info("Report data prepared with " + reportSubData.size() + " entries");



        Map<String, Object> projectsCountData = new HashMap<>();
        projectsCountData.put("label", "Employees in Projects");
        projectsCountData.put("value", employeesInProjectsCount);
        reportData.add(projectsCountData);

        Map<String, Object> tasksCountData = new HashMap<>();
        tasksCountData.put("label", "Employees in Tasks");
        tasksCountData.put("value", employeesInTasksCount);
        reportData.add(tasksCountData);

        Map<String, Object> remainingEmployeesData = new HashMap<>();
        remainingEmployeesData.put("label", "Remaining Employees");
        remainingEmployeesData.put("value", remainingEmployees);
        reportData.add(remainingEmployeesData);

        logger.info("Report data prepared with " + reportData.size() + " entries");


        try {

            // Load the main report template
            InputStream mainReportStream = getClass().getResourceAsStream("/reports/employee_report.jrxml");
            JasperReport mainReport = JasperCompileManager.compileReport(mainReportStream);
            JRSaver.saveObject(mainReport, "C:/src/employees/target/classes/reports/employee_report.jasper");

            // Load the sub report template
            InputStream subReportStream = getClass().getResourceAsStream("/reports/jasperStudioSubReport.jrxml");
            JasperReport subReport = JasperCompileManager.compileReport(subReportStream);
            JRSaver.saveObject(subReport, "C:/src/employees/target/classes/reports/jasperStudioSubReport.jasper");




            // Prepare parameters for the main report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("totalEmployees", totalEmployees);
            parameters.put("employeesInProjectsCount", employeesInProjectsCount);
            parameters.put("employeesInTasksCount", employeesInTasksCount);
            parameters.put("remainingEmployees", remainingEmployees);

            // Add the directory path for sub-reports
            String subReportDir = Objects.requireNonNull(getClass().getResource("/reports/")).getPath();
            parameters.put("SUBREPORT_DIR", subReportDir);
            System.out.println("Sub-report directory: " + subReportDir);


            JRBeanCollectionDataSource subDataSource  = new JRBeanCollectionDataSource(reportSubData);
            JRBeanCollectionDataSource mainReportDataSource = new JRBeanCollectionDataSource(reportData);

            // Add subReport data source to main report parameters
            parameters.put("subReportDataSource", subDataSource);

            JasperPrint jasperPrint = JasperFillManager.fillReport(mainReport, parameters, mainReportDataSource);


//            JasperPrint jasperPrint = null;
//
//            try {
//                jasperPrint = JasperFillManager.fillReport(mainReport,parameters, mainReportDataSource);
//            } catch (JRException e) {
//                logger.error("Failed to fill report: {}", e.getMessage());
//                throw new RuntimeException(e);
//            }
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }catch (JRException e) {
        e.printStackTrace(); // Log the stack trace for detailed error information
        throw new RuntimeException("Failed to compile Jasper report", e); // Throw an exception or handle appropriately
    }

    }

}

package com.enterprise.employees;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Skill;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.SkillRepository;
import com.enterprise.employees.service.EmailService;
import net.sf.jasperreports.engine.JasperCompileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import java.util.List;

@SpringBootApplication
public class EmployeesApplication implements CommandLineRunner {

	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	SkillRepository skillRepository;




	/**
	 * The main method of the application.
	 *
	 * @param  args  the command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(EmployeesApplication.class, args);

//		try {
//			// Compile employee_details.jrxml into employee_details.jasper
//			JasperCompileManager.compileReportToFile(
//					"src/main/resources/reports/employee_details.jrxml",
//					"src/main/resources/reports/employee_details.jasper"
//			);
//			System.out.println("Subreport compiled successfully.");
//		} catch (Exception e) {
//			System.err.println("Subreport compilation error: " + e.getMessage());
//			e.printStackTrace();
//		}
	}


	@Override
	public void run(String... args) throws Exception {
		if(departmentRepository.count() == 0) {
			Department d = new Department();
			d.setName("IT");

			Department d2 = new Department();
			d2.setName("HR");

			Department d3 = new Department();
			d3.setName("SOFTWARE");

			departmentRepository.saveAll(List.of(d, d2, d3));
		}

		if(skillRepository.count() == 0){
			Skill s = new Skill();
			s.setName("Networking");

			Skill s2 = new Skill();
			s2.setName("System Administration");

			Skill s3 = new Skill();
			s3.setName("Cybersecurity");

			Skill s4 = new Skill();
			s4.setName("Database Management");

			Skill s5 = new Skill();
			s5.setName("Cloud Computing");

			Skill s6 = new Skill();
			s6.setName("Technical Support and Troubleshooting");

			Skill s7 = new Skill();
			s7.setName("Software Development");

			Skill s8 = new Skill();
			s8.setName("Virtualization and Containerization");

			Skill s9 = new Skill();
			s9.setName("IT Project Management");

			skillRepository.saveAll(List.of(s, s2, s3, s4, s5, s6, s7, s8, s9));


		}

	}
}

package com.enterprise.employees;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.service.EmailService;
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




	/**
	 * The main method of the application.
	 *
	 * @param  args  the command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(EmployeesApplication.class, args);
	}

	/**
	 * Runs the application with the given command line arguments.
	 * This method is executed upon application startup and is used to initialize
	 * and save several Department entities to the departmentRepository.
	 *
	 * @param  args  the command line arguments
	 * @throws Exception  if an error occurs while running the application
	 */
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

	}
}

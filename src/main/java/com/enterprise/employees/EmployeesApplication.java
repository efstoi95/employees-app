package com.enterprise.employees;

import com.enterprise.employees.model.Department;
import com.enterprise.employees.model.Skill;
import com.enterprise.employees.repository.DepartmentRepository;
import com.enterprise.employees.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Collections;
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

	}


	@Override
	public void run(String... args) throws Exception {

		//getPosts();

	}

//	private void getPosts() {
//		RestClient restClient = RestClient.create();
//		var result = restClient.get()
//				.uri("https://jsonplaceholder.typicode.com/posts")
//				.retrieve()
//				.body(String.class);
//
//		System.out.println(result);
//	}
}





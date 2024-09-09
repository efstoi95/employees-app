package com.enterprise.employees.service;

import com.enterprise.employees.model.Poste;
import com.enterprise.employees.model.Project;
import com.enterprise.employees.model.ProjectDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostRestClientService {

    private final RestClient restClient = RestClient.create();
    String url= "https://jsonplaceholder.typicode.com/posts";


    public List<ProjectDTO> postProjectData(List<ProjectDTO> projects){
        List<ProjectDTO> responseList = new ArrayList<>();
        try {
            for (ProjectDTO project : projects) {
                ProjectDTO response = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(project)
                        .retrieve()
                        .body(ProjectDTO.class);
                responseList.add(response);
            }
            return responseList;

        } catch (RestClientException e) {
            System.out.println("Response status: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Poste> findAllPosts() {
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Poste>>() {});
    }
}

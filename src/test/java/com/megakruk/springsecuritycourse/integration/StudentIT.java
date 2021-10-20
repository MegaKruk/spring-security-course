package com.megakruk.springsecuritycourse.integration;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.megakruk.springsecuritycourse.student.Student;
import com.megakruk.springsecuritycourse.student.StudentRepo;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-it.properties")
@TestMethodOrder(OrderAnnotation.class)
@AutoConfigureMockMvc
public class StudentIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private RestTemplate restTemplate;

    private final Faker faker = new Faker();
    private final String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkb20iLCJhdXRob3JpdGllcyI6W" +
            "3siYXV0aG9yaXR5Ijoic3R1ZGVudDp3cml0ZSJ9LHsiYXV0aG9yaXR5Ijoic3R1ZGVudDpyZWFkIn0seyJhdXR" +
            "ob3JpdHkiOiJjb3Vyc2U6cmVhZCJ9LHsiYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9LHsiYXV0aG9yaXR5IjoiY" +
            "291cnNlOndyaXRlIn1dLCJpYXQiOjE2MzQ2Njk0NjcsImV4cCI6MTYzNTQ1ODQwMH0.rkUCjCxTnGrNbEEf-OT" +
            "2FU5MQw6ny_aahikpL7oU1REHAxZglgHFE42lqo9IFaaxqFGL76GVZwxqZi-5lmOfUg";

    private final static String REGISTER_URI = "/management/api/v1/students/add";
    private final static String GET_ALL_URI = "/management/api/v1/students/all";
    private final static String UPDATE_URL = "http://localhost:8080/management/api/v1/students/%s";
    private final static String UPLOAD_URL = "http://localhost:8080/management/api/v1/students/15/upload";
    private final static String DOWNLOAD_URL = "http://localhost:8080/management/api/v1/students/15/download/";
    private final static String DOWNLOAD_PATH = "/home/megakruk/workspace/IdeaProjects/spring-security-course/upload/test/";
    private final static String FILENAME = "17703339.jpeg";
    private final static String KEY = "file";
    private final static String EMAIL_FORMAT = "%s%s@gmail.com";

    @Test
    @Order(1)
    void canRegisterNewStudent() throws Exception {
        //given
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        String email = String.format(EMAIL_FORMAT,
                StringUtils.trimAllWhitespace(firstName.trim().toLowerCase()),
                StringUtils.trimAllWhitespace(lastName.trim().toLowerCase()));
        Student student = new Student(
                firstName,
                lastName,
                email
        );

        //when
        ResultActions resultActions = mockMvc
                .perform(post(REGISTER_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(objectMapper.writeValueAsString(student)));

        //then
        resultActions.andExpect(status().isCreated());
        List<Student> students = studentRepo.findAll();
        assertThat(students)
                .usingElementComparatorIgnoringFields("id")
                .contains(student);
    }

    @Test
    @Order(2)
    void canUpdateStudent() throws Exception {
        //given
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = String.format(EMAIL_FORMAT,
                StringUtils.trimAllWhitespace(firstName.trim().toLowerCase()),
                StringUtils.trimAllWhitespace(lastName.trim().toLowerCase()));
        Student student = new Student(
                firstName,
                lastName,
                email
        );
        MvcResult result = mockMvc.perform(get(GET_ALL_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        List<Student> students = objectMapper.readValue(resultAsString, new TypeReference<>() {});
        long idToUpdate = students.stream()
                .map(Student::getId)
                .max(Long::compare)
                .orElseThrow(() -> new IllegalStateException("Nothing to update"));

        //when
        ResultActions resultActions = mockMvc.perform(put(String.format(
                UPDATE_URL, idToUpdate))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .content(objectMapper.writeValueAsString(student)));

        //then
        resultActions.andExpect(status().isOk());
        List<Student> updatedStudents = studentRepo.findAll();
        Student shouldBeUpdatedStudent = updatedStudents.stream()
                .filter(s -> s.getId() == idToUpdate)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Updated student not found"));
        assertThat(shouldBeUpdatedStudent.getFirstName()).isEqualTo(student.getFirstName());
        assertThat(shouldBeUpdatedStudent.getLastName()).isEqualTo(student.getLastName());
        assertThat(shouldBeUpdatedStudent.getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    @Order(3)
    void canDeleteStudent() throws Exception {
        //given
        MvcResult result = mockMvc.perform(get(GET_ALL_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();
        String resultAsString = result.getResponse().getContentAsString();
        List<Student> students = objectMapper.readValue(resultAsString, new TypeReference<>() {});
        long idToDelete = students.stream()
                .map(Student::getId)
                .max(Long::compare)
                .orElseThrow(() -> new IllegalStateException("Nothing to delete"));

        //when
        ResultActions resultActions = mockMvc.perform(delete(String.format(
                UPDATE_URL, idToDelete))
                .header(HttpHeaders.AUTHORIZATION, token));

        //then
        resultActions.andExpect(status().isOk());
        boolean exists = studentRepo.existsById(idToDelete);
        assertThat(exists).isFalse();
    }

    @Test
    @Order(4)
    void testUpload() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(KEY, new ClassPathResource(FILENAME));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                UPLOAD_URL,
                httpEntity,
                Boolean.class
        );
        assertThat(response.getBody()).isNotNull();
        System.out.println(response.getBody());
    }

    @Test
    @Order(5)
    void testDownload() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        headers.setBearerAuth(token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                DOWNLOAD_URL + FILENAME,
                HttpMethod.GET,
                httpEntity,
                byte[].class
        );
        assertThat(response.getBody()).isNotNull();
        Files.write(Paths.get(DOWNLOAD_PATH + FILENAME), response.getBody());
    }
}

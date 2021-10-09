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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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

    private final Faker faker = new Faker();
    private final String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkb20iLCJhdXRob3JpdGllcy" +
            "I6W3siYXV0aG9yaXR5Ijoic3R1ZGVudDp3cml0ZSJ9LHsiYXV0aG9yaXR5Ijoic3R1ZGVudDpyZWFkIn0se" +
            "yJhdXRob3JpdHkiOiJjb3Vyc2U6cmVhZCJ9LHsiYXV0aG9yaXR5IjoiUk9MRV9BRE1JTiJ9LHsiYXV0aG9y" +
            "aXR5IjoiY291cnNlOndyaXRlIn1dLCJpYXQiOjE2MzM3MTM0OTQsImV4cCI6MTYzNDUwODAwMH0.Nkb1R19" +
            "sQWw0XNEC6LoJddeLubRUTPOLvppaOYoUpUmqEdWmRaSNYLxH3_aed01Uqqap2jCrKSJVGHjHxWIUxw";

    @Test
    @Order(1)
    void canRegisterNewStudent() throws Exception {
        //given
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String email = String.format("%s%s@gmail.com",
                StringUtils.trimAllWhitespace(firstName.trim().toLowerCase()),
                StringUtils.trimAllWhitespace(lastName.trim().toLowerCase()));
        Student student = new Student(
                firstName,
                lastName,
                email
        );

        //when
        ResultActions resultActions = mockMvc
                .perform(post("/management/api/v1/students/add")
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, token)
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
        String email = String.format("%s%s@gmail.com",
                StringUtils.trimAllWhitespace(firstName.trim().toLowerCase()),
                StringUtils.trimAllWhitespace(lastName.trim().toLowerCase()));
        Student student = new Student(
                firstName,
                lastName,
                email
        );
        MvcResult result = mockMvc.perform(get("/management/api/v1/students/all")
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, token))
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
                "http://localhost:8080/management/api/v1/students/%s", idToUpdate))
                .contentType(APPLICATION_JSON)
                .header(AUTHORIZATION, token)
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
        MvcResult result = mockMvc.perform(get("/management/api/v1/students/all")
                        .contentType(APPLICATION_JSON)
                        .header(AUTHORIZATION, token))
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
                "http://localhost:8080/management/api/v1/students/%s", idToDelete))
                .header(AUTHORIZATION, token));

        //then
        resultActions.andExpect(status().isOk());
        boolean exists = studentRepo.existsById(idToDelete);
        assertThat(exists).isFalse();
    }
}

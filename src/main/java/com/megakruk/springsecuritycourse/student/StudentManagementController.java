package com.megakruk.springsecuritycourse.student;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.http.MediaType.IMAGE_JPEG;

@RestController
@RequestMapping("management/api/v1/students")
public class StudentManagementController {

    private final StudentService studentService;
    @Value("${uploadDir}")
    private String UPLOAD_DIR;

    @Autowired
    public StudentManagementController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @ApiOperation(
            value = "Retrieves all students",
            notes = "A list of students",
            response = Student.class,
            produces = "application/json"
    )
    @GetMapping(path = "/all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMININTERN')")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.findAllStudents();
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Retrieves a student",
            notes = "A student",
            response = Student.class,
            produces = "application/json"
    )
    @GetMapping(path = "{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ADMININTERN')")
    public ResponseEntity<Student> getStudent(@PathVariable("id") Long id) {
        Student student = studentService.findStudentById(id);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Adds a student",
            notes = "Student registration",
            response = Student.class,
            produces = "application/json"
    )
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('student:write')")
    public ResponseEntity<Student> addStudent(@Valid @RequestBody Student student) {
        Student newStudent = studentService.addStudent(student);
        return new ResponseEntity<>(newStudent, HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Updates student info",
            notes = "Student update operation",
            response = Student.class,
            produces = "application/json"
    )
    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('student:write')")
    public ResponseEntity<Student> updateStudent(
            @PathVariable("id") Long id,
            @Valid @RequestBody Student student
    ) {
        studentService.updateStudent(id, student);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(
            value = "Deletes a student from repository",
            notes = "Student delete operation",
            response = Student.class
    )
    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('student:write')")
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("{id}/upload")
    @PreAuthorize("hasAuthority('student:write')")
    public boolean upload(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        String directory = UPLOAD_DIR + id + "/";
        File newFile = new File(directory + file.getOriginalFilename());
        if (!newFile.exists())
            newFile.mkdirs();
        file.transferTo(newFile);
        return true;
    }

    @GetMapping("{id}/download/{fileName}")
    public ResponseEntity<byte[]> download(
            @PathVariable("id") Long id,
            @PathVariable("fileName") String fileName
    ) throws IOException {
        String directory = UPLOAD_DIR + id + "/";
        Path path = new File(directory + fileName).toPath();
        byte[] fileData = Files.readAllBytes(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(IMAGE_JPEG);
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}

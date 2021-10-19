package com.megakruk.springsecuritycourse.student;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("management/api/v1/students")
public class StudentManagementController {

    private final StudentService studentService;

    @Autowired
    public StudentManagementController(StudentService studentService) {
        this.studentService = studentService;
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
    public ResponseEntity<Student> addStudent(@RequestBody Student student) {
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
            @RequestBody Student student
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
}

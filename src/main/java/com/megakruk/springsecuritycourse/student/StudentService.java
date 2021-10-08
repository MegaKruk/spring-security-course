package com.megakruk.springsecuritycourse.student;

import com.megakruk.springsecuritycourse.exception.BadRequestException;
import com.megakruk.springsecuritycourse.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepo studentRepo;

    @Autowired
    public StudentService(StudentRepo studentRepo) {
        this.studentRepo = studentRepo;
    }

    public Student addStudent(Student student) {
        Boolean existsEmail = studentRepo.selectExistsByEmail(student.getEmail());
        if(existsEmail)
            throw new BadRequestException("Email " + student.getEmail() + " is taken!");
        return studentRepo.save(student);
    }

    public List<Student> findAllStudents() {
        return studentRepo.findAll();
    }

    @Transactional
    public void updateStudent(Long id, Student student) {
        Student studentToBeUpdated = studentRepo
                .findStudentById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("Student with id " + id + " was not found"));
        studentToBeUpdated.setFirstName(student.getFirstName());
        studentToBeUpdated.setLastName(student.getLastName());
        studentToBeUpdated.setEmail(student.getEmail());
    }

    public Student findStudentById(Long id) {
        return studentRepo.findStudentById(id).orElseThrow(() ->
                new UserNotFoundException("Student with id " + id + " was not found"));
    }

    public void deleteStudent(Long id) {
        boolean exists = studentRepo.existsById(id);
        if(!exists)
            throw new UserNotFoundException("Student with id " + id + " was not found");
        studentRepo.deleteStudentById(id);
    }
}

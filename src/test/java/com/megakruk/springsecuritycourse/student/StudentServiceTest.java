package com.megakruk.springsecuritycourse.student;

import com.megakruk.springsecuritycourse.exception.BadRequestException;
import com.megakruk.springsecuritycourse.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private StudentService underTest;
    @Mock
    private StudentRepo studentRepo;

    @BeforeEach
    void setUp() {
        underTest = new StudentService(studentRepo);
    }

    @Test
    void canFindAllStudents() {
        //when
        underTest.findAllStudents();
        //then
        verify(studentRepo).findAll();
    }

    @Test
    void canAddStudent() {
        //given
        Student student = new Student(
                "Jim",
                "Bauman",
                "jimmy@gmail.com"
        );

        //when
        underTest.addStudent(student);

        //then
        ArgumentCaptor<Student> studentArgumentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepo).save(studentArgumentCaptor.capture());
        Student captorValue = studentArgumentCaptor.getValue();

        assertThat(captorValue).isEqualTo(student);
    }

    @Test
    void shouldThrowWhenAddStudent() {
        //given
        Student student = new Student(
                "Jim",
                "Bauman",
                "jimmy@gmail.com"
        );

        given(studentRepo.selectExistsByEmail(student.getEmail())).willReturn(true);

        //when

        //then
        assertThatThrownBy(() -> underTest.addStudent(student))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("taken");

        verify(studentRepo, never()).save(any());
    }

    @Test
    void canFindStudentById() {
        //given
        long id = 1;
        Student student = new Student(
                1L,
                "Mark",
                "Hamill",
                "mhamill@gmail.com"
        );
        given(studentRepo.findStudentById(id)).willReturn(java.util.Optional.of(student));

        //when
        underTest.findStudentById(id);

        //then
        verify(studentRepo).findStudentById(id);
    }

    @Test
    void ShouldThrowWhenFindStudentById() {
        //given
        long id = 1;

        given(studentRepo.findStudentById(id)).willReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> underTest.findStudentById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
    }

        @Test
    void canUpdateStudent() {
        //given
        long id = 1;
        Student student = new Student(
                id,
                "Jim",
                "Bauman",
                "jimmy@gmail.com"
        );
        given(studentRepo.findStudentById(id)).willReturn(java.util.Optional.of(student));

        //when
        Student updatedStudent = new Student(
                "Mark",
                "Hamill",
                "mhamill@gmail.com"
        );
        underTest.updateStudent(id, updatedStudent);

        //then
        assertThat(studentRepo.findStudentById(id).orElseThrow().getFirstName())
                .isEqualTo(updatedStudent.getFirstName());
        assertThat(studentRepo.findStudentById(id).orElseThrow().getLastName())
                .isEqualTo(updatedStudent.getLastName());
        assertThat(studentRepo.findStudentById(id).orElseThrow().getEmail())
                .isEqualTo(updatedStudent.getEmail());
    }

    @Test
    void canDeleteStudent() {
        //given
        long id = 1;
        given(studentRepo.existsById(id)).willReturn(true);

        //when
        underTest.deleteStudent(id);

        //then
        verify(studentRepo).deleteStudentById(id);
    }

    @Test
    void ShouldThrowWhenDeleteStudent() {
        //given
        long id = 1;
        given(studentRepo.existsById(id)).willReturn(false);

        //when

        //then
        assertThatThrownBy(() -> underTest.deleteStudent(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("not found");
        verify(studentRepo, never()).deleteStudentById(id);
    }
}
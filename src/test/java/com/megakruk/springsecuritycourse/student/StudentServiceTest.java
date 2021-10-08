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
    @Disabled
    void updateStudent() {
    }

    @Test
    @Disabled
    void findStudentById() {
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
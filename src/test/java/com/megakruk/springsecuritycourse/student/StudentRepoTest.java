package com.megakruk.springsecuritycourse.student;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class StudentRepoTest {

    @Autowired
    private StudentRepo underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void itShouldCheckStudentExistsByEmail() {
        //given
        String email = "jimmy@gmail.com";
        Student student = new Student(
                "Jim",
                "Bauman",
                email
        );
        underTest.save(student);

        //when
        boolean exists = underTest.selectExistsByEmail(email);

        //then
        assertThat(exists).isTrue();
    }

    @Test
    void itShouldCheckStudentNotExistsByEmail() {
        //given
        String email = "jimmy@gmail.com";

        //when
        boolean exists = underTest.selectExistsByEmail(email);

        //then
        assertThat(exists).isFalse();
    }
}
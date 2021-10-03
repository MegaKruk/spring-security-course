package com.megakruk.springsecuritycourse.auth;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.megakruk.springsecuritycourse.security.ApplicationUserRole.*;

@Repository("fake")
public class FakeApplicationUserDaoService implements ApplicationUserDao {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public FakeApplicationUserDaoService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(String username) {
        return getApplicationUsers()
                .stream()
                .filter(applicationUser -> username.equals(applicationUser.getUsername()))
                .findFirst();
    }

    // TODO: put this in a db
    private List<ApplicationUser> getApplicationUsers() {
        List<ApplicationUser> applicationUsers = Lists.newArrayList(
            new ApplicationUser(
                    STUDENT.getGrantedAuthorities(),
                    passwordEncoder.encode("password"),
                    "mark",
                    true,
                    true,
                    true,
                    true
            ),
            new ApplicationUser(
                    ADMIN.getGrantedAuthorities(),
                    passwordEncoder.encode("password2"),
                    "dom",
                    true,
                    true,
                    true,
                    true
            ),
            new ApplicationUser(
                    ADMININTERN.getGrantedAuthorities(),
                    passwordEncoder.encode("password3"),
                    "david",
                    true,
                    true,
                    true,
                    true
            )
        );
        return applicationUsers;
    }
}

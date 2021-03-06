package com.megakruk.springsecuritycourse.security;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.megakruk.springsecuritycourse.security.ApplicationUserPermission.*;

@Getter
@AllArgsConstructor
public enum ApplicationUserRole {
    STUDENT(Sets.newHashSet(COURSE_READ)),
    ADMIN(Sets.newHashSet(COURSE_READ, COURSE_WRITE, STUDENT_READ, STUDENT_WRITE)),
    ADMININTERN(Sets.newHashSet(COURSE_READ, STUDENT_READ));

    private final Set<ApplicationUserPermission> permissions;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}

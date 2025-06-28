package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class RoleAuditingTest {

    @Autowired
    private RoleRepository roleRepository;

    @Mock
    private SecurityContext securityContext;

    @PersistenceContext
    private EntityManager entityManager;
    @Mock
    private Authentication authentication;

    @BeforeEach
    public void init() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    public void testCreatedByAuditing() {
        when(authentication.getName()).thenReturn("admin");
        UserDetails userDetails = new User("admin", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Role role = Role.builder()
                .name("ROLE_USER")
                .build();

        Role actualResult = roleRepository.save(role);

        assertEquals("admin", actualResult.getCreatedBy());
        assertNotNull(actualResult.getCreatedAt());
        assertEquals("admin", actualResult.getUpdatedBy());
        assertNotNull(actualResult.getUpdatedAt());
    }

    @Test
    public void testModifiedByAuditing() {
        when(authentication.getName()).thenReturn("admin");
        UserDetails adminUser = new User("admin", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(adminUser);

        // create a role
        Role role = Role.builder()
                .name("ROLE_TEACHER")
                .build();

        Role savedRole = roleRepository.saveAndFlush(role);

        entityManager.clear();

        when(authentication.getName()).thenReturn("modifier");
        UserDetails modifierUser = new User("modifier", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(modifierUser);

        Role roleToUpdate = roleRepository.findById(savedRole.getId()).orElseThrow();
        roleToUpdate.setName("ROLE_INSTRUCTOR");

        Role actualResult = roleRepository.saveAndFlush(roleToUpdate);

        assertEquals("admin", actualResult.getCreatedBy());
        assertEquals("modifier", actualResult.getUpdatedBy());
        assertNotNull(actualResult.getCreatedAt());
        assertNotNull(actualResult.getUpdatedAt());
    }


    @Test
    public void testAuditingWithUnauthenticatedUser() {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(null);

        Role role = Role.builder()
                .name("ROLE_GUEST")
                .build();

        Role actualResult = roleRepository.save(role);

        assertNotNull(actualResult.getCreatedBy());
        assertNotNull(actualResult.getCreatedAt());
        assertNotNull(actualResult.getUpdatedAt());
        assertNotNull(actualResult.getUpdatedAt());
    }

    @Test
    public void testAuditingWithNullAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        Role role = Role.builder()
                .name("ROLE_ANONYMOUS")
                .build();

        Role actualResult = roleRepository.save(role);

        assertNotNull(actualResult.getCreatedBy());
        assertNotNull(actualResult.getCreatedAt());
        assertNotNull(actualResult.getUpdatedAt());
        assertNotNull(actualResult.getUpdatedAt());
    }

    @Test
    public void testAuditingWithStringPrincipal() {
        when(authentication.getName()).thenReturn("stringUser");
        when(authentication.getPrincipal()).thenReturn("stringUser");
        when(authentication.isAuthenticated()).thenReturn(true);

        Role role = Role.builder()
                .name("ROLE_STRING_USER")
                .build();

        Role actualResult = roleRepository.save(role);

        assertEquals("stringUser", actualResult.getCreatedBy());
        assertNotNull(actualResult.getCreatedAt());
    }


    @Test
    public void testDeleteTriggersJpaAuditLogListener() {
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getPrincipal()).thenReturn(new User("admin", "password", Collections.emptyList()));

        Role role = Role.builder()
                .name("ROLE_TO_DELETE")
                .build();
        Role savedRole = roleRepository.saveAndFlush(role);

        roleRepository.deleteById(savedRole.getId());
    }


    @Test
    public void testAuditTimestampsOrder() {
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getPrincipal()).thenReturn(new User("admin", "password", Collections.emptyList()));

        Role role = Role.builder()
                .name("ROLE_TIME_ORDER")
                .build();

        Role savedRole = roleRepository.saveAndFlush(role);
        entityManager.clear();

        when(authentication.getName()).thenReturn("modifier");
        when(authentication.getPrincipal()).thenReturn(new User("modifier", "password", Collections.emptyList()));

        Role toUpdate = roleRepository.findById(savedRole.getId()).orElseThrow();
        toUpdate.setName("ROLE_TIME_ORDER_MODIFIED");

        Role actualResult = roleRepository.saveAndFlush(toUpdate);

        assertNotNull(actualResult.getCreatedAt());
        assertNotNull(actualResult.getUpdatedAt());
        assertTrue(actualResult.getUpdatedAt().isAfter(actualResult.getCreatedAt()) || actualResult.getUpdatedAt().equals(actualResult.getCreatedAt()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob", "user123", "", "system", "🤖"})
    public void testAuditingWithVariousUsernames(String username) {
        when(authentication.getName()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(username);
        when(authentication.isAuthenticated()).thenReturn(true);

        Role role = Role.builder()
                .name("ROLE_" + username)
                .build();

        Role actualResult = roleRepository.save(role);

        assertEquals(username, actualResult.getCreatedBy());
    }

}
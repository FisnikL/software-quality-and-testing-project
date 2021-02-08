package com.finki.bnks.project.server.repository;

import com.finki.bnks.project.server.domain.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class AppUserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    public void shouldReturnAppUserWithUsernameEqualsTo_username() {
        // given
        AppUser appUser = new AppUser("username", "passwordHash", "secret", true, false);
        entityManager.persist(appUser);
        entityManager.flush();

        // when
        AppUser found = appUserRepository.findByUsername("username").orElse(null);

        // then
        assertThat(found.getUsername()).isEqualTo(appUser.getUsername());
    }

    @Test
    public void shouldReturnNull_UsernameDoesntExist() {
        // given

        // when
        AppUser found = appUserRepository.findByUsername("username").orElse(null);

        // then
        assertThat(found).isEqualTo(null);
    }
}

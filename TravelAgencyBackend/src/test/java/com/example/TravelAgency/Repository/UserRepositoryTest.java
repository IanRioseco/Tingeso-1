package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_returnsUser() {
        em.persist(UserEntity.builder()
                .fullName("U")
                .email("u@u.com")
                .documentId("DOC-1")
                .nationality("CL")
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build());
        em.flush();

        assertThat(userRepository.findByEmail("u@u.com")).isPresent();
    }
}


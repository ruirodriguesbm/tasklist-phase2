import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void saveAndFindById_ShouldReturnSavedUser() {
        User user = new User("jane", "password");
        int id = repository.save(user);

        User savedUser = repository.findById(id);

        assertNotNull(savedUser);
        assertEquals(user, savedUser);
    }

    @Test
    void findById_ShouldReturnNull_WhenUserDoesNotExist() {
        User result = repository.findById(999);

        assertNull(result);
    }

    @Test
    void save_ShouldGenerateDifferentIds_ForDifferentUsers() {
        User user1 = new User("alice", "pass1");
        User user2 = new User("bob", "pass2");

        int id1 = repository.save(user1);
        int id2 = repository.save(user2);

        assertNotEquals(id1, id2);
    }

    @Test
    void save_ShouldStoreMultipleUsersCorrectly() {
        User user1 = new User("alice", "pass1");
        User user2 = new User("bob", "pass2");

        int id1 = repository.save(user1);
        int id2 = repository.save(user2);

        assertEquals(user1, repository.findById(id1));
        assertEquals(user2, repository.findById(id2));
    }

    @Test
    void save_ShouldNotModifyUserObject() {
        User user = new User("john", "secret");

        repository.save(user);

        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
    }
}

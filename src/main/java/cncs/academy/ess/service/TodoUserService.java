package cncs.academy.ess.service;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import cncs.academy.ess.service.JwtService;

public class TodoUserService {
    private final UserRepository repository;

    JwtService jwtService = new JwtService();

    public TodoUserService(UserRepository userRepository) {
        this.repository = userRepository;
    }
    public User addUser(String username, String password) throws NoSuchAlgorithmException {
        User user = new User(username, password);
        int id = repository.save(user);
        user.setId(id);
        return user;
    }
    public User getUser(int id) {
        return repository.findById(id);
    }

    public void deleteUser(int id) {
        repository.deleteById(id);
    }

    public String login(String username, String password) throws NoSuchAlgorithmException {
        User user = repository.findByUsername(username);
        if (user == null) return null;

        boolean ok = PasswordHasher.verifyPBKDF2(
                password,
                user.getPasswordHash(),
                user.getPasswordSalt()
        );

        if (ok) {
            return jwtService.createAuthToken(user);
        }
        return null;
    }

//    private String createAuthToken(User user) {
//        return "Bearer " + user.getUsername();
//    }


}

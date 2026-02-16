package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;

import java.sql.*;
import java.util.List;

import cncs.academy.ess.service.PasswordHasher;
import org.apache.commons.dbcp2.BasicDataSource;

import java.util.ArrayList;

public class SQLUserRepository implements UserRepository {
    private final BasicDataSource dataSource;

    public SQLUserRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User findById(int userId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by ID", e);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users");
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    @Override
    public int save(User user) {
        try (Connection connection = dataSource.getConnection()) {

            byte[] salt = PasswordHasher.generateSalt();
            byte[] hash = PasswordHasher.hashPassword(
                    user.getPassword().toCharArray(),
                    salt
            );

            String saltHex = PasswordHasher.toHex(salt);
            String hashHex = PasswordHasher.toHex(hash);

            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (username, password_hash, password_salt) " +
                            "VALUES (?, ?, ?) ON CONFLICT (username) DO NOTHING",
                    Statement.RETURN_GENERATED_KEYS
            );

            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashHex);
            stmt.setString(3, saltHex);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public void deleteById(int userId) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public User findByUsername(String username) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT id, username, password_hash, password_salt FROM users WHERE username = ?"
            );
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("password_salt")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String pwd = rs.getString("password");
        return new User(username, pwd);
    }
}
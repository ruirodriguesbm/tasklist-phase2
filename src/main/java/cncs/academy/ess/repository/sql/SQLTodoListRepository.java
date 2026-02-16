package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLTodoListRepository implements TodoListsRepository {

    private final BasicDataSource dataSource;

    public SQLTodoListRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TodoList findById(int listId) {
        final String sql = "SELECT id, name, owner_id FROM lists WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, listId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTodoList(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo list by ID", e);
        }
    }

    @Override
    public List<TodoList> findAll() {
        final String sql = "SELECT id, name, owner_id FROM lists ORDER BY id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<TodoList> lists = new ArrayList<>();
            while (rs.next()) {
                lists.add(mapResultSetToTodoList(rs));
            }
            return lists;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all todo lists", e);
        }
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        final String sql = "SELECT id, name, owner_id FROM lists WHERE owner_id = ? ORDER BY id";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<TodoList> lists = new ArrayList<>();
                while (rs.next()) {
                    lists.add(mapResultSetToTodoList(rs));
                }
                return lists;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo lists by user ID", e);
        }
    }

    @Override
    public int save(TodoList todoList) {
        final String sql = "INSERT INTO lists (name, owner_id) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, todoList.getName()); // ajusta se for getName()
            stmt.setInt(2, todoList.getOwnerId());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save todo list", e);
        }
    }

    @Override
    public void update(TodoList todoList) {
        final String sql = "UPDATE lists SET name = ?, owner_id = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, todoList.getName()); // ajusta se for getName()
            stmt.setInt(2, todoList.getOwnerId());
            stmt.setInt(3, todoList.getListId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("TodoList not found for update (id=" + todoList.getListId() + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update todo list", e);
        }
    }

    @Override
    public boolean deleteById(int listId) {
        final String sql = "DELETE FROM lists WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, listId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete todo list", e);
        }
    }

    private TodoList mapResultSetToTodoList(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int ownerId = rs.getInt("owner_id");

        // Ajusta ao teu construtor real
        TodoList list = new TodoList(name, ownerId);
        list.setId(id);

        return list;
    }
}

package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoRepository;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLToDoRepository implements TodoRepository {

    private final BasicDataSource dataSource;

    public SQLToDoRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Todo findById(int todoId) {
        final String sql = """
            SELECT id, title, completed, list_id
            FROM todos
            WHERE id = ?
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, todoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTodo(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo by ID", e);
        }
    }

    @Override
    public List<Todo> findAll() {
        final String sql = """
            SELECT id, title, completed, list_id
            FROM todos
            ORDER BY id
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Todo> todos = new ArrayList<>();
            while (rs.next()) {
                todos.add(mapResultSetToTodo(rs));
            }
            return todos;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all todos", e);
        }
    }

    @Override
    public List<Todo> findAllByListId(int listId) {
        final String sql = """
            SELECT id, title, completed, list_id
            FROM todos
            WHERE list_id = ?
            ORDER BY id
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, listId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Todo> todos = new ArrayList<>();
                while (rs.next()) {
                    todos.add(mapResultSetToTodo(rs));
                }
                return todos;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todos by list ID", e);
        }
    }

    @Override
    public int save(Todo todo) {
        final String sql = """
            INSERT INTO todos (title, completed, list_id)
            VALUES (?, ?, ?)
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, todo.getDescription());
            stmt.setBoolean(2, todo.isCompleted());
            stmt.setInt(3, todo.getListId());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save todo", e);
        }
    }

    @Override
    public void update(Todo todo) {
        final String sql = """
            UPDATE todos
            SET title = ?, completed = ?, list_id = ?
            WHERE id = ?
        """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, todo.getDescription());
            stmt.setBoolean(2, todo.isCompleted());
            stmt.setInt(3, todo.getListId());
            stmt.setInt(4, todo.getId());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException(
                        "Todo not found for update (id=" + todo.getId() + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update todo", e);
        }
    }

    @Override
    public boolean deleteById(int todoId) {
        final String sql = "DELETE FROM todos WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, todoId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete todo", e);
        }
    }

    private Todo mapResultSetToTodo(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        boolean completed = rs.getBoolean("completed");
        int listId = rs.getInt("list_id");

        Todo todo = new Todo(id,title, completed, listId);
        todo.setId(id);

        return todo;
    }
}

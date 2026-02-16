package cncs.academy.ess;

import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.memory.InMemoryTodoRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import cncs.academy.ess.repository.sql.SQLUserRepository;
import cncs.academy.ess.repository.sql.SQLToDoRepository;
import cncs.academy.ess.repository.sql.SQLTodoListRepository;
import cncs.academy.ess.service.*;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import org.apache.commons.dbcp2.BasicDataSource;
import org.casbin.jcasbin.main.Enforcer;

import java.security.NoSuchAlgorithmException;

public class App {
    public static void main(String[] args) throws NoSuchAlgorithmException {

        SslPlugin plugin = new SslPlugin(conf -> {
            conf.pemFromPath("./cert.pem", "./key.pem" ,"password");
        });

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(plugin);
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start();

        // Initialize routes for user management
        //InMemoryUserRepository userRepository = new InMemoryUserRepository();
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        String connectURI = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", "localhost", "5432", "postgres", "postgres", "grupo1_ess");
        ds.setUrl(connectURI);

        SQLUserRepository userRepository = new SQLUserRepository(ds);
        SQLTodoListRepository listRepository = new SQLTodoListRepository(ds);
        SQLToDoRepository todoRepository = new SQLToDoRepository(ds);

        TodoUserService userService = new TodoUserService(userRepository);
        TodoListsService todolistsService = new TodoListsService(listRepository);
        TodoService todoService = new TodoService(todoRepository, listRepository);
        UserController userController = new UserController(userService);

        TodoListController todoListController = new TodoListController(todolistsService);

        /*InMemoryTodoListsRepository listsRepository = new InMemoryTodoListsRepository();
        TodoListsService toDoListService = new TodoListsService(listsRepository);


        InMemoryTodoRepository todoRepository = new InMemoryTodoRepository();
        TodoService todoService = new TodoService(todoRepository, listsRepository);*/
        TodoController todoController = new TodoController(todoService, todolistsService);

        Enforcer enforcer = CasbinLoader.loadFromResources();
        JwtService jwtService = new JwtService();
        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(enforcer, jwtService);


        // CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
        // Authorization middleware
        app.before(authMiddleware::handle);

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);

        // "To do" lists management
        /* POST /todolist
          {
              "listName": "Shopping list"
          }
         */
        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);
        app.post("/todolist/{listId}/share", todoListController::shareTodoList);


        // "To do" list items management
        /* POST /todo/item
          {
              "description": "Buy milk",
              "listId": 1
          }
         */
        app.post("/todo/item", todoController::createTodoItem);
        /* GET /todo/1/tasks */
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        /* GET /todo/1/tasks/1 */
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        /* DELETE /todo/1/tasks/1 */
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);

        fillDummyData(userService, todolistsService, todoService);
    }

    private static void fillDummyData(
            TodoUserService userService,
            TodoListsService toDoListService,
            TodoService todoService) throws NoSuchAlgorithmException {
        userService.addUser("user1", "password1");
        userService.addUser("user2", "password2");
        toDoListService.createTodoListItem("Shopping list", 1);
        toDoListService.createTodoListItem( "Other", 1);
        todoService.createTodoItem("Bread", 1);
        todoService.createTodoItem("Milk", 1);
        todoService.createTodoItem("Eggs", 1);
        todoService.createTodoItem("Cheese", 1);
        todoService.createTodoItem("Butter", 1);
    }
}

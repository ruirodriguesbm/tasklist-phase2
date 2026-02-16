package cncs.academy.ess.model;

public class User {
    private int id;
    private String username;

    // Apenas para input (register/login). Não vem da BD.
    private String password;

    // Vêm da BD
    private String passwordHash;
    private String passwordSalt;

    // Para login/register (input)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Para leitura da BD (sem password em texto)
    public User(int id, String username, String passwordHash, String passwordSalt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }

    // Só usado no input
    public String getPassword() { return password; }

    public String getPasswordHash() { return passwordHash; }
    public String getPasswordSalt() { return passwordSalt; }

    public void setId(int id) { this.id = id; }
}

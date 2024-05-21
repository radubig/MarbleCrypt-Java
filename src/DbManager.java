import java.sql.*;
import java.util.Date;

public class DbManager {
    private static DbManager dbManager;
    private Connection con;
    private IAuditService auditService;

    private PreparedStatement sqlGetAmount,
                              sqlSetAmount,
                              sqlGetPrice,
                              sqlSetPrice,
                              sqlUpdateMarble,
                              sqlCreateMarble,
                              sqlDeleteMarble;

    private DbManager() {
        // Initialize audit service
        auditService = new AuditService();
        auditService.OpenAuditFile("db_audit.csv");

        // Initialize database connection
        String host = "mysql-pao-pao-db.k.aivencloud.com";
        String port = "26240";
        String databaseName = "defaultdb";
        String userName = "avnadmin";
        String password = ""; // Set password here

        try {
            if (password.isEmpty())
                throw new RuntimeException("Database password is not set!");
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
            sqlGetAmount = con.prepareStatement("SELECT amount FROM wallet WHERE id = 1");
            sqlSetAmount = con.prepareStatement("UPDATE wallet SET amount = ? WHERE id = 1");
            sqlGetPrice = con.prepareStatement("SELECT price FROM wallet WHERE id = 1");
            sqlSetPrice = con.prepareStatement("UPDATE wallet SET price = ? WHERE id = 1");
            sqlUpdateMarble = con.prepareStatement("UPDATE marbles SET timestamp = ? WHERE id = ?");
            sqlCreateMarble = con.prepareStatement("INSERT INTO marbles (name, daily_yield, timestamp, rarity, texture1, texture2) VALUES (?, ?, ?, ?, ?, ?)",
                                                    Statement.RETURN_GENERATED_KEYS);
            sqlDeleteMarble = con.prepareStatement("DELETE FROM marbles WHERE id = ?");

            auditService.WriteAudit("Database connection established", new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found");
        }
    }

    public static synchronized DbManager getInstance() {
        if (dbManager == null) {
            dbManager = new DbManager();
        }
        return dbManager;
    }

    public void close() {
        try {
            if(con != null)
                con.close();
            auditService.WriteAudit("Database connection closed", new Date());
            auditService.CloseAuditFile();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet GetMarbleData() {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT rarity, name, texture_slot FROM marble_data");
            auditService.WriteAudit("Read * from marble_data", new Date());
            return rs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String GetAmount() {
        try {
            ResultSet rs = sqlGetAmount.executeQuery();
            rs.next();
            auditService.WriteAudit("Read amount from wallet", new Date());
            return rs.getString("amount");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void SetAmount(String amount) {
        try {
            sqlSetAmount.setString(1, amount);
            sqlSetAmount.executeUpdate();
            auditService.WriteAudit("Update amount from wallet", new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String GetPrice() {
        try {
            ResultSet rs = sqlGetPrice.executeQuery();
            rs.next();
            auditService.WriteAudit("Read price from wallet", new Date());
            return rs.getString("price");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void SetPrice(String price) {
        try {
            sqlSetPrice.setString(1, price);
            sqlSetPrice.executeUpdate();
            auditService.WriteAudit("Update price from wallet", new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet GetMarbles() {
        try {
            Statement stmt = con.createStatement();
            auditService.WriteAudit("Read all marbles", new Date());
            return stmt.executeQuery("SELECT * FROM marbles");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DeleteAllMarbles() {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM marbles");
            auditService.WriteAudit("Delete all marbles", new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int CreateMarble(String name, long daily_yield, long timestamp, String rarity, int texture1, int texture2) {
        try {
            sqlCreateMarble.setString(1, name);
            sqlCreateMarble.setLong(2, daily_yield);
            sqlCreateMarble.setLong(3, timestamp);
            sqlCreateMarble.setString(4, rarity);
            sqlCreateMarble.setInt(5, texture1);
            sqlCreateMarble.setInt(6, texture2);
            sqlCreateMarble.executeUpdate();

            ResultSet rs = sqlCreateMarble.getGeneratedKeys();
            rs.next();
            auditService.WriteAudit("Insert new marble with name " + name, new Date());
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void UpdateMarble(int id, long timestamp) {
        try {
            sqlUpdateMarble.setLong(1, timestamp);
            sqlUpdateMarble.setInt(2, id);
            sqlUpdateMarble.executeUpdate();
            auditService.WriteAudit("Update timestamp on marble " + id, new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DeleteMarble(int id) {
        try {
            sqlDeleteMarble.setInt(1, id);
            sqlDeleteMarble.executeUpdate();
            auditService.WriteAudit("Delete marble " + id, new Date());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

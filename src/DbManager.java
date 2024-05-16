import java.sql.*;

public class DbManager {
    private static DbManager dbManager;
    private Connection con;

    private PreparedStatement sqlGetAmount,
                              sqlSetAmount,
                              sqlGetPrice,
                              sqlSetPrice,
                              sqlUpdateMarble,
                              sqlCreateMarble,
                              sqlDeleteMarble;

    private DbManager() {
        // Initialize database connection
        String host = "mysql-pao-pao-db.k.aivencloud.com";
        String port = "26240";
        String databaseName = "defaultdb";
        String userName = "avnadmin";
        String password = "AVNS_7MkmMjz9ZDlCmNC5eMH";

        try {
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
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet GetMarbleData() {
        try {
            Statement stmt = con.createStatement();
            return stmt.executeQuery("SELECT rarity, name, texture_slot FROM marble_data");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String GetAmount() {
        try {
            ResultSet rs = sqlGetAmount.executeQuery();
            rs.next();
            return rs.getString("amount");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void SetAmount(String amount) {
        try {
            sqlSetAmount.setString(1, amount);
            sqlSetAmount.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String GetPrice() {
        try {
            ResultSet rs = sqlGetPrice.executeQuery();
            rs.next();
            return rs.getString("price");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void SetPrice(String price) {
        try {
            sqlSetPrice.setString(1, price);
            sqlSetPrice.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet GetMarbles() {
        try {
            Statement stmt = con.createStatement();
            return stmt.executeQuery("SELECT * FROM marbles");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DeleteAllMarbles() {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("DELETE FROM marbles");
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DeleteMarble(int id) {
        try {
            sqlDeleteMarble.setInt(1, id);
            sqlDeleteMarble.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

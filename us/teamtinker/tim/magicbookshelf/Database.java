package us.teamtinker.tim.magicbookshelf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tim Clancy
 */
public class Database {

    private final String connectionURL;
    private final String username;
    private final String password;
    private Connection connection;
    private Statement statement;

    public Database(String connectionURL, String username, String password) {
        this.connectionURL = connectionURL;
        this.username = username;
        this.password = password;
    }

    public ResultSet query(String query) {
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(connectionURL, username, password);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultSet;
    }

    public int update(String query) {
        int result = 0;
        try {
            connection = DriverManager.getConnection(connectionURL, username, password);
            statement = connection.createStatement();
            result = statement.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void close() {
        try {
            connection.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
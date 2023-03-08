package myjdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectorJDBC {

    public static Connection createConnection(String url, String user, String pass){
        Connection connection;

        try {
            System.out.println("Регистрация JDBC драйвера...");
            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Подключение к БД...");
            connection = DriverManager.getConnection(url, user, pass);

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return connection;
    }
}

import myjdbc.ConnectorJDBC;

import java.sql.*;

public class General {

    public static final String URL = "jdbc:mysql://localhost:3306/shop";
    public static final String USER = "root";
    public static final String PASSWORD = "12345678";

    public static void main(String[] args) {
        startApp();
    }

    public static void startApp() {
        Connection connection = ConnectorJDBC.createConnection(URL, USER, PASSWORD);
        Statement statement;
        String createTable = "CREATE TABLE IF NOT EXISTS products (\n" +
                "    id_product INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    name_product CHAR(50) NOT NULL UNIQUE,\n" +
                "    country CHAR(50) NOT NULL UNIQUE,\n" +
                "    count_now INT NOT NULL,\n" +
                "    price DECIMAL(5, 2) NOT NULL\n" +
                ");";

        try {
            statement = connection.createStatement();
            statement.execute(createTable);

            if (!isRows()) {
                boolean isAdd = addProductInTable();
                if (isAdd) {
                    System.out.println("Таблица товаров создана...");
                    System.out.println("Данные загружены...");
                } else {
                    System.out.println("Данные не загружены!");
                }
            } else  {
                System.out.println("Таблица товаров уже существует!");
            }

            statement.close();
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addProductInTable() {
        String[] name = {"Яблоко", "Груша", "Апельсин", "Ананас", "Черника"};
        String[] country = {"Польша", "Беларусь", "Катар", "Турция", "Ураина"};
        int[] count = {100, 50, 70, 200, 10};
        double[] price = {5, 10, 15, 5, 20};

        Connection connection = ConnectorJDBC.createConnection(URL, USER, PASSWORD);
        PreparedStatement preparedStatement;
        String addProduct = "INSERT products(name_product, country, count_now, price)\n" +
                "VALUES (?, ?, ?, ?);";

        int rows;

        try {
            preparedStatement = connection.prepareStatement(addProduct);
            for (int i = 0; i < 5; i++) {
                preparedStatement.setString(1, name[i]);
                preparedStatement.setString(2, country[i]);
                preparedStatement.setInt(3, count[i]);
                preparedStatement.setDouble(4, price[i]);
                preparedStatement.addBatch();
            }
            rows = preparedStatement.executeBatch().length;

            preparedStatement.close();
            connection.close();

            return rows != 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean isRows() {
        Connection connection = ConnectorJDBC.createConnection(URL, USER, PASSWORD);
        Statement statement;
        ResultSet resultSet;
        String checkRows = "SELECT * FROM products;";

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(checkRows);
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

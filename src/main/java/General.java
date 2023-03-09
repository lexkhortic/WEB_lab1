import myjdbc.ConnectorJDBC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class General {

    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static final String URL = "jdbc:mysql://localhost:3306/shop";
    public static final String USER = "root";
    public static final String PASSWORD = "12345678";
    public static Connection connection = ConnectorJDBC.createConnection(URL, USER, PASSWORD);
    private static String fioClient = "";

    public static void main(String[] args) throws IOException {
        startApp();
        System.out.print("Введите логин для входа: ");
        String loginInput = reader.readLine();
        if (checkClient(loginInput)) {
            System.out.println("Добро пожаловать " + fioClient);
            long idProduct = showAndSelectProduct();
            executeTransaction(idProduct);
        } else {
            System.out.println("Пользователя с таким логином не существует!!!");
        }
    }

    public static void startApp() {
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addProductInTable() {
        String[] name = {"Яблоко", "Груша", "Апельсин", "Ананас", "Черника"};
        String[] country = {"Польша", "Беларусь", "Катар", "Турция", "Ураина"};
        int[] count = {100, 50, 70, 200, 10};
        double[] price = {5, 10, 15, 5, 20};

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

            return rows != 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isRows() {
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

    public static boolean checkClient(String login) {
        PreparedStatement preparedStatement;
        ResultSet clients;
        String request = "SELECT * FROM clients WHERE login = ?;";

        try {
            preparedStatement = connection.prepareStatement(request);
            preparedStatement.setString(1, login);
            clients = preparedStatement.executeQuery();

            if (clients.next()) {
                fioClient = clients.getString("fio");
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static long showAndSelectProduct() {
        Statement statement;
        ResultSet products;
        String findAllProducts = "SELECT * FROM products;";

        try {
            statement = connection.createStatement();
            products = statement.executeQuery(findAllProducts);
            System.out.println("\nСписок товаров:");
            while (products.next()) {
                System.out.println("№" + products.getLong("id_product") +
                        " - " + products.getString("name_product"));
            }

            products.close();
            statement.close();

            System.out.print("Ваш выбор №: ");
            return Long.parseLong(reader.readLine());

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeTransaction(long idProduct) {

        try {
            connection.setAutoCommit(false);
            reduceCountProduct(idProduct);
            reduceCash(idProduct);
            insertInStories(fioClient, idProduct);
            connection.commit();
            System.out.println("Транзакция выполнена...");
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void reduceCountProduct(long idProduct) {
        PreparedStatement preparedStatement;
        String reduceCount = "UPDATE products\n" +
                "SET count_now = count_now - 1\n" +
                "WHERE id_product = ?;";

        try {
            preparedStatement = connection.prepareStatement(reduceCount, new String[]{"name_product"});
            preparedStatement.setLong(1, idProduct);
            preparedStatement.executeUpdate();

            System.out.println("Кол-во продукта уменьшилось...");

            preparedStatement.close();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reduceCash(long idProduct) {
        PreparedStatement preparedStatement;
        String reduceCash = "UPDATE clients\n" +
                "SET sum_money = sum_money - (SELECT price FROM products WHERE id_product = ?)\n" +
                "WHERE fio = ?;";

        try {
            preparedStatement = connection.prepareStatement(reduceCash);
            preparedStatement.setLong(1, idProduct);
            preparedStatement.setString(2, fioClient);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            System.out.println("Кол-во денег уменьшилось...");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertInStories(String client, long idProduct) {
        PreparedStatement preparedStatement;
        String insert = "INSERT INTO stories(fio, product)\n" +
                "VALUES (?, (SELECT name_product FROM products WHERE id_product = ?));";

        try {
            preparedStatement = connection.prepareStatement(insert);
            preparedStatement.setString(1, client);
            preparedStatement.setLong(2, idProduct);
            preparedStatement.executeUpdate();
            System.out.println("История обновлена...");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}

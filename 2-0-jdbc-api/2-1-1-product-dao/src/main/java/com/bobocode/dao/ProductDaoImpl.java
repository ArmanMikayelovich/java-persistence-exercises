package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDaoImpl implements ProductDao {

    private final DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products (name, producer, price, expiration_date, creation_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getProducer());
            statement.setBigDecimal(3, product.getPrice());
            statement.setTimestamp(4, Timestamp.valueOf(product.getExpirationDate().atStartOfDay()));
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                } else {
                    throw new DaoOperationException("Failed to obtain product ID.");
                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error saving product: " + product);
        }
    }


    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapRowToProduct(resultSet));
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products", e);
        }
        return products;
    }

    @Override
    public Product findOne(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        Product product = null;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    product = mapRowToProduct(resultSet);
                } else {
                    throw new DaoOperationException("");

                }
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding product by ID", e);
        }
        return product;
    }

    @Override
    public void update(Product product) {
        if (product.getId() == null) {
            throw new DaoOperationException("");
        }
        String sql = "UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ?, creation_time = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getProducer());
            statement.setBigDecimal(3, product.getPrice());
            statement.setTimestamp(4, Timestamp.valueOf(product.getExpirationDate().atStartOfDay()));
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(6, product.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoOperationException("Error updating product", e);
        }
    }

    @Override
    public void remove(Product product) {
        if (product.getId() == null) {
            throw new DaoOperationException("");
        }

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoOperationException("Error removing product", e);
        }
    }

    private Product mapRowToProduct(ResultSet resultSet) throws SQLException {
        return Product.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .producer(resultSet.getString("producer"))
                .price(resultSet.getBigDecimal("price"))
                .expirationDate(resultSet.getTimestamp("expiration_date").toLocalDateTime().toLocalDate())
                .creationTime(resultSet.getTimestamp("creation_time").toLocalDateTime())
                .build();
    }
}

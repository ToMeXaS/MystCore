package lt.tomexas.mystcore;

import lt.tomexas.mystcore.playerfontimage.impl.MinotarSource;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Database {

    private static Connection connection;

    public Database(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        try(Statement statement = connection.createStatement();) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    playerHead TEXT NOT NULL
                )
            """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addPlayer(Player player) {
        if (playerExists(player)) updatePlayer(player);
        else {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, playerHead) VALUES (?,?)")) {
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2,
                        Main.getInstance().getPlayerFontImage().getHeadAsString(player.getUniqueId(), true, new MinotarSource(true))
                );
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean playerExists(Player player) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")){
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updatePlayer(Player player) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET playerHead = ? WHERE uuid = ?")){
            preparedStatement.setString(1,
                    Main.getInstance().getPlayerFontImage().getHeadAsString(player.getUniqueId(), true, new MinotarSource(true))
            );
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerHead(Player player) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT playerHead FROM players WHERE uuid = ?")){
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return resultSet.getString("playerHead");
            else return "";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Map<UUID, String> getAllPlayerHeads() {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid, playerHead FROM players")) {
            Map<UUID, String> allHeads = new HashMap<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String playerHead = resultSet.getString("playerHead");
                allHeads.put(uuid, playerHead);
            }
            return allHeads;
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Failed to retrieve all player heads! " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}

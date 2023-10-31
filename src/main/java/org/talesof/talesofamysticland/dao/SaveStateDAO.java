package org.talesof.talesofamysticland.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.talesof.talesofamysticland.database.ConnectionManager;
import org.talesof.talesofamysticland.model.SaveState;

public class SaveStateDAO {

    public SaveState create(SaveState saveState) throws SQLException {
        String sql = """
            INSERT INTO Save_state(save_id, character_state_id, save_point_id, date)
            VALUES (?, ?, ?, ?);
        """;
       
        try (
            Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection
                .prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ) {
           
            statement.setInt(1, saveState.getSaveId());
            statement.setInt(2, saveState.getCharacterStateId());
            statement.setInt(3, saveState.getSavePointId());
            statement.setObject(4, saveState.getDate());

            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();

            if(rs.next()) {
                saveState.setId(rs.getInt(1));
            }

            rs.close();

            return saveState;
        }
       
    }

    public SaveState findById(Integer id) {
        String sql = "SELECT * FROM Save_state WHERE id = ?;";

        try (
            Connection connection = ConnectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return resultSetToSaveState(rs);
            }

            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public List<SaveState> findAll() {
        String sql = "SELECT * FROM Save_state;";
        List<SaveState> saveState = new ArrayList<>();

        try (
            Connection connection = ConnectionManager.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
        ) {
            while(rs.next()) {
                saveState.add(resultSetToSaveState(rs));
            }

            return saveState;
       
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private SaveState resultSetToSaveState(ResultSet rs) throws SQLException {
        return new SaveState(
            rs.getInt("id"),
            rs.getInt("save_id"),
            rs.getInt("character_state_id"),
            rs.getInt("save_point_id"),
            (LocalDateTime) rs.getObject("date")
        );
    }
}
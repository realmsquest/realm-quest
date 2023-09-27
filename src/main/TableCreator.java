package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class TableCreator {

    public static void main(String[] args) throws SQLException{
        
        String url = "jdbc:mysql://localhost/estudante?user=estudante&password=estudante&useSSL=true";
        Connection conn = DriverManager.getConnection(url);

        String sql = "CREATE TABLE [IF NOT EXISTS] Resolution(id INT PRIMARY KEY, width INT, height INT);";
        Statement stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Settings(id INT, id_resolution INT, volume_effects NUMERIC(3,1),volume_music NUMERIC(3,1), volume_geral NUMERIC(3,1), full_screen BOOLEAN, save_date DATETIME,PRIMARY KEY (id), FOREIGN KEY (id_resolution) REFERENCES Resolution(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Player(id INT, id_settings INT, name VARCHAR(30), email VARCHAR(50), password CHAR(60), verified BOOLEAN, verification_token CHAR(36), register_date DATETIME, PRIMARY KEY (id),FOREIGN KEY (id_settings) REFERENCES Settings(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Change_password(id INT, id_player INT, validated BOOLEAN, verification_date DATETIME, verification_token CHAR(36), PRIMARY KEY (id), FOREIGN KEY (id_player) REFERENCES Player(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Class(id INT, name VARCHAR(20), PRIMARY KEY(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Save(id INT, id_player INT, id_class INT, character_name VARCHAR(20), PRIMARY KEY(id), FOREIGN KEY(id_class) REFERENCES Class(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Save_point(id INT, name VARCHAR(50), map INT, world_x INT, world_y INT, PRIMARY KEY (id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Save_state(id INT, id_save_point INT, play_time DECIMAL(15), date DATETIME,experience INT, coins INT, strenght INT, resistance INT, constitution INT, dexterity INT, wisdom INT,PRIMARY KEY (id), FOREIGN KEY (id_save_point) REFERENCES Save_point(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Item(id INT , name VARCHAR(50), PRIMARY KEY (id));";
        stm = conn.createStatement();
        stm.execute(sql);

        sql = "CREATE TABLE [IF NOT EXISTS] Item_in_inventory(id INT PRIMARY KEY, id_save_state INT, id_item INT, amount INT,position INT, FOREIGN KEY (id_item) REFERENCES Item(id), FOREIGN KEY (id_save_state) REFERENCES Save_state(id));";
        stm = conn.createStatement();
        stm.execute(sql);

        stm.close();
        conn.close();
    }
}
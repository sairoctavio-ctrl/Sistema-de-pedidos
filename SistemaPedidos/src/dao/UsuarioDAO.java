package dao;

import sistemapedidos.DatabaseConfig;
import models.Usuario;
import javax.swing.*;
import java.sql.*;

public class UsuarioDAO {
    public static Usuario autenticar(String username, String password) {
        String sql = "SELECT * FROM usuarios WHERE username=? AND password=?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("rol")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error al autenticar: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Error de conexión: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return null;
    }
}
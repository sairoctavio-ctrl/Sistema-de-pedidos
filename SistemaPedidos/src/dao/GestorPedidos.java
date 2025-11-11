package dao;

import sistemapedidos.DatabaseConfig;
import models.Pedido;
import javax.swing.*;
import java.sql.*;
import java.util.*;

public class GestorPedidos {
    private static GestorPedidos instancia;
    
    private GestorPedidos() {}
    
    public static GestorPedidos getInstancia() {
        if (instancia == null) {
            instancia = new GestorPedidos();
        }
        return instancia;
    }
    
    public List<Pedido> getPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        
        System.out.println("🔍 Cargando pedidos desde la base de datos...");
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            if (conn == null || conn.isClosed()) {
                System.err.println("❌ La conexión está cerrada o es nula");
                return pedidos;
            }
            
            stmt = conn.createStatement();
            String sql = "SELECT id, cliente, producto, estado, fecha, destino, prioridad, telefono, email, monto, notas FROM pedidos ORDER BY fecha_creacion DESC";
            
            System.out.println("📝 Ejecutando consulta: " + sql);
            rs = stmt.executeQuery(sql);
            
            int count = 0;
            while (rs.next()) {
                try {
                    Pedido pedido = new Pedido(
                        rs.getString("id"),
                        rs.getString("cliente"),
                        rs.getString("producto"),
                        rs.getString("estado"),
                        rs.getString("fecha"),
                        rs.getString("destino"),
                        rs.getString("prioridad"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getDouble("monto"),
                        rs.getString("notas")
                    );
                    
                    cargarHistorialIndividual(pedido);
                    
                    pedidos.add(pedido);
                    count++;
                    
                } catch (SQLException e) {
                    System.err.println("❌ Error al procesar fila: " + e.getMessage());
                }
            }
            
            System.out.println("✅ " + count + " pedidos cargados exitosamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error en getPedidos: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar pedidos", e);
        } finally {
            cerrarRecursos(rs, stmt, conn);
        }
        
        return pedidos;
    }
    
    private void cargarHistorialIndividual(Pedido pedido) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT estado, DATE_FORMAT(fecha_cambio, '%Y-%m-%d %H:%i') as fecha " +
                        "FROM historial_estados WHERE pedido_id = ? ORDER BY fecha_cambio ASC";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pedido.getId());
            
            rs = pstmt.executeQuery();
            
            List<String> historial = new ArrayList<>();
            while (rs.next()) {
                String entrada = rs.getString("fecha") + " - " + rs.getString("estado");
                historial.add(entrada);
            }
            
            pedido.setHistorialEstados(historial);
            
        } catch (SQLException e) {
            System.err.println("❌ Error al cargar historial para " + pedido.getId() + ": " + e.getMessage());
            pedido.setHistorialEstados(new ArrayList<>());
        } finally {
            cerrarRecursos(rs, pstmt, null);
        }
    }
    
    public boolean agregarPedido(Pedido pedido) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "INSERT INTO pedidos (id, cliente, producto, estado, fecha, destino, prioridad, telefono, email, monto, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pedido.getId());
            pstmt.setString(2, pedido.getCliente());
            pstmt.setString(3, pedido.getProducto());
            pstmt.setString(4, pedido.getEstado());
            pstmt.setString(5, pedido.getFecha());
            pstmt.setString(6, pedido.getDestino());
            pstmt.setString(7, pedido.getPrioridad());
            pstmt.setString(8, pedido.getTelefono());
            pstmt.setString(9, pedido.getEmail());
            pstmt.setDouble(10, pedido.getMonto());
            pstmt.setString(11, pedido.getNotas());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                agregarAlHistorial(pedido.getId(), pedido.getEstado());
                System.out.println("✅ Pedido " + pedido.getId() + " agregado exitosamente");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al agregar pedido: " + e.getMessage());
            mostrarError("Error al agregar pedido", e);
        } finally {
            cerrarRecursos(null, pstmt, null);
        }
        
        return false;
    }
    
    public boolean actualizarPedido(Pedido pedido) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "UPDATE pedidos SET cliente=?, producto=?, destino=?, prioridad=?, " +
                        "telefono=?, email=?, monto=?, notas=? WHERE id=?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pedido.getCliente());
            pstmt.setString(2, pedido.getProducto());
            pstmt.setString(3, pedido.getDestino());
            pstmt.setString(4, pedido.getPrioridad());
            pstmt.setString(5, pedido.getTelefono());
            pstmt.setString(6, pedido.getEmail());
            pstmt.setDouble(7, pedido.getMonto());
            pstmt.setString(8, pedido.getNotas());
            pstmt.setString(9, pedido.getId());
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✅ Pedido " + pedido.getId() + " actualizado exitosamente");
                return true;
            } else {
                System.err.println("❌ No se pudo actualizar el pedido " + pedido.getId());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar pedido: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al actualizar pedido", e);
        } finally {
            cerrarRecursos(null, pstmt, null);
        }
        
        return false;
    }
    
    public boolean actualizarEstado(String id, String nuevoEstado) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nuevoEstado);
            pstmt.setString(2, id);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                agregarAlHistorial(id, nuevoEstado);
                System.out.println("✅ Estado actualizado para " + id + ": " + nuevoEstado);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar estado: " + e.getMessage());
            mostrarError("Error al actualizar estado", e);
        } finally {
            cerrarRecursos(null, pstmt, null);
        }
        
        return false;
    }
    
    public boolean eliminarPedido(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "DELETE FROM pedidos WHERE id = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("🗑️ Pedido " + id + " eliminado exitosamente");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar pedido: " + e.getMessage());
            mostrarError("Error al eliminar pedido", e);
        } finally {
            cerrarRecursos(null, pstmt, null);
        }
        
        return false;
    }
    
    public Pedido buscarPorId(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM pedidos WHERE id = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Pedido pedido = new Pedido(
                    rs.getString("id"),
                    rs.getString("cliente"),
                    rs.getString("producto"),
                    rs.getString("estado"),
                    rs.getString("fecha"),
                    rs.getString("destino"),
                    rs.getString("prioridad"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getDouble("monto"),
                    rs.getString("notas")
                );
                
                cargarHistorialIndividual(pedido);
                return pedido;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar pedido: " + e.getMessage());
            mostrarError("Error al buscar pedido", e);
        } finally {
            cerrarRecursos(rs, pstmt, null);
        }
        
        return null;
    }
    
    public String generarNuevoId() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT MAX(CAST(SUBSTRING(id, 5) AS UNSIGNED)) as max_id FROM pedidos WHERE id LIKE 'PED-%'";
            
            rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                String nuevoId = String.format("PED-%04d", maxId + 1);
                System.out.println("🆕 Nuevo ID generado: " + nuevoId);
                return nuevoId;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al generar ID: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, stmt, null);
        }
        
        return "PED-0001";
    }
    
    public Map<String, Integer> obtenerEstadisticas() {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT estado, COUNT(*) as cantidad FROM pedidos GROUP BY estado";
            
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                stats.put(rs.getString("estado"), rs.getInt("cantidad"));
            }
            
            System.out.println("📊 Estadísticas cargadas: " + stats);
            
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener estadísticas: " + e.getMessage());
        } finally {
            cerrarRecursos(rs, stmt, null);
        }
        
        return stats;
    }
    
    private void agregarAlHistorial(String pedidoId, String estado) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "INSERT INTO historial_estados (pedido_id, estado) VALUES (?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pedidoId);
            pstmt.setString(2, estado);
            pstmt.executeUpdate();
            
            System.out.println("📝 Historial actualizado para " + pedidoId + ": " + estado);
            
        } catch (SQLException e) {
            System.err.println("❌ Error al agregar al historial: " + e.getMessage());
        } finally {
            cerrarRecursos(null, pstmt, null);
        }
    }
    
    private void cerrarRecursos(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar ResultSet: " + e.getMessage());
        }
        
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar Statement: " + e.getMessage());
        }
        
        try {
            if (conn != null && !conn.isClosed()) {
                // No cerramos la conexión principal aquí
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar Connection: " + e.getMessage());
        }
    }
    
    private void mostrarError(String mensaje, SQLException e) {
        System.err.println(mensaje + ": " + e.getMessage());
        JOptionPane.showMessageDialog(null, 
            mensaje + "\n" + e.getMessage(), 
            "Error de Base de Datos", 
            JOptionPane.ERROR_MESSAGE);
    }
}
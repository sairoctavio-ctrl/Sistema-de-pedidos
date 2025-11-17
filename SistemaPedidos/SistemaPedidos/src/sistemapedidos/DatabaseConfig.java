package sistemapedidos;

import javax.swing.*;
import java.sql.*;

public class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_pedidos?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection = null;
    
    static {
        cargarDriver();
    }
    
    private static void cargarDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ Driver MySQL cargado correctamente");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ ERROR: No se pudo cargar el driver MySQL");
            System.err.println("Asegúrate de tener mysql-connector-j-8.x.x.jar en el classpath");
            mostrarDialogoErrorDriver();
        }
    }
    
    private static void mostrarDialogoErrorDriver() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                "ERROR: Driver MySQL no encontrado\n\n" +
                "Para solucionar:\n" +
                "1. Descarga mysql-connector-j-8.0.33.jar\n" +
                "2. Colócalo en la carpeta del proyecto\n" +
                "3. Compila con: javac -cp \".;mysql-connector-j-8.0.33.jar\" *.java\n" +
                "4. Ejecuta con: java -cp \".;mysql-connector-j-8.0.33.jar\" Main",
                "Driver no encontrado",
                JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("🔌 Conectando a la base de datos...");
            System.out.println("URL: " + URL);
            System.out.println("Usuario: " + USER);
            
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Conexión exitosa a MySQL");
                
                // Verificar que la base de datos existe
                if (!verificarBaseDatos()) {
                    System.err.println("La base de datos 'sistema_pedidos' no existe");
                    crearBaseDatos();
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error de conexión: " + e.getMessage());
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Error Code: " + e.getErrorCode());
                
                if (e.getErrorCode() == 1049) { // Base de datos no existe
                    System.out.println("La base de datos no existe, intentando crearla...");
                    crearBaseDatos();
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                } else {
                    throw e;
                }
            }
        }
        return connection;
    }
    
    private static boolean verificarBaseDatos() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC", USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE 'sistema_pedidos'")) {
            
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error al verificar base de datos: " + e.getMessage());
            return false;
        }
    }
    
    private static void crearBaseDatos() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Creando base de datos 'sistema_pedidos'...");
            stmt.execute("CREATE DATABASE IF NOT EXISTS sistema_pedidos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ Base de datos creada exitosamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al crear base de datos: " + e.getMessage());
        }
    }
    
    public static void inicializarBaseDatos() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("🔄 Inicializando tablas...");
            
            // Tabla de usuarios
            String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "rol VARCHAR(30) NOT NULL," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(sqlUsuarios);
            System.out.println("✅ Tabla 'usuarios' lista");
            
            // Tabla de pedidos
            String sqlPedidos = "CREATE TABLE IF NOT EXISTS pedidos (" +
                "id VARCHAR(20) PRIMARY KEY," +
                "cliente VARCHAR(100) NOT NULL," +
                "producto VARCHAR(200) NOT NULL," +
                "estado VARCHAR(30) NOT NULL," +
                "fecha DATE NOT NULL," +
                "destino VARCHAR(150) NOT NULL," +
                "prioridad VARCHAR(20) NOT NULL," +
                "telefono VARCHAR(20)," +
                "email VARCHAR(100)," +
                "monto DECIMAL(10,2) DEFAULT 0," +
                "notas TEXT," +
                "fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(sqlPedidos);
            System.out.println("✅ Tabla 'pedidos' lista");
            
            // Tabla de historial
            String sqlHistorial = "CREATE TABLE IF NOT EXISTS historial_estados (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "pedido_id VARCHAR(20) NOT NULL," +
                "estado VARCHAR(30) NOT NULL," +
                "fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(sqlHistorial);
            System.out.println("✅ Tabla 'historial_estados' lista");
            
            // Insertar usuarios por defecto
            String checkUsuarios = "SELECT COUNT(*) FROM usuarios";
            ResultSet rs = stmt.executeQuery(checkUsuarios);
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertUsuarios = "INSERT INTO usuarios (username, password, rol) VALUES " +
                    "('admin', 'admin123', 'Administrador')," +
                    "('usuario', 'user123', 'Usuario')," +
                    "('operador', 'op123', 'Operador')";
                stmt.execute(insertUsuarios);
                System.out.println("✅ Usuarios por defecto insertados");
            }
            
            System.out.println("🎉 Base de datos inicializada correctamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar base de datos: " + e.getMessage());
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
    
    public static boolean probarConexion() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
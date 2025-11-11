package ui;

import sistemapedidos.DatabaseConfig;
import dao.UsuarioDAO;
import models.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginSistemaPedidos extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnLimpiar;
    private JLabel lblMensaje;
    private JLabel lblConexion;
    
    public LoginSistemaPedidos() {
        configurarVentana();
        crearComponentes();
        verificarConexion();
    }
    
    private void verificarConexion() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    DatabaseConfig.getConnection();
                    DatabaseConfig.inicializarBaseDatos();
                    return true;
                } catch (SQLException e) {
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        lblConexion.setText("🟢 Conectado a la base de datos");
                        lblConexion.setForeground(new Color(39, 174, 96));
                    } else {
                        lblConexion.setText("🔴 Error de conexión a BD");
                        lblConexion.setForeground(new Color(231, 76, 60));
                    }
                } catch (Exception e) {
                    lblConexion.setText("🔴 Error de conexión");
                    lblConexion.setForeground(new Color(231, 76, 60));
                }
            }
        };
        worker.execute();
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Seguimiento de Pedidos - Inicio de Sesión");
        setSize(500, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(236, 240, 241));
    }
    
    private void crearComponentes() {
        setLayout(null);
        
        // Panel superior con degradado
        JPanel panelSuperior = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(41, 128, 185), 0, getHeight(), new Color(44, 62, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelSuperior.setBounds(0, 0, 500, 140);
        panelSuperior.setLayout(null);
        
        JLabel lblIcono = new JLabel("📦");
        lblIcono.setBounds(210, 20, 80, 50);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        panelSuperior.add(lblIcono);
        
        JLabel lblTitulo = new JLabel("MAPU.INC");
        lblTitulo.setBounds(0, 75, 500, 30);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo);
        
        JLabel lblSubtitulo = new JLabel("Gestión con Base de Datos MySQL");
        lblSubtitulo.setBounds(0, 105, 500, 20);
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(236, 240, 241));
        panelSuperior.add(lblSubtitulo);
        
        add(panelSuperior);
        
        // Estado de conexión
        lblConexion = new JLabel("⏳ Verificando conexión...");
        lblConexion.setBounds(50, 155, 400, 20);
        lblConexion.setHorizontalAlignment(SwingConstants.CENTER);
        lblConexion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblConexion.setForeground(new Color(243, 156, 18));
        add(lblConexion);
        
        // Panel de formulario
        JPanel panelFormulario = new JPanel();
        panelFormulario.setBounds(50, 185, 400, 400);
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        panelFormulario.setLayout(null);
        
        JLabel lblIconoUsuario = new JLabel("👤");
        lblIconoUsuario.setBounds(160, 10, 80, 60);
        lblIconoUsuario.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        lblIconoUsuario.setHorizontalAlignment(SwingConstants.CENTER);
        panelFormulario.add(lblIconoUsuario);
        
        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setBounds(20, 80, 100, 25);
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsuario.setForeground(new Color(52, 73, 94));
        panelFormulario.add(lblUsuario);
        
        txtUsuario = new JTextField();
        txtUsuario.setBounds(20, 110, 340, 40);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        panelFormulario.add(txtUsuario);
        
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setBounds(20, 165, 100, 25);
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(new Color(52, 73, 94));
        panelFormulario.add(lblPassword);
        
        txtPassword = new JPasswordField();
        txtPassword.setBounds(20, 195, 340, 40);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        txtPassword.addActionListener(e -> intentarLogin());
        panelFormulario.add(txtPassword);
        
        btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setBounds(20, 260, 340, 45);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(39, 174, 96));
        btnLogin.setForeground(Color.black);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorder(BorderFactory.createEmptyBorder());
        btnLogin.addActionListener(e -> intentarLogin());
        panelFormulario.add(btnLogin);
        
        btnLimpiar = new JButton("Limpiar campos");
        btnLimpiar.setBounds(20, 315, 340, 35);
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpiar.setBackground(new Color(236, 240, 241));
        btnLimpiar.setForeground(new Color(52, 73, 94));
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiar.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        btnLimpiar.addActionListener(e -> limpiarCampos());
        panelFormulario.add(btnLimpiar);
        
        add(panelFormulario);
        
        // Mensaje
        lblMensaje = new JLabel("");
        lblMensaje.setBounds(50, 595, 400, 25);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Segoe UI", Font.BOLD, 12));
        add(lblMensaje);
        
        // Información de usuarios de prueba
        JLabel lblInfo = new JLabel("<html><center>Usuarios de prueba:<br>" +
            "admin/admin123 | usuario/user123 | operador/op123</center></html>");
        lblInfo.setBounds(50, 620, 400, 40);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfo.setForeground(new Color(127, 140, 141));
        add(lblInfo);
        
        agregarEfectosHover();
    }
    
    private void agregarEfectosHover() {
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(46, 204, 113));
            }
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(new Color(39, 174, 96));
            }
        });
        
        btnLimpiar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnLimpiar.setBackground(new Color(220, 221, 225));
            }
            public void mouseExited(MouseEvent e) {
                btnLimpiar.setBackground(new Color(236, 240, 241));
            }
        });
    }
    
    private void intentarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Por favor complete todos los campos", new Color(231, 76, 60));
            return;
        }
        
        // Mostrar indicador de carga
        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");
        mostrarMensaje("Autenticando...", new Color(243, 156, 18));
        
        SwingWorker<Usuario, Void> worker = new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return UsuarioDAO.autenticar(usuario, password);
            }
            
            @Override
            protected void done() {
                try {
                    Usuario user = get();
                    
                    if (user != null) {
                        mostrarMensaje("✓ Acceso concedido", new Color(39, 174, 96));
                        final Usuario userFinal = user;
                        Timer timer = new Timer(800, new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                abrirVentanaPrincipal(userFinal);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        mostrarMensaje("✗ Usuario o contraseña incorrectos", new Color(231, 76, 60));
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("INICIAR SESIÓN");
                    }
                } catch (Exception ex) {
                    mostrarMensaje("Error al autenticar", new Color(231, 76, 60));
                    btnLogin.setEnabled(true);
                    btnLogin.setText("INICIAR SESIÓN");
                }
            }
        };
        worker.execute();
    }
    
    private void limpiarCampos() {
        txtUsuario.setText("");
        txtPassword.setText("");
        lblMensaje.setText("");
        txtUsuario.requestFocus();
    }
    
    private void mostrarMensaje(String mensaje, Color color) {
        lblMensaje.setText(mensaje);
        lblMensaje.setForeground(color);
    }
    
    private void abrirVentanaPrincipal(Usuario usuario) {
        dispose();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                VentanaPrincipalPedidos ventana = new VentanaPrincipalPedidos(usuario);
                ventana.setVisible(true);
            }
        });
    }    
}
package ui;

import dao.GestorPedidos;
import models.Pedido;
import models.Usuario;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VentanaPrincipalPedidos extends JFrame {
    private Usuario usuarioActual;
    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private GestorPedidos gestor;
    private JTextField txtBusqueda;
    private JComboBox<String> cmbFiltroEstado;
    private JLabel lblEstadisticas;
    
    public VentanaPrincipalPedidos(Usuario usuario) {
        this.usuarioActual = usuario;
        this.gestor = GestorPedidos.getInstancia();
        configurarVentana();
        crearComponentes();
        cargarPedidos();
        actualizarEstadisticas();
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Pedidos - " + usuarioActual.getRol() + ": " + usuarioActual.getUsername());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
    }
    
    private void crearComponentes() {
        setLayout(new BorderLayout(0, 0));
        
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);
        
        JPanel panelCentral = crearPanelCentral();
        add(panelCentral, BorderLayout.CENTER);
        
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setPreferredSize(new Dimension(0, 120));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel panelTitulo = new JPanel(new BorderLayout());
        panelTitulo.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("📦 SISTEMA DE GESTIÓN ");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelTitulo.add(lblTitulo, BorderLayout.NORTH);
        
        lblEstadisticas = new JLabel();
        lblEstadisticas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadisticas.setForeground(new Color(236, 240, 241));
        lblEstadisticas.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        panelTitulo.add(lblEstadisticas, BorderLayout.CENTER);
        
        panel.add(panelTitulo, BorderLayout.WEST);
        
        JButton btnCerrarSesion = new JButton("🚪 Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrarSesion.setBackground(new Color(231, 76, 60));
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        panel.add(btnCerrarSesion, BorderLayout.EAST);
        
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setOpaque(false);
        
        JLabel lblBuscar = new JLabel("🔍");
        lblBuscar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        panelBusqueda.add(lblBuscar);
        
        txtBusqueda = new JTextField(20);
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBusqueda.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtBusqueda.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filtrarPedidos();
            }
        });
        panelBusqueda.add(txtBusqueda);
        
        JLabel lblFiltro = new JLabel("Estado:");
        lblFiltro.setForeground(Color.WHITE);
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelBusqueda.add(lblFiltro);
        
        cmbFiltroEstado = new JComboBox<>(new String[]{
            "Todos", "Procesando", "En Almacén", "En Tránsito", "En Reparto", "Entregado"
        });
        cmbFiltroEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbFiltroEstado.addActionListener(e -> filtrarPedidos());
        panelBusqueda.add(cmbFiltroEstado);
        
        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnActualizar.setBackground(new Color(52, 152, 219));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnActualizar.addActionListener(e -> {
            cargarPedidos();
            actualizarEstadisticas();
        });
        panelBusqueda.add(btnActualizar);
        
        panel.add(panelBusqueda, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        String[] columnas = {"ID", "Cliente", "Producto", "Estado", "Fecha", "Destino", "Prioridad", "Monto"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 7) return Double.class;
                return String.class;
            }
        };
        
        tablaPedidos = new JTable(modeloTabla);
        tablaPedidos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaPedidos.setRowHeight(30);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPedidos.setGridColor(new Color(189, 195, 199));
        tablaPedidos.setSelectionBackground(new Color(52, 152, 219));
        tablaPedidos.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = tablaPedidos.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(52, 73, 94));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        
        tablaPedidos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(245, 247, 250));
                    }
                    c.setForeground(Color.BLACK);
                }
                
                if (column == 3 && !isSelected) {
                    String estado = value.toString();
                    switch (estado) {
                        case "Entregado":
                            c.setForeground(new Color(39, 174, 96));
                            break;
                        case "En Reparto":
                        case "En Tránsito":
                            c.setForeground(new Color(52, 152, 219));
                            break;
                        case "Procesando":
                            c.setForeground(new Color(243, 156, 18));
                            break;
                        case "En Almacén":
                            c.setForeground(new Color(142, 68, 173));
                            break;
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                
                if (column == 6 && !isSelected) {
                    String prioridad = value.toString();
                    switch (prioridad) {
                        case "Alta":
                            c.setForeground(new Color(231, 76, 60));
                            break;
                        case "Media":
                            c.setForeground(new Color(243, 156, 18));
                            break;
                        case "Baja":
                            c.setForeground(new Color(149, 165, 166));
                            break;
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                
                if (column == 7) {
                    setHorizontalAlignment(JLabel.RIGHT);
                    if (value instanceof Double) {
                        setText(String.format("$%,.0f", (Double)value));
                    }
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                
                return c;
            }
        });
        
        tablaPedidos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    verDetalles();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)));
        
        JButton btnNuevo = crearBoton("➕ Nuevo Pedido", new Color(39, 174, 96));
        JButton btnActualizar = crearBoton("🔄 Actualizar Estado", new Color(52, 152, 219));
        JButton btnDetalles = crearBoton("📋 Ver Detalles", new Color(142, 68, 173));
        JButton btnEditar = crearBoton("✏️ Editar", new Color(243, 156, 18));
        JButton btnEliminar = crearBoton("🗑️ Eliminar", new Color(231, 76, 60));
        JButton btnExportar = crearBoton("📊 Exportar", new Color(52, 73, 94));
        
        btnNuevo.addActionListener(e -> nuevoPedido());
        btnActualizar.addActionListener(e -> actualizarEstado());
        btnDetalles.addActionListener(e -> verDetalles());
        btnEditar.addActionListener(e -> editarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());
        btnExportar.addActionListener(e -> exportarDatos());
        
        panel.add(btnNuevo);
        panel.add(btnActualizar);
        panel.add(btnDetalles);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnExportar);
        
        return panel;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        boton.addMouseListener(new MouseAdapter() {
            Color colorOriginal = color;
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                boton.setBackground(colorOriginal);
            }
        });
        
        return boton;
    }
    
    private void cargarPedidos() {
        SwingWorker<List<Pedido>, Void> worker = new SwingWorker<List<Pedido>, Void>() {
            @Override
            protected List<Pedido> doInBackground() throws Exception {
                return gestor.getPedidos();
            }
            
            @Override
            protected void done() {
                try {
                    modeloTabla.setRowCount(0);
                    List<Pedido> pedidos = get();
                    
                    for (Pedido p : pedidos) {
                        modeloTabla.addRow(new Object[]{
                            p.getId(),
                            p.getCliente(),
                            p.getProducto(),
                            p.getEstado(),
                            p.getFecha(),
                            p.getDestino(),
                            p.getPrioridad(),
                            p.getMonto()
                        });
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VentanaPrincipalPedidos.this,
                        "Error al cargar pedidos: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void filtrarPedidos() {
        String busqueda = txtBusqueda.getText().toLowerCase().trim();
        String estadoFiltro = (String) cmbFiltroEstado.getSelectedItem();
        
        SwingWorker<List<Pedido>, Void> worker = new SwingWorker<List<Pedido>, Void>() {
            @Override
            protected List<Pedido> doInBackground() throws Exception {
                return gestor.getPedidos();
            }
            
            @Override
            protected void done() {
                try {
                    modeloTabla.setRowCount(0);
                    List<Pedido> pedidos = get();
                    
                    for (Pedido p : pedidos) {
                        boolean coincideBusqueda = busqueda.isEmpty() || 
                            p.getId().toLowerCase().contains(busqueda) ||
                            p.getCliente().toLowerCase().contains(busqueda) ||
                            p.getProducto().toLowerCase().contains(busqueda) ||
                            p.getDestino().toLowerCase().contains(busqueda);
                        
                        boolean coincideEstado = estadoFiltro.equals("Todos") || 
                            p.getEstado().equals(estadoFiltro);
                        
                        if (coincideBusqueda && coincideEstado) {
                            modeloTabla.addRow(new Object[]{
                                p.getId(),
                                p.getCliente(),
                                p.getProducto(),
                                p.getEstado(),
                                p.getFecha(),
                                p.getDestino(),
                                p.getPrioridad(),
                                p.getMonto()
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void actualizarEstadisticas() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<Pedido> pedidos = gestor.getPedidos();
                int total = pedidos.size();
                long entregados = pedidos.stream().filter(p -> p.getEstado().equals("Entregado")).count();
                long enProceso = pedidos.stream().filter(p -> !p.getEstado().equals("Entregado")).count();
                
                return String.format(
                    "Total de pedidos: %d  |  Entregados: %d  |  En proceso: %d", 
                    total, entregados, enProceso
                );
            }
            
            @Override
            protected void done() {
                try {
                    lblEstadisticas.setText(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void nuevoPedido() {
        JDialog dialogo = new JDialog(this, "Nuevo Pedido", true);
        dialogo.setSize(500, 650);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());
        
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelFormulario.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField txtCliente = new JTextField(20);
        JTextField txtProducto = new JTextField(20);
        JTextField txtDestino = new JTextField(20);
        JTextField txtTelefono = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtMonto = new JTextField("0.0", 20);
        JComboBox<String> cmbPrioridad = new JComboBox<>(new String[]{"Alta", "Media", "Baja"});
        JTextArea txtNotas = new JTextArea(4, 20);
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        txtNotas.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        JScrollPane scrollNotas = new JScrollPane(txtNotas);
        
        int fila = 0;
        agregarCampoFormulario(panelFormulario, gbc, "Cliente:", txtCliente, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Producto:", txtProducto, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Destino:", txtDestino, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Teléfono:", txtTelefono, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Email:", txtEmail, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Monto:", txtMonto, fila++);
        agregarCampoFormulario(panelFormulario, gbc, "Prioridad:", cmbPrioridad, fila++);
        
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.weightx = 0.3;
        JLabel lblNotas = new JLabel("Notas:");
        lblNotas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelFormulario.add(lblNotas, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.gridheight = 2;
        panelFormulario.add(scrollNotas, gbc);
        
        dialogo.add(panelFormulario, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton btnGuardar = crearBoton("💾 Guardar", new Color(39, 174, 96));
        JButton btnCancelar = crearBoton("❌ Cancelar", new Color(149, 165, 166));
        
        btnGuardar.addActionListener(e -> {
            if (validarCampos(txtCliente, txtProducto, txtDestino)) {
                try {
                    String id = gestor.generarNuevoId();
                    String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    double monto = txtMonto.getText().isEmpty() ? 0 : Double.parseDouble(txtMonto.getText().replace(",", ""));
                    
                    Pedido nuevoPedido = new Pedido(
                        id,
                        txtCliente.getText().trim(),
                        txtProducto.getText().trim(),
                        "Procesando",
                        fecha,
                        txtDestino.getText().trim(),
                        (String) cmbPrioridad.getSelectedItem(),
                        txtTelefono.getText().trim(),
                        txtEmail.getText().trim(),
                        monto,
                        txtNotas.getText().trim()
                    );
                    
                    if (gestor.agregarPedido(nuevoPedido)) {
                        cargarPedidos();
                        actualizarEstadisticas();
                        JOptionPane.showMessageDialog(dialogo, 
                            "✓ Pedido creado exitosamente\nID: " + id,
                            "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dialogo.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialogo,
                            "Error al guardar el pedido",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialogo, 
                        "El monto debe ser un número válido",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialogo, 
                    "Complete todos los campos obligatorios",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialogo.dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);
        
        dialogo.setVisible(true);
    }
    
    private void agregarCampoFormulario(JPanel panel, GridBagConstraints gbc, 
                                        String etiqueta, Component campo, int fila) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.gridheight = 1;
        panel.add(campo, gbc);
    }
    
    private boolean validarCampos(JTextField... campos) {
        for (JTextField campo : campos) {
            if (campo.getText().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private void editarPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un pedido de la tabla",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String id = modeloTabla.getValueAt(fila, 0).toString();
        Pedido pedido = gestor.buscarPorId(id);
        
        if (pedido == null) {
            JOptionPane.showMessageDialog(this,
                "No se pudo encontrar el pedido seleccionado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialogo = new JDialog(this, "Editar Pedido - " + id, true);
        dialogo.setSize(500, 650);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());
        
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelFormulario.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JTextField txtCliente = new JTextField(pedido.getCliente(), 20);
        JTextField txtProducto = new JTextField(pedido.getProducto(), 20);
        JTextField txtDestino = new JTextField(pedido.getDestino(), 20);
        JTextField txtTelefono = new JTextField(pedido.getTelefono() != null ? pedido.getTelefono() : "", 20);
        JTextField txtEmail = new JTextField(pedido.getEmail() != null ? pedido.getEmail() : "", 20);
        JTextField txtMonto = new JTextField(String.valueOf(pedido.getMonto()), 20);
        JComboBox<String> cmbPrioridad = new JComboBox<>(new String[]{"Alta", "Media", "Baja"});
        cmbPrioridad.setSelectedItem(pedido.getPrioridad());
        
        JTextArea txtNotas = new JTextArea(pedido.getNotas() != null ? pedido.getNotas() : "", 4, 20);
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        txtNotas.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        JScrollPane scrollNotas = new JScrollPane(txtNotas);
        
        int filaForm = 0;
        agregarCampoFormulario(panelFormulario, gbc, "Cliente:", txtCliente, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Producto:", txtProducto, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Destino:", txtDestino, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Teléfono:", txtTelefono, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Email:", txtEmail, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Monto:", txtMonto, filaForm++);
        agregarCampoFormulario(panelFormulario, gbc, "Prioridad:", cmbPrioridad, filaForm++);
        
        gbc.gridx = 0;
        gbc.gridy = filaForm++;
        gbc.weightx = 0.3;
        JLabel lblNotas = new JLabel("Notas:");
        lblNotas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelFormulario.add(lblNotas, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.gridheight = 2;
        panelFormulario.add(scrollNotas, gbc);
        
        dialogo.add(panelFormulario, BorderLayout.CENTER);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton btnGuardar = crearBoton("💾 Guardar Cambios", new Color(39, 174, 96));
        JButton btnCancelar = crearBoton("❌ Cancelar", new Color(149, 165, 166));
        
        btnGuardar.addActionListener(e -> {
            System.out.println("Botón Guardar presionado");
            
            if (txtCliente.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, 
                    "El campo Cliente es obligatorio",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
                txtCliente.requestFocus();
                return;
            }
            
            if (txtProducto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, 
                    "El campo Producto es obligatorio",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
                txtProducto.requestFocus();
                return;
            }
            
            if (txtDestino.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, 
                    "El campo Destino es obligatorio",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
                txtDestino.requestFocus();
                return;
            }
            
            double monto;
            try {
                monto = Double.parseDouble(txtMonto.getText().trim().isEmpty() ? "0" : txtMonto.getText().replace(",", ""));
                if (monto < 0) {
                    JOptionPane.showMessageDialog(dialogo, 
                        "El monto no puede ser negativo",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, 
                    "El monto debe ser un número válido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                txtMonto.requestFocus();
                return;
            }
            
            pedido.setCliente(txtCliente.getText().trim());
            pedido.setProducto(txtProducto.getText().trim());
            pedido.setDestino(txtDestino.getText().trim());
            pedido.setTelefono(txtTelefono.getText().trim());
            pedido.setEmail(txtEmail.getText().trim());
            pedido.setMonto(monto);
            pedido.setPrioridad((String) cmbPrioridad.getSelectedItem());
            pedido.setNotas(txtNotas.getText().trim());
            
            System.out.println("Actualizando pedido: " + pedido.getId());
            
            if (gestor.actualizarPedido(pedido)) {
                System.out.println("Pedido actualizado exitosamente");
                cargarPedidos();
                JOptionPane.showMessageDialog(dialogo, 
                    "✓ Pedido actualizado exitosamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dialogo.dispose();
            } else {
                System.err.println("Error al actualizar pedido en la base de datos");
                JOptionPane.showMessageDialog(dialogo,
                    "Error al actualizar el pedido en la base de datos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> {
            dialogo.dispose();
        });
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);
        
        dialogo.setVisible(true);
    }
    
    private void actualizarEstado() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un pedido de la tabla",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String id = modeloTabla.getValueAt(fila, 0).toString();
        Pedido pedido = gestor.buscarPorId(id);
        
        if (pedido == null) return;
        
        String[] estados = {"Procesando", "En Almacén", "En Tránsito", "En Reparto", "Entregado"};
        String estadoActual = pedido.getEstado();
        
        String nuevoEstado = (String) JOptionPane.showInputDialog(
            this,
            "Estado actual: " + estadoActual + "\n\nSeleccione el nuevo estado:",
            "Actualizar Estado - " + id,
            JOptionPane.QUESTION_MESSAGE,
            null,
            estados,
            estadoActual
        );
        
        if (nuevoEstado != null && !nuevoEstado.equals(estadoActual)) {
            if (gestor.actualizarEstado(id, nuevoEstado)) {
                cargarPedidos();
                actualizarEstadisticas();
                JOptionPane.showMessageDialog(this,
                    "✓ Estado actualizado a: " + nuevoEstado,
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al actualizar el estado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void verDetalles() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un pedido de la tabla",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String id = modeloTabla.getValueAt(fila, 0).toString();
        Pedido pedido = gestor.buscarPorId(id);
        
        if (pedido == null) return;
        
        JDialog dialogo = new JDialog(this, "Detalles del Pedido - " + id, true);
        dialogo.setSize(600, 700);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(10, 10));
        
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "Información del Pedido",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(52, 152, 219)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        agregarDetalle(panelInfo, "🆔 ID Pedido:", id);
        agregarDetalle(panelInfo, "👤 Cliente:", pedido.getCliente());
        agregarDetalle(panelInfo, "📦 Producto:", pedido.getProducto());
        agregarDetalle(panelInfo, "📍 Destino:", pedido.getDestino());
        agregarDetalle(panelInfo, "📞 Teléfono:", pedido.getTelefono() != null ? pedido.getTelefono() : "No especificado");
        agregarDetalle(panelInfo, "📧 Email:", pedido.getEmail() != null ? pedido.getEmail() : "No especificado");
        agregarDetalle(panelInfo, "💰 Monto:", String.format("$%,.0f", pedido.getMonto()));
        agregarDetalle(panelInfo, "⏰ Fecha:", pedido.getFecha());
        agregarDetalle(panelInfo, "⚡ Prioridad:", pedido.getPrioridad());
        agregarDetalle(panelInfo, "📊 Estado Actual:", pedido.getEstado());
        
        if (pedido.getNotas() != null && !pedido.getNotas().isEmpty()) {
            agregarDetalle(panelInfo, "📝 Notas:", pedido.getNotas());
        }
        
        JPanel panelSeguimiento = new JPanel();
        panelSeguimiento.setLayout(new BoxLayout(panelSeguimiento, BoxLayout.Y_AXIS));
        panelSeguimiento.setBackground(Color.WHITE);
        panelSeguimiento.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(142, 68, 173), 2),
                "Seguimiento del Pedido",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(142, 68, 173)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        String[] etapas = {"Procesando", "En Almacén", "En Tránsito", "En Reparto", "Entregado"};
        int estadoActual = obtenerIndiceEstado(pedido.getEstado());
        
        for (int i = 0; i < etapas.length; i++) {
            JPanel panelEtapa = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            panelEtapa.setOpaque(false);
            
            JLabel lblIcono = new JLabel();
            JLabel lblTexto = new JLabel();
            
            if (i < estadoActual) {
                lblIcono.setText("✅");
                lblTexto.setText(etapas[i] + " - Completado");
                lblTexto.setForeground(new Color(39, 174, 96));
                lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else if (i == estadoActual) {
                lblIcono.setText("⏩");
                lblTexto.setText(etapas[i] + " - En proceso");
                lblTexto.setForeground(new Color(52, 152, 219));
                lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                lblIcono.setText("⏸️");
                lblTexto.setText(etapas[i] + " - Pendiente");
                lblTexto.setForeground(new Color(149, 165, 166));
                lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            
            panelEtapa.add(lblIcono);
            panelEtapa.add(lblTexto);
            panelSeguimiento.add(panelEtapa);
        }
        
        JPanel panelHistorial = new JPanel(new BorderLayout());
        panelHistorial.setBackground(Color.WHITE);
        panelHistorial.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(243, 156, 18), 2),
                "Historial de Cambios",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(243, 156, 18)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JTextArea txtHistorial = new JTextArea(5, 40);
        txtHistorial.setEditable(false);
        txtHistorial.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtHistorial.setBackground(new Color(245, 247, 250));
        
        StringBuilder historial = new StringBuilder();
        for (String cambio : pedido.getHistorialEstados()) {
            historial.append("• ").append(cambio).append("\n");
        }
        txtHistorial.setText(historial.toString());
        
        JScrollPane scrollHistorial = new JScrollPane(txtHistorial);
        panelHistorial.add(scrollHistorial, BorderLayout.CENTER);
        
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.add(panelInfo);
        panelContenido.add(Box.createVerticalStrut(10));
        panelContenido.add(panelSeguimiento);
        panelContenido.add(Box.createVerticalStrut(10));
        panelContenido.add(panelHistorial);
        
        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        dialogo.add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBoton.setBackground(Color.WHITE);
        JButton btnCerrar = crearBoton("Cerrar", new Color(52, 73, 94));
        btnCerrar.addActionListener(e -> dialogo.dispose());
        panelBoton.add(btnCerrar);
        
        dialogo.add(panelBoton, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }
    
    private void agregarDetalle(JPanel panel, String etiqueta, String valor) {
        JPanel panelFila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFila.setOpaque(false);
        
        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEtiqueta.setPreferredSize(new Dimension(140, 20));
        
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panelFila.add(lblEtiqueta);
        panelFila.add(lblValor);
        panel.add(panelFila);
    }
    
    private int obtenerIndiceEstado(String estado) {
        switch (estado) {
            case "Procesando": return 0;
            case "En Almacén": return 1;
            case "En Tránsito": return 2;
            case "En Reparto": return 3;
            case "Entregado": return 4;
            default: return 0;
        }
    }
    
    private void eliminarPedido() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un pedido de la tabla",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = modeloTabla.getValueAt(fila, 0).toString();
        String cliente = modeloTabla.getValueAt(fila, 1).toString();

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea eliminar el pedido?\n\n" +
            "ID: " + id + "\nCliente: " + cliente,
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (gestor.eliminarPedido(id)) {
                cargarPedidos();
                actualizarEstadisticas();
                JOptionPane.showMessageDialog(this,
                    "✓ Pedido eliminado exitosamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el pedido",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportarDatos() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("=== REPORTE DE PEDIDOS ===\n");
                sb.append("Fecha de generación: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
                sb.append("Usuario: ").append(usuarioActual.getUsername()).append("\n\n");

                List<Pedido> pedidos = gestor.getPedidos();
                sb.append("Total de pedidos: ").append(pedidos.size()).append("\n");
                sb.append("================================================\n\n");

                for (Pedido p : pedidos) {
                    sb.append("ID: ").append(p.getId()).append("\n");
                    sb.append("Cliente: ").append(p.getCliente()).append("\n");
                    sb.append("Producto: ").append(p.getProducto()).append("\n");
                    sb.append("Estado: ").append(p.getEstado()).append("\n");
                    sb.append("Destino: ").append(p.getDestino()).append("\n");
                    sb.append("Prioridad: ").append(p.getPrioridad()).append("\n");
                    sb.append("Monto: $").append(String.format("%,.0f", p.getMonto())).append("\n");
                    sb.append("------------------------------------------------\n");
                }

                return sb.toString();
            }

            @Override
            protected void done() {
                try {
                    String reporte = get();
                    JTextArea txtReporte = new JTextArea(reporte);
                    txtReporte.setEditable(false);
                    txtReporte.setFont(new Font("Monospaced", Font.PLAIN, 11));

                    JScrollPane scroll = new JScrollPane(txtReporte);
                    scroll.setPreferredSize(new Dimension(600, 400));

                    JOptionPane.showMessageDialog(
                        VentanaPrincipalPedidos.this,
                        scroll,
                        "Reporte de Pedidos",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VentanaPrincipalPedidos.this,
                        "Error al generar reporte: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginSistemaPedidos login = new LoginSistemaPedidos();
                login.setVisible(true);
            });
        }
    }
}
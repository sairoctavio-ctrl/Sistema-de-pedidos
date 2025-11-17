package models;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String id;
    private String cliente;
    private String producto;
    private String estado;
    private String fecha;
    private String destino;
    private String prioridad;
    private String telefono;
    private String email;
    private double monto;
    private String notas;
    private List<String> historialEstados;
    
    public Pedido(String id, String cliente, String producto, String estado, 
                  String fecha, String destino, String prioridad, String telefono,
                  String email, double monto, String notas) {
        this.id = id;
        this.cliente = cliente;
        this.producto = producto;
        this.estado = estado;
        this.fecha = fecha;
        this.destino = destino;
        this.prioridad = prioridad;
        this.telefono = telefono;
        this.email = email;
        this.monto = monto;
        this.notas = notas;
        this.historialEstados = new ArrayList<>();
    }
    
    // Getters
    public String getId() { return id; }
    public String getCliente() { return cliente; }
    public String getProducto() { return producto; }
    public String getEstado() { return estado; }
    public String getFecha() { return fecha; }
    public String getDestino() { return destino; }
    public String getPrioridad() { return prioridad; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public double getMonto() { return monto; }
    public String getNotas() { return notas; }
    public List<String> getHistorialEstados() { return historialEstados; }
    
    // Setters
    public void setEstado(String estado) { this.estado = estado; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public void setProducto(String producto) { this.producto = producto; }
    public void setDestino(String destino) { this.destino = destino; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setMonto(double monto) { this.monto = monto; }
    public void setNotas(String notas) { this.notas = notas; }
    public void setHistorialEstados(List<String> historial) { this.historialEstados = historial; }
    
    @Override
    public String toString() {
        return "Pedido{" +
                "id='" + id + '\'' +
                ", cliente='" + cliente + '\'' +
                ", producto='" + producto + '\'' +
                ", estado='" + estado + '\'' +
                ", destino='" + destino + '\'' +
                ", prioridad='" + prioridad + '\'' +
                '}';
    }
}
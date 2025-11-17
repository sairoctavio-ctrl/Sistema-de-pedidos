package sistemapedidos;

import ui.LoginSistemaPedidos;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Configurar el look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Ejecutar la aplicación en el Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginSistemaPedidos login = new LoginSistemaPedidos();
                login.setVisible(true);
            }
        });
    }
}

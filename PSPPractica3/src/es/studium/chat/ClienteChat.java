package es.studium.chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

public class ClienteChat extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    // Variables para la conexión con el servidor
    Socket socket;
    DataInputStream fentrada;
    DataOutputStream fsalida;
    String nombre;
    
    // Componentes de la interfaz gráfica
    static JTextField mensaje = new JTextField();
    private JScrollPane scrollpane;
    static JTextArea textarea;
    JButton boton = new JButton("Enviar");
    JButton desconectar = new JButton("Salir");
    
    // Control de flujo del programa
    boolean repetir = true;
    boolean juegoTerminado = false;

    // Constructor del ClienteChat
    public ClienteChat(Socket socket, String nombre) {
        super(" Conexión del cliente chat: " + nombre); // Título de la ventana
        setLayout(null); // Diseño manual
        
        // Configuración de los componentes gráficos
        mensaje.setBounds(10, 10, 400, 30);
        add(mensaje);
        
        textarea = new JTextArea();
        scrollpane = new JScrollPane(textarea);
        scrollpane.setBounds(10, 50, 400, 300);
        add(scrollpane);
        
        boton.setBounds(420, 10, 100, 30);
        add(boton);
        
        desconectar.setBounds(420, 50, 100, 30);
        add(desconectar);
        
        textarea.setEditable(false); // Deshabilitar edición del área de texto
        boton.addActionListener(this); // Agregar evento al botón Enviar
        this.getRootPane().setDefaultButton(boton); // Activar con Enter
        desconectar.addActionListener(this); // Agregar evento al botón Salir
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.socket = socket;
        this.nombre = nombre;

        try {
            // Inicializar flujos de entrada y salida
            fentrada = new DataInputStream(socket.getInputStream());
            fsalida = new DataOutputStream(socket.getOutputStream());
            
            // Notificar al servidor que el usuario se ha conectado
            String texto = "SERVIDOR> " + nombre + " ha entrado al juego.";
            fsalida.writeUTF(texto);
        } catch (IOException ex) {
            System.out.println("Error de E/S");
            ex.printStackTrace();
            System.exit(0);
        }
    }

    // Método principal para iniciar el cliente
    public static void main(String[] args) throws Exception {
        int puerto = 44444;
        
        // Pedir el nombre del usuario
        String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
        Socket socket = null;
        
        try {
            socket = new Socket("localhost", puerto); // Intentar conectarse al servidor
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor\n" + ex.getMessage(), "Mensaje de Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        // Validar que el usuario haya ingresado un nombre
        if (!nombre.trim().equals("")) {
            ClienteChat cliente = new ClienteChat(socket, nombre);
            cliente.setBounds(0, 0, 540, 400);
            cliente.setVisible(true);
            cliente.ejecutar();
        } else {
            System.out.println("El nombre está vacío...");
        }
    }

    // Método que maneja los eventos de los botones
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boton && !juegoTerminado) {
            String input = mensaje.getText().trim();
            try {
                int apuesta = Integer.parseInt(input); // Verifica si es un número
                String texto = nombre + "> " + apuesta;
                mensaje.setText("");
                fsalida.writeUTF(texto); // Envía el mensaje al servidor
                boton.setEnabled(false);
                
                // Bloquear el botón por 3 segundos después de enviar el mensaje
                new Timer(3000, evt -> boton.setEnabled(true)).start();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Debes ingresar un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == desconectar) {
            try {
                // Notificar al servidor que el usuario se ha desconectado
                fsalida.writeUTF("SERVIDOR> " + nombre + " ha salido del juego.");
                fsalida.writeUTF("*");
                repetir = false;
                socket.close(); // Cerrar la conexión
                System.exit(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Método para recibir mensajes del servidor y mostrarlos en la interfaz
    public void ejecutar() {
        String texto = "";
        while (repetir) {
            try {
                texto = fentrada.readUTF(); // Recibe el mensaje del servidor
                textarea.setText(texto);
                
                // Si el servidor indica que el juego ha terminado, desactivar el botón
                if (texto.contains("SERVIDOR> Juego terminado")) {
                    boton.setEnabled(false);
                    juegoTerminado = true;
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor\n" + ex.getMessage(), "Mensaje de Error", JOptionPane.ERROR_MESSAGE);
                repetir = false;
            }
        }
    }
}

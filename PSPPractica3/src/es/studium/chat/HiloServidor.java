package es.studium.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import javax.swing.JOptionPane;

public class HiloServidor extends Thread 
{
	// Flujo de entrada de datos desde el cliente
	DataInputStream fentrada;
	// Socket para la conexión con el cliente
	Socket socket;
	// Variable para controlar el fin del juego
	boolean fin = false;
	// Número secreto aleatorio entre 1 y 100
	static int numeroSecreto = new Random().nextInt(100) + 1;

	// Constructor que inicializa el hilo con el socket del cliente
	public HiloServidor(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	// Método principal del hilo que maneja la comunicación con el cliente
	public void run()
	{
		Servidor.mensaje.setText("Número de conexiones actuales: " + Servidor.ACTUALES);
		String texto = Servidor.textarea.getText();
		EnviarMensajes(texto);

		while(!fin)
		{
			try
			{
				// Recibir mensaje del cliente
				String cadena = fentrada.readUTF();
				if(cadena.trim().equals("*"))
				{
					// Cliente se desconecta
					Servidor.ACTUALES--;
					Servidor.mensaje.setText("Número de conexiones actuales: " + Servidor.ACTUALES);
					fin = true;
				}
				else
				{
					String[] partes = cadena.split("> ");
					if (partes.length == 2) {
						String nombre = partes[0];
						if (cadena.contains("SERVIDOR>")) {
							// Mensaje del servidor
							Servidor.textarea.append(cadena + "\n");
							EnviarMensajes(Servidor.textarea.getText());
							continue;
						}
						try {
							int apuesta = Integer.parseInt(partes[1].trim());
							String respuesta = nombre + " piensa que el número es el " + apuesta + ". ";

							// Comprobar si el número es correcto
							if (apuesta < numeroSecreto) {
								respuesta += "Pero el número es mayor.";
							} else if (apuesta > numeroSecreto) {
								respuesta += "Pero el número es menor.";
							} else {
								respuesta += "Y HA ACERTADOOOO!!!!";
								fin = true;
							}

							// Enviar respuesta a todos los clientes
							Servidor.textarea.append(respuesta + "\n");
							EnviarMensajes(Servidor.textarea.getText());

							// Si el juego termina, mostrar el número correcto
							if (fin) {
								Servidor.mensaje.setText("Juego terminado. Número correcto: " + numeroSecreto);
							}
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null, "Debes enviar un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin = true;
			}
		}
	}

	// Método para enviar mensajes a todos los clientes conectados
	private void EnviarMensajes(String texto)
	{
		for(int i = 0; i < Servidor.CONEXIONES; i++)
		{
			Socket socket = Servidor.tabla[i];
			try
			{
				DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

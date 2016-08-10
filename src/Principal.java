import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Principal 
{
	private static ArrayList<Socket> lista = new ArrayList<Socket>(); // El arreglo de sockets

	public static void main(String[] args) 
	{
		
		ServerSocket socketServidor = null; //Se inicializa en null el socket del servidor
		System.out.println("Socket del servidor creado exitosamente");
		try
		{
			socketServidor = new ServerSocket(21); // Se le asigna el puerto 21 al socket
			System.out.println("Socket del servidor iniciado");
			for(;;)
			{
				System.out.println("Servidor en espera...");
				Socket socket = socketServidor.accept(); // Se genera la entrada de un socket nuevo
				lista.add(socket);// se agrega el socket nuevo al arreglo
				Thread hiloServidor = new HiloServidor(lista, socket);// se genera un hilo para ese socket
				hiloServidor.start(); //se inicia el hilo
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

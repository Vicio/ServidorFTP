import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class HiloServidor extends Thread
{
	private String directorioServer = "E:\\Servidor\\"; //Direccion estática del servidor
	private String directorioUsuario = ""; //Directorio del usuario
	private String directorioActual = ""; //El directorio en el que se encuentra
	private String directorioLocal = ""; //El directorio de la computadora cliente
	private String usuario = ""; //El nombre del usuario
	private String password = ""; //La contraseña
	private Socket socket; //El socket
	private ArrayList<Socket> lista; //El arreglo de sockets para soporte de múltiples usuarios
	private DataInputStream entradaCliente; // La entrada de mensajes
	
	public HiloServidor(ArrayList<Socket> lista, Socket socket)
	{
		try
		{
			this.socket = socket; // se toma el socket enviado como parametro
			this.lista = lista;// se toma la lista de sockets
			entradaCliente = new DataInputStream 
			(
					socket.getInputStream()// se asigna la entrada de mensajes al socket
			);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		//la sintaxis de mensajes será comando-^-param 1,param 2...param n - 1,param n
		//el primer mensaje será #password@usuario
		//el mensaje para crear será ^CREATE-^-password@usuario
		for(;;)
		{
			String mensajeEntrada = MESSAGEIN();//se recibe algun mensaje
			//Si el mensaje inicia con ^ significa que se creara un usuario nuevo
			if(mensajeEntrada.charAt(0) == '^')
			{
				//se separan el comando
				StringTokenizer ST = new StringTokenizer(mensajeEntrada, "-^-");
				String comando = ST.nextToken();
				//se separa el usuario y el password
				StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), "@");
				int limite = ST2.countTokens();
				//se asignan como arreglo de strings
				String[] parametros = new String[limite];
				for(int i = 0; i < limite; i++)
				{
					parametros[i] = ST2.nextToken();
				}
				
				
				if(comando.equals("CREATE"))
				{
					//se envian los parametros al metodo CREATE
					CREATE(parametros);
				}				
			}
			else
			//Si el mensaje llega con el caracter # significa que hay un intento de login
			if(mensajeEntrada.charAt(0) == '#')
			{
				//se separa el usuario y el password
				StringTokenizer ST2 = new StringTokenizer(mensajeEntrada, "@");
				int limite = ST2.countTokens();
				String[] tokens = new String[limite];
				for(int i = 0; i < limite; i++)
				{
					tokens[i] = ST2.nextToken();
				}
				usuario = tokens[1];
				password = tokens[0];
				//se remueve el # de password
				password = password.substring(1, password.length());
				//se asigna el directorio del usuario
				directorioUsuario = directorioServer + usuario + "\\";
				try
				{
					//se abre el archivo usuario.pass
					BufferedReader buferLectura = new BufferedReader
					(
						new FileReader
						(
							directorioUsuario + usuario + ".pass"
						)
					);
					//Se lee la contraseña del archivo
					String pass = buferLectura.readLine();
					buferLectura.close();// Se cierra el archivo
					//se comparan la contraseña del archivo con la escrita por el uusario
					if(pass.equals(password))
					{
						//Si es correcta la contraseña se carga como directorio actual el directorio del usuario
						directorioActual = directorioUsuario;
						//se llama el método LS
						LS();
					}
					else
					{
						//Se envia un mensaje de contraseña incorrecta
						MESSAGEOUT("#BADPASS");
					}
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			//en caso de que no tenga caracter especial al principio se llega aqui indicando que es un comando
			else
			{
				//se separa el comando
				StringTokenizer ST = new StringTokenizer(mensajeEntrada, "-^-");
				String comando = ST.nextToken();
				
				//dependiendo del texto se llega a los diferentes valores a escoger
				if(comando.equals("CLOSE"))
				{
					CLOSE();
				}
				if(comando.equals("GET"))
				{
					GET(ST.nextToken());
				}
				if(comando.equals("PUT"))
				{
					//se separan los parametros para ser procesados en el método
					StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), ",");
					int limite = ST2.countTokens();
					String[] parametros = new String[limite];
					for(int i = 0; i < limite; i++)
					{
						parametros[i] = ST2.nextToken();
					}
					PUT(parametros);
				}
				if(comando.equals("LCD"))
				{
					//se separan los parametros para ser procesados en el método
					StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), ",");
					int limite = ST2.countTokens();
					String[] parametros = new String[limite];
					for(int i = 0; i < limite; i++)
					{
						parametros[i] = ST2.nextToken();
					}
					LCD(parametros);
				}
				if(comando.equals("CD"))
				{
					CD(ST.nextToken());
				}
				if(comando.equals("LS"))
				{
					LS();
				}
				if(comando.equals("DELETE"))
				{
					//se separan los parametros para ser procesados en el método
					StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), ",");
					int limite = ST2.countTokens();
					String[] parametros = new String[limite];
					for(int i = 0; i < limite; i++)
					{
						parametros[i] = ST2.nextToken();
					}
					DELETE(parametros);
				}
				if(comando.equals("MPUT"))
				{
					//se separan los parametros para ser procesados en el método
					StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), ",");
					int limite = ST2.countTokens();
					String[] parametros = new String[limite];
					for(int i = 0; i < limite; i++)
					{
						parametros[i] = ST2.nextToken();
					}
					MPUT(parametros);
				}
				if(comando.equals("MGET"))
				{
					//se separan los parametros para ser procesados en el método
					StringTokenizer ST2 = new StringTokenizer(ST.nextToken(), ",");
					int limite = ST2.countTokens();
					String[] parametros = new String[limite];
					for(int i = 0; i < limite; i++)
					{
						parametros[i] = ST2.nextToken();
					}
					MGET(parametros);
				}
				if(comando.equals("MKDIR"))
				{
					MKDIR(ST.nextToken());
				}
				if(comando.equals("RMDIR"))
				{
					RMDIR(ST.nextToken());
				}
				if(comando.equals("PWD"))
				{
					PWD();
				}
			}
		}
	}
	//Método para crear un usuario nuevo y su directorio
	private void CREATE(String[] params)
	{
		//se obtienen el usuario y la contraseña de los parámetros
		String pass = params[0];
		String usu = params[1];
		try
		{
			//Se crea un archivo nuevo y se genera como directorio con el nombre del usuario
			File dir = new File(directorioServer + usu);
			dir.mkdir();
			//Se asignan los directorios de usuario y el actual para la navegación 
			directorioUsuario = directorioServer + usu + "\\";
			directorioActual = directorioUsuario;
			//Se crea el archivo donde se guardará la contraseña
			File archivoPass = new File(directorioUsuario + usu + ".pass");
			archivoPass.createNewFile();
			//Se abre el archivo y se escribie la contraseña
			BufferedWriter escArchivo = new BufferedWriter
			(
				new FileWriter(archivoPass)
			);
			escArchivo.write(pass);
			escArchivo.close();
			//Se envía un mensaje de nuevo usuario creado al cliente
			MESSAGEOUT("^NEWUSERCREATED");
			System.out.println("Nuevo usuario creado: " + usu);
			//Se llama el metodo para cargar los directorios
			LS();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	//Método para recibir mensajes
	private String MESSAGEIN()
	{
		String mensaje = "";
		
		try
		{
			//Se lee un mensaje del socket
			mensaje = entradaCliente.readUTF();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return mensaje;
	}
	//Método para enviar mensajes
	private void MESSAGEOUT(String mensaje)
	{
		try
		{
			//se asigna una salida por el socket y se envía el mensaje
			DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
			salida.writeUTF(mensaje);
		}
		catch(IOException ioe)
		{
			System.err.println("Error mensajeSalida: " + ioe.toString());
		}		
	}
	//Método para cerrar la sesión
	private void CLOSE()
	{
		try
		{
			//Se envía un mensaje de cierre de sesión al cliente
			MESSAGEOUT("^SESSIONEND");
			//se cierra el socket
			socket.close();
			//se remueve de la lista
			lista.remove(socket);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#SESSIONENDERR");
			System.out.println("Error al cerrar sesion del usuario: " + usuario);
		}
	}
	//Método para descargar un archivo del servidor
	private void GET(String param)
	{
		try
		{
			//se obtiene el nombre del archivo
			String nombreArchivo = param;
			//se crea una variable para determinar la cantidad de bytes enviados
			int cantBytes = 0;
			//se crea un nuevo archivo
			File archivo = new File(directorioActual + nombreArchivo);
			//se crea el lector del archivo
			InputStream entrada = new FileInputStream(archivo);
			OutputStream salida = socket.getOutputStream();
			//se genera un bufer con tamaño de 4k
			byte[] bufer = new byte[4096];
			//se envia un mensaje para que el cliente se prepare para recibir el archivo
			MESSAGEOUT("GET-^-" + nombreArchivo + "," + archivo.length());
			System.out.println("Servidor listo para enviar archivo al usuario: " + usuario);
			//se envían las partes del archivo mientras hasta que se llega al final del archivo
			while((cantBytes = entrada.read(bufer)) != -1)
			{
				if(cantBytes > 0)
					salida.write(bufer, 0, cantBytes);
			}
			//se cierra el lector del archivo
			entrada.close();
			System.out.println("Archivo descargado correctamente para el usuario: " + usuario);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#PACKETLOST");
			System.out.println("Paquete perdido para el usuario: " + usuario);
		}
	}
	//Método para subir archivos al servidor
	private void PUT(String[] params)
	{
		try
		{
			//se obtiene el nombre del archivo
			String nombreArchivo = params[0];
			//Se obtiene el tamaño del archivo local
			long longArchivoLocal = Long.parseLong(params[1]);
			//se genera una variable para el tamaño del archivo remoto
			long longArchivoRemoto = 0;
			//se crea una variable para determinar la cantidad de bytes enviados
			int cantBytes = 0;
			//se genera un bufer con tamaño de 4k
			byte[] bufer = new byte[4096];
			//se crea un nuevo archivo
			File archivo = new File(directorioActual + nombreArchivo);
			archivo.createNewFile();
			//Se genera el stream para guardar el archivo
			OutputStream salida = new FileOutputStream(archivo);
			//se envía mensaje de que el servidor está listo para recibir el archivo
			MESSAGEOUT("^PUTREADY");
			System.out.println("Servidor listo para recibir archivo del usuario: " + usuario);
			//se genera la entrada de bytes a través del socket
			InputStream entrada = socket.getInputStream();
			//el ciclo continua mientras hasta que el tamaño enviado como parámetro coincida con el del archivo del servidor
			while(longArchivoLocal > longArchivoRemoto)
			{
				//se obtiene la cantidad de bytes
				cantBytes = entrada.read(bufer);
				//se suman los bytes acumulados en el envío
				longArchivoRemoto += cantBytes;
				if(cantBytes < 0)
				{
					MESSAGEOUT("^EOF");
					System.out.println("Se llegó al fin del archivo para el usuario: " + usuario);
				}
				if(cantBytes > 0)
					salida.write(bufer, 0, cantBytes); //Se escriben los bytes en el archivo del servidor
			}
			//se cierra el archivo
			salida.close();
			//se envia mensaje de descarga finalizada
			MESSAGEOUT("^DONEUPLOAD");
			//se llaman el método para mostrar los archivos y directorios del servidor
			LS();
			System.out.println("Archivo subido correctamente para el usuario: " + usuario);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#PACKETLOST");
			System.out.println("Paquete perdido para el usuario: " + usuario);
		}		
	}
	//Método para que el servidor sepa el directorio del cliente
	private void LCD(String[] params)
	{
		try
		{
			//se separa la carpeta
			String carpeta = params[0];
			//si el nombre de la carpeta es .. entonces se obtiene el directorio padre del actual
			if(carpeta.equals(".."))
			{
				//se separa el directorio
				String[] dir = directorioLocal.split("\\");
				String newdir = "";
				//se vuelve a unir omitiendo el último
				for(int i = 0; i < directorioLocal.length() - 1; i++)
					newdir += dir[i] + "\\";
				directorioLocal = newdir;
				MESSAGEOUT("LCD-^-" + directorioLocal);
			}
			else
			{
				//se le agrega el nombre del directorio al actual
				directorioLocal += params[0] + "\\";
				MESSAGEOUT("LCD-^-" + directorioLocal);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#NODIR");
			System.out.println("Directorio no existente para el usuario: " + usuario);
		}
		
	}
	//Método para la navegación del directorio del servidor
	private void CD(String dir)
	{
		try
		{
			String carpeta = dir;
			//si el nombre de la carpeta es .. entonces se obtiene el directorio padre del actual
			if(carpeta.equals(".."))
			{
				//Solo se hace si el directorio actual no es igual al del usuario
				if(!directorioActual.equals(directorioUsuario))
				{
					//se separa el directorio
					StringTokenizer STDir = new StringTokenizer(directorioActual, "\\");
					int limite = STDir.countTokens();
					directorioActual = "";
					//se vuelve a unir omitiendo el último
					for(int i = 0; i < limite - 1; i++)
					{
						directorioActual += STDir.nextToken() + "\\";
					}
					
					LS();
					System.out.println("El usuario " + usuario + " se movió a " + directorioActual);					
				}
			}
			else
			{
				//Se crea un nuevo archivo
				File temp = new File(directorioActual + dir);
				//si resulta ser un directorio se cambia el directorio actual a este nuevo directorio
				// y se envía la nueva lista de archivos al cliente
				if(temp.isDirectory())
				{
					directorioActual += dir + "\\";
					LS();
					System.out.println("El usuario " + usuario + " se movió a " + directorioActual);					
				}
			}			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#NODIR");
			System.out.println("El usuario " + usuario + " se movió a un directorio inexistente.");
		}
	}
	//Método para enviar el listado de archivos dentro de la carpeta actual a el cliente
	private void LS()
	{
		try
		{
			//se genera el archivo
			File dir = new File(directorioActual);
			//se carga el directorio absoluto
			String path = dir.getAbsolutePath();
			//se obtiene el listado de archivos y carpetas
			String[] dirs = dir.list();
			//se envía un mensaje para decirle al cliente que limpie su listado actual
			MESSAGEOUT("^CLEARLIST");
			//se envían los nombres de los archivos y directorios
			//con el formato tipoArchivo:nombre donde tipoArchivo = DIR/FILE
			for(int i = 0; i < dirs.length; i++)
			{
				File temp = new File(path + "\\" + dirs[i]);
				String tipoArchivo = "FILE";
				if(temp.isDirectory())
					tipoArchivo = "DIR";
				MESSAGEOUT("LISTLOCATION-^-" + tipoArchivo + "," + dirs[i]);
				System.out.println("Lista de directorios enviada para el usuario: " + usuario);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#LISTERR");
			System.out.println("Error al solicitar la lista para el usuario: " + usuario);
		}
	}
	//Método para borrar un archivo del servidor
	private void DELETE(String[] params)
	{
		try
		{
			//Se borra el archivo del servidor
			for(int i = 0; i < params.length; i++)
			{
				File archivo = new File(directorioActual + params[i]);
				archivo.delete();
				System.out.println("Archivo borrado: " + params[i] + " del usuario: " + usuario);				
			}
			//Se envia un mensaje al cliente de archivos borrados
			MESSAGEOUT("^FILESDELETED");
			//se envia la lista de archivos otra vez
			LS();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#DELERROR");
			System.out.println("Error al borrar archivos para le usuario: " + usuario);				
		}
	}
	//pendiente
	private void MPUT(String[] params)
	{
		
	}
	//pendiente
	private void MGET(String[] params)
	{
		
	}
	//Método para crear un directorio en el servidor
	private void MKDIR(String param)
	{
		String dir = param;
		try
		{
			//se crea un archivo con el nombre enviado como parámetro
			File carpeta = new File(directorioActual + dir);
			//se genera como carpeta
			carpeta.mkdir();
			//se envia un mensaje de directorio creado
			MESSAGEOUT("^DIRCREATED");
			//se envia la lista de archivos
			LS();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#BADLOCATION");
			System.out.println("Error al crear el directorio para el usuario: " + usuario);
		}
	}
	//Método para remover un directorio del servidor
	private void RMDIR(String param)
	{
		String dir = param;
		try
		{
			//se genera el archivo y si es un directorio entonces se borra del servidor
			File carpeta = new File(directorioActual + dir);
			if(carpeta.isDirectory())
				carpeta.delete();
			//se envia un mensaje de directorio borrado
			MESSAGEOUT("^DIRDELETED");
			//se envia la lista de archivos
			LS();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MESSAGEOUT("#NOTEMPTY");
		}
	}
	//Método para enviar el directorio actual en el que se encuentra el servidor
	private void PWD()
	{
		//
		String[] dir = directorioActual.split("\\");
		String ldir = "";
		for(int i = 2; i < directorioActual.length(); i++)
			ldir += dir[i] + "\\";
		MESSAGEOUT("LOCATION-^-" + ldir);
	}
}

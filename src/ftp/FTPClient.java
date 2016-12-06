package ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class FTPClient {

	private BufferedReader reader;
	private PrintWriter out;
	private FileInputStream is;
	private BufferedReader br;

	private static String host;
	private static int port = 21;
	private String passHost;
	private int passPort;

	private String username;
	private String password;

	private boolean isPassMode = false;

	public FTPClient(Socket socket) throws Exception {

		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),
				true);

		br = new BufferedReader(new InputStreamReader(System.in));

	}

	public void uploadFile() throws Exception {
		String filename;

		System.out.print("Enter File Path :");
		filename = br.readLine();

		File f = new File(filename);
		if (!f.exists()) {
			System.out.println("File not Exists...");
			return;
		}

		is = new FileInputStream(f);
		BufferedInputStream input = new BufferedInputStream(is);

		String response;
		if (!isPassMode) {
			out.println("PASV");
			response = reader.readLine();
			System.out.println(response);
			if (!response.startsWith("227 ")) {
				throw new IOException("FTPClient could not request passive mode: "
						+ response);
			}

			int opening = response.indexOf('(');
			int closing = response.indexOf(')', opening + 1);
			if (closing > 0) {
				String dataLink = response.substring(opening + 1, closing);
				StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
				try {
					passHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
							+ tokenizer.nextToken() + "." + tokenizer.nextToken();
					passPort = Integer.parseInt(tokenizer.nextToken()) * 256
							+ Integer.parseInt(tokenizer.nextToken());
				} catch (Exception e) {
					throw new IOException(
							"FTPClient received bad data link information: "
									+ response);
				}
			}

			isPassMode = true;
		}

		// Send command STOR
		out.println("STOR " + f.getName());

		// Open data connection
		Socket dataSocket = new Socket(passHost, passPort);

		// Read command response
		response = reader.readLine();
		System.out.println(response);

		// Read data from server
		BufferedOutputStream output = new BufferedOutputStream(
				dataSocket.getOutputStream());
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}

		output.flush();
		output.close();
		input.close();
		dataSocket.close();

		response = reader.readLine();
		System.out.println(response);
	}


	public void download() throws Exception{

		String outputDir;
		System.out.print("Enter Output Dir: ");
		outputDir = br.readLine();

		String serverFilePath;
		System.out.print("Enter server file path :");
		serverFilePath = br.readLine();

		// Go to passive mode
		String response;
		if (!isPassMode) {
			out.println("PASV");
			response = reader.readLine();
			System.out.println(response);
			if (!response.startsWith("227 ")) {
				throw new IOException("FTPClient could not request passive mode: "
						+ response);
			}

			int opening = response.indexOf('(');
			int closing = response.indexOf(')', opening + 1);
			if (closing > 0) {
				String dataLink = response.substring(opening + 1, closing);
				StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
				try {
					passHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
							+ tokenizer.nextToken() + "." + tokenizer.nextToken();
					passPort = Integer.parseInt(tokenizer.nextToken()) * 256
							+ Integer.parseInt(tokenizer.nextToken());
				} catch (Exception e) {
					throw new IOException(
							"FTPClient received bad data link information: "
									+ response);
				}
			}

			isPassMode = true;
		}

		// Send RETR command
		out.println("RETR " + serverFilePath);

		// Open data connection
		Socket dataSocket = new Socket(passHost, passPort);

		// Read data from server
		BufferedOutputStream output = new BufferedOutputStream(
				new FileOutputStream(new File(outputDir, serverFilePath)));
		BufferedInputStream input = new BufferedInputStream(
				dataSocket.getInputStream());
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}

		output.flush();
		output.close();
		input.close();

		dataSocket.close();
	}

	public void delete() throws Exception{
		String filename ;
		System.out.print("Enter File Path For Delete :");
		filename = br.readLine();

		// Send DELE command
		out.println("DELE " + filename);

		String msg;
		while ((msg = reader.readLine()) != null) {
			System.out.println(msg);
			if (msg.startsWith("250")) {
				break;
			}
		}
	}

	public void makeDir() throws Exception{
		String dirname ;
		System.out.print("Enter Dir Name :");
		dirname = br.readLine();

		// Send MKD command
		out.println("MKD " + dirname);

		String msg;
		while((msg = reader.readLine()) != null) {
			System.out.println(msg);
			if(msg.startsWith("257")) {
				break;
			}
		}
	}


	public void rename() throws Exception{
		String oldName ;
		String newName;
		System.out.print("Enter Old Name :");
		oldName = br.readLine();

		System.out.print("Enter New Name :");
		newName = br.readLine();

		// Send RNFR command
		out.println("RNFR " + oldName);
		String msg1 = reader.readLine();
		System.out.println(msg1);

		// Send RNTO command
		out.println("RNTO " + newName);
		msg1 = reader.readLine();
		System.out.println(msg1);

	}

	public void list() throws Exception{
		String response;

		// Go to passive mode
		if (!isPassMode) {
			out.println("PASV");
			response = reader.readLine();
			System.out.println(response);
			if (!response.startsWith("227 ")) {
				throw new IOException("FTPClient could not request passive mode: "
						+ response);
			}

			int opening = response.indexOf('(');
			int closing = response.indexOf(')', opening + 1);
			if (closing > 0) {
				String dataLink = response.substring(opening + 1, closing);
				StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
				try {
					passHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
							+ tokenizer.nextToken() + "." + tokenizer.nextToken();
					passPort = Integer.parseInt(tokenizer.nextToken()) * 256
							+ Integer.parseInt(tokenizer.nextToken());
				} catch (Exception e) {
					throw new IOException(
							"FTPClient received bad data link information: "
									+ response);
				}
			}

			isPassMode = true;
		}

		// Send LIST command
		out.println("LIST");

		// Open data connection
		Socket dataSocket = new Socket(passHost, passPort);

		// Read command response
		response = reader.readLine();
		System.out.println(response);

		// Read data from server
		BufferedReader reader  = new BufferedReader(new InputStreamReader(
				dataSocket.getInputStream()));
	    String  line = null;;
	    while((line = reader.readLine()) != null)
	    {
	        System.out.println(line);
	    }
	    reader.close();
	    dataSocket.close();

	}

	public void displayMenu() throws Exception {

		while (true) {

			System.out.println("[ MENU ]");
			System.out.println("1. Upload File");
			System.out.println("2. Download File");
			System.out.println("3. Delete File");
			System.out.println("4. Make Dir");
			System.out.println("5. Remane File");
			System.out.println("6. List");
			System.out.print("\nEnter Choice :");
			int choice;
			choice = Integer.parseInt(br.readLine());
			if (choice == 1) {

				uploadFile();

			} else if (choice == 2) {
				download();
			} else if (choice == 3) {
				delete();
			} else if (choice == 4) {
				makeDir();
			}else if (choice == 5) {
				rename();
			}else if(choice == 6){
				list();
			}
		}
	}

	public void init() throws Exception {
		System.out.print("\nEnter Username :");
		String s = br.readLine();
		setUsername(s);

		System.out.print("\nEnter Password :");
		s = br.readLine();
		setPassword(s);

		String msg;
		do{
			msg = reader.readLine();
			System.out.println(msg);
		}while(!msg.startsWith("220 "));

		out.println("USER " + username);

		String response = reader.readLine();
		System.out.println(response);
		if (!response.startsWith("331 ")) {
			throw new IOException(
					"SimpleFTP received an unknown response after sending the user: "
							+ response);
		}

		out.println("PASS " + password);

		response = reader.readLine();
		System.out.println(response);
		if (!response.startsWith("230 ")) {
			throw new IOException(
					"SimpleFTP was unable to log in with the supplied password: "
							+ response);
		}
	}

	public void setHost(String host) {
		FTPClient.host = host;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("\nEnter Host :");
		host = br.readLine();

		Socket socket = new Socket(host, port);

		FTPClient ftp = new FTPClient(socket);
		ftp.init();
		ftp.displayMenu();
	}

}


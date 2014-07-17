package ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private PrintWriter out;
	private FileInputStream is;
	private BufferedInputStream upload;
	private BufferedReader br;
	private String host;
	private int port = 21;
	private String username;
	private String password;

	public FTPClient(Socket socket) throws Exception {

		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
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

		// socket = new Socket(host, port);

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

		is = new FileInputStream(f);
		BufferedInputStream input = new BufferedInputStream(is);
		
		out.println("PASV");
		response = reader.readLine();
		System.out.println(response);
		String ip = null;
		int port1 = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);
		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
						+ tokenizer.nextToken() + "." + tokenizer.nextToken();
				port = Integer.parseInt(tokenizer.nextToken()) * 256
						+ Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException(
						"SimpleFTP received bad data link information: "
								+ response);
			}
		}

		out.println("STOR " + f.getName());
		Socket dataSocket = new Socket(ip, port);
		System.out.println("IP: " + ip);

		response = reader.readLine();
		System.out.println(response);

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

		response = reader.readLine();
		System.out.println(response);
	}
	/**************************************************************************************/
	public void download() throws Exception{
		
		String filenameLocal;
		System.out.print("Enter Local File Path :");
		filenameLocal = br.readLine();
		File f = new File(filenameLocal);
		
		String filename;
		System.out.print("Enter File Path :");
		filename = br.readLine();
		
		
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
		
		
		
		out.println("PASV");
		response = reader.readLine();
		if (!response.startsWith("227 ")) {
			throw new IOException("FTPClient could not request passive mode: "
					+ response);
		}
		String ip = null;
		int port1 = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);
		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
						+ tokenizer.nextToken() + "." + tokenizer.nextToken();
				port1 = Integer.parseInt(tokenizer.nextToken()) * 256
						+ Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException(
						"FTPClient received bad data link information: "
								+ response);
			}
		}
		Socket dataSocket = new Socket(ip, port1);

		
		out.println("RETR " + filename);
		response = reader.readLine();
		System.out.println(response);

		BufferedOutputStream output = new BufferedOutputStream(
				new FileOutputStream(new File(filenameLocal, filename)));
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

		response = reader.readLine();
		System.out.println(response);
	}
	
	public void delete() throws Exception{
		String filename ;
		System.out.print("Enter File Path For Delete :");
		filename = br.readLine();
		
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
		
		
		out.println("DELE " + filename);
	}
	
	public void makeDir() throws Exception{
		String dirname ;
		System.out.print("Enter Dir Name :");
		dirname = br.readLine();
		
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
		
		
		out.println("MKD " + dirname);
	}
	
	
	public void rename() throws Exception{
		String oldName ;
		String newName;
		System.out.print("Enter Old Name :");
		oldName = br.readLine();
		
		System.out.print("Enter New Name :");
		newName = br.readLine();
		
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
		
		
		out.println("RNFR " + oldName);
		String msg1 = reader.readLine();
		System.out.println(msg1);

		out.println("RNTO " + newName);
		msg1 = reader.readLine();
		System.out.println(msg1);
		
	}
	
	public void list() throws Exception{
		
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
		
		
		out.println("PASV");
		response = reader.readLine();
		if (!response.startsWith("227 ")) {
			throw new IOException("FTPClient could not request passive mode: "
					+ response);
		}
		String ip = null;
		int port1 = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);
		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
						+ tokenizer.nextToken() + "." + tokenizer.nextToken();
				port1 = Integer.parseInt(tokenizer.nextToken()) * 256
						+ Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException(
						"FTPClient received bad data link information: "
								+ response);
			}
		}
		Socket dataSocket = new Socket(ip, port1);
		
		out.println("LIST -a -1");
		msg = reader.readLine();
		System.out.println("lista" + msg);
		msg = reader.readLine();
		
		System.out.println("lista" + msg);
		msg = reader.readLine();
		System.out.println("lista" + msg);
		
		
		msg = reader.readLine();
		System.out.println("lista" + msg);
		
	}

	public void displayMenu() throws Exception {
		

		System.out.println("Username");
		System.out.print("\nEnter Username :");
		String s = br.readLine();
		setUsername(s);

		System.out.println("Password");
		System.out.print("\nEnter Password :");
		s = br.readLine();
		setPassword(s);

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

	public void setHost(String host) {
		this.host = host;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

class Main {
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Host");
		System.out.print("\nEnter Host :");
		String h = br.readLine();
		
		Socket s = new Socket(h, 21);
		FTPClient ftp = new FTPClient(s);
		ftp.displayMenu();
	}
}

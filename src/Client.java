import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 4444;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Connexió acceptada.");

            Thread inputThread = new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readUTF();
                        if (message.trim().isEmpty()) continue; // Ignora missatges buits
                        if (message.equals("FI")) {
                            System.out.println("Servidor ha tancat la connexió.");
                            break;
                        }
                        System.out.println("Servidor: " + message);
                    }
                } catch (SocketException e) {
                    System.out.println("La connexió amb el servidor s'ha tancat.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            inputThread.start();

            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                    while (true) {
                        String message = reader.readLine();
                        if (message.trim().isEmpty()) continue; // Ignora missatges buits
                        out.writeUTF(message);
                        out.flush();
                        if (message.equals("FI")) {
                            System.out.println("Has tancat la connexió.");
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputThread.start();

            inputThread.join();
            outputThread.join();

        } catch (IOException | InterruptedException e) {
            System.out.println("Servidor no disponible.");
        }
    }
}

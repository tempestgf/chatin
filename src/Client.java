import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 4444;
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final boolean milenium = true;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            Thread inputThread = createInputThread(in);
            Thread outputThread = createOutputThread(out);

            inputThread.start();
            outputThread.start();

            inputThread.join();
            outputThread.join();

        } catch (ConnectException e) {
            System.out.println("Servidor no disponible.");
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Error connecting to server", e);
        }
    }

    private static Thread createInputThread(DataInputStream in) {
        return new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (isEmptyMessage(message)) continue;
                    if (message.equals("FI")) {
                        System.out.println("Servidor ha tancat la connexió.");
                        break;
                    }
                    System.out.println("Servidor: " + message);
                }
            } catch (SocketException e) {
                System.out.println("La connexió amb el servidor s'ha tancat.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading from server", e);
            }
        });
    }

    private static Thread createOutputThread(DataOutputStream out) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String message = reader.readLine();
                    if (isEmptyMessage(message)) continue;
                    out.writeUTF(message);
                    out.flush();
                    if (message.equals("FI")) {
                        System.out.println("Has tancat la connexió.");
                        break;
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error writing to server", e);
            }
        });
    }

    private static boolean isEmptyMessage(String message) {
        if (message == null) return true;
        return message.trim().isEmpty();
    }
}

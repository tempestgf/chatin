import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final int PORT = 4444;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final boolean milenium = true;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor esperant connexió...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Connexió acceptada.");

            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                out.writeUTF("Connexió acceptada.");
                out.flush();

                Thread inputThread = createInputThread(in);
                Thread outputThread = createOutputThread(out);

                inputThread.start();
                outputThread.start();

                inputThread.join();
                outputThread.join();

            } catch (IOException | InterruptedException e) {
                logger.log(Level.SEVERE, "Error in I/O or thread interruption", e);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in ServerSocket", e);
        }
    }

    private static Thread createInputThread(DataInputStream in) {
        return new Thread(() -> {
            try {
                while (true) {
                    String message = in.readUTF();
                    if (isEmptyMessage(message)) continue;
                    if (message.equals("FI")) {
                        System.out.println("Client ha tancat la connexió.");
                        break;
                    }
                    System.out.println("Client: " + message);
                }
            } catch (SocketException e) {
                System.out.println("La connexió amb el client s'ha tancat.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading from client", e);
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
                logger.log(Level.SEVERE, "Error writing to client", e);
            }
        });
    }

    private static boolean isEmptyMessage(String message) {
        if (message == null) return true;
        return message.trim().isEmpty();
    }
}

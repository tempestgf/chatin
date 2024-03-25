import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private static final int PORT = 4444;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor esperant connexió...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Connexió acceptada.");

            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                Thread inputThread = new Thread(() -> {
                    try {
                        while (true) {
                            String message = in.readUTF();
                            if (message.trim().isEmpty()) continue; // Ignora missatges buits
                            if (message.equals("FI")) {
                                System.out.println("Client ha tancat la connexió.");
                                break;
                            }
                            System.out.println("Client: " + message);
                        }
                    } catch (SocketException e) {
                        System.out.println("La connexió amb el client s'ha tancat.");
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
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

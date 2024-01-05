import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class ParkingServer {
    private static ArrayList<Map<Integer, Boolean>> mockUpData;
    private static ArrayList<Map<Integer, Boolean>> inputData;
    private static String mockUpMap;
    private static boolean update;

    public int getResponseStatusCode(boolean success) {
        return success ? 200 : 404;
    }

    public void sendResponseStatusCode(ObjectOutputStream objectOutputStream, int statusCode) throws IOException {
        objectOutputStream.writeObject(statusCode);
        objectOutputStream.flush();
    }

    public boolean getData(ArrayList<Map<Integer, Boolean>> inputData) {
        mockUpData = inputData;
        if (mockUpData instanceof ArrayList && mockUpData != null) {
            return true;
        } else {
            return false;
        }
    }

    public void sendData(ObjectOutputStream objectOutputStream, ArrayList<Map<Integer, Boolean>> mockUpData,
            String mockUpMap) throws IOException {
        objectOutputStream.writeObject(mockUpData);
        objectOutputStream.flush();
        objectOutputStream.writeObject(mockUpMap);
        objectOutputStream.flush();
    }

    public synchronized static ArrayList<Map<Integer, Boolean>> updateData(ArrayList<Map<Integer, Boolean>> data) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Update Data");
        System.out.println("Index:");
        int index = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Status:");
        String status = scanner.nextLine();

        Map<Integer, Boolean> mapToEdit = data.get(index - 1);
        boolean newStatus = false;
        if (status.equals("false")) {
            newStatus = false;
        }
        if (status.equals("true")) {
            newStatus = true;
        }
        if (mapToEdit.containsKey(index)) {
            mapToEdit.put(index, newStatus);
        }

        update = false;
        return data;
    }

    public static void main(String[] args) {
        ParkingServer parkingServer = new ParkingServer();
        Random random = new Random();

        inputData = new ArrayList<>();
        // Integer[] keys = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        // Boolean[] values = { true, false, false, false, true, false, true, false,
        // false, true };

        // for (int i = 0; i < keys.length; i++) {
        // Integer key = keys[i];
        // Boolean value = values[i];
        // Map<Integer, Boolean> temp = new HashMap<>();
        // temp.put(key, value);
        // inputData.add(temp);
        // }

        for (int i = 1; i <= 40; i++) {
            Integer key = i;
            Boolean value = random.nextBoolean();
            Map<Integer, Boolean> temp = new HashMap<>();
            temp.put(key, value);
            inputData.add(temp);
        }

        // mockUpMap = "/H___/"
        // + "____/"
        // + "_PP_/"
        // + "_PP_/"
        // + "_PP_/"
        // + "_PP_/"
        // + "_PP_/"
        // + "____/";
        // mockUpMap = "";

        mockUpMap = "/_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_______/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_PP_PP_/"
                + "_______/";

        // update = true;

        int portNumber = 8080;
        int statusCode;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is waiting for client connection on port " + portNumber);
            statusCode = parkingServer.getResponseStatusCode(true);

            // Thread for updating data
            Thread updateThread = new Thread(() -> {
                while (true) {
                    try {
                        synchronized (inputData) {
                            inputData = updateData(inputData);
                            boolean test = parkingServer.getData(inputData);
                        }
                        System.out.println("Complete update" + inputData);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Thread for handling client connections
            Thread serverThread = new Thread(() -> {
                while (parkingServer.getData(inputData)) {
                    try (Socket clientSocket = serverSocket.accept();
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                                    clientSocket.getOutputStream())) {

                        parkingServer.sendResponseStatusCode(objectOutputStream, statusCode);
                        System.err.println("Status Code send to client: " + statusCode);
                        parkingServer.sendData(objectOutputStream, mockUpData, mockUpMap);
                        System.err.println("mockUpData send to client: " + mockUpData);
                        System.err.println("mockUpMap send to client: " + mockUpMap);

                    } catch (IOException e) {
                        System.err.println("Error handling client request: " + e.getMessage());
                    }
                }
            });

            // Start both threads
            updateThread.start();
            serverThread.start();

            // Wait for both threads to finish
            try {
                updateThread.join();
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            e.printStackTrace();
        }

    }
}
import java.io.IOException;
import java.net.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

/**
 * Created by jordi
 * Exemple Client UDP extret dels apunts del IOC i ampliat
 * El server és DatagramSocketServer
 *
 * Aquest client reb del server el mateix que se li envia
 * Si s'envia adeu s'acaba la connexió
 */

public class Client {
    KeyPair parelldeclaus;
    InetAddress serverIP;
    int serverPort;
    DatagramSocket socket;
    Scanner sc;
    boolean primera = true;
    PublicKey serverPKey;
    public Client() {
        sc = new Scanner(System.in);
        parelldeclaus = CryptoKey.randomGenerate(1024);
    }

    public void init(String host, int port) throws SocketException, UnknownHostException {
        serverIP = InetAddress.getByName(host);
        serverPort = port;
        socket = new DatagramSocket();
    }

    public void runClient() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte [] receivedData = new byte[1024];
        byte [] sendingData;

        sendingData = getFirstRequest();
        while (true) {
            if (primera) {
                System.out.println(Arrays.toString(sendingData));
                DatagramPacket packet = new DatagramPacket(sendingData,sendingData.length,serverIP,serverPort);
                socket.send(packet);
                packet = new DatagramPacket(receivedData,1024);
                socket.receive(packet);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(packet.getData());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                serverPKey = keyFactory.generatePublic(keySpec);
                primera = false;
            }
            else {
                DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
                socket.send(packet);
                packet = new DatagramPacket(receivedData, 1024);
                socket.receive(packet);
                sendingData = getDataToRequest(packet.getData(), packet.getLength());
            }
        }
    }

    //Resta de conversa que se li envia al server
    private byte[] getDataToRequest(byte[] data, int length) {
        String rebut = new String(CryptoKey.decryptData(data, parelldeclaus.getPrivate()));
        //Imprimeix el nom del client + el que es rep del server i demana més dades
        System.out.println("Server: " + rebut);
        String msg = sc.nextLine();
        System.out.println("Client: " + msg);
        return CryptoKey.encryptData(msg.getBytes(), serverPKey);
    }

    //primer missatge que se li envia al server
    private byte[] getFirstRequest() {

    }

    //Si se li diu adeu al server, el client es desconnecta

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.init("localhost",5555);
            client.runClient();
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.getStackTrace();
        }

    }

}
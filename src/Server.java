import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by jordi.
 * Exemple Servidor UDP extret dels apunts IOC i ampliat
 * El seu Client és DatagramSocketClient
 */
public class Server {
    DatagramSocket socket;
    InetAddress clientIP;

    KeyPair parelldeclaus;
    Scanner sc;
    boolean primera = true;
    PublicKey clientPKey;
    public Server() {
        sc = new Scanner(System.in);
        parelldeclaus = CryptoKey.randomGenerate(1024);
    }

    //Instanciar el socket
    public void init(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public void runServer() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte [] receivingData = new byte[1024];
        byte [] sendingData;

        int clientPort;

        while(true) {
            if (primera) {
                DatagramPacket packet = new DatagramPacket(receivingData,receivingData.length);
                socket.receive(packet);
                System.out.println(Arrays.toString(packet.getData()));
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(packet.getData());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                clientPKey = keyFactory.generatePublic(keySpec);
                clientPort = packet.getPort();
                packet = new DatagramPacket(parelldeclaus.getPublic().getEncoded(), parelldeclaus.getPublic().getEncoded().length, clientIP, clientPort);
                socket.send(packet);
                primera = false;
            }
            else {
                DatagramPacket packet = new DatagramPacket(receivingData, 1024);
                socket.receive(packet);
                clientIP = packet.getAddress();
                sendingData = processData(packet.getData(), packet.getLength());
                //Llegim el port i l'adreça del client per on se li ha d'enviar la resposta
                clientPort = packet.getPort();
                packet = new DatagramPacket(sendingData, sendingData.length, clientIP, clientPort);
                socket.send(packet);
            }
        }
    }
    //El server retorna al client el mateix missatge que li arriba però en majúscules
    private byte[] processData(byte[] data, int lenght) {
        String rebut = new String(CryptoKey.decryptData(data, parelldeclaus.getPrivate()));
        System.out.println("Server: " + rebut);
        String msg = new String(data,0,lenght);
        msg = msg.toUpperCase();
        //Imprimir el missatge rebut i retornar-lo
        System.out.println("Server: " + msg);
        return CryptoKey.encryptData(msg.getBytes(), clientPKey);
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.init(5555);
            server.runServer();
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


}
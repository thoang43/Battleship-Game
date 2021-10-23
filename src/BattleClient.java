import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;

class BattleClient {

    private Socket clientSocket;
    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private static int port = 6789;

    public BattleClient(String serverAddress) throws Exception{
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        clientSocket = new Socket(serverAddress, port);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        inFromServer =new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));
    }

    public void play() throws Exception{
        String response;
        String output;
        try {
            while (true) {
                response = inFromServer.readLine();
                System.out.println(response);


                if(!response.startsWith("MESSAGE")) {
                    System.out.println("Your message: ");
                    output = inFromUser.readLine();
                    outToServer.writeBytes(output);
                    outToServer.writeBytes("\r\n");
                }
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            clientSocket.close();
        }

    }

    public static void main(String argv[]) throws Exception
    {
        String serverAddress="";

        if (argv.length != 1){
            System.out.println("Please follow the format: java Client server_address");
            System.exit(0);
        }else{
            serverAddress = argv[0];
        }

        while (true) {
            BattleClient client = new BattleClient(serverAddress);
            client.play();
        }


    }
}
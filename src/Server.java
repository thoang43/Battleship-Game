/**
 * The server class which receives connections from clients, processes messages, responds and determines game state.
 * @author Khanh Hoang
 */

import java.io.*;
import java.net.*;

class Server {

    public static void main(String argv[]) throws Exception {


        ServerSocket welcomeSocket = new ServerSocket(6789);

        System.out.println("Waiting for incoming connection Request...");
        try {
            while (true) {

                Game game = new Game();
                Game.Player player1 = game.new Player(welcomeSocket.accept(), "Player_1");
                Game.Player player2 = game.new Player(welcomeSocket.accept(), "Player_2");
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                game.currentPlayer = player1;
                player1.start();
                player2.start();
            }
        } finally {
            welcomeSocket.close();
        }
    }
}


    class Game {

        private int ready_board = 0;
        private int count1 = 0;
        private int count2 = 0;

        private int[][] board_1 = {{0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0}
        };
        private int[][] board_2 = {{0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0},
                {0, 0, 0, 0 ,0 , 0, 0, 0 ,0 ,0}
        };

        /**
         * The current player.
         */
        Player currentPlayer;


        /**
         * This function determines whether it is a player's turn,
         * what the result of their moves is and what to respond
         * @param response response received from client
         * @param player player who sent this response
         * @return indicators(-1,0,1,2,3)
         */
        public synchronized int legalMove(String response, Player player) {
            if (player == currentPlayer) {
                if(response.startsWith("Setup")){
                    currentPlayer = currentPlayer.opponent;
                    if(ready_board == 0) {
                        currentPlayer.otherPlayerMoved("SUBMIT Let choose your ships location");
                    }else{
                        currentPlayer.otherPlayerMoved("READY");
                    }
                    return 2;
                }
                if(response.startsWith("Strike")) {
                    currentPlayer = currentPlayer.opponent;

                    String[] message = response.split(" ");
                    int row = Integer.parseInt(message[1].split(",")[0]);
                    int col = Integer.parseInt(message[1].split(",")[1]);
                    if (player.player.equals("Player_1")) {
                        if (board_2[row][col] == 1) {
                            count1++;
                            currentPlayer.otherPlayerMoved("OPPONENT HIT " + row + "," + col);
                            if (count1 == 9) {
                                currentPlayer.otherPlayerMoved("DEFEATED");
                                try {
                                    currentPlayer.socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return 3;
                                //win indicator
                            }
                            return 1;
                        } else {
                            currentPlayer.otherPlayerMoved("OPPONENT MISS");
                            return 0;
                        }
                    } else {
                        if (board_1[row][col] == 1) {
                            count2++;
                            currentPlayer.otherPlayerMoved("OPPONENT HIT " + row + "," + col);
                            if (count2 == 9) {
                                currentPlayer.otherPlayerMoved("DEFEATED");
                                try {
                                    currentPlayer.socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return 3;
                                //win indicator
                            }
                            return 1;
                        } else {
                            currentPlayer.otherPlayerMoved("OPPONENT MISS");
                            return 0;
                        }
                    }
                }
            }
            return -1;
        }


        class Player extends Thread {
            Player opponent;
            Socket socket;
            BufferedReader input;
            PrintWriter output;
            String player;
            boolean setup = false;

            public Player(Socket socket, String player) {
                this.socket = socket;
                this.player = player;
                try {
                    input = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(socket.getOutputStream(), true);
                    output.println("MESSAGE " + player + "- Waiting for opponent to connect");
                } catch (IOException e) {
                    System.out.println("Error: " + e);
                }
            }

            public void setOpponent(Player player) {
                this.opponent = player;
            }

            public void otherPlayerMoved(String response) {
                output.println(response);
            }

            /**
             * This function decodes the response from clients and establish the boards in the server side
             * @param response response from a client
             */
            public void setUpBoard(String response){
                ready_board++;
                if(player.equals("Player_1")){
                    String[] ships = response.split(" ");
                    String[] ship_1 = ships[1].split(":");
                    String[] ship_2 = ships[2].split(":");
                    String[] ship_3 = ships[3].split(":");

                    System.out.println(ships[0] + " " + ships[1] + " " +ships[2] + " " + ships[3]);

                    for(int i=0;i<ship_1.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_1[i].split(",");
                            board_1[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }
                    for(int i=0;i<ship_2.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_2[i].split(",");
                            board_1[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }
                    for(int i=0;i<ship_3.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_3[i].split(",");
                            board_1[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }

                    System.out.println("Player_1:");
                    for(int i=0;i<10;i++){
                        for(int j=0;j<10;j++){
                            System.out.print(board_1[i][j] + " ");
                        }
                        System.out.println();
                    }

                }else{
                    ready_board++;
                    String[] ships = response.split(" ");
                    String[] ship_1 = ships[1].split(":");
                    String[] ship_2 = ships[2].split(":");
                    String[] ship_3 = ships[3].split(":");

                    for(int i=0;i<ship_1.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_1[i].split(",");
                            board_2[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }
                    for(int i=0;i<ship_2.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_2[i].split(",");
                            board_2[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }
                    for(int i=0;i<ship_3.length;i++) {
                        if(i==0) continue;
                        else {
                            String[] position = ship_3[i].split(",");
                            board_2[Integer.parseInt(position[0])][Integer.parseInt(position[1])] = 1;
                        }
                    }

                    System.out.println("Player_2:");
                    for(int i=0;i<10;i++){
                        for(int j=0;j<10;j++){
                            System.out.print(board_2[i][j] + " ");
                        }
                        System.out.println();
                    }

                }
            }

            public void run() {
                try {
                    // The thread is only started after everyone connects.
                    output.println("MESSAGE All players connected");

                    // Tell the first player that it is their turn.
                    if (player.equals("Player_1")) {
                        output.println("SUBMIT Let choose your ships location");
                    }
                    // Repeatedly get commands from the client and process them.
                    while (true) {
                        String response = input.readLine();
                        System.out.println(response);
                        int result = legalMove(response, this);
                        if (result == 2) {
                            //If clients send Setup: setup the board
                            if (response.startsWith("Setup")) {
                                setUpBoard(response);
                            }
                        }else if(result == 1){
                            output.println("HIT");
                        }else if(result == 0){
                            output.println("MISS");
                        }else if(result == 3){
                            output.println("WIN");
                            socket.close();
                        }

                    }

                } catch (IOException e) {
                    System.out.println("Error: " + e);
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

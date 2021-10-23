//View
/**

 * The GUI class which provides user interface and connection with the server
 * @author Seongho Lee, Khanh Hoang
 *
 * */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.*;


public class BattleshipGameGUI extends JFrame {

    //Client side components
    private Socket clientSocket;
    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private static int port = 6789;

    //Game logic
    int gridSize;
    int shipPosition = 0;
    int shipCount = 0;
    int consecutive = 0;
    int prev_row=-1;
    int prev_col=-1;
    boolean is_vertical = false;
    boolean is_horizontal = false;
    boolean increasing = false;
    String setup = "";
    boolean can_submit = false;
    JButton chosen = null;
    String aim = "";
    boolean first_strike = true;

    //panels in GUI
    JPanel panel;
    JPanel boardPanel;
    JPanel setUpPanel;
    JPanel opponentPanel;
    JPanel welcomePanel;
    JPanel instructionPanel;
    JPanel commandPanel;

    //button
    JButton confirm;
    JButton strike;
    JButton reset;

    //display log
    JTextArea serverLog;

    //creates 2 matrix for the game board
    public static JButton setUpButton[][];
    public static JButton opponentButton[][];

    static int rows;
    static int columns;

    /**
     * @brief Constructor for GUI
     * @details Sets configurations of frame and adds components to screen
     * @param gridSize 10
     *
     */
    public BattleshipGameGUI(int gridSize, String serverAddress) throws Exception{

        //Establish connection
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        clientSocket = new Socket(serverAddress, port);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        inFromServer =new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));
        this.gridSize = gridSize;

        //Create the main panel
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        //NORTH: Welcoming message
        welcomePanel = new JPanel();
        JLabel welcomeLabel = new JLabel("WELCOME TO BATTLESHIP GAME!");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 14f));
        welcomePanel.add(welcomeLabel);
        //////////////////////////////



        //WEST: Game Instruction / Server log
        instructionPanel = new JPanel();
        JLabel instructionLabel = new JLabel("Instruction: ");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD, 12f));

        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        instructionPanel.add(instructionLabel);
        instructionPanel.add(new JLabel("1. You are given three ships of size 2, 3, and 4."));
        instructionPanel.add(new JLabel("2. Choose the positions of your ships."));
        instructionPanel.add(new JLabel("3. Press confirm button after each time finish choosing a ship positions."));
        instructionPanel.add(new JLabel("4. Press reset to remove the positions you chose."));
        instructionPanel.add(new JLabel("5. After setting up your field, start striking" +
                " in the opponent field below."));
        instructionPanel.add(new JLabel("6. Choose a location and press strike."));
        instructionPanel.add(new JLabel("7. H denote the hits."));
        instructionPanel.add(new JLabel("8. M denote the misses."));



        JLabel serverLogLabel = new JLabel("Server response:");
        serverLog = new JTextArea();
        serverLog.setEditable(false);
        instructionPanel.add(serverLog);
        //////////////////////////////



        //EAST: Containing buttons to play game
        commandPanel = new JPanel();
        commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));

        confirm = new JButton("Confirm");
        confirm.addActionListener(new commandListener());
        confirm.setEnabled(false);
        strike = new JButton("Strike");
        strike.addActionListener(new commandListener());
        strike.setEnabled(false);
        reset = new JButton("Reset");
        reset.addActionListener(new commandListener());
        reset.setEnabled(false);

        commandPanel.add(confirm);
        commandPanel.add(reset);
        commandPanel.add(strike);
        //////////////////////////////



        //CENTER: 2 boards which are allies and opponent ships
        //Create the panel containg setup panel and gaming panel
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(2,1));

        //Create the setup panel
        setUpPanel = new JPanel();
        setUpPanel.setLayout(new GridLayout(gridSize, gridSize));

        //Create game panel
        opponentPanel = new JPanel();
        opponentPanel.setLayout(new GridLayout(gridSize, gridSize));

        //simply creating the main GUI, dimensions of 100x100 for each grid
        setUpButton = new JButton[gridSize][gridSize];
        opponentButton = new JButton[gridSize][gridSize];
        for (rows = 0; rows < gridSize; rows++) {
            for (columns = 0; columns < gridSize; columns++) {
                setUpButton[rows][columns] = new JButton();
                setUpButton[rows][columns].setPreferredSize(new Dimension(50,50));
                //action listener allows for program to react to user button presses in correct grid
                setUpButton[rows][columns].addActionListener(new shipListener(rows, columns));
                setUpButton[rows][columns].setEnabled(false);
                setUpPanel.add(setUpButton[rows][columns]);

                opponentButton[rows][columns] = new JButton();
                opponentButton[rows][columns].setPreferredSize(new Dimension(50,50));
                //action listener allows for program to react to user button presses in correct grid
                opponentButton[rows][columns].addActionListener(new aimListener(rows, columns));
                opponentButton[rows][columns].setEnabled(false);
                opponentPanel.add(opponentButton[rows][columns]);
            }
        }

        JPanel setUpPanelWrapper = new JPanel();
        JLabel setUpLabel = new JLabel("Your ships' locations");
        setUpPanelWrapper.setLayout(new BoxLayout(setUpPanelWrapper, BoxLayout.Y_AXIS));
        setUpPanelWrapper.add(setUpLabel);
        setUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        setUpPanelWrapper.add(setUpPanel);

        JPanel opponentPanelWrapper = new JPanel();
        JLabel opponentLabel = new JLabel("Your opponent");
        opponentPanelWrapper.setLayout(new BoxLayout(opponentPanelWrapper, BoxLayout.Y_AXIS));
        opponentPanelWrapper.add(opponentLabel);
        opponentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        opponentPanelWrapper.add(opponentPanel);

        boardPanel.add(setUpPanelWrapper);
        boardPanel.add(opponentPanelWrapper);
        //////////////////////////////

        //Add everything into the main panel
        panel.add(boardPanel, BorderLayout.CENTER);
        panel.add(welcomePanel, BorderLayout.NORTH);
        panel.add(instructionPanel, BorderLayout.WEST);
        panel.add(commandPanel, BorderLayout.EAST);

        //Set up frame containing the main panel
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setTitle("Battleship Client");
        this.setContentPane(panel);
        this.pack();
    }

    //listener for buttons creating ships
    private class shipListener implements ActionListener {

        private int row;
        private int column;

        public shipListener(int row, int column){
            this.row = row;
            this.column = column;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if(prev_row == -1 && prev_col == -1){
                consecutive++;
                prev_row = this.row;
                prev_col = this.column;
            }else{
                if((is_vertical && this.row == prev_row) || (is_horizontal && this.column == prev_col)){
                    JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                            "Make sure you choose consecutive position");
                    resetBoard();
                    return;
                }else {
                    if (this.row == prev_row) {
                        is_horizontal = true;
                        consecutive++;
                        if(this.column == prev_col+1){
                            increasing = true;
                        }else if(this.column+1 == prev_col){
                            increasing = false;
                        }else{
                            JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                                    "Make sure you choose consecutive position");
                            resetBoard();
                            return;
                        }
                        prev_col = this.column;
                    }
                    else if (this.column == prev_col) {
                        is_vertical = true;
                        consecutive++;
                        if(this.row == prev_row+1){
                            increasing = true;
                        }else if(this.row + 1 == prev_row){
                            increasing = false;
                        }else{
                            JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                                    "Make sure you choose consecutive position");
                            resetBoard();
                            return;
                        }
                        prev_row = this.row;
                    }
                }

            }

            if(consecutive > 4){
                JOptionPane.showMessageDialog(BattleshipGameGUI.this, "Invalid number of position");
                resetBoard();

            }else {

                if (shipPosition == 9) {
                    JOptionPane.showMessageDialog(BattleshipGameGUI.this, "Invalid number of position");
                    resetBoard();

                } else {
                    setUpButton[row][column].setEnabled(false);
                    setUpButton[row][column].setIcon(new ImageIcon("greenExample.png"));
                    shipPosition++;
                }
            }

        }

        public void resetBoard(){
            for (rows = 0; rows < gridSize; rows++) {
                for (columns = 0; columns < gridSize; columns++) {
                    setUpButton[rows][columns].setEnabled(true);
                    setUpButton[rows][columns].setIcon(null);
                }
            }
            shipPosition = 0;
            shipCount = 0;
            consecutive = 0;
            is_horizontal = false;
            is_vertical = false;
            increasing = false;
            prev_col = -1;
            prev_row = -1;
            setup = "";
        }
    }

    //listener for the opponent board
    private class aimListener implements ActionListener {

        private int row;
        private int column;

        public aimListener(int row, int column){
            this.row = row;
            this.column = column;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if(chosen == null){
                chosen = (JButton)e.getSource();
                chosen.setIcon(new ImageIcon("greenExample.png"));
                aim = row + "," + column;
            }else{
                chosen.setIcon(null);
                chosen = (JButton)e.getSource();
                chosen.setIcon(new ImageIcon("greenExample.png"));
                aim = row + "," + column;
            }
        }
    }


    //listener class for buttons confirm, reset, strike
    private class commandListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == confirm) {
                try {
                    confirmSetup();
                }catch (Exception err){
                    serverLog.append(err.getMessage());
                }
            }

            else if(e.getSource() == reset){
                resetBoard();
            }
            else if (e.getSource() == strike){
                try {
                    outToServer.writeBytes("Strike " + aim);
                    outToServer.writeBytes("\r\n");
                    strike.setEnabled(false);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        }

        public void resetBoard(){
            for (rows = 0; rows < gridSize; rows++) {
                for (columns = 0; columns < gridSize; columns++) {
                    setUpButton[rows][columns].setEnabled(true);
                    setUpButton[rows][columns].setIcon(null);
                }
            }
            shipPosition = 0;
            consecutive = 0;
            shipCount = 0;
            is_horizontal = false;
            is_vertical = false;
            increasing = false;
            prev_col = -1;
            prev_row = -1;
            setup = "";
        }

        public void confirmSetup() throws Exception {

            if(consecutive == 1 || consecutive == 0){
                JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                        "Make sure your ship has a size of 2, 3, or 4");
                resetBoard();

            }else{

                if(consecutive == 2){
                    if(is_horizontal){
                        if(increasing){
                            setup += " 2:" + prev_row + "," + (prev_col-1)
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 2:" + prev_row + "," + prev_col
                                    + ":" + prev_row + "," + (prev_col+1);
                        }
                    }else{
                        if(increasing){
                            setup += " 2:" + (prev_row-1) + "," + prev_col
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 2:" + prev_row + "," + prev_col
                                    + ":" + (prev_row+1) + "," + prev_col;
                        }
                    }

                }else if(consecutive == 3){
                    if(is_horizontal){
                        if(increasing){
                            setup += " 3:" + prev_row + "," + (prev_col-2)
                                    + ":" + prev_row + "," + (prev_col-1)
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 3:" + prev_row + "," + prev_col
                                    + ":" + prev_row + "," + (prev_col+1)
                                    + ":" + prev_row + "," + (prev_col+2);
                        }
                    }else{
                        if(increasing){
                            setup += " 3:"+ (prev_row-2) + "," + prev_col
                                    + ":"  + (prev_row-1) + "," + prev_col
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 3:" + prev_row + "," + prev_col
                                    + ":" + (prev_row+1) + "," + prev_col
                                    + ":" + (prev_row+2) + "," + prev_col;
                        }
                    }
                }else if(consecutive == 4){
                    if(is_horizontal){
                        if(increasing){
                            setup += " 4:" + prev_row + "," + (prev_col-3)
                                    + ":" + prev_row + "," + (prev_col-2)
                                    + ":" + prev_row + "," + (prev_col-1)
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 4:" + prev_row + "," + prev_col
                                    + ":" + prev_row + "," + (prev_col+1)
                                    + ":" + prev_row + "," + (prev_col+2)
                                    + ":" + prev_row + "," + (prev_col+3);
                        }
                    }else{
                        if(increasing){
                            setup += " 4:"+ (prev_row-3) + "," + prev_col
                                    + ":" + (prev_row-2) + "," + prev_col
                                    + ":" + (prev_row-1) + "," + prev_col
                                    + ":" + prev_row + "," + prev_col;
                        } else {
                            setup += " 4:" + prev_row + "," + prev_col
                                    + ":" + (prev_row+1) + "," + prev_col
                                    + ":" + (prev_row+2) + "," + prev_col
                                    + ":" + (prev_row+3) + "," + prev_col;
                        }
                    }
                }

                shipCount++;
                consecutive = 0;
                is_horizontal = false;
                is_vertical = false;
                prev_col = -1;
                prev_row = -1;
            }

            //Finish setting up, disabled everything
            if(shipCount == 3 && shipPosition == 9){
                for (rows = 0; rows < gridSize; rows++) {
                    for (columns = 0; columns < gridSize; columns++) {
                        setUpButton[rows][columns].setEnabled(false);
                    }
                }
                reset.setEnabled(false);
                confirm.setEnabled(false);
                serverLog.append("Your ships setup: " + setup + "\n");
                setup = "Setup" + setup;
                outToServer.writeBytes(setup);
                outToServer.writeBytes("\r\n");

            }else if(shipCount == 3 && shipPosition!=9){
                JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                        "Make sure your ship has a size of 2, 3, or 4");
                resetBoard();
            }
        }


    }

    public void play() throws Exception{
        String response;
        String output;
        try {
            while (true) {
                response = inFromServer.readLine();

                //Server starts the game, now we can choose positions
                if(response.startsWith("MESSAGE")){
                    serverLog.append(response.substring(8)+"\n");
                }

                //Server let client submit setup positions
                if(response.startsWith("SUBMIT") ){
                    serverLog.append("You can choose your ship positions now.\n");
                    confirm.setEnabled(true);
                    reset.setEnabled(true);
                    for (rows = 0; rows < gridSize; rows++) {
                        for (columns = 0; columns < gridSize; columns++) {
                            setUpButton[rows][columns].setEnabled(true);
                        }
                    }
                }

                //All players ready with their boards, allow strike now
                if(response.startsWith("READY")){
                    for (rows = 0; rows < gridSize; rows++) {
                        for (columns = 0; columns < gridSize; columns++) {
                            opponentButton[rows][columns].setEnabled(true);
                        }
                    }
                    first_strike = false;
                    strike.setEnabled(true);
                }

                if(response.startsWith("MISS")){
                    chosen.setIcon(null);
                    chosen.setText("M");
                    chosen.setEnabled(false);
                }

                if(response.startsWith("HIT")){
                    chosen.setIcon(null);
                    chosen.setText("H");
                    chosen.setEnabled(false);
                }

                if(response.startsWith("OPPONENT")){
                    if(first_strike){
                        for (rows = 0; rows < gridSize; rows++) {
                            for (columns = 0; columns < gridSize; columns++) {
                                opponentButton[rows][columns].setEnabled(true);
                            }
                        }
                        first_strike = false;
                    }
                    String[] message = response.split(" ");
                    if(message[1].equals("HIT")){
                        int row = Integer.parseInt(message[2].split(",")[0]);
                        int col = Integer.parseInt(message[2].split(",")[1]);
                        setUpButton[row][col].setIcon(new ImageIcon("hit.png"));
                    }
                    strike.setEnabled(true);
                }
                if(response.startsWith("WIN")){
                    JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                            "You win");
                    System.exit(0);
                }
                if(response.startsWith("DEFEATED")){
                    JOptionPane.showMessageDialog(BattleshipGameGUI.this,
                            "You lose");
                    System.exit(0);
                }
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }finally {
            clientSocket.close();
        }

    }

    public static void main(String[] args) throws Exception{

        String serverAddress="";

        if (args.length != 1){
            System.out.println("Please follow the format: java BattleshipGameGUI server_address");
            System.exit(0);
        }else{
            serverAddress = args[0];
        }

        while (true) {
            BattleshipGameGUI game = new BattleshipGameGUI(10, serverAddress);
            game.play();
        }
    }
}

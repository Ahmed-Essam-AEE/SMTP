package multiplue_servers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

class CLient {

    public static void main(String[] args) {

        String outputFromServer;
        String inputFromUser;
        String userName, Password;
        String option;
        int check;

        //We inform the user of the port numbers available to connect to
        //We set fixed port numbers so that the servers are able to communicate with each other
        System.out.println("Please enter server port number.");
        //Gmail server has post number 5000
        //Hotmail server has post number 6000
        //Yahoo server has post number 7000
        //System.out.println("Gmail server has post number 5000");
        //System.out.println("Hotmail server has post number 6000");
        //System.out.println("Yahoo server has post number 7000");
        Scanner getInput = new Scanner(System.in);
        int portNumber = getInput.nextInt();


        try {
            Socket ClientSocket = new Socket("localhost",portNumber);
            DataOutputStream writeToServer = new DataOutputStream(ClientSocket.getOutputStream());
            DataInputStream readFromServer = new DataInputStream(ClientSocket.getInputStream());

            outputFromServer = readFromServer.readUTF(); //get the message from the server
            System.out.println(outputFromServer);

            System.out.println("Please choose ‘REGISTER or ‘LOGIN’ or ‘QUIT’.");
            option = getInput.next();

            if (option.equalsIgnoreCase("Quit")) {
                writeToServer.writeUTF("QUIT");
                outputFromServer = readFromServer.readUTF();
                System.out.println("Server: " + outputFromServer);
                ClientSocket.close();
            }
            //Register part
            else if (option.equalsIgnoreCase("REGISTER")) {
                writeToServer.writeUTF("REGISTER");
                //Read from user
                System.out.println("Please enter an email and a password.");
                userName = getInput.next();
                Password = getInput.next();
                //Send to Server
                writeToServer.writeUTF(userName);
                writeToServer.writeUTF(Password);

                System.out.println("HELLO " + userName);
                writeToServer.writeUTF("HELLO " + userName);


                outputFromServer = readFromServer.readUTF();
                System.out.print(outputFromServer);

                while (true) {

                    System.out.println("Please choose ‘SEND’ or ‘QUIT’");
                    option = getInput.next();

                    if (option.equalsIgnoreCase("quit")) {

                        writeToServer.writeUTF("QUIT");
                        outputFromServer = readFromServer.readUTF();
                        System.out.println("Server: " + outputFromServer);
                        ClientSocket.close();
                        break;

                    } else if (option.equalsIgnoreCase("SEND")) {

                        System.out.println("MAIL FROM " + userName);
                        writeToServer.writeUTF("MAIL FROM " + userName);
                        outputFromServer = readFromServer.readUTF();
                        System.out.println("Server: " + outputFromServer);

                        if(outputFromServer.substring(0,3).equals("450"))break;
                        //System.out.println("Please enter the email you want to send to");
                        System.out.println("RCPT TO ");
                        writeToServer.writeUTF("RCPT TO ");
                        inputFromUser = getInput.next();
                        //System.out.println(inputFromUser);
                        writeToServer.writeUTF(inputFromUser);
                        outputFromServer = readFromServer.readUTF();
                        System.out.println("Server: " + outputFromServer);

                        if(outputFromServer.substring(0,3).equals("450"))break;
                        System.out.println("DATA");
                        writeToServer.writeUTF("DATA");
                        outputFromServer = readFromServer.readUTF();
                        System.out.println("Server: " + outputFromServer);


                        while (true) {
                            inputFromUser = getInput.nextLine();
                            if (inputFromUser.equals("&&&")) {
                                writeToServer.writeUTF("&&&");
                                break;
                            } else {
                                writeToServer.writeUTF(inputFromUser);
                            }
                        }

                        outputFromServer = readFromServer.readUTF();
                        System.out.println("Server: " + outputFromServer);

                    }
                }
            }
            //Login part
            else if (option.equalsIgnoreCase("LOGIN")) {
                writeToServer.writeUTF("LOGIN");

                System.out.println("Please enter an email and a password.");
                userName = getInput.next();
                Password = getInput.next();
                writeToServer.writeUTF(userName);
                writeToServer.writeUTF(Password);

                //Checking from server if the user name exist or not.
                check = readFromServer.readInt();
                if (check == 2) { //2 means there no such a user with this name or password
                    System.out.println("Invalid User Name or Password.");

                } else {
                    System.out.println("HELLO " + userName);
                    writeToServer.writeUTF("HELLO " + userName);
                    outputFromServer = readFromServer.readUTF();
                    System.out.println(outputFromServer);

                    while (true) {
                        System.out.println("Please choose ‘SEND’ or ‘QUIT’");
                        option = getInput.next();

                        if (option.equalsIgnoreCase("QUIT")) {
                            writeToServer.writeUTF("QUIT");
                            outputFromServer = readFromServer.readUTF();
                            System.out.println(outputFromServer);
                            ClientSocket.close();
                            break;
                        } else if (option.equalsIgnoreCase("SEND")) {
                            writeToServer.writeUTF("SEND");


                            System.out.println("MAIL FROM " + userName);
                            writeToServer.writeUTF("MAIL FROM " + userName);
                            outputFromServer = readFromServer.readUTF();
                            System.out.println("Server: " + outputFromServer);

                            if(outputFromServer.substring(0,3).equals("450"))break;
                            //System.out.println("Please enter the email you want to send to");
                            System.out.println("RCPT TO ");
                            writeToServer.writeUTF("RCPT TO");
                            inputFromUser = getInput.next();
                            writeToServer.writeUTF(inputFromUser);
                            outputFromServer = readFromServer.readUTF();
                            System.out.println("Server: " + outputFromServer);

                            if(outputFromServer.substring(0,3).equals("450"))break;
                            System.out.println("DATA");
                            writeToServer.writeUTF("DATA");
                            outputFromServer = readFromServer.readUTF();
                            System.out.println("Server: " + outputFromServer);


                            while(true) {
                                inputFromUser = getInput.nextLine();
                                if (inputFromUser.equals("&&&")) {
                                    writeToServer.writeUTF("&&&");
                                    break; //End the loop
                                } else {
                                    writeToServer.writeUTF(inputFromUser);
                                }
                            }
                            outputFromServer = readFromServer.readUTF();
                            System.out.println("Server: " + outputFromServer);
                        }

                    }

                }
            } else {
                System.out.println("Invalid Entry!!");
            }

        }
        catch(Exception e) {
            System.out.println("Error!");
        }


    }

}
package multiplue_servers;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

class Server {

    public static String extractUsername(String email) {
        for(int i=0; i<email.length();i++) {
            if(email.charAt(i)=='@') {
                return email.substring(0, i);
            }
        }
        return email;
    }

    public static String extractMailServer(String email) {
        for(int i=0; i<email.length();i++) {
            if(email.charAt(i)=='@') {
                return email.substring(i+1);
            }
        }
        return email;
    }

    public static void main(String[] args)
    {
        Scanner read = new Scanner(System.in);
        System.out.println("Please enter the server name and port number");
        // first we take server name and port number from user
        String serverName = read.next() ;
        int portNumber=read.nextInt() ;
        String sName="";
        // then we want only the name of server without (.com) so we loop on every char in string
        for(int i=0 ; i<serverName.length() ; i++)
        {
            if (serverName.charAt(i)=='.')      // if char = dot then we take only characters before this char
            {
                //here we make first char an upper case
                sName=serverName.substring(0,1).toUpperCase().concat(serverName.substring(1,i));
                break;
            }
        }



        //Creating the server folder which holds the credentials file and the files for all users
        File Project20 = new File("D:\\Project20");
        if(!Project20.exists())Project20.mkdir();
        File serverFile = new File("D:\\Project20\\"+serverName);
        File credentialsFile = new File("D:\\Project20\\"+serverName+"\\credentials.txt");
        try {
            if(serverFile.mkdir()) {
                credentialsFile.createNewFile();
            }
        } catch (IOException e) {
            System.out.print("Could not make a server folder");
        }

        try {
            ServerSocket myServer = new ServerSocket(portNumber);
            System.out.println(sName +" server with port number '" + portNumber +"' is booted up");
            while(true) {
                Socket clientSocket = myServer.accept();

                ClientConnection clientConnection = new ClientConnection(clientSocket, serverName, portNumber);
                clientConnection.start();
            }
        } catch (IOException e) {
            System.out.print("an error occured with connection with the client, now aborting...");
        }

    }



    static class ClientConnection extends Thread{
        Socket client;
        String serverName;
        int portNumber;
        Scanner read = new Scanner(System.in);

        ClientConnection(Socket clientSocket, String serverName, int portNumber){
            this.client = clientSocket;
            this.serverName = serverName;
            this.portNumber = portNumber;
        }

        public void run() {
            try {
                //The server process(responding to hosts)
                try {

                    while(true) {

                        //defining the data input and output streams for my clients
                        DataInputStream clientReadSource = new DataInputStream(client.getInputStream());
                        DataOutputStream clientWriteSource = new DataOutputStream(client.getOutputStream());

                        //We write a message to the client informing him that he has successfully connected to the server
                        clientWriteSource.writeUTF("220 "+ serverName);

                        //We write a message to the client asking him to choose weather to "register" or "login"
                        //clientWriteSource.writeUTF("Please choose 'REGSISTER' or 'LOGIN' or 'SERVER'");

                        //We then take input from the client specifying which mode he has chosen
                        String mode =  clientReadSource.readUTF().trim();
                        //String mode = read.next();



                        boolean access =false;
                        String currentUsername = "";
                        //If the user chose to register for the first time
                        if(mode.equalsIgnoreCase("register")){
                            String username = extractUsername(clientReadSource.readUTF());
                            String password = clientReadSource.readUTF();
                            //String username = extractUsername(read.next());
                            //String password = read.next();

                            File userFile = new File("D:\\Project20\\"+serverName+"\\"+username);
                            File inboxForUser = new File("D:\\Project20\\"+serverName+"\\"+username+"\\inbox.txt");
                            if(userFile.mkdir()) inboxForUser.createNewFile();

                            FileWriter credentialsFileWriter = new FileWriter("D:\\Project20\\"+serverName+"\\credentials.txt", true);
                            String credentialsLine ="";
                            credentialsLine += username + "\n";
                            credentialsLine += password + "\n";
                            currentUsername = username;
                            access = true;

                            credentialsFileWriter.append(credentialsLine);
                            credentialsFileWriter.close();
                        }

                        else if(mode.equalsIgnoreCase("quit")) {
                            clientWriteSource.writeUTF("exiting connection...");
                        }
                        //If the user chose to login to an already existing account
                        while(mode.equalsIgnoreCase("login") && !access) {
                            String email = clientReadSource.readUTF();
                            String username = extractUsername(email);
                            String mailServer = extractMailServer(email);
                            String password = clientReadSource.readUTF();
                            //String username = extractUsername(read.next());
                            //String password = read.next();

                            File credentialsFile = new File("D:\\Project20\\"+serverName+"\\credentials.txt");
                            Scanner credentialsFileReader = new Scanner(credentialsFile);
                            while(credentialsFileReader.hasNext()) {
                                String inputUsername = credentialsFileReader.next();
                                String inputPassword = credentialsFileReader.next();
                                if(inputUsername.equals(username)) {
                                    if(inputPassword.equals(password)) {
                                        access = true;
                                        currentUsername = inputUsername;
                                    }
                                }
                            }
                            credentialsFileReader.close();
                            if(access == true) {
                                //System.out.println("You have successfully logged in");
                                clientWriteSource.writeInt(1);
                            }
                            else {
                                //System.out.println("Failed to log in");
                                if(mailServer.equals(serverName))
                                    clientWriteSource.writeInt(2);
                            }
                        }

                        if(mode.equalsIgnoreCase("server")) {
                            String email = clientReadSource.readUTF();
                            String otherUsername = clientReadSource.readUTF();
                            FileWriter inbox = new FileWriter("D:\\Project20\\"+serverName+"\\"+otherUsername+"\\inbox.txt" , true);
                            inbox.append(email);
                            inbox.close();
                            clientWriteSource.writeUTF("250.. accepted for delivery");
                            client.close();
                        }



                        //read.nextLine();
                        //process the client commands
                        boolean helloFlag = false, senderFlag = false, recepientFlag = false;
                        String recepientMailServer ="";
                        String recepientUsername ="";
                        while(access) {
                            String clientCommand = clientReadSource.readUTF().trim();
                            //String clientCommand = read.nextLine();

                            if(clientCommand.length() >= 5 && clientCommand.substring(0,5).equals("HELLO")) {
                                //extract the email address from the client message
                                String email = clientCommand.substring(6);
                                String username = extractUsername(email);

                                //check if that email address exists
                                File user = new File("D:\\Project20\\"+serverName+"\\"+username);
                                if(user.exists() && username.equals(currentUsername)) {
                                    helloFlag = true;
                                }

                                //respond to the client acknowledging the email address
                                String serverResponse = "";
                                if(helloFlag) {
                                    serverResponse = "250 Hello " + username + " pleased to meet you\n";
                                }
                                else {
                                    serverResponse = "450 username not registered\n";
                                }
                                clientWriteSource.writeUTF(serverResponse);
                                //System.out.print(serverResponse);
                            }

                            else if(clientCommand.length() >= 9 && clientCommand.substring(0, 9).equals("MAIL FROM")) {
                                //extract the email address from the client message
                                String email = clientCommand.substring(10);
                                String username = extractUsername(email);

                                //check if the sender email address exists and matches the hello email address
                                //
                                if(helloFlag && username.equals(currentUsername))
                                    senderFlag = true;

                                //respond to the client acknowledging the email address
                                String serverResponse ="";
                                if(senderFlag){
                                    serverResponse = "250 " + email + "...Sender ok\n";
                                }
                                else {
                                    serverResponse = "450 user's mailbox unavailable\n";
                                }
                                clientWriteSource.writeUTF(serverResponse);
                                //System.out.print(serverResponse);
                            }

                            else if(clientCommand.length() >= 7 && clientCommand.substring(0, 7).equals("RCPT TO")) {
                                //extract the email address from the client message
                                String recepientEmail = clientReadSource.readUTF();
                                recepientUsername = extractUsername(recepientEmail);
                                recepientMailServer = extractMailServer(recepientEmail);
                                File recepient = new File("D:\\Project20\\" + recepientMailServer + "\\" + recepientUsername);
                                //System.out.print("D:\\Project20\\" + recepientMailServer + "\\" + recepientUsername);
                                //check if the recipient email address exists
                                if(senderFlag && recepient.exists()) {
                                    recepientFlag = true;
                                }

                                //respond to the client acknowledging the email address
                                String serverResponse = "";
                                if(recepientFlag) {
                                    serverResponse = "250 " + recepientEmail + "...Recipient ok\n";
                                }
                                else {
                                    serverResponse = "450 user's mailbox unavailable\n";
                                }
                                clientWriteSource.writeUTF(serverResponse);
                                //System.out.print(serverResponse);
                            }

                            else if(clientCommand.length() >= 4 && clientCommand.substring(0, 4).equals("DATA")) {
                                //tell the client that the server is ready to receive the data
                                String serverResponse = "";
                                if(recepientFlag)
                                    serverResponse =  "354 Please enter the body of your email ended by ‘&&&‘ on a line by itself\n";
                                clientWriteSource.writeUTF(serverResponse);
                                //System.out.print(serverResponse);

                                //receive the data from the client
                                String data = "";
                                String clientData = "";
                                while(!clientData.equals("&&&")) {
                                    data = data.concat(clientData + "\n");
                                    clientData = clientReadSource.readUTF();
                                    //clientData = read.nextLine();
                                }
                                String message = currentUsername + "@" + serverName + data;

                                if(recepientMailServer.equalsIgnoreCase(serverName)) {
                                    FileWriter inbox = new FileWriter("D:\\Project20\\" + recepientMailServer + "\\" + recepientUsername + "\\inbox.txt", true);
                                    inbox.append(message);
                                    inbox.close();
                                }
                                else {
                                    //First, we identify the port number we should be sending to
                                    int otherServerPortNumber = 0 ;
                                    if(recepientMailServer.equals("gmail.com")) otherServerPortNumber = 5000;
                                    else if(recepientMailServer.equals("hotmail.com")) otherServerPortNumber = 6000;
                                    else if(recepientMailServer.equals("yahoo.com")) otherServerPortNumber = 7000;
                                    else break;
                                    //Then we open a connection with the that port number and we send the email to it
                                    Socket myClient = new Socket("localhost",otherServerPortNumber);
                                    DataInputStream myClientReadSource = new DataInputStream(myClient.getInputStream());
                                    DataOutputStream myClientWriteSource = new DataOutputStream(myClient.getOutputStream());
                                    myClientReadSource.readUTF();
                                    myClientWriteSource.writeUTF("server");
                                    myClientWriteSource.writeUTF(message);
                                    myClientWriteSource.writeUTF(recepientUsername);
                                    myClientReadSource.readUTF();
                                    //myClient.close();
                                }

                                //respond to the client acknowledging the data
                                serverResponse = "250 Message accepted for delivery\n";
                                clientWriteSource.writeUTF(serverResponse);
                            }

                            else if(clientCommand.length() >= 4 && clientCommand.substring(0, 4).equals("QUIT")) {
                                //respond to the client by exiting
                                String serverResponse = "221 " + serverName+" closing connection\n";
                                clientWriteSource.writeUTF(serverResponse);
                                //System.out.print(serverResponse);
                                client.close();
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.print("Socket closed or an exception occured during the communication with the client, now aborting this thread");
                    client.close();
                }
            }
            catch(Exception e){
                System.out.print("couldnt connect with client" + client);
            }
        }
    }
}
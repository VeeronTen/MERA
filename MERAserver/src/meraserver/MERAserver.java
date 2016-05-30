package meraserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MERAserver {
    UserList users;
    ServerSocket serverSocket;
    String bufferPath="C:\\MERAserver\\";

    MERAserver(){
        try{
            new File(bufferPath).mkdir();
            users = new UserList();
            serverSocket = new ServerSocket(8080);
        }catch(Exception e){
            System.out.println("MERAserver has not been enabled");
        }
    }
    void go(){
        new Thread(new SocketListener()).start();
    }

    public static void main(String[] args) {
        new MERAserver().go();
    }

        class SocketListener implements Runnable{
            public void run(){
                while(true){
                    try{
                        users.newConnection(serverSocket.accept());
                    }catch(Exception e){
                        System.out.println("SocketListener create error");
                    }
                }
            }
        }

        class UserList{
            private LinkedList<ActiveUser> users;

            public UserList() {
                users = new LinkedList<ActiveUser>();
            }

            public void newConnection(Socket socket){
                new Thread(new ActiveUser(socket)).start();
            }

            void addUserToUsers(ActiveUser newUser){
                byte[] byteArray;
                byteArray = new byte[20];//count/2=length
                try{
                    newUser.is.read(byteArray);
                    newUser.userName = new String(byteArray, "UTF-8");
                }catch(Exception e){
                    System.out.println("NAMEexception");
                }

                for(ActiveUser i:users)
                        i.writeAboutNewUser(newUser.userName);


                users.add(newUser);
                for(ActiveUser i:users)
                        newUser.writeAboutNewUser(i.userName);
            }
            void deleteUserFromUsers(ActiveUser userForDel){
                users.remove(users.indexOf(userForDel));
                for(ActiveUser i : users)
                        i.writeAboudDelUser(userForDel.userName);
            }
            void sendFileFrom(ActiveUser sender){
                byte[] buffer = new byte[520];//max length of filename in Windows OS - 260. 2 bytes - char size.
                try{
                    sender.is.read(buffer);
                    String fileName = new String(buffer, "UTF-8").trim();
                    System.out.println(bufferPath+fileName);
                    readFileFrom(sender, fileName);

                    for(ActiveUser i : users){
                        //if(i!=sender){
                        i.os.write("file".getBytes());
                        i.os.write(buffer);
                        i.os.write(sender.userName.getBytes());

                    }
                }catch(Exception e){
                    System.out.println("sendFile problem");
                }


            }

            void readFileFrom(ActiveUser sender, String fileName){
                int count;
                byte[] buffer = new byte[8192];

                try{
                    FileOutputStream fo = new FileOutputStream(bufferPath+fileName);
                    while((count = sender.is.read(buffer))!=-1){
                        if(sender.socket.getSoTimeout()==0 && count!=8192){
                            System.out.println("setSoTimeout");
                            sender.socket.setSoTimeout(1000);
                        }
                        fo.write(buffer, 0, count);
                    }
                }catch(SocketTimeoutException se){
                    try{
                        System.out.println("file was been uploaded");
                        sender.socket.setSoTimeout(0);
                    }catch(Exception e){
                        System.out.println("setSoTimeout(0) problem");
                    }
                }catch(Exception e){
                    System.out.println("readFileFrom problem");
                }
            }

            class ActiveUser implements Runnable{
                String userName;
                Socket socket;
                InputStream is;
                OutputStream os;
                ActiveUser(Socket userSocket){
                    try{
                        socket = userSocket;
                        is = socket.getInputStream();
                        os = socket.getOutputStream();
                    }catch(Exception e){
                        System.out.println("ActiveUser Constructor Error");
                    }
                }
                public void run(){
                    byte[] byteArray;
                    try{
                        while(true){
                            byteArray = new byte[4];//count/2=length
                            is.read(byteArray);
                            String key = new String(byteArray, "UTF-8");//.trim();
                            switch(key){
                                case "user":
                                    addUserToUsers(this);
                                    break;
                                case "delt":
                                    deleteUserFromUsers(this);
                                    break;
                                case "file":
                                    System.out.println("file");
                                    sendFileFrom(this);
                                    break;
                                default:
                                    System.out.println("def");
                                    break;
                            }
                        }
                    }catch(Exception e){
                        deleteUserFromUsers(this);
                        Thread.currentThread().interrupt();
                    }
                }

                synchronized void writeAboutNewUser(String nameNewUser){
                    try{
                    os.write("user".getBytes());
                    os.write(nameNewUser.getBytes());
                    }catch(Exception e){
                        System.out.println(userName+": writeAboutNewUser problem");
                    }
                }

                synchronized void writeAboudDelUser(String nameDelUser){
                    try{
                   os.write("delt".getBytes());
                   os.write(nameDelUser.getBytes());
                    }catch(Exception e){
                        System.out.println(userName+": writeAboutDelUser problem");
                    }
                }
            }
        }

}

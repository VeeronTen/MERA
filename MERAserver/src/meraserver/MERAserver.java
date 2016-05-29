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

            void addUser(ActiveUser newUser){
                byte[] byteArray;
                byteArray = new byte[20];//count/2=length
                try{
                    newUser.is.read(byteArray);
                    newUser.userName = new String(byteArray, "UTF-8");
                }catch(Exception e){
                    System.out.println("NAMEexception");
                }

                for(ActiveUser i:users){
                    try{
                        i.os.write("user".getBytes());
                        i.os.write(newUser.userName.getBytes());
                    }catch(Exception e){
                        System.out.println("newUser was not add to "+i.userName);
                    }
                }
                users.add(newUser);
                for(ActiveUser i:users){
                    try{
                        newUser.os.write("user".getBytes());
                        newUser.os.write(i.userName.getBytes());
                    }catch(Exception e){
                        System.out.println(i.userName+" was not added to newUser");
                    }
                }
            }
            void deleteUser(ActiveUser uesrForDel){
                users.remove(users.indexOf(uesrForDel));
                for(ActiveUser i : users){
                    try{
                        i.os.write("del".getBytes());
                        i.os.write(uesrForDel.userName.getBytes());
                    }catch(Exception e){
                        System.out.println("delete user problem");
                    }
                }

            }
            void sendFileFrom(ActiveUser sender){
                byte[] buffer = new byte[520];//max length of filename in Windows OS - 260. 2 bytes - char size.
                try{
                    sender.is.read(buffer);
                    String fileName = new String(buffer, "UTF-8").trim();
                    System.out.println(bufferPath+fileName);
                    readFileFrom(sender, fileName);

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
                        if(count!=8192){
                            System.out.println(count);
                            sender.socket.setSoTimeout(1000);
                        }
                        fo.write(buffer, 0, count);
                        //if(count!=8192)
                         //   break;
                    }
                    //sender.socket.getSoTimeout()
                    //System.out.println(count);
                }catch(Exception e){
                    System.out.println("readFileFrom problem");
                }finally{
                    try{
                        sender.socket.setSoTimeout(0);
                    }catch(Exception e){
                        System.out.println("setSoTimeout(0) problem");
                    }
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
                            String key = new String(byteArray, "UTF-8").trim();
                            switch(key){
                                case "user":
                                    addUser(this);
                                    break;
                                case "del":
                                    deleteUser(this);
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
                        deleteUser(this);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

}

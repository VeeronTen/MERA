package meraserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
    MERAserver(){
        try{
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
            public void deleteUser(ActiveUser u){
                users.remove(users.indexOf(u));
                for(ActiveUser i : users){
                    try{
                        i.os.write("del".getBytes());
                        i.os.write(u.userName.getBytes());
                    }catch(Exception e){
                        System.out.println("delete user problem");
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
                                    System.out.println("del");
                                    ;
                                    break;
                                case "file":
                                    System.out.println("file");
                                    ;
                                    break;
                                default:
                                    System.out.println("def");
                                    break;
                            }
                        }
                    }catch(Exception e){
                        System.out.println("1");
                        deleteUser(this);
                        System.out.println("2");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

}

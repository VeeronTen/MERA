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
                        users.addNewUser(serverSocket.accept());
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

            public void addNewUser(Socket socket){
                ActiveUser newUser = new ActiveUser(socket);
                users.add(newUser);
                new Thread(newUser).start();
            }
            public void deleteUser(ActiveUser a){
                ;
            }

            class ActiveUser implements Runnable{
                String userName;
                Socket socket;
                BufferedInputStream is;
                BufferedOutputStream os;
                ActiveUser(Socket userSocket){
                    try{
                        socket = userSocket;
                        is = new BufferedInputStream(socket.getInputStream());
                        os = new BufferedOutputStream(socket.getOutputStream());
                    }catch(Exception e){
                        System.out.println("ActiveUser Constructor Error");
                    }
                }
                public void run(){
                    try{
                        byte[] byteArray = new byte[4];//count/2=length
                        is.read(byteArray);
                        String key = new String(byteArray, "UTF-8").trim();
                        switch(key){
                            case "user":
                                System.out.println("user");;
                                break;
                            case "del":
                                ;
                                break;
                            case "file":
                                ;
                                break;
                        }
                    }catch(Exception e){
                        System.out.println("ReadKeyException");
                    }
                }
            }
        }

}

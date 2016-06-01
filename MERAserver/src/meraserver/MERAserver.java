package meraserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class MERAserver implements Runnable{
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
    public static void main(String[] args) {
       new Thread(new MERAserver()).start();
    }
    public void run(){
        while(true){
            try{
                users.newConnection(serverSocket.accept());
            }catch(Exception e){
                System.out.println("serverSocket create error");
            }
        }
    }


        class UserList{
            private LinkedList<ActiveUser> users;

            UserList() {
                users = new LinkedList<ActiveUser>();
            }

            void newConnection(Socket socket){
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

                for(ActiveUser i: users)
                    if(i.userName.equals(newUser.userName))
                        try {
                            newUser.socket.close();
                            Thread.currentThread().interrupt();
                            return;
                        } catch (IOException ex) {
                            System.out.println("Socket can't close. why? i do not know");
                        }


                users.add(newUser);
                for(ActiveUser i:users)
                    if(i!=newUser)
                        i.writeAboutNewUser(newUser.userName);



                for(ActiveUser i:users)
                        newUser.writeAboutNewUser(i.userName);
            }
            void deleteUserFromUsers(ActiveUser userForDel){
                users.remove(users.indexOf(userForDel));
                for(ActiveUser i : users)
                        i.writeAboutDelUser(userForDel.userName);
            }

            void sendFileEventFrom(ActiveUser sender){
                byte[] Buffer = new byte[520];//max length of filename in Windows OS - 260. 2 bytes - char size.
                try{
                    sender.is.read(Buffer);
                    String fileName = new String(Buffer, "UTF-8").trim();
                    System.out.println(bufferPath+fileName);
                    readFileFrom(sender, fileName);

                    for(ActiveUser i : users)
                        i.writeAboutNewFile(sender.userName, fileName);

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
                    System.out.println("file was been unloaded");
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
                        while(!Thread.currentThread().isInterrupted()){
                            System.out.println(userName+"dodo");
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
                                    sendFileEventFrom(this);
                                    break;
                                case "acpt":
                                    writeFile();
                                    break;
                                default:
                                    System.out.println("def");
                                    break;
                            }
                        }
                    }catch(Exception e){
                        System.out.println("ddeell");
                        deleteUserFromUsers(this);
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

                synchronized void writeAboutDelUser(String nameDelUser){
                    try{
                   os.write("delt".getBytes());
                   os.write(nameDelUser.getBytes());
                    }catch(Exception e){
                        System.out.println(userName+": writeAboutDelUser problem");
                    }
                }

                synchronized void writeAboutNewFile(String nameSender, String nameNewFile){
                     try{
                     //if(i!=sender){
                    byte[] nameWithSpace = new byte[520];

                    byte[] name = nameNewFile.getBytes();
                    byte[] space = new byte[520-nameNewFile.getBytes().length];

                    System.arraycopy(name, 0, nameWithSpace, 0, name.length);
                    System.arraycopy(space, 0, nameWithSpace, name.length, space.length);

                        os.write("file".getBytes());
                        os.write(nameWithSpace);
                        os.write(nameSender.getBytes());
                     }catch(Exception e){
                         System.out.println(userName+": writeAboutNewFile");
                     }
                 }

                synchronized private void writeFile(){
                    System.out.println("WRITE");
                    try{
                        byte[] buffer = new byte[520];
                        is.read(buffer);
                        String fileName = new String(buffer,"UTF-8").trim();
                        BufferedInputStream fileLoader = new BufferedInputStream(new FileInputStream(bufferPath+fileName));

                        os.write("acpt".getBytes());
                        os.write(buffer);
                        int count;
                        buffer = new byte[8192];

                        while((count=fileLoader.read(buffer))!=-1)
                            os.write(buffer, 0, count);

                    }catch(Exception e){
                        System.out.println("writeFile problem");
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {;}
                }
            }
        }
}

package meraclient;
//когда оправляем файл - запретить читать файл
//при отключнии ресетить все

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MERAclient extends Application{
    Socket socket;

    InputStream is;
    OutputStream os;

    File unLoadFile;
    File downLoadDirectory;

    String userName;
    String ip ="localhost";

    VBox usersVB;
    VBox eventsVB;

    usersInListManager MNGusers = new usersInListManager();

    Boolean connected = false;

    public void start(Stage primaryStage){
        String badStyle = "-fx-base: salmon";
        String goodStyle = "-fx-base: lightgreen";

        usersVB = new VBox();

        TextField userNameTF = new TextField();//подсказку
        HBox ipHBmain = new HBox();
            TextField ipTF = new TextField("localhost");//подсказку
            Button ipBTN = new Button("Подключиться");
            ipHBmain.getChildren().addAll(ipTF, ipBTN);

        VBox unLoadVBmain = new VBox();
            Label unLoadPathLBL = new Label("Выберите файл для отправки");
            HBox unLoadHB = new HBox();
                Button unLoadChoiceBTN = new Button("Выбрать");
                Button unLoadSendBTN = new Button("Отправить");
                unLoadHB.getChildren().addAll(unLoadChoiceBTN, unLoadSendBTN);
            unLoadVBmain.getChildren().addAll(unLoadPathLBL, unLoadHB);
            //unLoadVBmain.setDisable(true);

        VBox downLoadVBmain = new VBox();
            Label downLoadPathLBL = new Label("Выберите директорию для сохранения");
            Button downLoadChoiceBTN = new Button("Выбрать");
            downLoadVBmain.getChildren().addAll(downLoadPathLBL, downLoadChoiceBTN);
            //downLoadVBmain.setDisable(true);

        eventsVB = new VBox();

        GridPane root = new GridPane();
            root.setPadding(new Insets(10, 10, 10, 10));
            root.setHgap(5);
            root.setVgap(30);

        GridPane.setConstraints(userNameTF, 0, 0); GridPane.setConstraints(usersVB, 1, 0);
        GridPane.setConstraints(ipHBmain, 0, 1);
        GridPane.setConstraints(unLoadVBmain, 0, 2);
        GridPane.setConstraints(downLoadVBmain, 0, 3);
        GridPane.setConstraints(eventsVB, 0, 4);
        root.getChildren().addAll(usersVB, userNameTF, ipHBmain, unLoadVBmain, downLoadVBmain, eventsVB);



        userNameTF.textProperty().addListener(event->{
            userName = userNameTF.getText();
        });

        ipTF.textProperty().addListener(event->{
            ip = ipTF.getText();
        });

        ipBTN.setOnAction(event->{//при переподключении должно все ресетаться
            connect();
            if(connected){
                ipTF.setStyle(goodStyle);
                unLoadVBmain.setDisable(false);
                    //unLoadSendBTN.setDisable(true);
                downLoadVBmain.setDisable(false);
            }else{
                ipTF.setStyle(badStyle);
                unLoadVBmain.setDisable(true);
                downLoadVBmain.setDisable(true);
            }
        });

        unLoadChoiceBTN.setOnAction(event->{
            new FilesManager("unload", unLoadPathLBL);
        });

        unLoadPathLBL.textProperty().addListener(event->{
            unLoadFile = new File(unLoadPathLBL.getText());
        });

        unLoadSendBTN.setOnAction(event->{
            new Thread(new Runnable() {
                public void run() {
                    sendFile();
                }
            }).start();
        });

        downLoadChoiceBTN.setOnAction(event->{
            new FilesManager("download", downLoadPathLBL);
        });
        downLoadPathLBL.textProperty().addListener(event->{
            downLoadDirectory = new File(downLoadPathLBL.getText());
        });
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }

    void connect(){
        try {
                socket = new Socket(ip, 8080);
                is =  socket.getInputStream();
                os =  socket.getOutputStream();

                os.write("user".getBytes());
                os.write(userName.getBytes());

                connected=true;

                Thread conThread = new Thread(new Connection());
                conThread.setDaemon(true);
                conThread.start();

            } catch (IOException ex) {
                System.out.println("Connection problem");
            }
    }

    void sendFile(){
        try{
            os.write("file".getBytes());
            byte[] nameWithSpace = new byte[520];

            byte[] name = unLoadFile.getName().getBytes();
            byte[] space = new byte[520-unLoadFile.getName().length()];

            System.arraycopy(name, 0, nameWithSpace, 0, name.length);
            System.arraycopy(space, 0, nameWithSpace, name.length, space.length);

            os.write(nameWithSpace);
            BufferedInputStream fileLoader = new BufferedInputStream(new FileInputStream(unLoadFile));

            int count;
            byte[] buffer = new byte[8192];

            while((count=fileLoader.read(buffer))!=-1)
                os.write(buffer, 0, count);

        }catch(Exception e){
            System.out.println("sendFile problem");
        }
    }

    void readyRead(String fileName){
        try{
        os.write("acpt".getBytes());

        byte[] buffer = new byte[520];

        byte[] name = fileName.getBytes();
        byte[] space = new byte[520-fileName.getBytes().length];

        System.arraycopy(name, 0, buffer, 0, name.length);
        System.arraycopy(space, 0, buffer, name.length, space.length);

        os.write(buffer);
        }catch(Exception e){
            System.out.println("readyRead problem");
        }
    }
    void readFile(){


        byte[] buffer = new byte[520];
        String fileName;
        try{
            is.read(buffer);
            fileName = new String(buffer, "UTF-8").trim();
                try{
                    int count;
                    buffer = new byte[8192];
                    System.out.println("sssss");
                    FileOutputStream fo = new FileOutputStream(downLoadDirectory+"\\"+fileName);
                    while((count = is.read(buffer))!=-1){
                        System.out.println(count);{
                        if(socket.getSoTimeout()==0 && count!=8192){
                            System.out.println("setSoTimeout");
                            socket.setSoTimeout(1000);
                        }
                        fo.write(buffer, 0, count);
                    }}
                }catch(SocketTimeoutException se){
                    System.out.println("file was been downloaded");
                }catch(Exception e){
                    System.out.println("read pr1oblem");
                }
                finally{
                    try{
                        System.out.println("0");
                        socket.setSoTimeout(0);
                    }catch(Exception e){
                        System.out.println("setSoTimeout(0) problem");
                    }
                }
        }catch(Exception e){
            System.out.println("readFile  problem");
        }
    }

    class Connection implements Runnable{
        public void run(){
            byte[] byteArray;
            while(!Thread.currentThread().isInterrupted()){
                try{
                    byteArray = new byte[4];//count/2=length
                    is.read(byteArray);
                    String key = new String(byteArray, "UTF-8");//.trim();
                    switch(key){
                        case "user":
                            byteArray = new byte[20];
                            is.read(byteArray);
                            String newUserName = new String(byteArray, "UTF-8").trim();
                            MNGusers.newUser(newUserName);
                            break;
                        case "delt":
                            byteArray = new byte[20];
                            is.read(byteArray);
                            String delName = new String(byteArray, "UTF-8").trim();
                            MNGusers.delUser(delName);
                            break;
                        case "file":
                            String name;
                            String file;
                            System.out.println("file");

                            byteArray = new byte[520];
                            is.read(byteArray);
                            file = new String(byteArray, "UTF-8").trim();

                            byteArray = new byte[20];
                            is.read(byteArray);
                            name = new String(byteArray, "UTF-8").trim();

                            new FileEvent(name, file);
                            break;
                        case "acpt":
                            readFile();
                            break;
                        default:
                            Thread.currentThread().interrupt();
                            System.out.println("reset");
                            break;
                    }
                }catch(Exception e){
                    System.out.println("ReadKeyException");
                }
            }
        }
    }

    class FileEvent{

        FileEvent(String userName, String fileName){
            HBox eventHB = new HBox();
            Label nameLBL = new Label(userName);
            Button okBTN = new Button(fileName);
            Button skipBTN = new Button("Отказаться");

            eventHB.getChildren().addAll(nameLBL, okBTN, skipBTN);

            okBTN.setOnAction(event->{
                    readyRead(fileName);
            });
            skipBTN.setOnAction(event->{
                delEvent(userName, fileName);
            });
            Platform.runLater(new Runnable(){
                public void run(){
                    try{
                        eventsVB.getChildren().add(eventHB);
                    }catch(Exception e){
                        System.out.println("fileEventsManager.newEvent() problem");
                    }
                }
            });
            System.out.println(userName+" "+fileName);
        }
        private void delEvent(String userName, String fileName){
        Object[] events = eventsVB.getChildren().toArray();
            for(Object o : events)
                if(((Label)((HBox)o).getChildren().get(0)).getText().equals(userName))
                    if(((Button)((HBox)o).getChildren().get(1)).getText().equals(fileName)){
                        HBox delHB = (HBox)o;
                        Platform.runLater(new Runnable(){
                            public void run(){
                                try{
                                    eventsVB.getChildren().remove(o);
                                }catch(Exception e){
                                    ;
                                }
                            }
                        });
                    }
        }

    }

    class usersInListManager{
        void newUser(String name){
            System.out.println(userName +" add "+name);
            Label nameLBL = new Label(name);
            Platform.runLater(new Runnable(){
                public void run(){
                    try{
                        usersVB.getChildren().add(nameLBL);
                    }catch(Exception e){
                        System.out.println("usersInListManager.newUser() problem");
                    }
                }
            });
        }
        void delUser(String name){
            Object[] users = usersVB.getChildren().toArray();
                for(Object o : users)
                    if(((Label)o).getText().equals(name)){
                        Label delLabel =(Label)o;

                        Platform.runLater(new Runnable(){
                            public void run(){
                                try{
                                    usersVB.getChildren().remove(delLabel);
                                }catch(Exception e){
                                    ;
                                }
                            }
                        });
                    }
        }
    }

    void resetGUI(){
        ;
    }
}

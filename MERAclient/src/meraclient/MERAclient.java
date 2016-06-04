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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    Button unLoadSendBTN;

    TextField userNameTF;
    TextField ipTF;
    Button loginBTN;
    Button logoutBTN;

    HBox onlineHB;

    usersInListManager MNGusers = new usersInListManager();

    public void start(Stage primaryStage){
        String badStyle = "-fx-base: salmon";
        String goodStyle = "-fx-base: lightgreen";
        String backStyle = "-fx-base: thistle";

        VBox logVBmain = new VBox();
            userNameTF = new TextField();
                userNameTF.setPromptText("Username");
                userNameTF.setMaxWidth(100);
            ipTF = new TextField("localhost");
                ipTF.setPromptText("Server IP");
                ipTF.setMaxWidth(100);
            HBox logHB = new HBox();
                loginBTN = new Button("Login");
                    loginBTN.setDisable(true);
                logoutBTN = new Button("Logout");
                    logoutBTN.setDisable(true);
                logHB.getChildren().addAll(loginBTN, logoutBTN);
            logVBmain.getChildren().addAll(userNameTF, ipTF, logHB);

        VBox unLoadVBmain = new VBox();
            Label unLoadPathLBL = new Label("File for send");
            HBox unLoadHB = new HBox();
                Button unLoadChoiceBTN = new Button("Choose");
                unLoadSendBTN = new Button("Send");
                    unLoadSendBTN.setDisable(true);
                unLoadHB.getChildren().addAll(unLoadChoiceBTN, unLoadSendBTN);
            unLoadVBmain.getChildren().addAll(unLoadPathLBL, unLoadHB);

        VBox downLoadVBmain = new VBox();
            Label downLoadPathLBL = new Label("Directory for download");
            Button downLoadChoiceBTN = new Button("Choose");
            downLoadVBmain.getChildren().addAll(downLoadPathLBL, downLoadChoiceBTN);

        onlineHB = new HBox(20);
            VBox usersVBwithLBL = new VBox(10);
                Label usersLBL = new Label("Users OnLine");
                usersVB = new VBox();
                usersVBwithLBL.getChildren().addAll(usersLBL, usersVB);
            VBox eventsVBwithLBL = new VBox(10);
                Label eventsLBL = new Label("Files for download");
                eventsVB = new VBox();
                eventsVBwithLBL.getChildren().addAll(eventsLBL, eventsVB);
            onlineHB.getChildren().addAll(usersVBwithLBL, eventsVBwithLBL);
            onlineHB.setVisible(false);

        HBox root = new HBox(10);
            VBox mainVB = new VBox(30);
                mainVB.getChildren().addAll(logVBmain, unLoadVBmain, downLoadVBmain);
            root.getChildren().addAll(mainVB, onlineHB);
            root.setStyle(backStyle);



        userNameTF.textProperty().addListener(event->{
            String s = userNameTF.getText();
            if(s.length()>10){
                s = s.substring(0,10);
                userNameTF.setText(s);
            }
            s=s.trim();
            if(s.length()>0){
                loginBTN.setDisable(false);
                userName=s;
            }
            else loginBTN.setDisable(true);
        });

        ipTF.textProperty().addListener(event->{
            ip = ipTF.getText();
        });

        loginBTN.setOnAction(event->{
            try {
                socket = new Socket(ip, 8080);
                is =  socket.getInputStream();
                os =  socket.getOutputStream();

                os.write("user".getBytes());
                os.write(userName.getBytes());

                Thread conThread = new Thread(new Connection());
                conThread.setDaemon(true);
                conThread.start();

                ipTF.setStyle(goodStyle);
                userNameTF.setDisable(true);
                ipTF.setDisable(true);
                ipTF.setStyle(goodStyle);
                loginBTN.setDisable(true);
                logoutBTN.setDisable(false);
                unLoadSendBTN.setDisable(false);

                onlineHB.setVisible(true);
            } catch (IOException ex) {
                ipTF.setStyle(badStyle);
            }
        });
        logoutBTN.setOnAction(event->{
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println("logoutBTN problem");
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
        primaryStage.setTitle("Posa");
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
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
                    FileOutputStream fo = new FileOutputStream(downLoadDirectory+"\\"+fileName);
                    while((count = is.read(buffer))!=-1){
                        if(socket.getSoTimeout()==0 && count!=8192)
                            socket.setSoTimeout(1000);
                        fo.write(buffer, 0, count);
                    }
                }catch(SocketTimeoutException se){
                    //System.out.println("file was been downloaded");
                }catch(Exception e){
                    System.out.println("read pr1oblem");
                }
                finally{
                    try{
                        socket.setSoTimeout(0);
                    }catch(Exception e){
                        System.out.println("setSoTimeout(0) problem");
                    }
                }
        }catch(Exception e){
            System.out.println("readFile  problem");
        }finally{
            Platform.runLater(new Runnable(){
                public void run(){
                    Object[] events = eventsVB.getChildren().toArray();
                    for(Object o : events)
                        ((Button)((HBox)o).getChildren().get(1)).setDisable(false);
                }
            });
        }
    }

    void resetGUI(){
        Platform.runLater(new Runnable(){
            public void run(){
                eventsVB.getChildren().clear();
                usersVB.getChildren().clear();

                userNameTF.setDisable(false);
                ipTF.setDisable(false);
                    ipTF.setStyle("");
                loginBTN.setDisable(false);
                logoutBTN.setDisable(true);
                unLoadSendBTN.setDisable(true);

                onlineHB.setVisible(false);
            }
        });
    }

    class Connection implements Runnable{
        public void run(){
            byte[] byteArray;
            try{
                while(!Thread.currentThread().isInterrupted()){
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
                            resetGUI();
                    }
                }
            }catch(Exception e){
                resetGUI();
            }
        }
    }
    class FileEvent{
        FileEvent(String userName, String fileName){
            HBox eventHB = new HBox();
                Button okBTN = new Button(userName+": "+fileName);;
                Button skipBTN = new Button("Skip");
                eventHB.getChildren().addAll(okBTN, skipBTN);
            okBTN.setOnAction(event->{
                Object[] events = eventsVB.getChildren().toArray();
                delEvent(userName, fileName);
                for(Object o : events)
                    ((Button)((HBox)o).getChildren().get(1)).setDisable(true);
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
        }
        private void delEvent(String userName, String fileName){
        Object[] events = eventsVB.getChildren().toArray();
            for(Object o : events)
                if(((Button)((HBox)o).getChildren().get(1)).getText().equals(userName+": "+fileName)){
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
}

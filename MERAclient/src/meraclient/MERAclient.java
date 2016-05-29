package meraclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    //BufferedInputStream fis;
    //BufferedOutputStream fos;

    File upLoadFile;
    File downLoadDirectory;
    String userName;
    String ip ="localhost";

    VBox eventVB;

    Boolean connected = false;

    public void start(Stage primaryStage){
        String badStyle = "-fx-base: salmon";
        String goodStyle = "-fx-base: lightgreen";

        eventVB = new VBox();

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

        GridPane root = new GridPane();
            root.setPadding(new Insets(10, 10, 10, 10));
            root.setHgap(5);
            root.setVgap(30);

        GridPane.setConstraints(userNameTF, 0, 0); GridPane.setConstraints(eventVB, 1, 0);
        GridPane.setConstraints(ipHBmain, 0, 1);
        GridPane.setConstraints(unLoadVBmain, 0, 2);
        GridPane.setConstraints(downLoadVBmain, 0, 3);


        root.getChildren().addAll(eventVB, userNameTF, ipHBmain, unLoadVBmain, downLoadVBmain);
////////////////////////////////////////////////////////////////////////////
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
            unLoadManager manager = new unLoadManager(unLoadPathLBL);
        });

        unLoadPathLBL.textProperty().addListener(event->{
            upLoadFile = new File(unLoadPathLBL.getText());
        });

        unLoadSendBTN.setOnAction(event->{
            sendFile();
        });

        downLoadChoiceBTN.setOnAction(event->{
            downLoadManager manager = new downLoadManager(downLoadPathLBL);
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
            os.write("first first, i am the second".getBytes());
            //os.write(upLoadFile.getName().getBytes());
        }catch(Exception e){
            System.out.println("sendFile problem");
        }
    }
    class Connection implements Runnable{
        public void run(){
            byte[] byteArray;
            while(true){
                    try{
                        byteArray = new byte[4];//count/2=length
                        is.read(byteArray);
                        String key = new String(byteArray, "UTF-8").trim();
                        switch(key){
                            case "user":
                                byteArray = new byte[20];
                                is.read(byteArray);
                                Label newLabel = new Label(new String(byteArray, "UTF-8").trim());

                                Platform.runLater(new Runnable(){
                                    public void run(){
                                        try{
                                            eventVB.getChildren().add(newLabel);
                                        }catch(Exception e){
                                            ;
                                        }
                                    }
                                });

                                break;
                            case "del":
                                byteArray = new byte[20];
                                is.read(byteArray);
                                String delName = new String(byteArray, "UTF-8").trim();

                                Object[] events = eventVB.getChildren().toArray();
                                for(Object o : events)
                                    if(((Label)o).getText().equals(delName)){
                                        Label delLabel =(Label)o;

                                        Platform.runLater(new Runnable(){
                                            public void run(){
                                                try{
                                                    eventVB.getChildren().remove(delLabel);
                                                }catch(Exception e){
                                                    ;
                                                }
                                            }
                                        });
                                    }
                                break;
                            case "file":
                                System.out.println("file");
                                break;
                            default:
                                System.out.println("def");
                                break;
                        }
                    }catch(Exception e){
                        System.out.println("ReadKeyException");
                    }
            }
        }
    }

}

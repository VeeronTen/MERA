package meraclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
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
    Socket clientSocket;
    BufferedInputStream is;
    BufferedOutputStream os;
    BufferedInputStream fis;
    BufferedOutputStream fos;
    File upLoadFile;
    File downLoadDirectory;
//    MERAclient(){
//        try{
//            clientSocket = new Socket("localhost", 8080);
//            is = new BufferedInputStream(clientSocket.getInputStream());
//            os = new BufferedOutputStream(clientSocket.getOutputStream());
//            fis = new BufferedInputStream(new FileInputStream("C:\\ICAN\\1.bmp"));
//            fos = new BufferedOutputStream(new FileOutputStream("C:\\ICAN\\22.bmp"));
//        }catch(Exception e){
//            System.out.println("Client has not been enabled");
//            exit(0);
//        }
//    }
    public void start(Stage primaryStage){
        String badStyle = "-fx-base: salmon";
        String goodStyle = "-fx-base: lightgreen";

        HBox ipHBmain = new HBox();
            TextField ipTF = new TextField("localhost");
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
        GridPane.setConstraints(ipHBmain, 0, 0);
        GridPane.setConstraints(unLoadVBmain, 0, 1);
        GridPane.setConstraints(downLoadVBmain, 0, 2);


        root.getChildren().addAll(ipHBmain, unLoadVBmain, downLoadVBmain);
////////////////////////////////////////////////////////////////////////////

        ipBTN.setOnAction(event->{//при переподключении должно все ресетаться
            try{
                clientSocket = new Socket(ipTF.getText(), 8080);
                ipTF.setStyle(goodStyle);
                unLoadVBmain.setDisable(false);
                    unLoadSendBTN.setDisable(true);
                downLoadVBmain.setDisable(false);
            }catch (IOException ex){
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


        downLoadChoiceBTN.setOnAction(event->{
            downLoadManager manager = new downLoadManager(downLoadPathLBL);
        });

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
//        try {
//            int count;
//            byte[] byteArray = new byte[8192];
//            while ((count = fis.read(byteArray)) != -1){
//                System.out.println(count);
//                fos.write(byteArray,0,count);
//                fos.flush();
//            }
//        } catch (IOException ex) {
//            System.out.println("apapapa");;
//    }

    }
    public static void main(String[] args) {
        launch(args);
    }

}


package meraclient;

import java.io.File;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class FilesManager {
    VBox filesVBmain;
    VBox filesVB;
    Label pathLBL;

    Stage stage;
    Label ExternalLabel;
    String presentDir;

    String file;
    String managerType;
    FilesManager(String type, Label label){
        ExternalLabel = label;
        managerType=type;

        stage = new Stage();
            if(managerType=="download")
                stage.setTitle("Download Manager");
            if(managerType=="unload")
                stage.setTitle("Unload Manager");

        presentDir="C:\\";
        file = null;
        stage = new Stage();
        start();
    }

    VBox getDirectoryView(String path){
        File dir = new File(path);
        VBox newFilesVB = new VBox();
        if(dir.listFiles()!=null){
        for(File item : dir.listFiles())
            if(item.isDirectory()){
                Button b = new Button(item.getName());
                addDirectoryEvent(b);
                b.setStyle("-fx-base:yellow");
                newFilesVB.getChildren().add(b);
            }
        if(managerType=="unload")
        for(File item : dir.listFiles())
            if(item.isFile()){
                Button b = new Button(item.getName());
                addFileEvent(b);
                newFilesVB.getChildren().add(b);
            }
        }
        presentDir=path;
        pathLBL.setText(presentDir);
        return newFilesVB;
    }

    void start(){
        filesVBmain = new VBox();
            pathLBL = new Label(presentDir);
            HBox buttonsHB = new HBox(10);
                Button upBTN = new Button("↑↑↑");
                Button chooseBTN = new Button("Выбрать");
                    if(managerType!="download")
                        chooseBTN.setDisable(true);
                buttonsHB.getChildren().addAll(upBTN, chooseBTN);
            filesVB = getDirectoryView(presentDir);
            filesVBmain.getChildren().addAll(pathLBL, buttonsHB,filesVB);

        upBTN.setOnAction(event->{
            String newPath = new File(presentDir).getParent();
            if(newPath!=null){
                filesVBmain.getChildren().remove(filesVB);
                filesVB=getDirectoryView(newPath);
                filesVBmain.getChildren().add(filesVB);
            }
        });
        chooseBTN.setOnAction(event->{
            ExternalLabel.setText(presentDir);
            stage.close();
        });
        Scene Scene = new Scene(filesVBmain);
        stage.setScene(Scene);
        stage.show();
    }

    void addDirectoryEvent(Button b){
        b.setOnAction(event->{
            filesVBmain.getChildren().remove(filesVB);
            filesVB=getDirectoryView(presentDir+b.getText()+"\\");
            filesVBmain.getChildren().add(filesVB);
        });
    }

    void addFileEvent(Button b){
        b.setOnAction(event->{
            file = presentDir+b.getText();
            ExternalLabel.setText(file);
            stage.close();
        });
    }
}

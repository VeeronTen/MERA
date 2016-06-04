
package meraclient;

import java.io.File;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class FilesManager {
    HBox filesHB;
    Label pathLBL;

    VBox root;

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

    HBox getDirectoryView(String path){
        File dir = new File(path);
        HBox result = new HBox();
        if(dir.listFiles()!=null){
        result = new HBox();
            VBox dirVB = new VBox();
                Label dirLBL = new Label("Directories");
                VBox dirContent = new VBox();
                dirVB.getChildren().addAll(dirLBL, dirContent);
            VBox filesVB = new VBox();
                Label filesLBL = new Label("Files");
                VBox filesContent = new VBox();
                filesVB.getChildren().addAll(filesLBL, filesContent);
            result.getChildren().addAll(dirVB, filesVB);


        for(File item : dir.listFiles())
            if(item.isDirectory()){
                Button b = new Button(item.getName());
                addDirectoryEvent(b);
                b.setStyle("-fx-base:yellow");
                dirContent.getChildren().add(b);
            }
        if(managerType=="unload")
        for(File item : dir.listFiles())
            if(item.isFile()){
                Button b = new Button(item.getName());
                addFileEvent(b);
                filesContent.getChildren().add(b);
            }
        }
        presentDir=path;
        pathLBL.setText(presentDir);
        return result;
    }

    void start(){
        String backStyle = "-fx-base: thistle";
        root = new VBox(10);
            VBox pathVB = new VBox();
                pathLBL = new Label(presentDir);
                HBox buttonsHB = new HBox();
                    Button upBTN = new Button("Up");
                    Button chooseBTN = new Button("Choose");
                        if(managerType!="download")
                            chooseBTN.setDisable(true);
                    buttonsHB.getChildren().addAll(upBTN, chooseBTN);
                pathVB.getChildren().addAll(pathLBL, buttonsHB);
            filesHB = getDirectoryView(presentDir);
            root.getChildren().addAll(pathVB,filesHB);
            root.setStyle(backStyle);

        upBTN.setOnAction(event->{
            String newPath = new File(presentDir).getParent();
            if(newPath!=null){
                root.getChildren().remove(filesHB);
                filesHB=getDirectoryView(newPath);
                root.getChildren().add(filesHB);
            }
        });
        chooseBTN.setOnAction(event->{
            ExternalLabel.setText(presentDir);
            stage.close();
        });
        Scene Scene = new Scene(root);
        stage.setScene(Scene);
        stage.show();
    }

    void addDirectoryEvent(Button b){
        b.setOnAction(event->{
            root.getChildren().remove(filesHB);
            filesHB=getDirectoryView(presentDir+b.getText()+"\\");
            root.getChildren().add(filesHB);
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

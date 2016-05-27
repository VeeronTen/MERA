package meraclient;
import java.io.File;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class downLoadManager {
    VBox filesVBmain;
    VBox filesVB;
    Label pathLBL;

    Stage stage;
    Label ExternalLabel;
    String presentDir;

    public downLoadManager(Label label){
        ExternalLabel = label;
        stage = new Stage();
            stage.setTitle("Download Manager");
        presentDir="C:\\";
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
        }
        presentDir=path;
        pathLBL.setText(presentDir);
        return newFilesVB;
    }

    void start(){
        filesVBmain = new VBox();
            HBox pathVB = new HBox(10);
                Button upBTN = new Button("↑↑↑");
                Button chooseBTN = new Button("Выбрать");
                pathLBL = new Label(presentDir);
                pathVB.getChildren().addAll(upBTN, chooseBTN, pathLBL);
            filesVB = getDirectoryView(presentDir);
            filesVBmain.getChildren().addAll(pathVB,filesVB);

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
}

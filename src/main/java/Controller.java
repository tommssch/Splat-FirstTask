import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

@SuppressWarnings("unchecked")
public class Controller
{
    @FXML
    public TextField searchText;
    @FXML
    public TextField dir;
    @FXML
    public TextField ext;
    @FXML
    public TreeView table;
    @FXML
    public Pane main;
    @FXML
    public AnchorPane tree;

    @FXML
    private  void initialize(){
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println(newValue.toString());
            }
        });
    }
    public void clearButton(ActionEvent actionEvent){searchText.clear();}

    public void browseDir(ActionEvent actionEvent) {

        dir.clear();
        DirectoryChooser browse=new DirectoryChooser();
        browse.setTitle("Choose Directory");
        dir.appendText(browse.showDialog(new Stage()).getAbsolutePath());
    }
    public void startFind(ActionEvent actionEvent) throws IOException {

      Path testpath= Paths.get(dir.getText());
      Finder finder=new Finder(testpath,ext.getText(),searchText.getText());
      Files.walkFileTree(testpath,finder);
      fillTree(finder);
    }
    public void fillTree(Finder finder1){
        TreeItem root= new TreeItem(dir.getText()+"(ROOT)");
        root.setExpanded(true);
        for(int i=0;i<finder1.getFiles_matched().size();i++)
        {
            String st=finder1.getFiles_matched().get(i).getName_of_file().replace(dir.getText()+"\\","");
            String[] children=st.split("\\\\");
            root.getChildren().add(new TreeItem(children[0]));
            TreeItem treeItem=(TreeItem)root.getChildren().get(i);
            treeItem.setExpanded(true);
            for(int j=1;j<children.length;j++)
            {
               treeItem.getChildren().add(new TreeItem(children[j]));
               treeItem=(TreeItem)treeItem.getChildren().get(0);
               treeItem.setExpanded(true);
            }
        }
        table.setRoot(root);
    }
}
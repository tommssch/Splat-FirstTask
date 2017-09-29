import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public Button starter;
    @FXML
    public TextArea output;

    private Finder finder;
    private int start_caret;
    private int end_caret;

    @FXML
    private  void initialize(){
        addListener();
    }
    public void clearButton(ActionEvent actionEvent){searchText.clear();}

    public void browseDir(ActionEvent actionEvent) {

        dir.clear();
        DirectoryChooser browse=new DirectoryChooser();
        browse.setTitle("Choose Directory");
        File selected=browse.showDialog(new Stage());
        dir.appendText(selected!=null?selected.getAbsolutePath():"");
    }

    public void startFind(ActionEvent actionEvent) throws IOException {

        output.clear();
        Path path= Paths.get(dir.getText());
        String exten=ext.getText().equals("")?ext.getPromptText():ext.getText();
        finder = new Finder(path, exten, searchText.getText());
        Task task=new Task() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        starter.setDisable(true);
                        table.setDisable(true);
                        try {
                            Files.walkFileTree(path, finder);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fillTree(finder);
                        table.setDisable(false);
                        starter.setDisable(false);
                    }
                });

                return null;
            }
        };
        Thread th= new Thread(task);
        th.start();
    }
    private void fillTree(Finder finder1){
        String s;
        if(dir.getText().charAt(dir.getText().length()-1)=='\\')
            s=dir.getText().replace("\\","");
        else
            s=dir.getText();

        TreeItem root= new TreeItem(dir.getText());
        root.setExpanded(true);
        for(int i=0;i<finder1.getFiles_matched().size();i++)
        {
            String st=finder1.getFiles_matched().get(i).getName_of_file().replace(s+"\\","");
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
    private void addListener(){
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TreeItem<String> selected = (TreeItem<String>) newValue;
                if(newValue != null)
                    if(selected.getChildren().size()==0) {
                    start_caret=0;
                    end_caret=0;
                        StringBuilder abspath = new StringBuilder(selected.getValue());
                   /* while(selected.getChildren().size()!=0) {
                    abspath.append("\\").append(selected.getChildren().get(0).getValue());
                    selected=selected.getChildren().get(0);
                }
                    selected=(TreeItem<String>)newValue;*/

                        while (selected.getParent() != null) {
                            abspath.insert(0, selected.getParent().getValue() + "\\");
                            selected = selected.getParent();
                        }
                        try(BufferedReader br=new BufferedReader(new FileReader(abspath.toString()))) {
                            String s;
                            StringBuilder str= new StringBuilder();
                            while ((s=br.readLine())!=null)
                                str.append(s).append('\n');
                            output.setText(str.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });
    }

    public void selectForward(ActionEvent actionEvent) {

         start_caret=output.getText().indexOf(searchText.getText(),end_caret);
         if(start_caret==-1)
             start_caret=output.getText().indexOf(searchText.getText(),0);
         end_caret= start_caret+searchText.getText().length();
         output.selectRange(start_caret,end_caret);
    }

    public void selectAll(ActionEvent actionEvent) {

        while(start_caret!=-1) {
            start_caret = output.getText().indexOf(searchText.getText(), end_caret);
            end_caret = start_caret + searchText.getText().length();
            output.selectRange(start_caret, end_caret);
        }

    }
    public void selectBack(ActionEvent actionEvent) {

        start_caret= output.getText().lastIndexOf(searchText.getText(),start_caret-1);
        if(start_caret==-1)
            start_caret=output.getText().lastIndexOf(searchText.getText());
        end_caret= start_caret+searchText.getText().length();
        output.selectRange(start_caret,end_caret);
    }
}
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.*;
//Запуск сцены с иницилизацией табфолдера
public class Main extends Application {
   private int i=1;
   private TabPane tabPane;
    @Override
    public void start(Stage primaryStage) throws Exception {
        tabPane= new TabPane();
        tabPane.setPrefSize(1200,800);
        initAddButton();
        addTabs();
        AnchorPane box= new AnchorPane(tabPane);
        primaryStage.setTitle("Finder 3000");
        primaryStage.setScene(new Scene(new Pane(box)));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

    //Метод по добавлению табов
    private void addTabs() throws IOException {
        Pane root= FXMLLoader.load(getClass().getResource("interface.fxml"));
        Tab tab=new Tab("Tab "+String.valueOf(i++));
        tab.setContent(root);
        tab.setOnCloseRequest(ev->{
            if((tabPane.getTabs().size()!=2))
                tabPane.getTabs().remove(tab);
            for(int j=0;j<tabPane.getTabs().size()-1;j++)
                    tabPane.getTabs().get(j).setText("Tab " + String.valueOf(j + 1));

            i = tabPane.getTabs().size();
            ev.consume();
        });
        Tab tb=tabPane.getTabs().get(tabPane.getTabs().size()-1);
        tabPane.getTabs().set(tabPane.getTabs().size()-1,tab);
        tabPane.getTabs().add(tb);
    }
    //Инициализация кнопки по добавлению табов
    private void initAddButton(){
        Tab add_tab=new Tab();
        add_tab.setClosable(false);
        Button add=new Button("+");
        add.setStyle("\n" +
                "    -fx-border-width: 0;\n" +
                "    -fx-background-radius: 0;\n"+
                "    -fx-background-color: transparent;\n" );
        add.setOnAction(ae->{
            try {
                addTabs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        add_tab.setGraphic(add);
        add_tab.setDisable(true);
        tabPane.getTabs().add(add_tab);
    }
}

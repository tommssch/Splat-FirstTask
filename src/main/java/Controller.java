import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Класс обрабатывающий запросы от пользователя
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
    public AnchorPane out_text;
    @FXML
    public Button forwrd;
    @FXML
    public Button all;
    @FXML
    public Button backwrd;

    private InlineCssTextArea textArea;
    private Finder finder;
    private int start_caret;
    private int end_caret;
    private BufferedReader bufread;
    private BufferedReader bufread1;
    private long num_of_lines;
    private StringBuilder abspath;
    private AnimationTimer at;
    private StringBuilder buffer;
    private ArrayList<Long> marks;
    private int position_up;
    private long symbols;

    @FXML
    public void initialize(){
        all.addEventHandler(MouseEvent.MOUSE_CLICKED,event -> {
            if(event.getButton().equals(MouseButton.SECONDARY)) {
                at.stop();
                end_caret=0;
                start_caret=0;
            } });
        marks=new ArrayList<>();
        turnButtons(true);
        initTextArea();
        addListener();
    }
    private void turnButtons(boolean key){
        all.setDisable(key);
        backwrd.setDisable(key);
        forwrd.setDisable(key);
    }
    public void clearButton(){searchText.clear();}

    public void browseDir() {

        dir.clear();
        DirectoryChooser browse=new DirectoryChooser();
        browse.setTitle("Choose Directory");
        File selected=browse.showDialog(new Stage());
        dir.appendText(selected!=null?selected.getAbsolutePath():"");
    }

    public void startFind() {
        turnButtons(true);
        if(checkFields()&&checkDir()) {
            textArea.clear();
            startThread();
        }
    }
    //метод по поиску файлов
    private void startThread(){
        Path path = Paths.get(dir.getText());
        String exten = ext.getText().equals("") ? ext.getPromptText() : ext.getText();
        finder = new Finder(path, exten, searchText.getText());
        starter.setDisable(true);
        table.setDisable(true);
        Service<Void> service= new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            Files.walkFileTree(path, finder);
                            finder.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Platform.runLater(()-> table.setRoot(fillTree(finder)));

                        table.setDisable(false);
                        starter.setDisable(false);
                        return null;
                    }};}};
        service.start();
    }
    //метод по заполнению TreeView результами поиска
    private TreeItem fillTree(Finder finder){
        String s;
        if(dir.getText().charAt(dir.getText().length()-1)=='\\')
            s=dir.getText().replace("\\","");
        else
            s=dir.getText();

        TreeItem root= new TreeItem(dir.getText());
        root.setExpanded(true);
        TreeItem item;
        for(int i=0;i<finder.getFiles_matched().size();i++)
        {   String st=finder.getFiles_matched().get(i).getName_of_file().replace(s+"\\","");
            String[] children=st.split("\\\\");
            if(!containsInChildren(root,children[0])) {
                item=new TreeItem(children[0]);
                item.setExpanded(true);
                root.getChildren().add(item);
            }
            else
                item=getChildByName(root,children[0]);

            for(int j=1;j<children.length;j++)
            {

                if(!containsInChildren(item,children[j])){
                    TreeItem item1=new TreeItem(children[j]);
                    item1.setExpanded(true);
                    item.getChildren().add(item1);
                    item=item1;
                }
                else
                    item=getChildByName(item,children[j]);
            }


        }
        return root;
    }
    private boolean containsInChildren(TreeItem root,String item){
        for(int i=0;i<root.getChildren().size();i++)
        {
            TreeItem it= (TreeItem) root.getChildren().get(i);
            if(it.getValue().equals(item))
                return true;
        }
        return false;
    }
    private TreeItem getChildByName(TreeItem root,String item){
        for(int i=0;i<root.getChildren().size();i++)
        {
            TreeItem it= (TreeItem) root.getChildren().get(i);
            if(it.getValue().equals(item))
                return it;
        }
        return null;
    }
    //метод по выводу содержимого файла пользователю
    private void addListener(){
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try
            {
                toStart();
                turnButtons(true);
                TreeItem<String> selected = (TreeItem<String>) newValue;
                if (newValue != null) {
                    Pattern ext = Pattern.compile("\\.\\p{L}+");
                    Matcher mat = ext.matcher(selected.getValue());
                    if ((selected.getChildren().size() == 0) && (mat.find()))
                        readFromSelectedItem(selected);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } });
    }

    private void readFromSelectedItem(TreeItem<String> selected) throws IOException {
        abspath.delete(0,abspath.length());
        abspath.append(selected.getValue());
        while (selected.getParent() != null) {
            abspath.insert(0, selected.getParent().getValue() + "\\");
            selected = selected.getParent();
        }
        marks.add(0L);
        Path path = Paths.get(abspath.toString());
        bufread =Files.newBufferedReader(path,Charset.defaultCharset());
        add100lines(bufread);
        turnButtons(false);

    }
    private void add100lines(BufferedReader bf) throws IOException {

        int i=0;
        for(String line;i<100&&(line=bf.readLine())!=null;i++) {
            if (finder.getSearch().matcher(line).find()) {
                buffer.append(line).append('\n');
                num_of_lines++;
                symbols=symbols+line.length()+2;
            }
        }
        if(buffer.length()>0) {
            marks.add(symbols);
            textArea.appendText(buffer.toString());
        }

    }
    //метод выделения искомого текста
    public void selectForward() {

        if(at!=null)
            at.stop();
        textArea.setStyle(0,textArea.getLength(), "-rtfx-background-color:white");
        start_caret=textArea.getText().indexOf(searchText.getText(),end_caret);
         if(start_caret==-1)
             start_caret=textArea.getText().indexOf(searchText.getText(),0);
         end_caret= start_caret+searchText.getText().length();
         textArea.moveTo(end_caret);
         textArea.requestFollowCaret();
         textArea.setStyle(start_caret, end_caret, "-rtfx-background-color:lightblue");

    }

    public void selectAll() {
        if(at!=null)
            at.stop();
        at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                start_caret = textArea.getText().indexOf(searchText.getText(), end_caret);
                if (start_caret != -1) {
                    end_caret = start_caret + searchText.getText().length();
                    textArea.setStyle(start_caret, end_caret, "-rtfx-background-color:lightblue");
                    textArea.moveTo(end_caret);
                    textArea.requestFollowCaret();
                }
                else
                    at.stop();
            }
        };
        at.start();
    }
    public void selectBack() {
        if(at!=null) {
            at.stop();
        }
        textArea.setStyle(0,textArea.getLength(), "-rtfx-background-color:white");
        start_caret= textArea.getText().lastIndexOf(searchText.getText(),start_caret-1);
        if(start_caret==-1)
            start_caret=textArea.getText().lastIndexOf(searchText.getText());
        end_caret= start_caret+searchText.getText().length();
        textArea.moveTo(end_caret);
        textArea.requestFollowCaret();
        textArea.setStyle(start_caret, end_caret, "-rtfx-background-color:lightblue");
    }
    private void initTextArea() {
        buffer=new StringBuilder();
        abspath=new StringBuilder();
        textArea = new InlineCssTextArea();
        VirtualizedScrollPane vsp = new VirtualizedScrollPane(textArea);
        initVsp(vsp);
        out_text.getChildren().add(vsp);
        textArea.setStyle("-fx-font-size: 20px");
        textArea.setDisable(true);
    }
    private void initVsp(VirtualizedScrollPane vsp){
        vsp.setMinSize(out_text.getPrefWidth(), out_text.getPrefHeight());
        vsp.setFocusTraversable(true);
        initVspListener(vsp);
    }
    // Слушатель для передвижение по найденному в файле.подгрузка файлов( механизм обеспечивающий считывание больших файлов)
    private void initVspListener(VirtualizedScrollPane vsp){
        vsp.addEventFilter(ScrollEvent.SCROLL,event -> {

            if(event.getTarget() instanceof VirtualizedScrollPane)
                textArea.scrollBy(-event.getDeltaX(),-event.getDeltaY());

            try {
                buffer.delete(0,buffer.length());
                if(((Double)vsp.totalHeightEstimateProperty().getValue()-383.0==vsp.getEstimatedScrollY())
                        && (event.getDeltaY()<0))
                    add100lines(bufread);
                if(vsp.getEstimatedScrollY()==0&&position_up>0&&event.getDeltaY()>0)
                {
                    if(bufread1!=null)
                        bufread1.close();
                    bufread1=Files.newBufferedReader(Paths.get(abspath.toString()),Charset.defaultCharset());
                    bufread1.skip(marks.get(position_up-1));
                    int i=0;
                    for(String line;(line=bufread1.readLine())!=null&&i<100;i++) {
                        if (finder.getSearch().matcher(line).find())
                        {
                            buffer.append(line).append('\n');
                            num_of_lines++;
                        }
                    }
                    textArea.insertText(0,buffer.toString());
                    position_up--;
                }
                //внутри TextArea может храниться только 600 строк
                if(num_of_lines>600)
                {
                    textArea.clear();
                    textArea.appendText(buffer.toString());
                    if(event.getDeltaY()<0)
                        position_up=position_up+6;

                    if(bufread!=null)
                        bufread.close();
                    bufread=Files.newBufferedReader(Paths.get(abspath.toString()),Charset.defaultCharset());
                    bufread.skip(marks.get(position_up+1));

                    start_caret=0;
                    end_caret=0;
                    num_of_lines=100;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    //методы по проверке полей
    private boolean checkFields(){
        if(dir.getText().equals("")||(searchText.getText().equals("")))
        {
            Alert alert= new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Fill empty fields");
            alert.show();
            return false;
        }
        return true;
    }
    private boolean checkDir(){
        Pattern pattern=Pattern.compile("((\\\\\\\\)((\\p{L})+[\\\\]?)+)");
        Pattern pattern1=Pattern.compile("[A-Z]:\\\\(\\p{L}+[\\\\]?)*");
        Pattern pattern2=Pattern.compile("//\\p{L}+(\\p{L}+[/]?)*");
        boolean key=pattern.matcher(dir.getText()).find()||pattern1.matcher(dir.getText()).find();
        key=key||pattern2.matcher(dir.getText()).find();
        if(key)
            return true;
        else
        {
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Wrong directory format");
            alert.show();
            return false;
        }
    }
    private void toStart() throws IOException {
        if(at!=null)
            at.stop();
        textArea.clear();
        start_caret=0;
        end_caret=0;
        symbols=0;
        num_of_lines=0;
        marks.clear();
        position_up=0;
        buffer.delete(0,buffer.length());
        if(bufread1!=null)
            bufread1.close();
        if(bufread!=null)
            bufread.close();
    }

}
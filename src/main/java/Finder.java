import lombok.Data;
import java.awt.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

//Класс реализующий механизм обхода файловой системы и поиска файлов с искомым текстом
@Data
public class Finder implements FileVisitor {
    private String search_text;
    private Pattern search;
    private Pattern ext;
    private Path dir;
    private final ArrayList<Container> files_matched;
    private ExecutorService service;

    public Finder(Path directory, String extension, String searchText) {
        ext = Pattern.compile("\\." + extension + "$");
        search = Pattern.compile("(" + searchText + ")+");
        dir = directory;
        files_matched = new ArrayList<>(1);
        this.search_text=searchText;
        service=Executors.newCachedThreadPool();
    }

    @Override
    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) {

        File[] files=new File(dir.toString()).listFiles();
        if(files==null) {
            return FileVisitResult.CONTINUE;
        }
        else
            for (final File file : files) {
                if (!file.isFile())
                    continue;

                if (ext.matcher(file.toString()).find()) {
                    System.out.println(Thread.currentThread().getName()+" "+file.getAbsolutePath());
                    service.execute(() -> {
                        synchronized (files_matched) {
                            if (matchInText(file.toString())) {
                                Container container = new Container(file.toString());
                                files_matched.add(container);
                            }
                        }
                    });
                }
            }

        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Object file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Object dir, IOException exc) {

        return FileVisitResult.CONTINUE;
    }

    private boolean matchInText(String dir){
        Path path=Paths.get(dir);
        try(BufferedReader br=Files.newBufferedReader(path,Charset.defaultCharset())){
            for(String line;(line=br.readLine())!=null;)
                if(search.matcher(line).find())
                    return true;
        }
        catch(IOException e){
            try(BufferedReader br=Files.newBufferedReader(path,StandardCharsets.UTF_8)) {
                for (String line; (line = br.readLine()) != null; )
                    if (search.matcher(line).find())
                        return true;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }
    public void close(){
        service.shutdown();
        try {
            service.awaitTermination(30,TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toolkit.getDefaultToolkit().beep();
        Collections.sort(files_matched);
    }
}

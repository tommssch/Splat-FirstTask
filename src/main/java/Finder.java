import lombok.Data;
import lombok.Getter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Finder implements FileVisitor {

    private Pattern search;
    private Pattern ext;
    private Path dir;
    private ArrayList<Container> files_matched;

    public Finder(Path directory, String extension, String searchText) {
        ext = Pattern.compile("\\." + extension + "$");
        search = Pattern.compile("(" + searchText + ")+");
        dir = directory;
        files_matched = new ArrayList<>(1);
    }

    @Override
    public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {

        if (ext.matcher(file.toString()).find()) {
            Container container = new Container(file.toString());
            if (matchInText(container.getName_of_file()))
                files_matched.add(container);
        }
        Collections.sort(files_matched);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public StringBuilder readFromFile(String dir1) throws FileNotFoundException {
        StringBuilder cont = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(dir1))) {
            String s;
            while ((s = br.readLine()) != null) {
                cont.append(s).append('\n');
            }
           /* try(FileChannel ch=new FileInputStream(dir1).getChannel()){
                byte[] barray= new byte[262144];
                ByteBuffer bb=ByteBuffer.wrap(barray);
                long checkSum=0L;
                int nRead;
                while ((nRead=ch.read(bb))!=-1){
                    for (int i=0;i<nRead;i++){
                        System.out.println(Arrays.toString(barray));
                        checkSum += barray[i];
                    }

                    bb.clear();
                }*/
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return cont;
    }

    private boolean matchInText(String dir) {
        try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (search.matcher(s).find())
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
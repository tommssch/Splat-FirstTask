import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class Container implements Comparable<Container> {
    private StringBuilder content;
    private String name_of_file;

    public Container(){
        content=new StringBuilder();
    }

    @Override
    public int compareTo(Container o) {
        return this.getName_of_file().length()- o.getName_of_file().length();
    }
}

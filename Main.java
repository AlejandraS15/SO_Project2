import com.cow.Data;
import com.cow.FileDescriptor;

public class Main {
    public static void main(String[] args) {
        Data data = new Data();
        data.create("jj.txt");
        data.create("kk.png");
        data.open("jj.txt");
        data.write("jj.txt", "jsanjnan");
        data.read("jj.txt", 5);
        data.close("jj.txt");
        
    }
}

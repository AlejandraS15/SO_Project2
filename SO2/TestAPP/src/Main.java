import com.cow.Data;
import com.cow.FileDescriptor;

public class Main {
    public static void main(String[] args) {
        Data data = new Data();
        data.create("jj.txt");
        data.create("kk.png");
        data.open("jj.txt");
        
        data.write("jj.txt", "Queso");

        data.open("kk.png");
        data.write("kk.png", "Hola");
    

        data.write("jj.txt","Arepa");

        data.write("kk.png", "Hola");
        data.write("kk.png", "Hola");
        data.read("jj.txt");
        data.read("kk.png");
        data.close("jj.txt");
        
    }
}

import java.io.IOException;
import java.io.File;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Prueba ");
        Data txt = new Data();
        
        File file = new File("archivo_14kb.txt");
        File file2 = new File("bloques_14kb.txt");
        //txt.saveDataBin(file);
        txt.saveDataBin(file2);
        int []bloques = {1,3,5,7}; //Aqui se estan realizando pruebas tomando bloques arbitrarios de ambos archivos, cada cambio de letras son 4kb
        //txt.openFileBin("test.bin", bloques);
        txt.openFileMemoryMap(bloques);

        
    }
}

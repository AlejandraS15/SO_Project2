import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Pruebita ");
        /*
              VACAMU gestor = new VACAMU();
        gestor.crear("ejemplo");
        gestor.escribir("ejemplo", 0 , "archivo de prueba");
        gestor.escribir("ejemplo", 0 ,"segunda versión del archivo");
        gestor.escribir("ejemplo", 0 ,"tercera versión con más cambios");

        for(int i = 0; i < 50; i++){
            gestor.escribir("ejemplo", 0,  "version" + i);
        }

        gestor.leerultimo("ejemplo");

        gestor.listarVersiones("ejemplo");

        gestor.leerVersion("ejemplo", "version_2025-03-21T153833949530300.txt");
        */

        String nombre1 = "Archivo";
        Data txt = new Data();
        
        txt.create();
        txt.write("HOLAAAA");
        txt.read();


    }
}

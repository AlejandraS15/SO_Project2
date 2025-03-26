import java.io.File;
import java.util.Base64;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStreamReader;


public class Data {

    public static File datatxt;
    public static File metatxt;

    Data(){
        
        datatxt = new File("Data.bin");
        metatxt = new File("Meta.txt");

        if(!datatxt.exists()){
            //datatxt.mkdir();
            try{
                datatxt.createNewFile();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        if(!metatxt.exists()){
            //datatxt.mkdir();
            try{
                metatxt.createNewFile();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    void create(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try{
            
            RandomAccessFile metawriter = new RandomAccessFile(metatxt, "rw");
            BufferedWriter datawriter = new BufferedWriter(new FileWriter(datatxt), 4096);
            //BufferedWriter metawriter = new BufferedWriter(new FileWriter(metatxt), 4096);

            //Tabla Archivos -> %ARCHIVO?INICIO/ARCHIVO2:INICIO2%
            // Informacion Sintaxis -> |ARCHIVO?ULTIMA:VERSION<FECHA>$METADATOS$|
            // RUTA -> En caso de especificar donde esta el archivo
            // ARCHIVO -> Nombre del archivo
            // ULTIMA   -> Indica en que Byte empieza la información de la ultima version
            // VERSION -> Que Version del archivo es
            // FECHA -> Fecha de esa version
            //Metadatos -> indice de bloques bytes del archivo
            // Eg -> |imagen.png?1024*4096:2<2025-03-23T09:30:45.123>$0-12-343-2342-4323$|

            char[] reservados = { '"', '$', '%', '*', '/', ':', '<', '>', '?', '\\', '|' }; //Caracteres reservados que no se podrán usar en el nombre

            System.out.println("Nombre del archivo: ");

            String nombreArchivo = br.readLine();
            
            //Verifica que el nombre del archivo no contenga un caracter reservado
            for (char c : nombreArchivo.toCharArray()) {
                for (char reservado : reservados) {
                    if (c == reservado) {
                        System.out.println("Nombre de archivo no valido, contiene alguno de estos caracteres reservados: ");
                        System.out.println(" '\"', '$', '%', '*', '/', ':', '<', '>', '?', '\\', '|'");
                        return;
                    }
                }
            }
            System.out.println("Nombre correcto");
            

            //Escritura de Metadatos
            metawriter.seek(metawriter.length());
            //metawriter.write(nombreArchivo.getBytes());
            metawriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }


    }

    void write(String texto){
        try (RandomAccessFile rw = new RandomAccessFile(datatxt, "rw")){

           
            //BufferedWriter bf = new BufferedWriter(new FileWriter(datatxt), 4069);

            byte[] txtb = texto.getBytes();

            //String strtxtb64 = Base64.getEncoder().encodeToString(txtb64);

            rw.seek(rw.length());
            rw.writeBytes(texto);
            rw.seek(2);

            /*String str = "a";
            byte[] a = str.getBytes();*/

            rw.write(323);


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    void read(){
        try{

            RandomAccessFile r = new RandomAccessFile(datatxt, "r");
            //BufferedReader br = new BufferedReader(new FileReader(datatxt), 4096);

            /*
                
            char[] buff = new char[4096];
            int len = br.read(buff);
            String strb64 = new String(buff, 0, len);
            byte[] bytes = Base64.getDecoder().decode(strb64);
            String strdecb64 = new String(bytes);

            System.out.println(strdecb64);
            

            System.out.println(br);

            */
            byte[] bytes = new byte[4096];
 
            r.seek(0);
            r.read(bytes);

            

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void writeFileName(String nombre){ //Esta funcio se va a encargaar de escribir el nombre del archivo en el encabezado
                                        //%ARCHIVO:INICIO?ARCHIVO2:INICIO2%    <- Aqui

        try{

            RandomAccessFile metatxtwriter = new RandomAccessFile(metatxt, "rw");





        } catch(Exception e){
            e.printStackTrace();
        }
    }

    void writeMetadataFile(String metadata){ 
        //Esta funcion se va a encargar de escribir los metadatos del archivo es decir a cuales bloques de bytes apunta cada version
        //Eg: imagen.png1?8016:2<2025-03-23T09:30:45.123>$0-12-343-2342-4323$:3<2025-03-23T09:30:45.123>$0-13-343-2342-4311$

        

    }


   int calcMetaBlockSize(int R, int K, int Nd) {
        // constantes
        final int Lf = 255;
        final int Lt = 23;
        
        // cálculo de Lb con techo
        int Lb = (int) Math.ceil(Math.log10(Nd));
        
        // cálculo del puntero dentro del archivo de metadatos con techo
        int puntero = (int) Math.ceil((Math.log(K) / Math.log(2)) / 8);
        
        // cálculo del espacio para la lista de bloques
        int listaBloques = (Nd * Lb) + (Nd - 1);
        
        // cálculo total con techo aplicado a toda la expresión
        return (int) Math.ceil(Lf + (Lt * R) + puntero + (R * listaBloques));

        /*
         * R: cantidad de versiones en el bloque
         *
         * Lf = 255: longitud máxima del nombre del archivo en Windows
         *
         * Lt = 23: longitud fija de la fecha en el sistema
         *
         * Lt × R: bytes para almacenar las fechas de todas las versiones en el bloque
         *
         * ⌈log2(K) / 8⌉: bytes para almacenar un puntero dentro del archivo de
         * metadatos a la version mas reciente de ese archivo
         * - K: longitud total del archivo de metadatos en bytes
         * - log2(K): bits para direccionar cualquier byte dentro del archivo
         * - Se divide por 8 para convertir a bytes y se usa techo para redondear
         *
         * Nd: cantidad de bloques en que se fragmenta el archivo
         *
         * Lb = ⌈log10(Nd)⌉: bytes necesarios para representar el número de cada bloque
         *
         * (Nd × Lb) + (Nd - 1): bytes para almacenar la lista de bloques
         * - Nd × Lb: bytes usados para numerar cada bloque
         * - Nd - 1: separadores entre números de bloque
         *
         * R × ((Nd × Lb) + (Nd - 1)): bytes para almacenar la lista de bloques de todas
         * las versiones del bloque
         */
    }
}

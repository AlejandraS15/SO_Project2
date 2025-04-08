import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.io.InputStreamReader;
import java.awt.Desktop;


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

    void create(String filePath){
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

            try {
                File file = new File(filePath);

                if (file.exists()) {
                    System.out.println("El archivo ya existe: " + filePath);

                    return;
                }

                if (file.createNewFile()) {
                    System.out.println("Archivo creado exitosamente: " + filePath);
                    //Verifica que el nombre del archivo no contenga un caracter reservado
                    for (char c : filePath.toCharArray()) {
                        for (char reservado : reservados) {
                            if (c == reservado) {
                                System.out.println("Nombre de archivo no valido, contiene alguno de estos caracteres reservados: ");
                                System.out.println(" '\"', '$', '%', '*', '/', ':', '<', '>', '?', '\\', '|'");
                                return;
                            }
                        }
                    }
                    System.out.println("Nombre correcto");
                    writeMetadata(filePath,1);

                } else {
                    System.out.println("No se pudo crear el archivo.");
                }

            } catch (IOException e) {
                System.err.println("Error al crear el archivo: " + e.getMessage());
            }
            

        }catch(Exception e){
            e.printStackTrace();
        }


    }

    void write(String texto){
        try (RandomAccessFile rw = new RandomAccessFile(datatxt, "rw")){

           
            //BufferedWriter bf = new BufferedWriter(new FileWriter(datatxt), 4069);

            byte[] txtb = texto.getBytes();

            //String strtxtb64 = Base64.getEncoder().encodeToString(txtb64);

            rw.seek(5);
            rw.writeBytes(texto);
            rw.seek(0);

            /*String str = "a";
            byte[] a = str.getBytes();*/

            rw.write(323);


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // Eg -> |imagen.png?1024*4096:2<2025-03-23T09:30:45.123>$0-12-343-2342-4323$|
    private void writeMetadata(String filePath, int version) {
        try (RandomAccessFile metawriter = new RandomAccessFile(metatxt, "rw")) {
            String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Map<String, List<Integer>> vectorbloques = new HashMap<>();

            metawriter.seek(metawriter.length());

            String metadata = "|"+filePath+"?"+":"+version+"<"+fechaCreacion+">$"+vectorbloques;
            metawriter.writeBytes(metadata);

            System.out.println("Metadatos añadidos: " + metadata);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de metadatos: " + e.getMessage());
        }
    }

    RandomAccessFile open(String nombreArchivo) { //Hay que cambiarle el mombre a esta función
        try {
            RandomAccessFile metareader = new RandomAccessFile(metatxt, "r");
            String line;
    
            // Nos ubicamos en data
            RandomAccessFile datafile = new RandomAccessFile(datatxt, "rw");

            while ((line = metareader.readLine()) != null) {
                if (line.startsWith(nombreArchivo + "?")) {
                    String[] partes = line.split("\\$");
                    if (partes.length < 2 || partes[1].isEmpty()) {
                        System.out.println("El archivo no tiene bloques asignados.");
                        datafile.close();
                        return null;
                    }
    
                    // Tomamos el primer bloque del archivo
                    String[] bloques = partes[1].split("-");
                    int primerBloque = Integer.parseInt(bloques[0]); // Primer bloque asignado
    
                    // Nos ubicamos en data
                    //RandomAccessFile datafile = new RandomAccessFile(datatxt, "rw");
                    datafile.seek(primerBloque * 4096); // Ir al inicio del archivo en data
    
                    metareader.close();
                    return datafile;
                }
            }
            metareader.close();
            System.out.println("Archivo no encontrado en meta.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void saveDataBin(File archivoGuardar){//Convierte un archivo y lo guarda en bloques de 4Kb en el archivo de binarios
        try{

            RandomAccessFile rw = new RandomAccessFile(archivoGuardar, "r");
            RandomAccessFile rw2 = new RandomAccessFile(datatxt, "rw");

            byte [] buffer = new byte [4096];
            int tamanoBloque = 4096;
            long tamanoActual = rw2.length();
            long siguienteBloque = ((tamanoActual + tamanoBloque-1)/tamanoBloque)*tamanoBloque;
            rw2.seek(siguienteBloque);    

            for(int i = 0; i <= rw.length()/tamanoBloque; i++){
                //System.out.println(i);
                rw.seek(i*tamanoBloque);
                int bytesLeidos = rw.read(buffer);
                rw2.write(buffer, 0, bytesLeidos);
            }

        } catch(Exception e){
            e.printStackTrace();
        }

    }

    
   int calcMetaBlockSize(int R, int K, int Nd) {//Esta funcion se tiene pensado para calcular el espacio necesario para almacenar la metadata de un archivo (todavía no se sabe si se va a usar)
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

    void openFileBin(String nombreArchivo, int[] bloques){ //Funcion Antigua de Abrir archivos copiando la informacion
        //Esta funcio reconstruye un archivo tomando los binarios en datatxt, se le ingresa un array con los bloques que conforman el archivo
        //Y un nombre String que la idea es que sea el extraido de metatxt

        try{
            File tempFile = new File(nombreArchivo);

            RandomAccessFile rw = new RandomAccessFile(datatxt, "r");
            RandomAccessFile rw2 = new RandomAccessFile(tempFile, "rw");

            byte[] buffer = new byte[4096]; // tamaño fijo de 4kb
            long tamanoArchivo = rw.length();
            int tamanoBloque = 4096;
            
            for (int i = 0; i < bloques.length; i++) {

                long byteActual = bloques[i] * tamanoBloque;

                rw.seek(byteActual); // posicionarse en el byte exacto
                int bytesLeidos = rw.read(buffer); // leer siempre 4kb (o menos si es el último bloque)

                if (bytesLeidos > 0) {
                    rw2.write(buffer, 0, bytesLeidos); // escribir los bytes leídos
                    byteActual += bytesLeidos; // avanzar correctamente
                }

            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    public void test() { //Esta función toma un archivo, lees sus bytes, los escribe en binario en otro archivo y abre el archivo binario recreando el archivo original (Abrir antiguo)
        File docx = new File("Proyecto2_SO.docx");
        File docxbin = new File("Proyecto2_SO.bin");
    
        try (RandomAccessFile rw = new RandomAccessFile(docx, "r");
             RandomAccessFile rw2 = new RandomAccessFile(datatxt, "rw")) {
    
            // limpiar el archivo antes de escribir
            rw2.setLength(0);
    
            byte[] buffer = new byte[4096]; // tamaño fijo de 4kb
            long byteActual = 0;
            long tamanoArchivo = rw.length();
    
            while (byteActual < tamanoArchivo) {
                rw.seek(byteActual); // posicionarse en el byte exacto
                int bytesLeidos = rw.read(buffer); // leer siempre 4kb (o menos si es el último bloque)
    
                if (bytesLeidos > 0) {
                    rw2.write(buffer, 0, bytesLeidos); // escribir los bytes leídos
                    byteActual += bytesLeidos; // avanzar correctamente
                }
            }
    
            System.out.println("archivo convertido a binario correctamente");
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // abrir el archivo binario después de que se haya cerrado correctamente
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(docxbin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void openFileMemoryMap(int[] bloques) throws IOException { //Variante de la versión antigua de abrir un archivo copiando en disco pero con Memory Map
        int blockSize = 4096;
        try (RandomAccessFile file = new RandomAccessFile(datatxt, "r");
             FileChannel channel = file.getChannel()) {
            for (int bloque : bloques) {
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, (long) bloque * blockSize, blockSize);
                byte[] data = new byte[blockSize];
                buffer.get(data);
                System.out.write(data);
            }
        }

    }
    /* //Se supone esta funcion era un test para abrir un archivo usando un mapeo sin duplicar el archivo temporalmente en el disco
     * void openFileMemoryMap(){
        try (RandomAccessFile file = new RandomAccessFile("Data.bin", "r"); FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();
            int blockSize = 4096;  // tamaño del bloque

            int [] bloquesALeer = 

            for (long position = 0; position < fileSize; position += blockSize) {
                // definimos tamaño del bloque (último bloque puede ser más pequeño)
                long remaining = Math.min(blockSize, fileSize - position);

                // mapeamos solo la porción de 4kb del archivo
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, position, remaining);

                // leer los bytes del bloque
                byte[] data = new byte[(int) remaining];
                buffer.get(data);

                // mostrar los bytes en hexadecimal
                System.out.print("bloque en posición " + position + ": ");
                for (byte b : data) {
                    System.out.printf("%02X ", b);
                }
                System.out.println("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

}

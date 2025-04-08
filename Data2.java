import java.io.File;
import java.util.Base64;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Data2 {

    public static File datatxt;
    public static File metatxt;
    private RandomAccessFile file;
    Data2(){
        
        datatxt = new File("Data");
        metatxt = new File("Meta");

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

    RandomAccessFile open(String nombreArchivo) {
        try {
            RandomAccessFile metareader = new RandomAccessFile(metatxt, "r");
            String line;
    
            while ((line = metareader.readLine()) != null) {
                if (line.startsWith(nombreArchivo + "?")) {
                    String[] partes = line.split("\\$");
                    if (partes.length < 2 || partes[1].isEmpty()) {
                        System.out.println("El archivo no tiene bloques asignados.");
                        return null;
                    }
    
                    // Tomamos el primer bloque del archivo
                    String[] bloques = partes[1].split("-");
                    int primerBloque = Integer.parseInt(bloques[0]); // Primer bloque asignado
    
                    // Nos ubicamos en data
                    RandomAccessFile datafile = new RandomAccessFile(datatxt, "rw");
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

    void write(String texto){
        try{

            BufferedWriter bf = new BufferedWriter(new FileWriter(datatxt), 4069);

            byte[] txtb64 = texto.getBytes();

            String strtxtb64 = Base64.getEncoder().encodeToString(txtb64);

            bf.write(strtxtb64);
            bf.flush();


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    void read(){
        try{
            BufferedReader br = new BufferedReader(new FileReader(datatxt), 4096);

            char[] buff = new char[4096];

            int len = br.read(buff);
            String strb64 = new String(buff, 0, len);
            byte[] bytes = Base64.getDecoder().decode(strb64);
            String strdecb64 = new String(bytes);

            System.out.println(strdecb64);
            

            System.out.println(br);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
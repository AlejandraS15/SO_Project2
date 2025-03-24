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

            //Verificacion caracteres reservados
            for(int i = 0; i < nombreArchivo.length(); i++){
                //Verifica el nombre del archivo no contenga un caracter reservado
                if((nombreArchivo.charAt(i) > 34 && nombreArchivo.charAt(i) < 47) || (nombreArchivo.charAt(i)> 58 && nombreArchivo.charAt(i) < 63) || nombreArchivo.charAt(i) == 92 || nombreArchivo.charAt(i) == 124){
                    for(int j = 0; j < reservados.length; j++){
                        if(nombreArchivo.charAt(i) == reservados[j]){
                            System.out.println("Nombre de archivo no válido, contiene alguno de estos caracteres reservados: ");
                            System.out.println(" '\"', '$', '%', '*', '/', ':', '<', '>', '?', '\\', '|'");
                            return;
                        }
                    }
                }
            }

            //Escritura de Metadatos
            metawriter.seek(metawriter.length());
            //metawriter.writeBytes();
            metawriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }


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

import java.io.RandomAccessFile;
import java.util.List;

    public class FileDescriptor {
        //Esta clase ayuda a llevar registro de los programas abiertos, nombre de los archivos abiertos, el modo de lectura el Random Acces File asociado;

        public static List<FileDescriptor> filedescriptors;
        private static int fdcount;

        private RandomAccessFile raf;
        private String filename;
        private int fd;
        private int[] blocks;
        private String mode; // 0 = "r", 1 = "rw"
        private int fileSize;
        
        private int offset;
        private int blockOffset; 
        private int indexBlock; //Indice del bloque actual en el array de bloques
        private byte [] loadedBlock; //Bloque cargado a memoria para leer/escribir
        private int blockSize = 4096;

        public FileDescriptor(RandomAccessFile raf, String filename, int[] bloques, String modo) { //Cada file descriptor
            this.raf = raf; //Random Acces File del archivo que queremos leer/escribir
            this.filename = filename; //Nombre del archivo
            this.blocks = bloques; //Array con los bloques que conforman este archivo
            this.mode = modo; //modo del random acces file que abrió el archivo, lectura o escritura

            FileDescriptor.fdcount++; //int de la clase para asignar un numero entero a cada file descriptor
            this.fd = fdcount; //establece el entero correspondiente a este file descriptor

            this.offset = 0; // el offset empieza en cero
            this.fileSize = this.blocks.length*blockSize; //calcula el tamaño del archivo

            FileDescriptor.filedescriptors.add(this); //añade este file descriptor a la lista estática de los filedescriptros
        }


        public void read(int bytes) { //Esta función recibe como parametros la cantidad de bytes que se van a leer del archivo


            if(offset + bytes > fileSize){
                System.out.println("No se pueden leer más bytes de los que tiene el archivo");
                return;
            }


            while (bytes > 0) {
        
                if (loadedBlock == null) {
                    readBlock(indexBlock); // Carga el bloque actual
                }
        
                int bytesDispBlock = blockSize - blockOffset;
                int bytesToRead = bytes - bytesDispBlock;
        
                if (bytesToRead <= 0) { //Hay suficientes bytes en el bloque actual
                    for (int i = 0; i < bytes; i++) {
                        System.out.println(loadedBlock[blockOffset + i]);
                    }
        
                    blockOffset += bytes;
                    offset += bytes;
                    bytes = 0;
                }
        
                if (bytesToRead > 0) { // No hay suficientes bytes en el bloque actual, leemos lo que queda y pasamos al siguiente

                    for (int i = 0; i < bytesDispBlock; i++) {
                        System.out.println(loadedBlock[blockOffset + i]);
                    }
        
                    offset += bytesDispBlock;
                    blockOffset = 0;
                    bytes = bytesToRead;
        
                    indexBlock += 1;
                    loadedBlock = null;
                }
            }
        }
        
        

        public void readBlock(int numBlock){
            try{

                loadedBlock = new byte[blockSize];

                this.raf.seek(this.blocks[indexBlock]*blockSize); //Busca el byte correspondiente al inicio del índice del bloque actual en el array de bloques
                raf.read(loadedBlock);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }


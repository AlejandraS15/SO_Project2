import java.io.RandomAccessFile;
import java.util.List;

    public class FileDescriptor {
        //Esta clase ayuda a llevar registro de los programas abiertos, nombre de los archivos abiertos, el modo de lectura el Random Acces File asociado;

        public static List<FileDescriptor> filedescriptors;
        private static int fdcount;

        private RandomAccessFile raf;
        private String filename;
        private int[] bloques;
        private int fd;
        private String modo; // 0 = "r", 1 = "rw"
        
        private int offset; 
        private int bloqueActual;
        private byte [] bloqueCargado;

        public FileDescriptor(RandomAccessFile raf, String filename, int[] bloques, String modo) { //Cada file descriptor
            this.raf = raf;
            this.filename = filename;
            this.bloques = bloques;
            this.modo = modo;

            FileDescriptor.fdcount++;
            this.fd = fdcount;

            this.offset = 0;

            FileDescriptor.filedescriptors.add(this);
        }


        public void read(int numBytes){

        }

        public void readBlock(){
            this.raf
        }
    }


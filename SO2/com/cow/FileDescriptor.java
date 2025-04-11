package com.cow;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileDescriptor {
    //Esta clase ayuda a llevar registro de los programas abiertos, nombre de los archivos abiertos, el modo de lectura el Random Acces File asociado;
    public static List<FileDescriptor> filedescriptors = new ArrayList<>();
    private RandomAccessFile raf;
    private String filename;
    private int[] blocks;
    private int fileSize;
    private int offset;
    private int blockOffset;
    private int indexBlock; // Índice del bloque actual en el array de bloques
    private byte[] loadedBlock; // Bloque cargado a memoria para leer/escribir
    private static final int blockSize = 4096;



    public FileDescriptor(RandomAccessFile raf, String filename, int[] bloques, String modo) {
        this.raf = raf; // RandomAccessFile del archivo que queremos leer/escribir
        this.filename = filename; // Nombre del archivo
        this.blocks = bloques; // Array con los bloques que conforman este archivo
        this.offset = 0; // El offset empieza en cero
        this.fileSize = blocks.length * blockSize; // Calcula el tamaño del archivo
        FileDescriptor.filedescriptors.add(this); // Añade este file descriptor a la lista estática
    }
    public String getFilename() {
        return filename;
    }
    //READ
    public void read(int bytes) {

        loadedBlock = null;

        if (offset + bytes > fileSize) {
            System.out.println("No se pueden leer más bytes de los que tiene el archivo");
            return;
        }
    
        try {
            while (bytes > 0) {
                if (loadedBlock == null) {
                    readBlock(indexBlock);
                }
    
                int bytesDispBlock = blockSize - blockOffset;
                int bytesToRead = Math.min(bytes, bytesDispBlock);
    
                for (int i = 0; i < bytesToRead; i++) {
                    System.out.print((char) loadedBlock[blockOffset + i]); // o imprimir como byte
                }
    
                blockOffset += bytesToRead;
                offset += bytesToRead;
                bytes -= bytesToRead;
    
                if (blockOffset >= blockSize) {
                    blockOffset = 0;
                    indexBlock++;
                    loadedBlock = null;
                }
            }
    
            System.out.println(); // salto de línea final
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }
    
    private void readBlock(int i) throws IOException {
        int bloque = blocks[i];
        loadedBlock = new byte[blockSize];
        raf.seek(bloque * blockSize);
        raf.readFully(loadedBlock);
    }
    
    public void read() {
        try {
            for (int i = 0; i < blocks.length; i++) {
                int bloque = blocks[i];
                byte[] buffer = new byte[blockSize];
                raf.seek(bloque * blockSize);
                raf.readFully(buffer);
    
                for (int j = 0; j < blockSize; j++) {
                    System.out.print((char) buffer[j]); // O imprimir como byte si prefieres
                }
            }
    
            System.out.println(); // Salto de línea final
    
        } catch (IOException e) {
            System.err.println("Error al leer los bloques: " + e.getMessage());
        }
    }
    

    //WRITE
    public void write(String dataToWrite) {
        byte[] byteData = dataToWrite.getBytes(); 
        int bytes = byteData.length;
    
        if (offset + bytes > fileSize) {
            System.out.println("No se pueden escribir más bytes de los que tiene el archivo");
            return;
        }
    
        try {
            int dataOffset = 0;
    
            while (bytes > 0) {
    
                if (loadedBlock == null) {
                    readBlock(indexBlock); // Cargar el bloque actual en memoria
                }
    
                int bytesDispBlock = blockSize - blockOffset;
                int bytesToWrite = Math.min(bytes, bytesDispBlock);
    
                for (int i = 0; i < bytesToWrite; i++) {
                    loadedBlock[blockOffset + i] = byteData[dataOffset + i]; // Copia byte por byte
                }
    
                writeBlock(indexBlock, loadedBlock); // Escribir bloque actualizado
                blockOffset += bytesToWrite;
                offset += bytesToWrite;
                dataOffset += bytesToWrite;
                bytes -= bytesToWrite;

                if(offset > fileSize){
                    fileSize = offset;
                }
    
                if (blockOffset >= blockSize) {
                    blockOffset = 0;
                    indexBlock += 1;
                    loadedBlock = null;
                }
            }
    
        } catch (IOException e) {
            System.err.println("Error al escribir: " + e.getMessage());
        }
    }
    public void writeBlock(int numBlock, byte[] toWriteBlock) {
        try {
            raf.seek(this.blocks[numBlock] * blockSize);
            raf.write(toWriteBlock, 0, blockSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static FileDescriptor getFileDescriptorByFilename(String filename) {
        for (FileDescriptor fd : filedescriptors) {
            if (fd.filename.equals(filename)) {
                return fd;
            }
        }
        return null; // Si no se encuentra el archivo
    }
    
    public void close() {
        try {
            // Si hay algún bloque cargado que no ha sido guardado, lo escribimos
            if (loadedBlock != null) {
                writeBlock(indexBlock, loadedBlock);  // Escribir cualquier bloque pendiente en memoria
            }
            
            // Cerrar el RandomAccessFile (raf) después de realizar todas las escrituras
            raf.close();
            System.out.println("Archivo cerrado correctamente.");
            
        } catch (IOException e) {
            System.err.println("Error al cerrar el archivo: " + e.getMessage());
        }
    }
    
    
    
}


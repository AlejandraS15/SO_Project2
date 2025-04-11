package com.cow;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;

public class Data {

    public static File datatxt = new File("Data.bin");
    public static File metatxt = new File("Meta.txt");
    private static List<FileDescriptor> fileDescriptors = new ArrayList<>();
    private static final int HEADER_SIZE = 4096; //Tamaño

    public Data() {
        try {
            if (!datatxt.exists()) datatxt.createNewFile();
    
            if (!metatxt.exists()) {
                try (RandomAccessFile meta = new RandomAccessFile(metatxt, "rw")) {
                    // Reservar 4096 bytes para el encabezado con espacios
                    meta.setLength(HEADER_SIZE);
                }
            }
        } catch (IOException e) {
            System.err.println("Error creando los archivos base: " + e.getMessage());
        }
    }
    
    
    // CREATE
    //Nos crea nuevos archivos, nos agrega archivos a meta
    public void create(String fileName) {
        // Caracteres no permitidos
        char[] reservados = { '"', '$', '%', '*', '/', ':', '<', '>', '?', '\\', '|' };
    
        try (RandomAccessFile meta = new RandomAccessFile(metatxt, "rw")) {
            // Validar caracteres
            for (char c : fileName.toCharArray()) {
                for (char r : reservados) {
                    if (c == r) {
                        System.out.println("Nombre inválido. Contiene caracteres reservados.");
                        return;
                    }
                }
            }
            // Leer encabezado
            byte[] headerBytes = new byte[HEADER_SIZE];
            meta.seek(0);
            meta.read(headerBytes);
            String header = new String(headerBytes).trim();
    
            if (header.contains("%" + fileName + ":")) {
                System.out.println("El archivo ya existe en Meta: " + fileName);
                return;
            }
            // Crear nueva entrada con versión 0 y sin bloques
            writeMetadata(fileName, 0, new ArrayList<>());
        } catch (IOException e) {
            System.err.println("Error al registrar archivo: " + e.getMessage());
        }
    }
    //Se escribe en meta el archivo asi:
    // Eg -> |imagen.png?1024*4096:2<2025-03-23T09:30:45.123>$0-12-343-2342-4323$|
    public void writeMetadata(String filePath, int version, List<Integer> bloques) {
        try (RandomAccessFile metawriter = new RandomAccessFile(metatxt, "rw")) {
            String fechaCreacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            // Saltar al final del archivo (después del encabezado)
            long offset = metawriter.length();
            if (offset < HEADER_SIZE) {
                offset = HEADER_SIZE;  // Asegúrate de no escribir en el encabezado
            }
            System.out.println(offset);
            // Actualiza el encabezado con la nueva versión y posición
            updateEnunciado(filePath, version, offset);
            // Ir al final para escribir metadata
            metawriter.seek(offset);
            // Convertir lista de bloques a string
            StringBuilder bloquesStr = new StringBuilder();
            for (int i = 0; i < bloques.size(); i++) {
                bloquesStr.append(bloques.get(i));
                if (i != bloques.size() - 1) bloquesStr.append("-");
            }
            // Formato del bloque de metadatos
            String metadata = "\n"+"|" + filePath + "?:" + version + "<" + fechaCreacion + ">$" + bloquesStr + "$|";
            metawriter.writeBytes(metadata);
            System.out.println("Metadatos añadidos: " + metadata);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo de metadatos: " + e.getMessage());
        }
    }
    //Actualiza el enunciado con las nuevas versiones (cambios que se hacen en el archivo)
    public void updateEnunciado(String fileName, int version, long offset) throws IOException {
        try (RandomAccessFile metawriter = new RandomAccessFile(metatxt, "rw")) {
            byte[] headerBytes = new byte[HEADER_SIZE];
            metawriter.seek(0);
            metawriter.read(headerBytes);
            String enunciado = new String(headerBytes).trim();
            StringBuilder nuevoEnunciado = new StringBuilder();
            boolean archivoEncontrado = false;
            for (String linea : enunciado.split("%")) {
                if (linea.isEmpty()) continue;
                if (linea.startsWith(fileName + ":")) {
                    archivoEncontrado = true;
                    String actualizaciones = linea + ";" + version + "@" + offset;
                    nuevoEnunciado.append("%").append(actualizaciones).append("%");
                } else {
                    nuevoEnunciado.append("%").append(linea).append("%");
                }
            }
            if (!archivoEncontrado) {
                nuevoEnunciado.append("%").append(fileName).append(":").append(version).append("@").append(offset).append("%");
            }
            // Validar tamaño del nuevo encabezado
            String finalHeader = nuevoEnunciado.toString();
            if (finalHeader.length() > HEADER_SIZE) {
                throw new IOException("Enunciado excede el tamaño de 4096 bytes.");
            }
            // Rellenar con espacios para mantener el bloque completo
            metawriter.seek(0);
            metawriter.writeBytes(String.format("%-" + HEADER_SIZE + "s", finalHeader));
        }
    }

    //OPEN
    //OPEN
    public FileDescriptor open(String nombreArchivo) {
        try {
            RandomAccessFile meta = new RandomAccessFile(metatxt, "rw");
            try (RandomAccessFile data = new RandomAccessFile(datatxt, "rw")) {
                // Leer encabezado
                byte[] headerBytes = new byte[HEADER_SIZE];
                meta.seek(0);
                meta.read(headerBytes);
                String enunciado = new String(headerBytes).trim();
                boolean encontrado = false;
                long offsetMeta = -1;
                // Verificar existencia del archivo en el encabezado y obtener offset
                for (String entry : enunciado.split("%")) {
                    if (entry.startsWith(nombreArchivo + ":")) {
                        String[] versiones = entry.split(";");
                        String ultima = versiones[versiones.length - 1];
                        String[] partes = ultima.split("@");
                        //Guarda la version mas reciente
                        offsetMeta = Long.parseLong(partes[1]);
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    System.out.println("Archivo no encontrado en encabezado de meta.");
                    meta.close();
                    return null;
                }
                // Leer metadata completa desde el offset
                meta.seek(offsetMeta);
                String metadata = meta.readLine(); // |archivo?:version<fecha>$bloques$|
                String[] partes = metadata.split("\\$");
                int[] bloques = new int[0];
                if (partes.length >= 2 && !partes[1].isEmpty()) {
                    String[] bloquesStr = partes[1].split("-");
                    bloques = new int[bloquesStr.length];
                    for (int i = 0; i < bloquesStr.length; i++) {
                        bloques[i] = Integer.parseInt(bloquesStr[i]);
                    }
                }
                // Si no tiene bloques asignados, asignar el siguiente bloque libre
                if (bloques.length == 0) {
                    long tamData = data.length();
                    int nuevoBloque = (int)(tamData / 4096);
                    bloques = new int[] { nuevoBloque };
                    data.setLength((nuevoBloque + 1) * 4096); // Reservar espacio
                    // Actualizar metadata con el nuevo bloque
                    List<Integer> nuevoBloqueList = new ArrayList<>();
                    nuevoBloqueList.add(nuevoBloque);
                    writeMetadata(nombreArchivo, bloques.length, nuevoBloqueList); // versión siguiente y nuevos bloques
                }
                // Crear descriptor
                FileDescriptor descriptor = new FileDescriptor(data, metadata, bloques, metadata);
                System.out.println("Archivo abierto: " + nombreArchivo + ", Bloques: " + Arrays.toString(bloques));
                meta.close();
                return descriptor;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //READ
    public void read(String fileName, int bytes) {
        FileDescriptor abierto = null;

        for (FileDescriptor fd : FileDescriptor.filedescriptors) {
            if (fd.getFilename().equals(fileName)) {  // Usamos el método getFilename()
                abierto = fd;
                break;
            }
        }

        if (abierto == null) {
            System.out.println("Archivo no está abierto.");
            return;
        }

        // Usamos el método de la instancia
        abierto.read(bytes);
    }
    

    //WRITE
    public void write(String fileName, String text) {
        for (FileDescriptor fd : FileDescriptor.filedescriptors) {
            if (fd.getFilename().equals(fileName)) {  // Usamos el método getFilename()
                fd.write(text);  // Usamos el método de la instancia
                return;
            }
        }

        System.out.println("El archivo no está abierto. Ábralo antes de escribir.");
    }

    //CLOSE
    public void close(String fileName) {
        FileDescriptor descriptorToClose = null;

        // Buscar el descriptor del archivo que queremos cerrar
        for (FileDescriptor fd : fileDescriptors) {
            if (fd.getFilename().equals(fileName)) {
                descriptorToClose = fd;
                break;
            }
        }

        if (descriptorToClose != null) {
            descriptorToClose.close(); // Llamar al método close de FileDescriptor
            fileDescriptors.remove(descriptorToClose); // Eliminar de la lista de descriptores abiertos
            System.out.println("Archivo cerrado: " + fileName);
        } else {
            System.out.println("El archivo no está abierto.");
        }
    }

    
}
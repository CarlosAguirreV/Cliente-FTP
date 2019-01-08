package clienteftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Clase HiloGenerico. Permite crear conexiones paralelas al servidor. Permite
 * conectar, descargar y subir archivos.
 *
 * @author Carlos Aguirre Vozmediano
 */
public class HiloGenerico implements Runnable {

    private FTPClient clienteFtp;
    private Control padre;
    private byte accion;
    private String nombreArchivo, rutaActual;

    // 0 - Conectar.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia) {
        this.padre = padre;
        this.clienteFtp = new FTPClient();
        this.padre.conectar(servidor, usuario, contrasenia, clienteFtp);
        this.accion = 0;
    }

    // 1 - Subir.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia, File archivoSeleccionado, String rutaActual) {
        this(padre, servidor, usuario, contrasenia);
        setRutaActualRemota(rutaActual);
        this.accion = 1;
    }

    // 2 - Descargar.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia, String nombreArchivo, String rutaActual) {
        this(padre, servidor, usuario, contrasenia);
        this.nombreArchivo = nombreArchivo;
        setRutaActualRemota(rutaActual);
        this.accion = 2;
    }

    private void descargar() {
        String cadenaResultado = "";
        String errores = "";
        boolean correcto = true;

        
        System.out.println(padre.getRutaActualRemota() + "/" + nombreArchivo);
        
        
        
        try (FileOutputStream escritorLocal = new FileOutputStream(padre.getRutaCompletaDescargas() + "\\" +  nombreArchivo)) {
            if (!clienteFtp.retrieveFile(nombreArchivo, escritorLocal)) {
                correcto = false;
                errores += nombreArchivo + " ";
            }
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex);
        }
        if (correcto) {
            cadenaResultado = "Archivos descargados correctamente."; // ESTA YA NO VA A AQUÍ.
        } else {
            cadenaResultado += "Error al descargar los archivos: " + errores;
        }
        this.padre.setMensajeCliente(cadenaResultado);
    }

    private void setRutaActualRemota(String ruta) {
        try {
            this.clienteFtp.changeWorkingDirectory(ruta);
        } catch (IOException ex) {
            Logger.getLogger(HiloGenerico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        switch (accion) {
            case 0: // Conectar.
                System.out.println("CONECTAR");
                break;
            case 1: // Subir.
                System.out.println("SUBIR");
                break;
            case 2: // Descargar.
                System.out.println("DESCARGAR");
                descargar();
                break;
        }
    }
}

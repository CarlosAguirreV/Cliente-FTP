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
    private String servidor, usuario, contrasenia;
    private boolean fin;

    // 0 - Conectar.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia) {
        this.padre = padre;
        this.servidor = servidor;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.clienteFtp = new FTPClient();
        this.accion = 0;
        this.fin = false;
    }

    // 1 - Subir.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia, File archivoSeleccionado, String rutaActual) {
        this(padre, servidor, usuario, contrasenia);
        this.rutaActual = rutaActual;
        this.accion = 1;
    }

    // 2 - Descargar.
    public HiloGenerico(Control padre, String servidor, String usuario, String contrasenia, String nombreArchivo, String rutaActual) {
        this(padre, servidor, usuario, contrasenia);
        this.nombreArchivo = nombreArchivo;
        this.rutaActual = rutaActual;
        this.accion = 2;
    }

    // Comprueba si la conexion se puede establecer.
    protected synchronized boolean conectar(String servidor, String usuario, String contrasenia, FTPClient cliente) {
        boolean correcto = false;
        try {
            cliente.connect(servidor);
            correcto = cliente.login(usuario, contrasenia);
            if (correcto) {
                cliente.setFileType(FTPClient.BINARY_FILE_TYPE);
            }
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
        return correcto;
    }

    /**
     * Lo uso para interrumpir la operación que esté realizando.
     */
    protected void desconectarHilo() {
        try {
            this.fin = true;
            this.clienteFtp.disconnect();
        } catch (IOException ex) {
            System.out.println("ERROR al desconectar el hilo." + ex);
        }
    }

    private void descargar() {
        String cadenaResultado = "";
        boolean correcto = true;

        try (FileOutputStream escritorLocal = new FileOutputStream(padre.getRutaCompletaDescargas() + "\\" + nombreArchivo)) {
            if (!clienteFtp.retrieveFile(nombreArchivo, escritorLocal)) {
                correcto = false;
            }
            // Cuando fuerzo a desconectar el cliente FTP llega a este punto.
        } catch (Exception ex) {
            System.out.println("ERROR al descargar, es normal si desconecto las sesiones de los hilos: " + ex);
        }
        if (correcto) {
            padre.archivoDescargado(true);
        } else {
            cadenaResultado += "Error al descargar el elemento: " + nombreArchivo;
            padre.archivoDescargado(false);
            this.padre.setMensajeCliente(cadenaResultado);
        }
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
        // Antes que nada conecta:
        boolean estoyLogueado = this.conectar(servidor, usuario, contrasenia, clienteFtp);

        switch (accion) {
            case 0: // Conectar.
                System.out.println("Accion: conectar");
                if (!fin) {
                    if (this.clienteFtp.isConnected() && estoyLogueado) {
                        this.padre.resultadoConexion(this.clienteFtp);
                    } else {
                        this.padre.resultadoConexion(null);
                    }
                }

                break;
            case 1: // Subir.
                System.out.println("Accion: subir");
                this.setRutaActualRemota(rutaActual);
                break;
            case 2: // Descargar.
                System.out.println("Accion: descargar " + nombreArchivo);
                this.setRutaActualRemota(rutaActual);
                this.descargar();
                break;
        }
    }
}

package clienteftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Clase Control. Controla todo el proceso de comunicación con el servidor FTP.
 *
 * @version 05/01/2019
 * @author Carlos Aguirre Vozmediano
 */
public class Control {

    private final File carpetaDescargas;
    private final File archivoSesionAnterior;
    private GuiLogueo vLogueo;
    private GuiClienteFtp vCliente;
    private String servidor;
    private String usuario;
    private String contrasenia;
    private FTPClient clienteFtp;

    /**
     * Contructor de la clase control. Se encarga de dejar todos los elementos
     * bien inicializados y preparados.
     */
    public Control() {
        this.carpetaDescargas = new File("Descargas");
        this.archivoSesionAnterior = new File("sesionAnterior.bin");
        this.vLogueo = new GuiLogueo(this);
        this.vCliente = new GuiClienteFtp(this);
        this.cargarDatosSesionAnterior();
        this.vLogueo.mostrar(true);
        this.clienteFtp = new FTPClient();
    }

    // Comprueba si existe la carpeta de descargas y si no la crea.
    private void comprobarDirectorioDescargas() {
        if (!this.carpetaDescargas.exists()) {
            carpetaDescargas.mkdir();
        }
    }

    /**
     * Obtiene la ruta completa de la carpeta de descargas.
     *
     * @return Cadena con la ruta completa.
     */
    protected String getRutaCompletaDescargas() {
        return carpetaDescargas.getAbsolutePath().toString();
    }

    /**
     * Lee los datos de la última sesión que se logró conectar.
     *
     * @return booleano que informa de si se ha cargado bien o no.
     */
    public boolean cargarDatosSesionAnterior() {
        try (FileInputStream fis = new FileInputStream(archivoSesionAnterior); DataInputStream lector = new DataInputStream(fis);) {
            this.vLogueo.setServidor(lector.readUTF());
            this.vLogueo.setUsuario(lector.readUTF());
            this.vLogueo.setContrasenia(lector.readUTF());

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Almacena los datos de la sesión que ha conseguido conectarse.
     */
    public void guardarDatosSesionAnterior() {
        try (FileOutputStream fos = new FileOutputStream(archivoSesionAnterior); DataOutputStream escritor = new DataOutputStream(fos);) {
            escritor.writeUTF(this.servidor);
            escritor.writeUTF(this.usuario);
            escritor.writeUTF(this.contrasenia);
            escritor.flush();

        } catch (IOException ex) {
            System.out.println("No se pueden guardar los datos de sesión.");
        }
    }

    /**
     * Obtiene los datos de sesión e intenta conectarse.
     */
    protected void pulsadoConectar() {
        // Obtener servidor.
        this.servidor = this.vLogueo.getServidor().trim();

        // Obtener usuario.
        this.usuario = this.vLogueo.getUsuario().trim();
        if (this.usuario.equals("")) {
            this.usuario = "Anonimous";
        }

        // Obtener contraseña.
        char[] pass = this.vLogueo.getContrasenia();
        this.contrasenia = "";
        for (char c : pass) {
            this.contrasenia += c;
        }

        // Intentar conectar.
        if (conectar(this.servidor, this.usuario, this.contrasenia)) {

            // Si se consigue conectar y listar los elementos muestra la ventana del cliente FTP.
            if (this.refrescarListado()) {
                vLogueo.mostrar(false);
                this.vCliente.setEstado("Conectado.");
                this.vCliente.setNombreServidor(this.servidor);
                this.vCliente.setNombreUsuario(this.usuario);
                this.actualizarRutaActualFtp();
                this.vCliente.mostrar(true);
                this.guardarDatosSesionAnterior();
            } else {
                this.vLogueo.setEstado("Fallo listar elementos, comprueba el cortafuegos.");
            }

        } else {
            this.vLogueo.setEstado("Fallo al conectar.");
        }
    }

    // Comprueba si la conexion se puede establecer.
    private boolean conectar(String servidor, String usuario, String contrasenia) {
        boolean correcto = false;
        try {
            this.clienteFtp.connect(servidor);
            correcto = clienteFtp.login(usuario, contrasenia);
            if (correcto) {
                this.clienteFtp.setFileType(FTPClient.BINARY_FILE_TYPE);
                this.vLogueo.setEstado("Pulse en conectar cuando esté listo.");
            }
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
        return correcto;
    }

    /**
     * Sube los archivos seleccionados a la nube.
     *
     * @param archivos Listado de archivos que se subirán al servidor FTP.
     */
    protected void subirArchivos(File[] archivos) {
        this.vCliente.setEstado("Subiendo archivos...");
        String cadenaResultado = "";
        String errores = "";
        boolean correcto = true;

        for (File f : archivos) {
            try (FileInputStream escritorRemoto = new FileInputStream(f.getPath())) {
                if (!clienteFtp.storeFile(f.getName(), escritorRemoto)) {
                    correcto = false;
                    errores += f.getName() + " ";
                }
            } catch (Exception ex) {
                correcto = false;
                errores += f.getName() + " ";
                System.out.println("ERROR: " + ex);
            }
        }
        if (correcto) {
            cadenaResultado = "Archivos subidos correctamente.";
        } else {
            cadenaResultado += "Error al subir los siguientes elementos: " + errores;
        }
        this.vCliente.setEstado(cadenaResultado);
        this.refrescarListado();
    }

    /**
     * Descarga los archivos en la carpeta descargas.
     *
     * @param nombreArchivos Listado con los nombres de elementos que se van a
     * descargar.
     */
    protected void bajarArchivos(List<String> nombreArchivos) {
        if (nombreArchivos.size() > 0) {
            this.vCliente.setEstado("Descargando archivos...");
            String cadenaResultado = "";
            String errores = "";
            boolean correcto = true;

            for (String nombreArchivo : nombreArchivos) {
                this.comprobarDirectorioDescargas();
                try (FileOutputStream escritorLocal = new FileOutputStream(carpetaDescargas.getName() + "\\" + nombreArchivo)) {
                    if (!clienteFtp.retrieveFile(nombreArchivo, escritorLocal)) {
                        correcto = false;
                        errores += nombreArchivo + " ";
                    }
                } catch (Exception ex) {
                    System.out.println("ERROR: " + ex);
                }
                if (correcto) {
                    cadenaResultado = "Archivos descargados correctamente.";
                } else {
                    cadenaResultado += "Error al descargar los archivos: " + errores;
                }
                this.vCliente.setEstado(cadenaResultado);
            }
        } else {
            this.vCliente.setEstado("No hay nada que descargar.");
        }
        this.refrescarListado();
    }

    /**
     * Elimina tanto los archivos como los directorios seleccionados.
     *
     * @param nombreElementos Listado de nombres de elementos que se desean
     * borrar.
     */
    protected void eliminarArchivoDirectorio(List<String> nombreElementos) {

        // Ejecuta solo si hay al menos un elemento que borrar
        if (nombreElementos.size() > 0) {
            this.vCliente.setEstado("Borrando elementos...");
            String cadenaResultado = "";
            String errores = "";
            boolean correcto = true;

            try {
                FTPFile[] elementos = clienteFtp.listFiles();

                for (String nombre : nombreElementos) {
                    for (FTPFile f : elementos) {
                        if (f.getName().equals(nombre)) {
                            if (f.isFile()) {
                                if (!clienteFtp.deleteFile(nombre)) {
                                    correcto = false;
                                    errores += nombre + " ";
                                }
                            } else {
                                if (!clienteFtp.removeDirectory(nombre)) {
                                    correcto = false;
                                    errores += nombre + " ";
                                }
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("ERROR: " + ex);
            }

            if (correcto) {
                cadenaResultado = "Archivos borrados correctamente.";
            } else {
                cadenaResultado += "No se pueden borrar los elementos: " + errores;
            }

            this.vCliente.setEstado(cadenaResultado);
        } else {
            this.vCliente.setEstado("No hay nada que borrar.");
        }
        this.refrescarListado();
    }

    /**
     * Crea una carpeta en el directorio local
     *
     * @param nombreCarpeta Nombre de la carpeta que se va a crear.
     */
    protected void crearCarpeta(String nombreCarpeta) {
        this.vCliente.setEstado("Creando carpeta...");
        String cadenaResultado = "";
        try {
            if (clienteFtp.makeDirectory(nombreCarpeta)) {
                cadenaResultado = "Carpeta " + nombreCarpeta + " creada correctamente.";
            } else {
                cadenaResultado += "Error al crear el directorio.";
            }
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex);
        }
        this.vCliente.setEstado(cadenaResultado);
        this.refrescarListado();
    }

    /**
     * Recarga el listado de archivos y directorios del cliente FTP
     *
     * @return boolean Si se ha podido o no refrescar.
     */
    protected boolean refrescarListado() {
        String[] nombreElementos = null;

        try {
            FTPFile[] archivos = clienteFtp.listFiles();
            nombreElementos = new String[archivos.length];
            int i = 0;

            for (FTPFile f : archivos) {
                nombreElementos[i] = f.getName();
                i++;
            }

            this.vCliente.setListado(nombreElementos);
            return true;

        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
            return false;
        }
    }

    /**
     * Desconecta la conexión con el servidor FTP.
     */
    protected void desconectar() {
        try {
            this.clienteFtp.disconnect();
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
    }

    /**
     * Lo que sucede cuando se pulsa el botón desconectar en la ventana de
     * GuiLogueo.
     */
    protected void pulsadoDesconectar() {
        this.desconectar();
        this.vCliente.dispose();
        this.vLogueo.mostrar(true);
    }

    /**
     * Cambia al directorio especificado.
     *
     * @param directorio Directorio al que se desea cambiar.
     */
    protected void cambiarDirectorio(String directorio) {
        try {
            boolean puedoEntrar = this.clienteFtp.changeWorkingDirectory(directorio);
            if (puedoEntrar) {
                this.vCliente.setEstado("2 ClicIzq (Entrar) / 1 ClicDcho (Volver)");
                this.refrescarListado();
            } else {
                this.vCliente.setEstado("No puedes meterte dentro de un archivo.");
            }
            this.vCliente.setRutaDirectoriosRemoto(this.clienteFtp.printWorkingDirectory());
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
    }

    /**
     * Cambia al directorio anterior.
     */
    protected void volverDirectorioAnterior() {
        try {
            boolean puedoVolver = this.clienteFtp.changeToParentDirectory();
            if (puedoVolver) {
                this.actualizarRutaActualFtp();
                this.vCliente.setEstado("2 ClicIzq (Entrar) / 1 ClicDcho (Volver)");
            } else {
                this.clienteFtp.changeWorkingDirectory("/");
                this.actualizarRutaActualFtp();
                this.vCliente.setEstado("Problema al volver, se regresó a la raíz.");
            }
            this.refrescarListado();

        } catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
    }

    /**
     * Muestra la ruta actual remota.
     */
    private void actualizarRutaActualFtp() {
        try {
            this.vCliente.setRutaDirectoriosRemoto(this.clienteFtp.printWorkingDirectory());
        } catch (IOException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Muestra información sobre el estado del cliente FTP.
     */
    public void mostrarInfoFtp() {
        System.out.println("INFO FTP: " + this.clienteFtp.getReplyString());
    }

    /**
     * Metodo principal.
     *
     * @param args Parametros que se pasan al programa nada más ejecutarlo.
     */
    public static void main(String[] args) {
        new Control();
    }
}

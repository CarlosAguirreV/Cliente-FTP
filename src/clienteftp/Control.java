package clienteftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Clase Control. Controla todo el proceso de comunicación con el servidor FTP.
 *
 * @version 14/01/2019
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
    private HiloGenerico[] grupoDescarga;
    private HiloGenerico[] grupoSubida;
    private HiloGenerico hiloConexion;
    private boolean descargando, subiendo;
    private boolean intentantoConectar;
    private int numeroArchivosDescargados;
    private int numeroArchivosADescargar;
    private int numeroArchivosSubidos;
    private int numeroArchivosASubir;

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
        this.descargando = false;
        this.subiendo = false;
        this.intentantoConectar = false;
        this.numeroArchivosADescargar = 0;
        this.numeroArchivosDescargados = 0;
        this.numeroArchivosASubir = 0;
        this.numeroArchivosSubidos = 0;
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
        return carpetaDescargas.getAbsolutePath();
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
        if (this.intentantoConectar) {
            this.hiloConexion.desconectarHilo();

            // Indico que estoy cancelando la conexión.
            this.intentantoConectar = false;
            this.vLogueo.setIntentandoConectar(false);
            this.vLogueo.setEstado("Has cancelado la conexion.");

        } else {
            // Obtener servidor.
            this.servidor = this.vLogueo.getServidor().trim();

            if (this.servidor.isEmpty()) {
                this.vLogueo.setEstado("No has definido un servidor.");
            } else {
                // Indico que estoy intentando conectarme.
                this.intentantoConectar = true;
                this.vLogueo.setIntentandoConectar(true);

                // Obtener usuario.
                this.usuario = this.vLogueo.getUsuario().trim();
                if (this.usuario.equals("")) {
                    this.usuario = "Anonimous";
                    this.vLogueo.setUsuario(this.usuario);
                }

                // Obtener contraseña.
                char[] pass = this.vLogueo.getContrasenia();
                this.contrasenia = "";
                for (char c : pass) {
                    this.contrasenia += c;
                }

                // Crea un hilo para conectar.
                this.vLogueo.setEstado("Intentando conectar, espera un momento.");
                this.hiloConexion = new HiloGenerico(this, servidor, usuario, contrasenia);
                this.crearHilo(this.hiloConexion);
            }
        }
    }

    /**
     * Obtiene un resultado enviado por el hilo genérico. Null si no se ha
     * podido realizar la conexión o una instancia de FPTClient si ha conseguido
     * conectarse y loguearse.
     *
     * @param cliente El cliente FTP o null si no ha logrado conectar y loguear.
     */
    protected void resultadoConexion(FTPClient cliente) {
        // Intentar conectar.
        if (!(cliente == null)) {
            // Si se consigue conectar y listar los elementos muestra la ventana del cliente FTP.
            this.clienteFtp = cliente;
            if (this.refrescarListado()) {
                this.intentantoConectar = false;
                this.vLogueo.setIntentandoConectar(false);
                this.vCliente.setEstado("Conectado.");
                this.vCliente.setNombreServidor(this.servidor);
                this.vCliente.setNombreUsuario(this.usuario);
                this.actualizarRutaActualFtp();
                this.vLogueo.mostrar(false);
                this.vCliente.mostrar(true);
                this.vLogueo.setEstado("Pulsa en conectar para volver a conectar de nuevo.");
                this.guardarDatosSesionAnterior();
            } else {
                this.vLogueo.setEstado("Fallo listar elementos, comprueba el cortafuegos.");
                this.intentantoConectar = false;
                this.vLogueo.setIntentandoConectar(false);
            }

        } else {
            this.vLogueo.setEstado("Fallo al conectar.");
            this.intentantoConectar = false;
            this.vLogueo.setIntentandoConectar(false);
        }
    }

    /**
     * Sube los archivos seleccionados a la nube.
     */
    protected void pulsadoSubir() {

        // Cancelar subida
        if (this.subiendo) {
            this.anularSubidas();
            // Empezar a subir si hay archivos
        } else {
            // Obtener los archivos
            File archivos[] = this.vCliente.seleccionarArchivos();

            if (archivos != null) {
                this.numeroArchivosASubir = archivos.length;
                this.subiendo = true;
                this.vCliente.setSubiendo(true);
                this.vCliente.setEstado("Subiendo archivos...");
                this.grupoSubida = new HiloGenerico[this.numeroArchivosASubir];

                HiloGenerico hiloTemporal;
                int i = 0;
                for (File archivo : archivos) {
                    hiloTemporal = new HiloGenerico(this, servidor, usuario, contrasenia, archivo, getRutaActualRemota());
                    this.grupoSubida[i] = hiloTemporal;
                    this.crearHilo(hiloTemporal);
                    i++;
                }
            } else {
                this.vCliente.setEstado("No hay nada que subir.");
            }
        }
    }

    // Anula todas las subidas.
    private void anularSubidas() {
        this.subiendo = false;
        this.vCliente.setSubiendo(false);
        this.desconectarHilos(this.grupoSubida);
        this.numeroArchivosASubir = 0;
        this.numeroArchivosSubidos = 0;
        this.vCliente.setEstado("Subidas canceladas.");
        System.out.println("Subidas anuladas.");
    }

    /**
     * Notifica que el archivo ha sido descargado, para llevar el control.
     *
     * @param valor Indica si se ha subido o no el archivo.
     */
    protected synchronized void archivoSubido(boolean valor) {
        if (valor) {
            this.numeroArchivosSubidos++;
        } else {
            this.numeroArchivosASubir -= 1;
        }

        if (numeroArchivosSubidos >= numeroArchivosASubir && this.subiendo == true) {
            this.numeroArchivosSubidos = 0;
            this.numeroArchivosASubir = 0;
            this.subiendo = false;
            this.vCliente.setSubiendo(false);

            this.vCliente.setEstado("Archivos subidos correctamente.");
            System.out.println("Todo subido.\n");
        }

        this.refrescarListado();
    }

    /**
     * Descarga los archivos en la carpeta descargas.
     *
     * @param nombreArchivos Listado con los nombres de elementos que se van a
     * descargar.
     */
    protected void pulsadoDescargar(List<String> nombreArchivos) {

        // Cancelar descarga.
        if (this.descargando) {
            this.anularDescargas();
            // Empezar descargar si hay algo seleccionado.
        } else {
            this.numeroArchivosADescargar = nombreArchivos.size();
            if (this.numeroArchivosADescargar > 0) {
                this.descargando = true;
                this.vCliente.setDescargando(true);
                this.vCliente.setEstado("Descargando archivos...");
                this.comprobarDirectorioDescargas();
                this.grupoDescarga = new HiloGenerico[this.numeroArchivosADescargar];

                HiloGenerico hiloTemporal;
                int i = 0;
                for (String nombreArchivo : nombreArchivos) {
                    hiloTemporal = new HiloGenerico(this, servidor, usuario, contrasenia, nombreArchivo, getRutaActualRemota());
                    this.grupoDescarga[i] = hiloTemporal;
                    this.crearHilo(hiloTemporal);
                    i++;
                }

            } else {
                this.vCliente.setEstado("No hay nada que descargar.");
            }
        }
    }

    // Anula las descargas.
    private void anularDescargas() {
        this.descargando = false;
        this.vCliente.setDescargando(false);
        this.desconectarHilos(this.grupoDescarga);
        this.numeroArchivosADescargar = 0;
        this.numeroArchivosDescargados = 0;
        this.vCliente.setEstado("Descargas canceladas.");
        System.out.println("Descargas anuladas.");
    }

    /**
     * Notifica que el archivo ha sido descargado, para llevar el control.
     *
     * @param valor Indica si se ha bajado o no el archivo.
     */
    protected synchronized void archivoDescargado(boolean valor) {
        if (valor) {
            this.numeroArchivosDescargados++;
        } else {
            this.numeroArchivosADescargar -= 1;
        }

        if (numeroArchivosDescargados >= numeroArchivosADescargar && this.descargando == true) {
            this.numeroArchivosDescargados = 0;
            this.numeroArchivosADescargar = 0;
            this.descargando = false;
            this.vCliente.setDescargando(false);

            this.vCliente.setEstado("Archivos descargados correctamente.");
            System.out.println("Todo descargado.\n");
        }

        this.refrescarBarraProgreso();
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
                cadenaResultado = "Elementos borrados correctamente.";
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
        this.refrescarBarraProgreso();

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

        System.out.println("Accion: desconectar");
        this.anularDescargas();
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
            this.vCliente.setRutaDirectoriosRemoto(getRutaActualRemota());
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
        this.vCliente.setRutaDirectoriosRemoto(getRutaActualRemota());
    }

    /**
     * Obtiene la ruta actual remota.
     *
     * @return Cadena con el nombre de la ruta remota.
     */
    protected String getRutaActualRemota() {
        try {
            return this.clienteFtp.printWorkingDirectory();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Permite escribir un mensaje en la ventana cliente.
     *
     * @param mensaje Cadena con el mensaje.
     */
    protected void setMensajeCliente(String mensaje) {
        this.vCliente.setEstado(mensaje);
    }

    /**
     * Muestra información sobre el estado del cliente FTP.
     */
    public void mostrarInfoFtp() {
        System.out.println("INFO FTP: " + this.clienteFtp.getReplyString());
    }

    // Inicia el hilo con el objeto pasado.
    private void crearHilo(HiloGenerico objetoHilo) {
        Thread hilo = new Thread(objetoHilo);
        hilo.start();
    }

    // Avisa a todos los hilos pasados por parámetro que han de dejar lo que estén haciendo.
    private void desconectarHilos(HiloGenerico[] grupoHilos) {
        if (!(grupoHilos == null)) {
            for (HiloGenerico h : grupoHilos) {
                h.desconectarHilo();
            }
        }
    }

    // Controla la barra de progreso por numero de archivos subidos / descargados.
    private void refrescarBarraProgreso() {
        int archivosEnCola = this.numeroArchivosASubir + this.numeroArchivosADescargar;
        int archivosTerminados = this.numeroArchivosSubidos + this.numeroArchivosDescargados;

        this.vCliente.actualizarProgreso(archivosEnCola, archivosTerminados);
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

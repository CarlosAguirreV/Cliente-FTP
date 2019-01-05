package clienteftp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

/**
 * Clase GuiClienteFtp. Interactua con el servidor FTP.
 *
 * @since 05/01/2019
 * @author Carlos Aguirre Vozmediano
 */
public class GuiClienteFtp extends JFrame {

    private Control padre;
    private JButton btnSubir, btnDescargar, btnEliminar, btnCrearDirectorio, btnRefrescar, btnDesconectar;
    private JPanel pnlGlobal, pnlGlobalCentro, pnlCentroNorte, pnlGlobalEste, pnlCentroNorteA, pnlGlobalSur;
    private DefaultListModel modeloLista;
    private JList listadoElementos;
    private JLabel lblServidor, lblUsuario, lblDirectorioFtp, lblEstado;
    private JProgressBar pbProgreso;
    private JFileChooser escogedorArchivos;

    /**
     * Contructor de la clase control. Se encarga de dejar todos los elementos
     * bien inicializados y preparados.
     *
     * @param padre Permite la comunicación con la instancia de control.
     */
    public GuiClienteFtp(Control padre) {
        this.padre = padre;
        this.crearObjetos();
        this.definirTexto();
        this.definirTextoAyuda();
        this.definirEstilo();
        this.crearDistribucion();
        this.aniadirElementos();
        this.eventos();

        this.setSize(600, 400);
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
    }

    // Crea los objetos.
    private void crearObjetos() {
        this.btnSubir = new JButton();
        this.btnDescargar = new JButton();
        this.btnEliminar = new JButton();
        this.btnCrearDirectorio = new JButton();
        this.btnRefrescar = new JButton();
        this.btnDesconectar = new JButton();
        this.pnlGlobal = new JPanel();
        this.pnlGlobalCentro = new JPanel();
        this.pnlCentroNorte = new JPanel();
        this.pnlGlobalEste = new JPanel();
        this.pnlCentroNorteA = new JPanel();
        this.pnlGlobalSur = new JPanel();
        this.modeloLista = new DefaultListModel();
        this.listadoElementos = new JList(modeloLista);
        this.lblServidor = new JLabel();
        this.lblUsuario = new JLabel();
        this.lblDirectorioFtp = new JLabel();
        this.lblEstado = new JLabel();
        this.pbProgreso = new JProgressBar(0, 100);
        this.escogedorArchivos = new JFileChooser();
    }

    // Define el texto de todos los elementos.
    private void definirTexto() {
        this.setTitle("Conexión a servidor FTP");
        this.btnSubir.setText("Subir");
        this.btnDescargar.setText("Descargar");
        this.btnEliminar.setText("Eliminar");
        this.btnCrearDirectorio.setText("Crear directorio");
        this.btnRefrescar.setText("Refrescar");
        this.btnDesconectar.setText("Desconectar");
    }

    // Define los textos que se mostrarán al colocar el mouse sobre algun elemento.
    private void definirTextoAyuda() {
        this.btnSubir.setToolTipText("Selecciona el archivo/s que deseas subir");
        this.btnDescargar.setToolTipText("Descarga los archivos seleccionados en la ruta " + this.padre.getRutaCompletaDescargas());
        this.btnEliminar.setToolTipText("Elimina los archivos y directorios seleccionados");
        this.btnCrearDirectorio.setToolTipText("Crea una nueva carpeta en el directorio remoto del servidor ftp");
        this.btnRefrescar.setToolTipText("Refrescar listado de directorios y archivos");
        this.btnDesconectar.setToolTipText("Sale de esta sesión y vuelve al menu de logueo");
    }

    // Define los estilos de todos los elementos de la interfaz, colores, iconos y bordes.
    private void definirEstilo() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(GuiClienteFtp.class.getResource("/recursos/icono.png")));
        this.btnSubir.setIcon(new ImageIcon(getClass().getResource("/recursos/subir.png")));
        this.btnDescargar.setIcon(new ImageIcon(getClass().getResource("/recursos/descargar.png")));
        this.btnEliminar.setIcon(new ImageIcon(getClass().getResource("/recursos/eliminar.png")));
        this.btnCrearDirectorio.setIcon(new ImageIcon(getClass().getResource("/recursos/crear.png")));
        this.btnRefrescar.setIcon(new ImageIcon(getClass().getResource("/recursos/limpiar.png")));
        this.btnDesconectar.setIcon(new ImageIcon(getClass().getResource("/recursos/desconectar.png")));
        this.lblEstado.setIcon(new ImageIcon(getClass().getResource("/recursos/info.png")));
        this.pnlGlobal.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.pnlCentroNorteA.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        this.pnlGlobalEste.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        this.pnlGlobalSur.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        this.pnlCentroNorte.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.pnlGlobalCentro.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.pbProgreso.setForeground(GuiLogueo.COLOR_BOTONES);
        this.pbProgreso.setBackground(GuiLogueo.COLOR_CAMPOS);
        this.pnlCentroNorteA.setBackground(GuiLogueo.COLOR_FONDO);
        this.pnlCentroNorte.setBackground(GuiLogueo.COLOR_FONDO);
        this.pnlGlobalEste.setBackground(GuiLogueo.COLOR_FONDO2);
        this.pnlGlobalSur.setBackground(GuiLogueo.COLOR_FONDO2);
        this.pnlGlobal.setBackground(GuiLogueo.COLOR_FONDO2);
        this.btnSubir.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnDescargar.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnEliminar.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnCrearDirectorio.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnRefrescar.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnDesconectar.setBackground(GuiLogueo.COLOR_BOTONES);
        this.btnSubir.setCursor(GuiLogueo.CURSOR_BOTONES);
        this.btnDescargar.setCursor(GuiLogueo.CURSOR_BOTONES);
        this.btnEliminar.setCursor(GuiLogueo.CURSOR_BOTONES);
        this.btnCrearDirectorio.setCursor(GuiLogueo.CURSOR_BOTONES);
        this.btnDesconectar.setCursor(GuiLogueo.CURSOR_BOTONES);
        this.lblEstado.setForeground(GuiLogueo.COLOR_LETRA);
        this.pbProgreso.setStringPainted(false);
    }

    // Crea las distintas distribuciones para los distintos paneles.
    private void crearDistribucion() {
        this.getContentPane().setLayout(new BorderLayout());
        this.pnlGlobal.setLayout(new BorderLayout());
        this.pnlGlobalCentro.setLayout(new BorderLayout());
        this.pnlCentroNorte.setLayout(new GridLayout(2, 1));
        this.pnlGlobalEste.setLayout(new GridLayout(6, 1, 10, 10));
        this.pnlCentroNorteA.setLayout(new GridLayout(1, 2, 5, 5));
        this.pnlGlobalSur.setLayout(new GridLayout(2, 1, 5, 5));
    }

    // Añade elementos a los paneles.
    private void aniadirElementos() {
        this.getContentPane().add(this.pnlGlobal);
        this.pnlGlobal.add(pnlGlobalCentro, BorderLayout.CENTER);
        this.pnlGlobal.add(pnlGlobalSur, BorderLayout.SOUTH);
        this.pnlGlobal.add(pnlGlobalEste, BorderLayout.EAST);
        this.pnlCentroNorteA.add(lblServidor);
        this.pnlCentroNorteA.add(lblUsuario);
        this.pnlGlobalCentro.add(pnlCentroNorte, BorderLayout.NORTH);
        this.pnlCentroNorte.add(pnlCentroNorteA);
        this.pnlCentroNorte.add(lblDirectorioFtp);
        this.pnlGlobalCentro.add(new JScrollPane(listadoElementos), BorderLayout.CENTER);
        this.pnlGlobalEste.add(btnSubir);
        this.pnlGlobalEste.add(btnDescargar);
        this.pnlGlobalEste.add(btnEliminar);
        this.pnlGlobalEste.add(btnCrearDirectorio);
        this.pnlGlobalEste.add(btnRefrescar);
        this.pnlGlobalEste.add(btnDesconectar);
        this.pnlGlobalSur.add(pbProgreso);
        this.pnlGlobalSur.add(lblEstado);
    }

    /**
     * Muestra la ventana.
     *
     * @param valor true mostrar, false ocultar.
     */
    public void mostrar(boolean valor) {
        this.setVisible(valor);
    }

    /**
     * Muestra el nombre del servidor, su dirección IP.
     *
     * @param nombre Direccion del servidor.
     */
    protected void setNombreServidor(String nombre) {
        this.lblServidor.setText("Servidor: " + nombre);
    }

    /**
     * Muestra el nombre del usuario.
     *
     * @param nombre Nombre del usuario.
     */
    protected void setNombreUsuario(String nombre) {
        this.lblUsuario.setText("Usuario: " + nombre);
    }

    /**
     * Muestra el nombre del directorio en el que estamos actualmente.
     *
     * @param nombre Nombre del directorio remoto.
     */
    protected void setRutaDirectoriosRemoto(String nombre) {
        this.lblDirectorioFtp.setText("Ruta remoto: " + nombre);
    }

    /**
     * Muestra el mensaje que tu quieras.
     *
     * @param estado Cadena con el mensaje.
     */
    protected void setEstado(String estado) {
        this.lblEstado.setText("Estado: " + estado);
    }

    /**
     * Muestra un listado con las cadenas obtenidas.
     *
     * @param nombresArchivo Listado con los nombres de archivo.
     */
    protected void setListado(String[] nombresArchivo) {
        this.modeloLista.clear();
        if (nombresArchivo.length == 0) {
            this.listadoElementos.setBackground(GuiLogueo.COLOR_FONDO_VACIO);
        } else {
            this.listadoElementos.setBackground(Color.white);
            for (String nombre : nombresArchivo) {
                this.modeloLista.addElement(nombre);
            }
        }

    }

    /**
     * Controla todos los eventos de la interfaz gráfica.
     */
    private void eventos() {
        // Botón subir.
        this.btnSubir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                escogedorArchivos.setMultiSelectionEnabled(true);
                int botonPulsado = escogedorArchivos.showDialog(GuiClienteFtp.this, "Seleccionar el archivo/s");
                if (botonPulsado == JFileChooser.APPROVE_OPTION) {
                    File[] archivos = escogedorArchivos.getSelectedFiles();
                    padre.subirArchivos(archivos);
                }
                pbProgreso.setStringPainted(true);
            }
        });
        
        // Botón descargar.
        this.btnDescargar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                padre.bajarArchivos(listadoElementos.getSelectedValuesList());
            }
        });
        
        // Botón eliminar.
        this.btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                padre.eliminarArchivoDirectorio(listadoElementos.getSelectedValuesList());
            }
        });
        
        // Botón crear directorio.
        this.btnCrearDirectorio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String nombreCarpeta = JOptionPane.showInputDialog(GuiClienteFtp.this, "Escribe el nombre de la carpeta", "Crear carpeta", JOptionPane.QUESTION_MESSAGE);
                if (!(nombreCarpeta == null)) {
                    if (!nombreCarpeta.trim().isEmpty()) {
                        padre.crearCarpeta(nombreCarpeta);
                    }
                }
            }
        });
        
        // Botón refrescar.
        this.btnRefrescar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                padre.refrescarListado();
                setEstado("Refrescado correctamente.");
            }
        });
        
        // Botón desconectar.
        this.btnDesconectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                GuiClienteFtp.this.padre.pulsadoDesconectar();
            }
        });
        
        // Eventos del ratón.
        this.listadoElementos.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {

                // Doble clic + Elementos seleccionados es al menos 1.
                if (me.getClickCount() == 2 && listadoElementos.getSelectedIndex() > -1) {
                    padre.cambiarDirectorio(listadoElementos.getSelectedValue().toString());
                }

                // Clic derecho.
                if (me.getButton() == 3) {
                    padre.volverDirectorioAnterior();
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });

        // Al cerrar la ventana.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GuiClienteFtp.this.padre.desconectar();
                System.exit(0);
            }
        });
    }
}

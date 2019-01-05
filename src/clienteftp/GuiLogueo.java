package clienteftp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Clase Logueo. Pide los datos de sesión al usuario para permitir la conexión
 * con el servidor FTP.
 *
 * @since 05/01/2019
 * @author Carlos Aguirre Vozmediano
 */
public class GuiLogueo extends JFrame {

    private Control padre;
    private JPanel pnlCentro, pnlSur;
    private JTextField txtUsuario, txtServidor;
    private JPasswordField passContrasenia;
    private boolean mostrarContrasenia;
    private JButton btnLimpiar, btnCargarSesion, btnConectar, btnSalir;
    private JLabel lblEstado, lblContraseniaInfo;
    protected static final Color COLOR_FONDO = new Color(254, 244, 235);
    protected static final Color COLOR_FONDO2 = new Color(20, 133, 135);
    protected static final Color COLOR_FONDO_VACIO = new Color(250, 240, 220);
    protected static final Color COLOR_CAMPOS = new Color(234, 255, 235);
    protected static final Color COLOR_BOTONES = new Color(250, 217, 183);
    protected static final Color COLOR_LETRA = new Color(255, 255, 255);
    protected static final Cursor CURSOR_BOTONES = new Cursor(Cursor.HAND_CURSOR);

    /**
     * Contructor de la clase control. Se encarga de dejar todos los elementos
     * bien inicializados y preparados.
     *
     * @param padre Permite la comunicación con la instancia de control.
     */
    public GuiLogueo(Control padre) {
        this.padre = padre;
        this.mostrarContrasenia = false;
        this.crearObjetos();
        this.definirTexto();
        this.definirTextoAyuda();
        this.definirEstilo();
        this.crearDistribucion();
        this.aniadirElementos();
        this.eventos();

        this.setSize(640, 240);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
    }

    // Crea los objetos.
    private void crearObjetos() {
        this.pnlCentro = new JPanel();
        this.pnlSur = new JPanel();
        this.txtUsuario = new JTextField();
        this.passContrasenia = new JPasswordField();
        this.txtServidor = new JTextField();
        this.btnLimpiar = new JButton();
        this.btnCargarSesion = new JButton();
        this.btnConectar = new JButton();
        this.btnSalir = new JButton();
        this.lblEstado = new JLabel();
        this.lblContraseniaInfo = new JLabel();
    }

    // Define el texto de todos los elementos.
    private void definirTexto() {
        this.setTitle("Conexión a servidor FTP");
        this.btnConectar.setText("Conectar");
        this.btnLimpiar.setText("Limpiar");
        this.btnCargarSesion.setText("Sesion anterior");
        this.btnSalir.setText("Salir");
        this.lblEstado.setText("Introduce los datos de sesión");
        this.lblContraseniaInfo.setText("Contraseña (ocultar)");
    }

    // Define los textos que se mostrarán al colocar el mouse sobre algun elemento.
    private void definirTextoAyuda() {
        this.txtServidor.setToolTipText("Escribe la direccion del servidor, ejemplo: 192.168.1.100");
        this.txtUsuario.setToolTipText("Nombre de usuario, si no se escribe nada por defecto es Anonimous");
        this.passContrasenia.setToolTipText("Contraseña de usuario *Pulsa INTRO para ver/ocultar la contraseña");
        this.btnLimpiar.setToolTipText("Vacia todos los campos de texto");
        this.btnCargarSesion.setToolTipText("Carga la sesión anterior si la hay");
        this.btnConectar.setToolTipText("Intenta conectar con el servidor ftp");
        this.btnSalir.setToolTipText("Sale de la aplicación");
    }

    // Define los estilos de todos los elementos de la interfaz, colores, iconos y bordes.
    private void definirEstilo() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(GuiLogueo.class.getResource("/recursos/icono.png")));
        this.btnLimpiar.setIcon(new ImageIcon(getClass().getResource("/recursos/limpiar.png")));
        this.btnCargarSesion.setIcon(new ImageIcon(getClass().getResource("/recursos/cargar.png")));
        this.btnConectar.setIcon(new ImageIcon(getClass().getResource("/recursos/conectar.png")));
        this.btnSalir.setIcon(new ImageIcon(getClass().getResource("/recursos/salir.png")));
        this.pnlCentro.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.pnlSur.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.txtUsuario.setMargin(new Insets(0, 5, 0, 5));
        this.passContrasenia.setMargin(new Insets(0, 5, 0, 5));
        this.txtServidor.setMargin(new Insets(0, 5, 0, 5));
        this.pnlCentro.setBackground(COLOR_FONDO);
        this.txtUsuario.setBackground(COLOR_CAMPOS);
        this.passContrasenia.setBackground(COLOR_CAMPOS);
        this.txtServidor.setBackground(COLOR_CAMPOS);
        this.pnlSur.setBackground(COLOR_FONDO2);
        this.btnLimpiar.setBackground(COLOR_BOTONES);
        this.btnCargarSesion.setBackground(COLOR_BOTONES);
        this.btnConectar.setBackground(COLOR_BOTONES);
        this.btnSalir.setBackground(COLOR_BOTONES);
        this.btnLimpiar.setCursor(CURSOR_BOTONES);
        this.btnCargarSesion.setCursor(CURSOR_BOTONES);
        this.btnConectar.setCursor(CURSOR_BOTONES);
        this.btnSalir.setCursor(CURSOR_BOTONES);
    }

    // Crea las distintas distribuciones para los distintos paneles.
    private void crearDistribucion() {
        this.getContentPane().setLayout(new BorderLayout());
        this.pnlCentro.setLayout(new GridLayout(4, 2, 7, 5));
        this.pnlSur.setLayout(new GridLayout(1, 4, 7, 7));
    }

    // Añade elementos a los paneles.
    private void aniadirElementos() {
        this.getContentPane().add(this.pnlCentro, BorderLayout.CENTER);
        this.getContentPane().add(this.pnlSur, BorderLayout.SOUTH);

        this.pnlCentro.add(new JLabel("Servidor"));
        this.pnlCentro.add(this.txtServidor);

        this.pnlCentro.add(new JLabel("Usuario"));
        this.pnlCentro.add(this.txtUsuario);

        this.pnlCentro.add(this.lblContraseniaInfo);
        this.pnlCentro.add(this.passContrasenia);

        this.pnlCentro.add(new JLabel("Estado"));
        this.pnlCentro.add(this.lblEstado);

        this.pnlSur.add(this.btnLimpiar);
        this.pnlSur.add(this.btnCargarSesion);
        this.pnlSur.add(this.btnConectar);
        this.pnlSur.add(this.btnSalir);
    }

    /**
     * Muestra la ventana.
     *
     * @param valor true mostrar, false ocultar.
     */
    public void mostrar(boolean valor) {
        this.setVisible(valor);
    }

    // Muestra u oculta la contraseña.
    // Al llamar al metodo se alterna de forma automática.
    private void verOcultarContrasenia() {
        mostrarContrasenia = !mostrarContrasenia;
        if (mostrarContrasenia) {
            this.passContrasenia.setEchoChar((char) 0);
            this.lblContraseniaInfo.setText("Contraseña (ver)");
        } else {
            this.passContrasenia.setEchoChar('•');
            this.lblContraseniaInfo.setText("Contraseña (ocultar)");
        }
    }

    /**
     * Obtiene el nombre del servidor.
     *
     * @return String Nombre del sevidor.
     */
    protected String getServidor() {
        return this.txtServidor.getText();
    }

    /**
     * Obtiene el nombre de usuario.
     *
     * @return String Nombre de usuario.
     */
    protected String getUsuario() {
        return this.txtUsuario.getText();
    }

    /**
     * Obtiene la contraseña desglosada en un listado de caracteres.
     *
     * @return char[] Listado de caracteres que conforman la contraseña.
     */
    protected char[] getContrasenia() {
        return this.passContrasenia.getPassword();
    }

    /**
     * Establecer un nombre de servidor.
     *
     * @param cadena El nombre del servidor.
     */
    protected void setServidor(String cadena) {
        this.txtServidor.setText(cadena);
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param cadena El nombre del usuario.
     */
    protected void setUsuario(String cadena) {
        this.txtUsuario.setText(cadena);
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param cadena La contraseña del usuario.
     */
    protected void setContrasenia(String cadena) {
        this.passContrasenia.setText(cadena);
    }

    /**
     * Muestra el mensaje que tu quieras.
     *
     * @param estado Cadena con el mensaje.
     */
    protected void setEstado(String estado) {
        this.lblEstado.setText(estado);
    }

    /**
     * Controla todos los eventos de la interfaz gráfica.
     */
    private void eventos() {
        // Botón limpiar.
        this.btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                GuiLogueo.this.txtServidor.setText("");
                GuiLogueo.this.txtUsuario.setText("");
                GuiLogueo.this.passContrasenia.setText("");
                GuiLogueo.this.lblEstado.setText("Introduce los datos de sesión.");
                if (GuiLogueo.this.mostrarContrasenia) {
                    GuiLogueo.this.verOcultarContrasenia();
                }
            }
        });

        // Botón cargar sesión.
        this.btnCargarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (padre.cargarDatosSesionAnterior()) {
                    setEstado("Cargada correctamente.");
                } else {
                    setEstado("No se han encontrado datos de una sesión anterior.");
                }
            }
        });

        // Botón conectar.
        this.btnConectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                GuiLogueo.this.padre.pulsadoConectar();
                if (GuiLogueo.this.mostrarContrasenia) {
                    GuiLogueo.this.verOcultarContrasenia();
                }
            }
        });

        // Botón salir.
        this.btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });

        // Al pulsar la tecla INTRO en el campo de contraseñas. 
        this.passContrasenia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (ae.getID() == ActionEvent.ACTION_PERFORMED) {
                    verOcultarContrasenia();
                }
            }
        });
    }
}

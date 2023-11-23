import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class ListasDeYoutube {
    final ArrayList<EnlaceYouTube> enlaces = new ArrayList<>();
    private final Random random = new Random();
    private String ultimaBusqueda = null;

    public String getUltimaBusqueda() {
        return ultimaBusqueda;
    }

    public void setUltimaBusqueda(String ultimaBusqueda) {
        this.ultimaBusqueda = ultimaBusqueda;
    }

    public void agregarEnlace(String nombre, String enlace) {
        if (!enlaceExistente(enlace)) {
            agregarNuevoEnlace(new EnlaceYouTube(nombre, enlace, random.nextInt(201) + 120));
        }
    }

    public void eliminarEnlace(String codigoHash) {
        enlaces.removeIf(enlace -> enlace.getCodigoHash().equals(codigoHash));
    }

    public String buscarPorNombre(String parametroBusqueda) {
        ultimaBusqueda = parametroBusqueda;
        return enlaces.stream()
                .filter(enlace -> enlace.getNombre().toLowerCase().contains(parametroBusqueda.toLowerCase()))
                .map(EnlaceYouTube::toString)
                .collect(Collectors.joining("\n\n"));
    }

    public String compartir() {
        return enlaces.stream().map(EnlaceYouTube::toString).collect(Collectors.joining("\n\n"));
    }

    private boolean enlaceExistente(String enlace) {
        return enlaces.stream().anyMatch(e -> e.getEnlace().equals(enlace));
    }

    private void agregarNuevoEnlace(EnlaceYouTube enlaceYouTube) {
        enlaces.add(enlaceYouTube);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ListasDeYoutube lista = new ListasDeYoutube();
            new InterfazUsuario(lista);
        });
    }
}

class InterfazUsuario {
    private final ListasDeYoutube lista;
    private final JFrame frame;
    private final JTextPane textPane;

    private static final String LOGO_URL = "https://th.bing.com/th/id/OIG.AEu2YaRJGpZFKgTEyk9V?w=200&h=200&rs=1&pid=ImgDetMain";

    public InterfazUsuario(ListasDeYoutube lista) {
        this.lista = lista;

        frame = new JFrame("Lista de Enlaces de YouTube");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");

        textPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                abrirEnlaceEnNavegador(e.getDescription());
            }
        });

        JScrollPane scrollPane = new JScrollPane(textPane);

        JButton agregarButton = crearBoton("Agregar Enlace", e -> {
            String nombre = JOptionPane.showInputDialog(frame, "Nombre del Video:");
            String enlace = JOptionPane.showInputDialog(frame, "Enlace del Video:");
            lista.agregarEnlace(nombre, enlace);
            actualizarTextPane();
        });

        JButton buscarButton = crearBoton("Buscar Enlace", e -> {
            String parametroBusqueda = JOptionPane.showInputDialog(frame, "Nombre a buscar:");
            String resultado = lista.buscarPorNombre(parametroBusqueda);
            mostrarVisualizacion(resultado);
        });

        JButton volverButton = crearBoton("Volver", e -> {
            mostrarVisualizacion(lista.compartir());
        });

        JButton eliminarButton = crearBoton("Eliminar Enlace", e -> {
            String codigoHash = JOptionPane.showInputDialog(frame, "Código Hash del Enlace a eliminar:");
            lista.eliminarEnlace(codigoHash);
            actualizarTextPane();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(agregarButton);
        buttonPanel.add(buscarButton);
        buttonPanel.add(volverButton);
        buttonPanel.add(eliminarButton);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private JButton crearBoton(String texto, ActionListener actionListener) {
        JButton button = new JButton(texto);
        button.addActionListener(actionListener);
        return button;
    }

    private void abrirEnlaceEnNavegador(String enlace) {
        try {
            Desktop.getDesktop().browse(new URI(enlace));
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    private void mostrarVisualizacion(String resultado) {
        String contenidoHtml = "<html><style>" +
                "div.enlace-container { border: 1px solid #808080; margin-bottom: 10px; padding: 10px; background-color: #808080; color: #ffffff; font-family: 'Arial', sans-serif; }" +
                "table { border-collapse: collapse; width: 100%; }" +
                "td { border: 1px solid #dddddd; text-align: left; padding: 8px; font-size: 14px; }" +
                "img { max-width: 100%; height: auto; display: block; margin: 0 auto; }" +
                "</style>";

        String[] enlacesArray = resultado.split("\n\n");
        for (String enlace : enlacesArray) {
            contenidoHtml += "<div class='enlace-container'>";
            contenidoHtml += "<table>";
            contenidoHtml += "<tr>";
            contenidoHtml += "<td><img src='" + LOGO_URL + "'></td>";
            contenidoHtml += "<td>" + enlace + "</td>";
            contenidoHtml += "</tr>";
            contenidoHtml += "</table>";
            contenidoHtml += "</div>";
        }

        contenidoHtml += "</html>";
        textPane.setText(contenidoHtml);
    }

    private void actualizarTextPane() {
        String resultado = lista.compartir();
        mostrarVisualizacion(resultado);
    }
}

class EnlaceYouTube {
    private final String nombre;
    private final String enlace;
    private final int duracion;
    private final String codigoHash;

    public EnlaceYouTube(String nombre, String enlace, int duracion) {
        this.nombre = nombre;
        this.enlace = enlace;
        this.duracion = duracion;
        this.codigoHash = String.valueOf(enlace.hashCode());
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public String getEnlace() {
        return enlace;
    }

    public String obtenerMiniatura() {
        return "https://img.youtube.com/vi/" + enlace.substring(enlace.lastIndexOf('/') + 1) + "/hqdefault.jpg";
    }

    public String toString() {
        return "Nombre: " + nombre + "<br>Enlace: <a href=\"" + enlace + "\">Ver en YouTube</a><br>Duración: " + duracion + " segundos<br>Código Hash: " + codigoHash;
    }
}

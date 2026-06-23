package com.cafeuniv.ppi.controller;

import com.cafeuniv.ppi.domain.Producto;
import com.cafeuniv.ppi.dto.CarritoItem;
import com.cafeuniv.ppi.dto.ClienteCarrito;
import com.cafeuniv.ppi.repository.ProductoRepository;
import com.cafeuniv.ppi.service.CarritoService;
import com.cafeuniv.ppi.service.SessionContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.text.Normalizer;

/**
 * Controlador del Dashboard para Clientes.
 * Muestra la interfaz principal para clientes autenticados y permite armar un carrito.
 */
public class DashboardClienteController {
    @FXML private Label nombreClienteLabel;
    @FXML private Label totalLabel;
    @FXML private Label mensajeLabel;
    @FXML private Label fichoLabel;
    @FXML private TextField buscarProductoField;
    @FXML private TableView<Producto> productosTable;
    @FXML private TableColumn<Producto, String> prodNombreCol;
    @FXML private TableColumn<Producto, Double> prodPrecioCol;
    @FXML private TableColumn<Producto, Void> prodAccionCol;
    @FXML private ImageView productImageView;
    @FXML private Label productImagePlaceholder;

    @FXML private TableView<CarritoItem> carritoTable;
    @FXML private TableColumn<CarritoItem, String> carritoNombreCol;
    @FXML private TableColumn<CarritoItem, Integer> carritoCantidadCol;
    @FXML private TableColumn<CarritoItem, Double> carritoPrecioCol;
    @FXML private TableColumn<CarritoItem, Double> carritoSubtotalCol;
    @FXML private TableColumn<CarritoItem, Void> carritoAccionCol;

    private final ProductoRepository productoRepository = new ProductoRepository();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private final ObservableList<CarritoItem> carrito = FXCollections.observableArrayList();
    private final CarritoService carritoService = CarritoService.getInstance();
    private final DateTimeFormatter fichoFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public void initialize() {
        if (SessionContext.getCurrentUser() != null) {
            nombreClienteLabel.setText("Hola, " + SessionContext.getCurrentUser().getNombre());
        }
        mensajeLabel.setStyle("-fx-text-fill: red;");
        fichoLabel.setText("");
        configurarTablas();
        cargarProductos();
        configurarBusquedaProductos();
        configurarSeleccionImagen();
        actualizarTotal();
        // Inicialización normal: no se muestra la ruta, solo la imagen cuando exista
    }

    /**
     * Configura el listener de selección de la tabla de productos.
     * Al cambiar la selección carga la imagen correspondiente en
     * `productImageView` y alterna la visibilidad del `productImagePlaceholder`.
     * Si no hay imagen válida se limpia la vista y se muestra el placeholder.
     */
    private void configurarSeleccionImagen() {
        productosTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
                productImageView.setImage(null);
                if (productImagePlaceholder != null) productImagePlaceholder.setVisible(true);
                return;
            }
            String imagePath = findImageForProduct(newItem);
            if (imagePath != null) {
                try {
                    Image img = new Image(imagePath, 180, 180, true, true, true);
                    productImageView.setImage(img);
                    if (productImagePlaceholder != null) productImagePlaceholder.setVisible(false);
                } catch (Exception e) {
                    productImageView.setImage(null);
                    if (productImagePlaceholder != null) productImagePlaceholder.setVisible(true);
                }
            } else {
                productImageView.setImage(null);
                if (productImagePlaceholder != null) productImagePlaceholder.setVisible(true);
            }
        });
    }

    /**
     * Busca y devuelve una URI (como String) válida para la imagen del producto.
     * Estrategia de búsqueda:
     *  1) Si `producto.getImagePath()` contiene un valor válido (URL, URI `file:` o ruta local), se usa.
     *  2) Si no, se generan variantes normalizadas del `producto.getNombre()` y se prueban extensiones
     *     comunes: .webp, .jpg, .jpeg, .png.
     *  3) Fallback: intenta buscar por id con el patrón `product-<id>.<ext>`.
     * Devuelve `null` si no encuentra ningún archivo.
     * Nota: la carpeta base está actualmente hardcodeada en `baseDir`.
     */
    private String findImageForProduct(Producto producto) {
        if (producto == null || producto.getNombre() == null) return null;
        // Carpeta donde se almacenan las imágenes locales de prueba.
        // TODO: externalizar esta ruta a `application.properties` o variable de entorno según el entorno.
        String baseDir = "C:/Users/esteb/Desktop/trabajos2semestre/PPI/imagenesproductos";
        // Primero, si el producto tiene imagePath explícito en la BD, úsalo
        try {
            String ip = producto.getImagePath();
            if (ip != null && !ip.isBlank()) {
                ip = ip.trim();
                // Si es URI file: o http(s):, devolverlo tal cual (o comprobar existencia local)
                if (ip.startsWith("file:") || ip.startsWith("http://") || ip.startsWith("https://")) {
                    return ip;
                }
                // Si es ruta absoluta en Windows (C:\\...) o UNIX (/...), comprobar existencia
                java.io.File abs = new java.io.File(ip);
                if (abs.exists() && abs.isFile()) {
                    return abs.toURI().toString();
                }
                // Si es nombre relativo/archivo (p. ej. cocacola.png), buscar en carpeta base
                java.io.File rel = new java.io.File(baseDir + java.io.File.separator + ip);
                if (rel.exists() && rel.isFile()) {
                    return rel.toURI().toString();
                }
            }
        } catch (Exception ignored) {}
        String rawName = producto.getNombre().trim();
        String normalized = normalizeName(rawName);
        String noSpaces = normalized.replaceAll("\\s+", "");
        String withHyphens = normalized.replaceAll("\\s+", "-");
        String[] candidates = new String[]{noSpaces, withHyphens, normalized.replaceAll("\\s+", ""), rawName.replaceAll("\\s+", ""), rawName.replaceAll("\\s+", "-"), rawName};
        String[] exts = new String[]{".webp", ".jpg", ".jpeg", ".png"};

        for (String cand : candidates) {
            if (cand == null || cand.isBlank()) continue;
            for (String ext : exts) {
                java.io.File f = new java.io.File(baseDir + java.io.File.separator + cand + ext);
                if (f.exists() && f.isFile()) {
                    return f.toURI().toString();
                }
            }
        }

        // Fallback: try by product id (e.g., product-12.webp)
        try {
            Long id = producto.getId();
            if (id != null) {
                for (String ext : exts) {
                    java.io.File f = new java.io.File(baseDir + java.io.File.separator + "product-" + id + ext);
                    if (f.exists() && f.isFile()) {
                        return f.toURI().toString();
                    }
                }
            }
        } catch (Exception ignored) {}

        return null;
    }
    /**
     * Normaliza un nombre de producto para buscar coincidencias en archivos:
     * - Quita acentos/diacríticos
     * - Convierte a minúsculas
     * - Elimina caracteres no alfanuméricos salvo espacios y guiones
     */
    private String normalizeName(String input) {
        if (input == null) return "";
        String n = Normalizer.normalize(input, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", ""); // elimina diacríticos (acentos)
        n = n.toLowerCase().replaceAll("[^a-z0-9\\s-]", "");
        n = n.trim();
        return n;
    }

    /**
     * Configura las tablas de productos y carrito.
     * Para productos usamos una FilteredList para poder filtrar por nombre desde la UI.
     */
    private void configurarTablas() {
        prodNombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        prodPrecioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));
        configurarBotonAgregar();

        // Lista filtrada que se muestra en la tabla (permite búsqueda por nombre)
        productosFiltrados = new FilteredList<>(productos, p -> true);
        productosTable.setItems(productosFiltrados);

        carritoNombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        carritoCantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        carritoPrecioCol.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        carritoSubtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        configurarBotonEliminarCarrito();
        carritoTable.setItems(carrito);
    }

    /**
     * Configura el campo de búsqueda para filtrar los productos por nombre
     * en la misma tabla del POS.
     */
    private void configurarBusquedaProductos() {
        if (buscarProductoField == null) return;

        buscarProductoField.textProperty().addListener((obs, oldValue, newValue) -> {
            String filtro = normalizeName(newValue);
            if (productosFiltrados == null) return;

            productosFiltrados.setPredicate(prod -> {
                if (prod == null) return false;
                if (filtro == null || filtro.isBlank()) return true;
                String nombreNorm = normalizeName(prod.getNombre());
                return nombreNorm.contains(filtro);
            });
        });
    }

    private void configurarBotonAgregar() {
        prodAccionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addButton = new Button("Agregar");
            {
                addButton.setOnAction(evt -> {
                    Producto producto = getTableView().getItems().get(getIndex());
                    agregarAlCarrito(producto);
                });
                addButton.getStyleClass().add("primary");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addButton);
                }
            }
        });
    }

    private void configurarBotonEliminarCarrito() {
        carritoAccionCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Quitar");
            {
                removeButton.setOnAction(evt -> {
                    CarritoItem item = getTableView().getItems().get(getIndex());
                    carrito.remove(item);
                    actualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    private void cargarProductos() {
        productos.setAll(productoRepository.findAll());
    }

    private void agregarAlCarrito(Producto producto) {
        if (producto == null) return;

        // Validar stock disponible antes de agregar al carrito del cliente
        int stockDisponible = producto.getStockActual();
        int yaEnCarrito = carrito.stream()
                .filter(i -> i.getProductoId().equals(producto.getId()))
                .mapToInt(CarritoItem::getCantidad)
                .sum();
        int totalSolicitado = yaEnCarrito + 1; // aquí siempre se agrega de a 1
        if (totalSolicitado > stockDisponible) {
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setText(String.format(
                    "Stock insuficiente para %s. Disponible: %d, solicitado: %d",
                    producto.getNombre(), stockDisponible, totalSolicitado));
            return;
        }

        CarritoItem existente = carrito.stream()
                .filter(i -> i.getProductoId().equals(producto.getId()))
                .findFirst()
                .orElse(null);
        if (existente != null) {
            existente.incrementarCantidad(1);
            carritoTable.refresh();
        } else {
            carrito.add(new CarritoItem(producto.getId(), producto.getNombre(), producto.getPrecio(), 1));
        }
        mensajeLabel.setText("");
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    protected void onGenerarFicho(ActionEvent event) {
        if (carrito.isEmpty()) {
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setText("Agrega al menos un producto antes de generar el ficho.");
            return;
        }
        Long userId = SessionContext.getCurrentUser() != null ? SessionContext.getCurrentUser().getId() : null;
        ClienteCarrito ficho = carritoService.crearFicho(userId, carrito);
        fichoLabel.setText(String.format("Ficho %s generado. Preséntalo antes de las %s.", ficho.getFicho(), ficho.getExpira().format(fichoFormatter)));
        mensajeLabel.setStyle("-fx-text-fill: #2e7d32;");
        mensajeLabel.setText("¡Listo! Te esperamos en caja.");
        carrito.clear();
        actualizarTotal();
    }

    @FXML
    protected void onCerrarSesion(ActionEvent event) {
        SessionContext.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("JazzCoffee - Bienvenido");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


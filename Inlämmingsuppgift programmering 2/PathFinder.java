//PROG2 VT2023, Inlämningsuppgift, del 2


import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class PathFinder extends Application {

    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private Pane outputArea;


    private Map<String, City> nodes = new HashMap<>();
    private Set<Line> lines = new HashSet<>();

    private ListGraph<Object> listGraph = new ListGraph<>();
    private City city1 = null;
    private City city2 = null;
    private final Warning warning = new Warning();

    private boolean itemChanged = false;

    private int nameVersion = 1;

    private final Button btnFindPath = new Button("Find Path");
    private final Button btnShowConnection = new Button("Show Connection");
    private final Button btnNewPlace = new Button("New Place");
    private final Button btnNewConnection = new Button("New Connection");
    private final Button btnChangeConnection = new Button("Change Connection");
    private ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

    private ImageView imageView;


    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root = new BorderPane();

        VBox vbox = new VBox();
        root.setTop(vbox);

        outputArea = new Pane();
        outputArea.setId("outputArea");
        root.getChildren().add(outputArea);


        MenuBar menu = new MenuBar();
        menu.setId("menu");
        vbox.getChildren().add(menu);
        Menu menuFile = new Menu("File");
        menuFile.setId("menuFile");
        menu.getMenus().add(menuFile);


        MenuItem menuNewMap = new MenuItem("New Map");
        menuNewMap.setId("menuNewMap");
        menuFile.getItems().add(menuNewMap);
        menuNewMap.setOnAction(new NewMapHandler());

        MenuItem menuOpenFile = new MenuItem("Open");
        menuOpenFile.setId("menuOpenFile");
        menuFile.getItems().add(menuOpenFile);
        menuOpenFile.setOnAction(new OpenHandler());


        MenuItem menuSaveFile = new MenuItem("Save");
        menuSaveFile.setId("menuSaveFile");
        menuFile.getItems().add(menuSaveFile);
        menuSaveFile.setOnAction(new SaveHandler());

        MenuItem menuSaveImage = new MenuItem("Save Image");
        menuSaveImage.setId("menuSaveImage");
        menuFile.getItems().add(menuSaveImage);
        menuSaveImage.setOnAction(new SaveImageHandler());

        MenuItem menuExit = new MenuItem("Exit");
        menuExit.setId("menuExit");
        menuFile.getItems().add(menuExit);
        menuExit.setOnAction(new ExitItemHandler());

        FlowPane buttons = new FlowPane();
        vbox.getChildren().add(buttons);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(5));
        buttons.setHgap(5);

        btnFindPath.setId("btnFindPath");
        btnFindPath.setOnAction(new FindPathHandler());

        btnShowConnection.setId("btnShowConnection");
        btnShowConnection.setOnAction(new ShowConnectionHandler());

        btnNewPlace.setId("btnNewPlace");
        btnNewPlace.setOnAction(new NewPlaceHandler());

        btnNewConnection.setId("btnNewConnection");
        btnNewConnection.setOnAction(new NewConnectionHandler());

        btnChangeConnection.setId("btnChangeConnection");
        btnChangeConnection.setOnAction(new ChangeConnectionHandler());

        buttons.getChildren().addAll(btnFindPath, btnShowConnection, btnNewPlace, btnNewConnection, btnChangeConnection);

        scene = new Scene(root, 600, 60);
        stage.setTitle("PathFinder");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(new ExitHandler());
        stage.show();
    }

    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Object object = event.getSource();
            if (object.getClass() == City.class) {
                City city = nodes.get(((City) object).getName());
                if (city.isSelected()) {
                    if (city1 == city) {
                        city1 = null;
                    }
                    if (city2 == city) {
                        city2 = null;
                    }
                    nodes.get(city.getName()).setUnselected();


                } else {
                    if (city1 == null) {
                        nodes.get(city.getName()).setSelected();
                        city1 = nodes.get(city.getName());

                    } else if (city1 != null && city2 == null) {
                        nodes.get(city.getName()).setSelected();
                        city2 = nodes.get(city.getName());
                    }
                }
            }
        }
    }

    class ExitItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class ExitHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent event) {
            if (itemChanged) {
                if(!warning.unsavedChanges()){
                    event.consume();
                }
            }
        }
    }


    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!itemChanged || warning.unsavedChanges()) {
                resetMap();

                Image backgroundImage = new Image("file:europa.gif");
                imageView = new ImageView();
                imageView.setImage(backgroundImage);

                outputArea.setLayoutY(60);
                outputArea.getChildren().add(imageView);
                stage.setWidth(backgroundImage.getWidth());
                stage.setHeight(60 + backgroundImage.getHeight());
                stage.setY(10);
                itemChanged = true;
            }
        }
    }

    private void open() {
        try {
            resetMap();
            BufferedReader reader = new BufferedReader(new FileReader("europa.graph"));
            imageView.setImage(new Image(reader.readLine()));
            outputArea.getChildren().add(imageView);

            String line = reader.readLine();
            if (line != null) {
                String[] tokens = line.split(";");
                for (int i = 0; i < tokens.length; i += 3) {

                    double numberX = (double) (Math.round(Double.parseDouble(tokens[i + 1])));
                    double numberY = (double) (Math.round(Double.parseDouble(tokens[i + 2] + 60)));

                    createCity(tokens[i], numberX, numberY);
                }

                while ((line = reader.readLine()) != null) {
                    String[] edges = line.split(";");
                    City cityFrom = nodes.get(edges[0]);
                    City cityTo = nodes.get(edges[1]);
                    String name = edges[2];
                    int w = Integer.parseInt(edges[3]);

                    try {
                        listGraph.connect(cityFrom, cityTo, name, w);
                        setLinesBetween(cityFrom, cityTo);
                    } catch (IllegalStateException e) {
                        System.err.println("STOP THERE!! This edge from " + cityFrom + ": " + listGraph.getEdgeBetween(cityTo, cityFrom) + ", already exists!\n");
                    }
                }
            }

            itemChanged = true;
            reader.close();

        } catch (FileNotFoundException e) {
            warning.noMapToOpen();
        } catch (IOException e) {
            System.err.println("ops");
        }
    }

    class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!itemChanged || warning.unsavedChanges()) {
                open();
            }

        }
    }

    private void save() {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("europa.graph"));
            writer.write(imageView.getImage().getUrl() + "\n");
            StringBuilder edgeInfo = new StringBuilder();

            if (!nodes.isEmpty()) {
                for (City city : nodes.values()) {

                    double numberX = (double) (Math.round(city.getPosX()));
                    double numberY = (double) (Math.round(city.getPosY()));

                    writer.write(city.getName() + ";" + numberX + ";" + numberY + ";");
                    Collection<Edge> edges = listGraph.getEdgesFrom(city);

                    for (var edge : edges) {
                        edgeInfo.append(city.getName()).append(";").append(edge.getDestination()).append( ";").append(edge.getName()).append( ";").append(edge.getWeight()).append( "\n");
                    }
                }

                writer.write("\n" + edgeInfo);
            }
            itemChanged = false;
            writer.close();

        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Can't open file!");
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
            alert.showAndWait();
        }
    }


    class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            save();
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                WritableImage image = root.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            resetNodes();
            btnNewPlace.setDisable(true);
            scene.setCursor(Cursor.CROSSHAIR);

            outputArea.setOnMouseClicked(eventMouse -> {
                double y = eventMouse.getY();
                double x = eventMouse.getX();
                String defaultName = "node" + nameVersion;
                if (nodes.containsKey(defaultName)) {
                    nameVersion += 1;
                    defaultName = "node" + nameVersion;
                }
                TextInputDialog textDialog = new TextInputDialog(defaultName);
                textDialog.setTitle("Name");
                textDialog.setHeaderText(null);
                textDialog.setContentText("Name of place:");
                Optional<String> inputValue = textDialog.showAndWait();

                if(inputValue.isPresent()){
                    if (inputValue.get().equals("")) {
                        throw new IllegalArgumentException();
                    }
                    if (inputValue.get().trim().isEmpty()) {
                        warning.emptyInput();
                        return;
                    } else {
                        outputArea.setOnMouseClicked(null);
                        createCity(inputValue.get(), x, y);
                        itemChanged = true;
                    }
                    scene.setCursor(Cursor.DEFAULT);
                    btnNewPlace.setDisable(false);
                }
            });
        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {

            if (city1 != null && city2 != null) {

                if (listGraph.getEdgeBetween(city1, city2) != null) {
                    warning.edgeExist();
                    return;
                }

                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.setTitle("Connection");
                dialog.setHeaderText("Connection from " + city1.getName() + " to " + city2.getName());

                okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

                GridPane gridPane = setGridLayout(new GridPane());

                TextField name = setNameInput();
                TextField weight = setWeightInput();

                gridPane.addRow(1, new Label("Name:"), name);
                gridPane.addRow(2, new Label("Weight:"), weight);

                dialog.getDialogPane().setContent(gridPane);
                Platform.runLater(name::requestFocus);
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == okButton && city1 != null && city2 != null) {
                        return new Pair<>(name.getText(), weight.getText());
                    } else
                        return null;
                });

                Optional<Pair<String, String>> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (name.getText().trim().equals("") || weight.getText().trim().equals("")) {
                        warning.emptyInput();
                    } else {
                        try {
                            listGraph.connect(city1, city2, name.getText(), Integer.parseInt(weight.getText()));
                            setLinesBetween();
                            itemChanged = true;
                        } catch (IllegalStateException e) {
                            warning.edgeExist();
                            dialog.close();
                        }
                    }
                }
            } else {
                warning.selectTwo();
            }
        }
    }

    class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (city1 == null || city2 == null) {
                warning.selectTwo();
                return;
            }
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Connection");
            GridPane gridPane = new GridPane();
            setGridLayout(gridPane);

            TextField name = new TextField(listGraph.getEdgeBetween(city1, city2).getName().toString());
            String edgeWeight = listGraph.getEdgeBetween(city1, city2).getWeight() + "";
            TextField time = new TextField(edgeWeight);
            dialog.setHeaderText("Connection from " + city1 + " to " + city2);
            okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

            gridPane.addRow(1, new Label("Name:"), name);
            gridPane.addRow(2, new Label("Time:"), time);
            name.setDisable(true);
            time.setDisable(true);
            dialog.getDialogPane().setContent(gridPane);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButton) {
                    return new Pair<>(name.getText(), time.getText());
                } else {
                    warning.selectTwo();
                }
                return null;
            });
            dialog.showAndWait();
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (city1 == null || city2 == null) {
                warning.selectTwo();
                return;
            }
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Connection");
            GridPane gridPane = new GridPane();
            setGridLayout(gridPane);

            try {
                String edgeWeight = listGraph.getEdgeBetween(city1, city2).getWeight() + "";
                TextField name = new TextField(listGraph.getEdgeBetween(city1, city2).getName().toString());
                TextField time = new TextField(edgeWeight);
                dialog.setHeaderText("Connection from " + city1 + " to " + city2);
                okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

                gridPane.addRow(1, new Label("Name:"), name);
                gridPane.addRow(2, new Label("Time:"), time);
                name.setDisable(true);
                dialog.getDialogPane().setContent(gridPane);

                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == okButton && city1 != null && city2 != null) {
                        return new Pair<>(name.getText(), time.getText());
                    }
                    return null;
                });
                Optional<Pair<String, String>> result = dialog.showAndWait();

                if (result.isPresent()) {
                    if (time.getText().trim().equals("")) {
                        warning.emptyInput();
                    } else {
                        listGraph.setConnectionWeight(city1, city2, Integer.parseInt(time.getText()));
                        resetNodes();
                        itemChanged = true;
                    }
                }
            } catch (NullPointerException e) {
                warning.noEdgeExist();
            } catch (NoSuchElementException e) {
                warning.selectTwo();
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> { //OK
        @Override
        public void handle(ActionEvent event) {
            StringBuilder edgeInfo = new StringBuilder();
            if (city1 != null && city2 != null) {
                if (listGraph.getEdgesFrom(city1).isEmpty() || listGraph.getEdgesFrom(city2).isEmpty()) {
                    warning.noEdgeExist();
                    return;
                }

                Dialog<Pair<String, String>> dialog = new Dialog<>();
                dialog.setTitle("Message");
                GridPane gridPane = new GridPane();
                setGridLayout(gridPane);

                if (listGraph.getPath(city1, city2).isEmpty()) {
                    dialog.setContentText("There is no path between" + city1 + " and " + city2 + ".");
                } else {
                    int number = 0;
                    for (Object e : listGraph.getPath(city1, city2)) {
                        edgeInfo.append(e.toString()).append("\n");
                        String[] edgeString = e.toString().split(" takes ");
                        int i = Integer.parseInt(edgeString[edgeString.length - 1]);
                        number += i;
                    }
                    edgeInfo.append("Total ").append(number);
                }

                TextArea textArea = new TextArea(edgeInfo.toString());
                dialog.setHeaderText("The Path from " + city1 + " to " + city2);
                okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(okButton);
                textArea.setEditable(false);
                gridPane.addRow(1, textArea);
                dialog.getDialogPane().setContent(gridPane);
                dialog.showAndWait();
            } else {
                warning.selectTwo();
            }
        }
    }

    private void resetMap() {
        resetNodes();
        listGraph = new ListGraph<>();
        outputArea.getChildren().clear();
        nodes.clear();
        lines.clear();
    }

    private void resetNodes() {
        if (city1 != null) {
            nodes.get(city1.getName()).setUnselected();
        }
        if (city2 != null) {
            nodes.get(city2.getName()).setUnselected();
        }
        city1 = null;
        city2 = null;
    }

    private void setLinesBetween(City node1, City node2) {
        Line line = new Line();
        line.setStartX(node1.getPosX());
        line.setStartY(node1.getPosY());
        line.setEndX(node2.getPosX());
        line.setEndY(node2.getPosY());
        line.setStrokeWidth(2);
        line.setDisable(true);
        outputArea.getChildren().add(line);
        node1.toFront();
        node2.toFront();
        lines.add(line);
    }

    private void setLinesBetween() {
        if (city1 != null && city2 != null) {
            setLinesBetween(city1, city2);
        }
    }

    private GridPane setGridLayout(GridPane gridPane) {
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        return gridPane;
    }


    private void createCity(String name, double posX, double posY) {
        ClickHandler handler = new ClickHandler();
        City city = new City(name, posX, posY, 8.0);
        city.setOnMouseClicked(handler);
        city.setId(city.getName());
        nodes.put(city.getName(), city);
        listGraph.add(city);
        Text text = new Text(city.getPosX(), city.getPosY() + 18, city.getName());
        outputArea.getChildren().addAll(city, text);
    }

    private TextField setNameInput() {
        return new TextField(city1.getName() + " -> " + city2.getName());
    }

    private TextField setWeightInput() {
        return new TextField("15");
    }

}
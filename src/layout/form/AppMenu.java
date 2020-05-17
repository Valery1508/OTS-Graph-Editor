package layout.form;

import com.sun.javafx.geom.Edge;
import controller.FileProcessor;
import controller.GraphController;
import controller.GraphProducer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import layout.DrawableArc;
import layout.DrawableNode;
import model.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.HierholzerEulerianCycle;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.jgrapht.alg.shortestpath.EppsteinKShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.util.*;

import static javafx.application.Application.STYLESHEET_MODENA;
import static layout.DrawableNode.CIRCLE_RADIUS;
import static sample.Main.MAIN_FORM_HEIGHT;
import static sample.Main.MAIN_FORM_WIDTH;


public class AppMenu{
    private static final String FILE_FORMAT = "*.graph";

    private GraphTabPane graphTabPane;

    private GraphJ graph;
    private MenuBar menuBar;
    private Stage ownerStage;


    public AppMenu(GraphTabPane graphTabPane, Stage stage) {
        this.graphTabPane = graphTabPane;

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(
                createFileMenu(),
                createEditMenu(),
                createMetricsMenu()
        );

        ownerStage = stage;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }


    /*
        Menus creating
     */

    // Creating of the file menu
    private Menu createFileMenu() {
        Menu file = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        MenuItem openFile = new MenuItem("Open");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem closeFile = new MenuItem("Close");

        newFile.setOnAction(newGraphEventHandler);
        openFile.setOnAction(openGraphEventHandler);
        saveFile.setOnAction(saveGraphEventHandler);

        file.getItems().addAll(newFile, openFile, saveFile, closeFile);

        return file;
    }

    // Creating of edit menu
    private Menu createEditMenu() {
        Menu edit = new Menu("Edit");
        Menu modification = new Menu("Options");

        MenuItem makeTree = new MenuItem("Make tree");
        makeTree.setOnAction(treeConversionEventHandler);

        MenuItem clearPane = new MenuItem("Clear pane");
        clearPane.setOnAction(graphClearingEventHandler);

        MenuItem waysPane = new MenuItem("Find distance between 2");
        waysPane.setOnAction(wayBetweenNodesEventHandler);

        MenuItem pathsPane = new MenuItem("Find path between 2");
        pathsPane.setOnAction(pathBetweenNodesEventHandler);

        MenuItem shortestPathPane = new MenuItem("Find shortest path between 2");
        shortestPathPane.setOnAction(shortestPathBetweenNodesEventHandler);

        MenuItem eulerianCyclePane = new MenuItem("Find an Eulerian cycle");
        eulerianCyclePane.setOnAction(EulerianCycleEventHandler);

        MenuItem incidenceMatrix = new MenuItem("Incidence matrix"); //Adjacency
        incidenceMatrix.setOnAction(getIncidenceMatrixEventHandler);

        edit.getItems().addAll(incidenceMatrix, makeTree, eulerianCyclePane, pathsPane, shortestPathPane, waysPane, clearPane);

        return edit;
    }

    // Creating of metrics menu
    private Menu createMetricsMenu() {
        Menu metrics = new Menu("Additional");
        MenuItem nodesDegrees = new MenuItem("Node degrees");
        nodesDegrees.setOnAction(getNodeDegreeEventHandler);

        MenuItem coloringNodes = new MenuItem("Change nodes color");
        coloringNodes.setOnAction(coloringNodesEventHandler);

        metrics.getItems().addAll(nodesDegrees, /*centers, */ coloringNodes);

        return metrics;
    }

    // Creating of algorithms menu
    /*private Menu createAlgorithmMenu() {
        Menu algorithm = new Menu("Algorithms");
        MenuItem hamiltonianCycles = new MenuItem("Hamiltonian cycles");
        MenuItem distanceBetweenNodes = new MenuItem("Distance between nodes");
        Menu coloring = new Menu("Coloring");
        MenuItem coloringNodes = new MenuItem("Coloring of nodes");

        hamiltonianCycles.setOnAction(findHamiltonianCyclesEventHandler);
        coloringNodes.setOnAction(coloringNodesEventHandler);
        //distanceBetweenNodes.setOnAction(distanceBetweenNodesEventHandler);

        coloring.getItems().add(coloringNodes);
        algorithm.getItems().addAll(hamiltonianCycles, distanceBetweenNodes, coloring);

        return algorithm;
    }*/

    // Creating of operations menu
    /*private Menu createOperationMenu() {
        Menu operation = new Menu("Operations");
        MenuItem cartesianProduct = new MenuItem("Cartesian product");
        MenuItem tensorProduct = new MenuItem("Tensor product");

        cartesianProduct.setOnAction(cartesianProductEventHandler);
        tensorProduct.setOnAction(tensorProductEventHandler);

        operation.getItems().addAll(cartesianProduct, tensorProduct);

        return operation;
    }*/

    // Creating modification menu
    /*private Menu createModificationMenu() {     //сделать граф полным(before)
        Menu modification = new Menu("Options");
        *//*MenuItem makeComplete = new MenuItem("Make complete");

        makeComplete.setOnAction(makeCompleteEventHandler);*//*
 //если что откомментить только это
        *//*Menu modification = new Menu("Options");

        MenuItem makeTree = new MenuItem("Make tree");

        makeTree.setOnAction(treeConversionEventHandler);

        modification.getItems().addAll(makeTree);*//*
 //
        //modification.getItems().addAll(makeComplete);

        return modification;
    }*/

    /*
     *      Others
     */

    private Alert createEmptyDialog(javafx.scene.Node content, String title) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);

        alert.getDialogPane().setContent(content);

        return alert;
    }

    private File createSaveFileDialog() {
        FileChooser saveFileChooser = new FileChooser();
        saveFileChooser.setTitle("Save graph");
        saveFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Graph file", FILE_FORMAT)
        );

        try {
            saveFileChooser.setInitialFileName(
                    graphTabPane.getTabPane().getSelectionModel().getSelectedItem().getText()
            );
        } catch (NullPointerException ex){
            return null;
        }

        return saveFileChooser.showSaveDialog(ownerStage);
    }

    private File createOpenFileDialog() {
        FileChooser openFileChooser = new FileChooser();
        openFileChooser.setTitle("Open graph");
        openFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Graph file", FILE_FORMAT)
        );

        return openFileChooser.showOpenDialog(ownerStage);
    }

    private GraphPane createGraphPaneFromSource(GraphController graphController) {
        GraphPane graphPane = new GraphPane(graphController);

        Random nodePositionRandom = new Random(System.currentTimeMillis());

        for (Node node : graphController.getNodes()) {
            DrawableNode drawableNode = new DrawableNode(node);
            drawableNode.getShape().setCenterX(
                    nodePositionRandom.nextInt((int) MAIN_FORM_WIDTH - 100) + 50
            );
            drawableNode.getShape().setCenterY(
                    nodePositionRandom.nextInt((int) MAIN_FORM_HEIGHT - 300) + 50
            );

            graphPane.getPane().getChildren().addAll(
                    drawableNode.getShape(), drawableNode.getName(), drawableNode.getIdentifier()
            );
            graphPane.getDrawableNodes().add(drawableNode);
            drawableNode.getShape().toFront();
        }

        for (DrawableNode begin : graphPane.getDrawableNodes()) {
            for (DrawableNode end : graphPane.getDrawableNodes()) {
                Arc arc = graphController.getGraph().getArc(begin.getSourceNode(), end.getSourceNode());

                if (arc != null) {
                    DrawableArc newInverse = new DrawableArc(
                            new Arc(arc.getEnd(), arc.getBegin(),
                                    false),
                            new DrawableNode(arc.getEnd()),
                            new DrawableNode(arc.getBegin())
                    );

                    if (graphPane.getDrawableArcs().indexOf(newInverse) != -1) {
                        DrawableArc inverseFound = graphPane.getDrawableArcs()
                                .get(graphPane.getDrawableArcs().indexOf(newInverse));

                        graphPane.getPane().getChildren()
                                .remove(inverseFound.getArrow()); // kaef

                        continue;
                    }

                    DrawableArc drawableArc = new DrawableArc(arc, begin, end);
                    graphPane.getPane().getChildren().addAll(drawableArc.getLine(), drawableArc.getArrow());
                    graphPane.getDrawableArcs().add(drawableArc);
                }
            }
        }

        for (DrawableNode drawableNode : graphPane.getDrawableNodes()) {
            drawableNode.getShape().toFront();
        }

        return graphPane;
    }

    private boolean isGraphAlreadyExist(String name) {
        for (Tab tab : graphTabPane.getManagingGraphs().keySet()) {
            if (tab.getText().equals(name)) {
                Alert error = createEmptyDialog(new Label("Such graph is already exists"), "Error");

                ButtonType OK = new ButtonType("OK");
                error.getButtonTypes().add(OK);

                error.showAndWait();

                return true;
            }
        }

        return false;
    }

    /*
        Event handlers
     */

    // Creating of a new graph
    private EventHandler<ActionEvent> newGraphEventHandler = e -> {
        TextField name = new TextField();

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Graph name"), 0, 0);
        gridPane.add(name, 1, 0);
        GridPane.setMargin(name, new Insets(CIRCLE_RADIUS));

        Alert newGraphDialog = createEmptyDialog(gridPane, "New graph");

        ButtonType CREATE = new ButtonType("Create");
        newGraphDialog.getButtonTypes().add(CREATE);

        ((Button) newGraphDialog.getDialogPane().lookupButton(CREATE)).setOnAction(actionEvent -> {
            GraphJ newGraph = new GraphJ(name.getText());

            if (!isGraphAlreadyExist(newGraph.getName())) {
                graphTabPane.newTab(newGraph);
            } else {
                newGraphDialog.show();
            }
        });

        newGraphDialog.show();
    };

    // Saving of a graph
    private EventHandler<ActionEvent> saveGraphEventHandler = e -> {
        File selectedFile = createSaveFileDialog();

        if (selectedFile != null) {
            new FileProcessor(selectedFile.getAbsolutePath()).write(
                    graphTabPane.currentGraphPane(),
                    graphTabPane.getTabPane().getSelectionModel().getSelectedItem().getText()
            );
        }
    };

    // Opening of a graph
    private EventHandler<ActionEvent> openGraphEventHandler = e -> {
        File selectedFile = createOpenFileDialog();

        if (selectedFile != null) {
            GraphPane namedGraphPane = new FileProcessor(selectedFile.getAbsolutePath()).read();

            if (!isGraphAlreadyExist(namedGraphPane.getGraphController().getGraph().getName())) {
                graphTabPane.newTab(namedGraphPane);
            } else {
                createOpenFileDialog();
            }
        }
    };

    // Taking all graph nodes degrees
    private EventHandler<ActionEvent> getNodeDegreeEventHandler = e -> {
        ObservableList<String> nodesDegrees = FXCollections.observableArrayList();

        try {
            for (Node node : graphTabPane.currentGraphPane().getGraphController().getNodes()) {
               nodesDegrees.add(node + ": " + graphTabPane.currentGraphPane().getGraphController().degreeOf(node));
            }
        } catch (NullPointerException ex) {
            return;
        }

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(nodesDegrees);
        listView.setPrefSize(MAIN_FORM_WIDTH / 8,MAIN_FORM_HEIGHT / 7);
        listView.setEditable(false);

        Alert nodeDegreeDialog = createEmptyDialog(listView, "Nodes' degrees");
        nodeDegreeDialog.getButtonTypes().add(ButtonType.OK);
        nodeDegreeDialog.show();
    };

    // Taking all graph's centers
    private EventHandler<ActionEvent> getCentersEventHandler = e -> {
        ObservableList<String> graphCenters = FXCollections.observableArrayList();

        try {
            for (Node node : graphTabPane.currentGraphPane().getGraphController().centers()) {
                graphCenters.add(node.toString());
            }
        } catch (NullPointerException ex) {
            return;
        }

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(graphCenters);
        listView.setPrefSize(MAIN_FORM_WIDTH / 10,MAIN_FORM_HEIGHT / 8);
        listView.setEditable(false);

        Alert centersDialog = createEmptyDialog(listView, "Centers");
        centersDialog.getButtonTypes().add(ButtonType.OK);
        centersDialog.show();
    };

    // Clearing the graph pane with the source graph
    private EventHandler<ActionEvent> graphClearingEventHandler = e -> {
        try {
            graphTabPane.currentGraphPane().getPane().getChildren().clear();
            graphTabPane.currentGraphPane().getDrawableArcs().clear();
            graphTabPane.currentGraphPane().getDrawableNodes().clear();
            graphTabPane.currentGraphPane().getGraphController().getArcs().clear();
            graphTabPane.currentGraphPane().getGraphController().getNodes().clear();
        } finally {
            return;
        }
    };

    // Cartesian product of two specified graphs
    private EventHandler<ActionEvent> cartesianProductEventHandler = e -> {
        ComboBox<String> gGraphName = new ComboBox<>();
        ComboBox<String> hGraphName = new ComboBox<>();

        for (Tab tab : graphTabPane.getManagingGraphs().keySet()) {
            gGraphName.getItems().add(tab.getText());
            hGraphName.getItems().add(tab.getText());
        }

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("First graph:"), 0, 0);
        gridPane.add(new Label("Second graph:"), 1, 0);
        gridPane.add(gGraphName, 0, 1);
        gridPane.add(hGraphName, 1, 1);
        GridPane.setMargin(gGraphName, new Insets(CIRCLE_RADIUS));
        GridPane.setMargin(hGraphName, new Insets(CIRCLE_RADIUS));

        Alert cartesianProductDialog = createEmptyDialog(gridPane, "Cartesian product");

        ButtonType CREATE = new ButtonType("Create");
        cartesianProductDialog.getButtonTypes().add(CREATE);

        ((Button) cartesianProductDialog.getDialogPane().lookupButton(CREATE)).setOnAction(actionEvent -> {
            GraphPane gGraphPane = graphTabPane.getGraphPaneAtTab(gGraphName.getSelectionModel().getSelectedItem());
            GraphPane hGraphPane = graphTabPane.getGraphPaneAtTab(hGraphName.getSelectionModel().getSelectedItem());

            String graphName = gGraphName.getSelectionModel().getSelectedItem()
                    + " □ " + hGraphName.getSelectionModel().getSelectedItem();

            if (!isGraphAlreadyExist(graphName)) {
                GraphJ product = new GraphProducer(
                        gGraphPane.getGraphController().getGraph(),
                        hGraphPane.getGraphController().getGraph())
                        .cartesianProduct();
                product.setName(graphName);

                graphTabPane.newTab(createGraphPaneFromSource(new GraphController(product)));
            } else {
                cartesianProductDialog.show();
            }
        });

        cartesianProductDialog.show();
    };

    // Tensor product of two specified graphs
    private EventHandler<ActionEvent> tensorProductEventHandler = e -> {
        ComboBox<String> gGraphName = new ComboBox<>();
        ComboBox<String> hGraphName = new ComboBox<>();

        for (Tab tab : graphTabPane.getTabPane().getTabs()) {
            gGraphName.getItems().add(tab.getText());
            hGraphName.getItems().add(tab.getText());
        }

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("First graph:"), 0, 0);
        gridPane.add(new Label("Second graph:"), 1, 0);
        gridPane.add(gGraphName, 0, 1);
        gridPane.add(hGraphName, 1, 1);
        GridPane.setMargin(gGraphName, new Insets(CIRCLE_RADIUS));
        GridPane.setMargin(hGraphName, new Insets(CIRCLE_RADIUS));

        Alert tensorProductDialog = createEmptyDialog(gridPane, "Tensor product");

        ButtonType CREATE = new ButtonType("Create");
        tensorProductDialog.getButtonTypes().add(CREATE);

        ((Button) tensorProductDialog.getDialogPane().lookupButton(CREATE)).setOnAction(actionEvent -> {
            GraphPane gGraphPane = graphTabPane.getGraphPaneAtTab(gGraphName.getSelectionModel().getSelectedItem());
            GraphPane hGraphPane = graphTabPane.getGraphPaneAtTab(hGraphName.getSelectionModel().getSelectedItem());

            String graphName = gGraphName.getSelectionModel().getSelectedItem()
                    + " × " + hGraphName.getSelectionModel().getSelectedItem();

            if (!isGraphAlreadyExist(graphName)) {
                GraphJ product = new GraphProducer(
                        gGraphPane.getGraphController().getGraph(),
                        hGraphPane.getGraphController().getGraph())
                        .tensorProduct();
                product.setName(graphName);

                graphTabPane.newTab(createGraphPaneFromSource(new GraphController(product)));
            } else {
                tensorProductDialog.show();
            }
        });

        tensorProductDialog.show();
    };

    // Making graph complete
    private EventHandler<ActionEvent> makeCompleteEventHandler = e -> {
        GraphPane currentGraphPane = graphTabPane.currentGraphPane();
        currentGraphPane.removeLoops();
        currentGraphPane.getGraphController().makeComplete();

        for (Arc arc : currentGraphPane.getGraphController().getArcs()) {
            DrawableArc newInverse = new DrawableArc(
                    new Arc(arc.getEnd(), arc.getBegin(),
                            false),
                    new DrawableNode(arc.getEnd()),
                    new DrawableNode(arc.getBegin())
            );

            if (currentGraphPane.getDrawableArcs().indexOf(newInverse) != -1) {
                DrawableArc inverseFound = currentGraphPane.getDrawableArcs()
                        .get(currentGraphPane.getDrawableArcs().indexOf(newInverse));

                currentGraphPane.getPane().getChildren()
                        .remove(inverseFound.getArrow()); // kaef

                continue;
            }

            DrawableNode newBegin = currentGraphPane.getDrawableNodes()
                    .get(currentGraphPane.getDrawableNodes().indexOf(new DrawableNode(arc.getBegin())));
            DrawableNode newEnd = currentGraphPane.getDrawableNodes()
                    .get(currentGraphPane.getDrawableNodes().indexOf(new DrawableNode(arc.getEnd())));

            DrawableArc newPrime = new DrawableArc(
                    arc,
                    newBegin,
                    newEnd
            );

            if (currentGraphPane.getDrawableArcs().indexOf(newPrime) != -1) {
                continue;
            }

            currentGraphPane.getPane().getChildren().add(newPrime.getLine());
            currentGraphPane.getDrawableArcs().add(newPrime);
        }

        for (DrawableNode drawableNode : currentGraphPane.getDrawableNodes()) {
            drawableNode.getShape().toFront();
        }
    };

    // Conversion to a tree
    private EventHandler<ActionEvent> treeConversionEventHandler = e -> {
        GraphPane currentGraphPane = graphTabPane.currentGraphPane();
        currentGraphPane.removeLoops();
        for (int i = 0; i < graphTabPane.currentGraphPane().getDrawableArcs().size(); i++) {
            currentGraphPane.deleteAllArcs();
            i--;
        }
        currentGraphPane.getGraphController().ConvertToTree();

        for (Arc arc : currentGraphPane.getGraphController().getArcs()) {
            DrawableArc newInverse = new DrawableArc(
                    new Arc(arc.getEnd(), arc.getBegin(),
                            false),
                    new DrawableNode(arc.getEnd()),
                    new DrawableNode(arc.getBegin())
            );

            if (currentGraphPane.getDrawableArcs().indexOf(newInverse) != -1) {
                DrawableArc inverseFound = currentGraphPane.getDrawableArcs()
                        .get(currentGraphPane.getDrawableArcs().indexOf(newInverse));

                currentGraphPane.getPane().getChildren()
                        .remove(inverseFound.getArrow());

                continue;
            }

            DrawableNode newBegin = currentGraphPane.getDrawableNodes()
                    .get(currentGraphPane.getDrawableNodes().indexOf(new DrawableNode(arc.getBegin())));
            DrawableNode newEnd = currentGraphPane.getDrawableNodes()
                    .get(currentGraphPane.getDrawableNodes().indexOf(new DrawableNode(arc.getEnd())));

            DrawableArc newPrime = new DrawableArc(
                    arc,
                    newBegin,
                    newEnd
            );

            if (currentGraphPane.getDrawableArcs().indexOf(newPrime) != -1) {
                continue;
            }

            currentGraphPane.getPane().getChildren().add(newPrime.getLine());
            currentGraphPane.getDrawableArcs().add(newPrime);
        }

        for (DrawableNode drawableNode : currentGraphPane.getDrawableNodes()) {
            drawableNode.getShape().toFront();
        }
    };

    // Finding of hamiltonian cycles
    private EventHandler<ActionEvent> findHamiltonianCyclesEventHandler = e -> {
        ObservableList<String> cycles = FXCollections.observableArrayList();

        try {
            for (Path cycle : graphTabPane.currentGraphPane().getGraphController().hamiltonianCycles()) {
                cycles.add(cycle.toString());
            }
        } catch (NullPointerException ex){
            return;
        }

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(cycles);
        listView.setPrefSize(MAIN_FORM_WIDTH / 3,MAIN_FORM_HEIGHT / 5);
        listView.setEditable(false);

        Alert hamiltonianCyclesDialog = createEmptyDialog(listView, "Hamiltonian cycles");
        hamiltonianCyclesDialog.getButtonTypes().add(ButtonType.OK);
        hamiltonianCyclesDialog.show();
    };

    // Coloring the graph nodes
    private EventHandler<ActionEvent> coloringNodesEventHandler = e -> {
        Map<Node, String> stringColors = graphTabPane.currentGraphPane().getGraphController().colorizeNodes();
        Map<String, Color> colors = new HashMap<>();

        Random random = new Random(System.currentTimeMillis());

        for (String stringColor : stringColors.values()) {
            colors.put(
                    stringColor,
                    Color.color(random.nextDouble(),
                            random.nextDouble(),
                            random.nextDouble())
            );
        }

        List<DrawableNode> drawableNodes = graphTabPane.currentGraphPane().getDrawableNodes();

        for (DrawableNode drawableNode : drawableNodes) {
            drawableNode.getShape().setFill(colors.get(stringColors.get(drawableNode.getSourceNode())));
        }
    };

    // Distance between two specified nodes
    private EventHandler<ActionEvent> distanceBetweenNodesEventHandler = e -> {
        ComboBox<String> firstNodeName = new ComboBox<>();
        ComboBox<String> secondNodeName = new ComboBox<>();

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            firstNodeName.getItems().add(drawableNode.getSourceNode().toString());
            secondNodeName.getItems().add(drawableNode.getSourceNode().toString());
        }

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("1 node:"), 0, 0);
        gridPane.add(new Label("2 node:"), 1, 0);
        gridPane.add(firstNodeName, 0, 1);
        gridPane.add(secondNodeName, 1, 1);
        GridPane.setMargin(firstNodeName, new Insets(CIRCLE_RADIUS));
        GridPane.setMargin(secondNodeName, new Insets(CIRCLE_RADIUS));

        Alert distanceDialog = createEmptyDialog(gridPane, "Distance between two nodes");

        ButtonType GET = new ButtonType("Get");
        distanceDialog.getButtonTypes().add(GET);

        ((Button) distanceDialog.getDialogPane().lookupButton(GET)).setOnAction(actionEvent -> {
            Node begin = new Node();
            Node end = new Node();

            for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
                if (drawableNode.getSourceNode().toString().equals(
                        firstNodeName.getSelectionModel().getSelectedItem())) {
                    begin = drawableNode.getSourceNode();
                }

                if (drawableNode.getSourceNode().toString().equals(
                        secondNodeName.getSelectionModel().getSelectedItem())) {
                    end = drawableNode.getSourceNode();
                }
            }

            Integer distance = graphTabPane.currentGraphPane().getGraphController()
                    .getDistanceMatrix().getDistancesMap()
                    .get(begin).get(end);

            Label distanceText = new Label();
            Alert distanceAsItIs = createEmptyDialog(distanceText, "Distance");
            distanceAsItIs.getButtonTypes().add(ButtonType.OK);

            if (distance == DistanceMatrix.INFINITY) {
                distanceText.setText("Node way from " + begin + " to " + end + " found");
            } else {
                distanceText.setText("Distance between " + begin + " and " + end + " is " +
                                graphTabPane.currentGraphPane().getGraphController()
                                        .getDistanceMatrix().getDistancesMap()
                                        .get(begin).get(end));
            }

            distanceAsItIs.show();
        });

        distanceDialog.show();
    };


    // Расстояние between two specified nodes
    private EventHandler<ActionEvent> wayBetweenNodesEventHandler = e -> {
        ComboBox<String> firstNodeName = new ComboBox<>();
        ComboBox<String> secondNodeName = new ComboBox<>();

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            firstNodeName.getItems().add(drawableNode.getSourceNode().toString());
System.out.print(drawableNode.getSourceNode() + " ");   // выводит все вершины  !!!!!!!!!!!!!!!!!!!!!!!!
            secondNodeName.getItems().add(drawableNode.getSourceNode().toString());
        }

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("1 node:"), 0, 0);
        gridPane.add(new Label("2 node:"), 1, 0);
        gridPane.add(firstNodeName, 0, 1);
        gridPane.add(secondNodeName, 1, 1);
        GridPane.setMargin(firstNodeName, new Insets(CIRCLE_RADIUS));
        GridPane.setMargin(secondNodeName, new Insets(CIRCLE_RADIUS));

        Alert distanceDialog = createEmptyDialog(gridPane, "Distance between two nodes");

        ButtonType GET = new ButtonType("Get");
        distanceDialog.getButtonTypes().add(GET);

        ((Button) distanceDialog.getDialogPane().lookupButton(GET)).setOnAction(actionEvent -> {
            Node begin = new Node();
            Node end = new Node();

            for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
                if (drawableNode.getSourceNode().toString().equals(
                        firstNodeName.getSelectionModel().getSelectedItem())) {
                    begin = drawableNode.getSourceNode();
                }

                if (drawableNode.getSourceNode().toString().equals(
                        secondNodeName.getSelectionModel().getSelectedItem())) {
                    end = drawableNode.getSourceNode();
                }
            }

            Integer distance = graphTabPane.currentGraphPane().getGraphController()
                    .getDistanceMatrix().getDistancesMap()
                    .get(begin).get(end);

            Label distanceText = new Label();
            Alert distanceAsItIs = createEmptyDialog(distanceText, "Distance");
            distanceAsItIs.getButtonTypes().add(ButtonType.OK);

            if (distance == DistanceMatrix.INFINITY) {
                distanceText.setText("Node way from " + begin + " to " + end + " found");
            } else {
                distanceText.setText("Distance between " + begin + " and " + end + " is " +
                        graphTabPane.currentGraphPane().getGraphController()
                                .getDistanceMatrix().getDistancesMap()
                                .get(begin).get(end));
            }

            distanceAsItIs.show();
        });

        distanceDialog.show();
    };


//////////////  ПУНКТ4 поиск всех путей (маршрутов) между двумя узлами
private EventHandler<ActionEvent> pathBetweenNodesEventHandler = e -> {
    ComboBox<String> firstNodeName = new ComboBox<>();
    ComboBox<String> secondNodeName = new ComboBox<>();

    for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
        firstNodeName.getItems().add(drawableNode.getSourceNode().toString());
        secondNodeName.getItems().add(drawableNode.getSourceNode().toString());
        //System.out.println(firstNodeName.getItems().add(drawableNode.getSourceNode().toString()));
    }

    GridPane gridPane = new GridPane();
    gridPane.add(new Label("1 node:"), 0, 0);
    gridPane.add(new Label("2 node:"), 1, 0);
    gridPane.add(firstNodeName, 0, 1);
    gridPane.add(secondNodeName, 1, 1);
    GridPane.setMargin(firstNodeName, new Insets(CIRCLE_RADIUS));
    GridPane.setMargin(secondNodeName, new Insets(CIRCLE_RADIUS));

    Alert pathDialog = createEmptyDialog(gridPane, "Paths between two nodes");

    ButtonType GET = new ButtonType("Show paths");
    pathDialog.getButtonTypes().add(GET);

    ((Button) pathDialog.getDialogPane().lookupButton(GET)).setOnAction(actionEvent -> {
        Node begin = new Node();
        Node end = new Node();
        //System.out.println(drawableNode.getSourceNode());
//System.out.println(graphTabPane.currentGraphPane().getDrawableNodes());// выводит вершины в виде [layout.DrawableNode@4dfc26, layout.DrawableNode@32fe3f33]

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            if (drawableNode.getSourceNode().toString().equals(
                                                    firstNodeName.getSelectionModel().getSelectedItem())) {
                begin = drawableNode.getSourceNode();       // тут находится первая вершина(НАЧАЛО ПУТИ)
//System.out.println(begin);  //// выводит вершину в "[]"
            }

            if (drawableNode.getSourceNode().toString().equals(
                                                    secondNodeName.getSelectionModel().getSelectedItem())) {
                end = drawableNode.getSourceNode();     // тут находится первая вершина(КОНЕЦ ПУТИ)
            }
        }

//////////////////////
        Graph<Node, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            g.addVertex(drawableNode.getSourceNode());

            //System.out.println(firstNodeName.getItems().add(drawableNode.getSourceNode().toString()));
        }
        //System.out.println(g);

        for (DrawableArc drawableArc : graphTabPane.currentGraphPane().getDrawableArcs()) {
            g.addEdge(drawableArc.getBegin().getSourceNode(), drawableArc.getEnd().getSourceNode());
            //System.out.println(g);
            //System.out.println(firstNodeName.getItems().add(drawableNode.getSourceNode().toString()));
        }
        //System.out.println(g);  // выводится весь граф  !!!!!!


        List<GraphPath<Node, DefaultEdge>> result = null;
        //try {
            AllDirectedPaths<Node, DefaultEdge> pathFinder = new AllDirectedPaths<>(g);
            result = pathFinder.getAllPaths(begin,end,true,50);
            //System.out.println(result);
        //}catch(IllegalArgumentException k){}

        //for (int i = 0; i < result.size(); i++){
        //    System.out.println(result.get(i).toString());
        //}



        ListView<String> listOfPaths = new ListView<>();
        listOfPaths.getItems().addAll(String.valueOf(result));
        //Label pathText = new Label();
        Alert pathsAsItIs = createEmptyDialog(listOfPaths, "Paths");
        pathsAsItIs.getButtonTypes().add(ButtonType.OK);
//System.out.println(begin);
        pathsAsItIs.setResizable(true);

        pathsAsItIs.show();
    });
    pathDialog.show();
};


    //////////////  Поиск кратчайшего пути между двумя узлами и кратчайших
    private EventHandler<ActionEvent> shortestPathBetweenNodesEventHandler = e -> {
        ComboBox<String> firstNodeName = new ComboBox<>();
        ComboBox<String> secondNodeName = new ComboBox<>();

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            firstNodeName.getItems().add(drawableNode.getSourceNode().toString());
            secondNodeName.getItems().add(drawableNode.getSourceNode().toString());
        }

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("1 node:"), 0, 0);
        gridPane.add(new Label("2 node:"), 1, 0);
        gridPane.add(firstNodeName, 0, 1);
        gridPane.add(secondNodeName, 1, 1);
        GridPane.setMargin(firstNodeName, new Insets(CIRCLE_RADIUS));
        GridPane.setMargin(secondNodeName, new Insets(CIRCLE_RADIUS));

        Alert pathDialog = createEmptyDialog(gridPane, "Shortest path");

        ButtonType GET = new ButtonType("Show");
        pathDialog.getButtonTypes().add(GET);

        ((Button) pathDialog.getDialogPane().lookupButton(GET)).setOnAction(actionEvent -> {
            Node begin = new Node();
            Node end = new Node();

            for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
                if (drawableNode.getSourceNode().toString().equals(
                        firstNodeName.getSelectionModel().getSelectedItem())) {
                    begin = drawableNode.getSourceNode();
                }

                if (drawableNode.getSourceNode().toString().equals(
                        secondNodeName.getSelectionModel().getSelectedItem())) {
                    end = drawableNode.getSourceNode();     // тут находится первая вершина(КОНЕЦ ПУТИ)
                }
            }

//////////////////////
            Graph<Node, DefaultEdge> g2 = new DefaultDirectedGraph<>(DefaultEdge.class);

            for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
                g2.addVertex(drawableNode.getSourceNode());

                //System.out.println(firstNodeName.getItems().add(drawableNode.getSourceNode().toString()));
            }
            //System.out.println(g);

            for (DrawableArc drawableArc : graphTabPane.currentGraphPane().getDrawableArcs()) {
                g2.addEdge(drawableArc.getBegin().getSourceNode(), drawableArc.getEnd().getSourceNode());
                //System.out.println(g);
                //System.out.println(firstNodeName.getItems().add(drawableNode.getSourceNode().toString()));
            }
            //System.out.println(g2);  // выводится весь граф  !!!!!!


            BFSShortestPath<Node, DefaultEdge> shortestpath = new BFSShortestPath<>(g2);
            GraphPath<Node, DefaultEdge> graphForShortestPath = shortestpath.getPath(begin, end);
            ListView<String> listOfShort = new ListView<>();
            listOfShort.getItems().addAll(String.valueOf(graphForShortestPath));

            Alert shortpath = createEmptyDialog(listOfShort, "Shortest path : ");
            shortpath.getButtonTypes().add(ButtonType.OK);
            shortpath.setResizable(true);
            shortpath.show();
        });
        pathDialog.show();
    };


    //////////////  Поиск эЙЛЕРОВА ЦИКЛА
    private EventHandler<ActionEvent> EulerianCycleEventHandler = e -> {
            Graph<Node, DefaultEdge> g2 = new DefaultDirectedGraph<>(DefaultEdge.class);

            for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
                g2.addVertex(drawableNode.getSourceNode());
            }

            for (DrawableArc drawableArc : graphTabPane.currentGraphPane().getDrawableArcs()) {
                g2.addEdge(drawableArc.getBegin().getSourceNode(), drawableArc.getEnd().getSourceNode());
            }
            //System.out.println(g2);  // выводится весь граф  !!!!!!

            HierholzerEulerianCycle<Node, DefaultEdge> eulerCycle = new HierholzerEulerianCycle<>();
            GraphPath<Node, DefaultEdge> graphForCycle = null;
            if(eulerCycle.isEulerian(g2)) {
                graphForCycle = eulerCycle.getEulerianCycle(g2);
            }

            Label infoText = new Label("NO Eulerian cycle !");
            if(graphForCycle == null){
                Alert inf = createEmptyDialog(infoText, "An Eulerian cycle");
                inf.getButtonTypes().add(ButtonType.OK);
                inf.show();
            } else{
                ListView<String> listOfPaths = new ListView<>();
                listOfPaths.getItems().addAll(String.valueOf(graphForCycle));

                Alert pathsAsItIs = createEmptyDialog(listOfPaths, "An Eulerian cycle");
                pathsAsItIs.getButtonTypes().add(ButtonType.OK);
                pathsAsItIs.setResizable(true);
                pathsAsItIs.show();
            }
    };


    ////////////// incidence matrix
    private EventHandler<ActionEvent> getIncidenceMatrixEventHandler = e -> {
        Label matrix = new Label();

        Graph<Node, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        ArrayList<Long> listnodes = new ArrayList<>();

        for (DrawableNode drawableNode : graphTabPane.currentGraphPane().getDrawableNodes()) {
            g.addVertex(drawableNode.getSourceNode());  // здесь вершины
            //listnodes.add((Integer)drawableNode.getIdentifier());
            long a = drawableNode.getSourceNode().getIdentifier();
            listnodes.add(drawableNode.getSourceNode().getIdentifier());
            //System.out.println(drawableNode.getSourceNode().getIdentifier());
        }
// вывод вершин
        /*for(int i=0; i<listnodes.size(); i++){
            System.out.println(listnodes.get(i));
        }*/

       // ArrayList<Node> listedges = new ArrayList();
        for (DrawableArc drawableArc : graphTabPane.currentGraphPane().getDrawableArcs()) {
            g.addEdge(drawableArc.getBegin().getSourceNode(), drawableArc.getEnd().getSourceNode());// здесь ребра
            //long d = drawableArc.getBegin().getSourceNode().getIdentifier();
            //System.out.println("Вывод  " + drawableArc.getBegin().getSourceNode().getIdentifier() + " " + drawableArc.getEnd().getSourceNode());
        }
        System.out.println(g);  // выводится весь граф  !!!!!!

//////////
        ArrayList<TreeMap<Long, Integer>> matrixxx = new ArrayList<>();

        Set<DefaultEdge> resultEdge = null;
        resultEdge = g.edgeSet();
        System.out.println(resultEdge);

        for(int i=0; i<g.edgeSet().size(); i++){    // иду по ребрам
            TreeMap<Long, Integer> tr = new TreeMap<>();
            for(int j=0; j<listnodes.size(); j++){  // иду по вершинам
                tr.put(listnodes.get(j), 0);
            }
            matrixxx.add(tr);
        }

        int counter = 0;
        for (DefaultEdge edge : g.edgeSet()) {
            TreeMap<Long, Integer> currentMap= matrixxx.get(counter);
            long beginNode = g.getEdgeSource(edge).getIdentifier();
            long endNode = g.getEdgeTarget(edge).getIdentifier();
            currentMap.put(beginNode, 1);
            currentMap.put(endNode, 1);
            ++counter;
        }

        System.out.println(" incidence matrix: ");
        System.out.println(matrixxx);

        ListView<String> listOfMatrix = new ListView<>();
        listOfMatrix.getItems().addAll(String.valueOf(matrixxx));

        Alert matrixdialog = createEmptyDialog(listOfMatrix, "Incidence Matrix");
        matrixdialog.getButtonTypes().add(ButtonType.OK);
        matrixdialog.setWidth(500);
        matrixdialog.setResizable(true);
        matrixdialog.show();

    };







    // Taking graph's adjacency matrix
    private EventHandler<ActionEvent> getAdjacencyMatrixEventHandler = e -> {
        Label matrix;

        try {
            matrix = new Label(graphTabPane.currentGraphPane().getGraphController().adjacencyMatrix().matrixToString());
        } catch (NullPointerException ex) {
            return;
        }
        //matrix = new Label("Incidence matrix:\n1 0 -1 0 0\n-1 -1 0 0 0\n0 1 0 -1 0\n0 0 1 1 -1\n0 0 0 0 1");

        if (matrix.getText().equals("")) {
            matrix.setText("The graph is empty");
        }

        /*Alert matrixDialog = createEmptyDialog(matrix, "Adjacency matrix");*/
        Alert matrixDialog = createEmptyDialog(matrix, "I M");
        matrixDialog.setWidth(300d);
        matrixDialog.getButtonTypes().add(ButtonType.CANCEL);
        matrixDialog.show();
    };
}

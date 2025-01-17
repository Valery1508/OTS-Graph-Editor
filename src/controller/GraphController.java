package controller;

import controller.verifier.PlanarityVerifier;
import controller.verifier.TreeVerifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.*;

import java.util.*;

import static model.DistanceMatrix.INFINITY;


public class GraphController {
    private GraphJ graph;
    private DistanceMatrix distanceMatrix;
    private AdjacencyMatrix adjacencyMatrix;


    public GraphController(GraphJ graph) {
        this.graph = graph;
        distanceMatrix = new DistanceMatrix(graph);
        adjacencyMatrix = new AdjacencyMatrix(graph);
    }

    public GraphJ getGraph() {
        return graph;
    }

    public AdjacencyMatrix adjacencyMatrix() {
        return adjacencyMatrix;
    }

    public ObservableList<Node> getNodes() { return graph.getNodes(); }

    public ObservableList<Arc> getArcs() { return graph.getArcs(); }

    public AdjacencyMatrix getAdjacencyMatrix() {
        return adjacencyMatrix;
    }

    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    public void addNode(Node node) {
        graph.getNodes().add(node);
    }

    public void removeNode(Node node) {
        graph.getNodes().remove(node);

        ObservableList<Arc> arcsToRemove = FXCollections.observableArrayList();

        for (Arc arc : graph.getArcs()) {
            if (arc.getBegin().equals(node) || arc.getEnd().equals(node)) {
                arcsToRemove.add(arc);
            }
        }

        graph.getArcs().removeAll(arcsToRemove);
    }

    public void addArc(Arc arc) {
        graph.getArcs().add(arc);
    }

    public void removeArc(Arc arc) {
        graph.getArcs().remove(arc);
    }

    /*
        Metrics
     */

    // Calculation of a node degree
    public int degreeOf(Node node) {
        int degree = 0;

        for (Arc arc : graph.getArcs()) {
            if (arc.getBegin().equals(node) || arc.getEnd().equals(node)) {
                degree++;
            }
        }

        return degree;
    }

    // Calculation of the nodes' eccentricities
    private Map<Node, Integer> eccentricities() {
        Map<Node, Integer> eccentricities = new HashMap<>();

        int eccentricity;

        for (Node node : distanceMatrix.getDistancesMap().keySet()) {
            eccentricity = 0;

            for (Integer distance : distanceMatrix.getDistancesMap().get(node).values()) {
                if ((distance > eccentricity) && (distance != INFINITY)) {
                    eccentricity = distance;
                }
            }

            eccentricities.put(node, eccentricity);
        }

        return eccentricities;
    }

    // Calculation of a graph diameter
    public int diameter() {
        int diameter = 0;

        for (Integer eccentricity : eccentricities().values()) {
            if ((eccentricity > diameter) && (eccentricity != INFINITY)) {
                diameter = eccentricity;
            }
        }

        return diameter;
    }

    // Calculation of a graph radius
    public int radius() {
        int radius = INFINITY;

        for (Integer eccentricity : eccentricities().values()) {
            if ((eccentricity < radius) && (eccentricity != 0)) {
                radius = eccentricity;
            }
        }

        return radius == INFINITY ? 0 : radius;
    }

    // Taking of graph centers
    public ObservableList<Node> centers() {
        ObservableList<Node> centres = FXCollections.observableArrayList();
        int radius = radius();

        for (Node node : eccentricities().keySet()) {
            if (eccentricities().get(node) == radius) {
                centres.add(node);
            }
        }

        return centres;
    }

    // Check for graph planarity
    public boolean isPlanar() {
        return new PlanarityVerifier(graph).verify();
    }

    // Check for graph complete
    public boolean isComplete() {
        return !graph.containsLoop() && (graph.getArcs().size() == graph.getNodes().size() * (graph.getNodes().size() - 1));
    }

    // Check is graph a tree
    public boolean isTree() {
        return !graph.containsLoop() && new TreeVerifier(graph).verify();
    }

    /*
     *      Other algorithms
     */

    // Finding all of hamiltonian cycles in the graph
    public ObservableList<Path> hamiltonianCycles() {
        ObservableList<Path> hamiltonianCycles = FXCollections.observableArrayList();

        for (Node begin : graph.getNodes()) {
            ObservableList<Path> cycles = findAllHamiltonianCyclesFrom(begin);
            for (Path cycleFromThisNode : cycles) {
                if (!hamiltonianCycles.contains(cycleFromThisNode)) {
                    hamiltonianCycles.add(cycleFromThisNode);
                }
            }
        }

        return hamiltonianCycles;
    }

    // Coloring of nodes
    public Map<Node, String> colorizeNodes() {
        return new Colorer(graph).colorizeNodes();
    }

    public void ConvertToTree() {
        List<Node> unusedNodes = new ArrayList<Node>();
        unusedNodes.addAll(graph.getNodes());
        List<Node> usedNodes = new ArrayList<Node>();
        System.out.println(graph.getArcs().size());
        for(int i = 0; i < graph.getArcs().size(); i++){
            graph.getArcs().remove(i);
            i--;
        }
//graph.getArcs().removeAll();
        System.out.println(graph.getArcs().size());

        for(int i = 0; i < graph.getNodes().size(); i++) {
            Node tNode = graph.getNodes().get(i);
            unusedNodes.remove(tNode);
            usedNodes.add(tNode);
            for(int j = 0; j < 2; j++) {
                try {
                    if(!usedNodes.contains(unusedNodes.get(j))) {
                        graph.getArcs().add(new Arc(tNode, unusedNodes.get(j)));
                        usedNodes.add(unusedNodes.get(j));
                        unusedNodes.remove(j);
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        }
        System.out.println(graph.getArcs().size());
//return graph;
    }

    // Making all nodes adjacent to all nodes
    public void makeComplete() {
        List<Arc> arcsToDelete = new ArrayList<>();

        for (Node begin : graph.getNodes()) {
            for (Node end : graph.getNodes()) {
                if (begin.equals(end)) {
                    continue;
                }

                if (!graph.getArcs().contains(new Arc(begin, end))) {
                    graph.getArcs().add(new Arc(begin, end));
                }
            }
        }

        for (Arc arc : graph.getArcs()) {
            if (arc.getBegin().equals(arc.getEnd())) {
                arcsToDelete.add(arc);
                continue;
            }

            arc.setDirected(false);
        }

        graph.getArcs().removeAll(arcsToDelete);
    }

    /*
     *      Utility
     */

    // Finds all possible Hamiltonian cycles begins with the node given
    private ObservableList<Path> findAllHamiltonianCyclesFrom(Node begin) {
        Map<Node, Boolean> visitedNodes = new HashMap<>();
        ObservableList<Path> hamiltonianCyclesBeginsWithThisNode = FXCollections.observableArrayList();
        Path trackingCycle = new Path();

        for (Node node : graph.getNodes()) {
            visitedNodes.put(node, false);
        }

        dfsHamiltonianCycle(begin, trackingCycle, visitedNodes, hamiltonianCyclesBeginsWithThisNode);

        return hamiltonianCyclesBeginsWithThisNode;
    }

    private void dfsHamiltonianCycle(Node begin, Path trackingCycle,
                                     Map<Node, Boolean> visitedNodes,
                                     ObservableList<Path> hamiltonianCyclesBeginsWithThisNode) {

        if (trackingCycle.getPath().size() == graph.getNodes().size()) {
            if (graph.getArcs().contains(new Arc(trackingCycle.getPath().get(trackingCycle.getPath().size() - 1),
                    trackingCycle.getPath().get(0)))) {
                Path hamiltonianCycle = new Path(trackingCycle);
                hamiltonianCycle.getPath().add(trackingCycle.getPath().get(0));

                for (Path cycle : hamiltonianCyclesBeginsWithThisNode) {
                    if (hamiltonianCycle.getPath().contains(cycle.getPath())) {
                        return;
                    }
                }

                hamiltonianCyclesBeginsWithThisNode.add(hamiltonianCycle);

                return;
            }
        }

        for (Node adjacentNode : adjacencyMatrix.adjacentNodesOf(begin)) {
            if (!visitedNodes.get(adjacentNode)) {
                visitedNodes.replace(adjacentNode, true);
                trackingCycle.getPath().add(adjacentNode);

                dfsHamiltonianCycle(adjacentNode, trackingCycle, visitedNodes, hamiltonianCyclesBeginsWithThisNode);

                visitedNodes.replace(adjacentNode, false);
                trackingCycle.getPath().remove(trackingCycle.getPath().size() - 1);
            }
        }
    }
}

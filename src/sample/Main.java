package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import layout.form.AppForm;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.net.URI;


public class Main extends Application {
    public static final double MAIN_FORM_HEIGHT = 800;  //900
    public static final double MAIN_FORM_WIDTH = 800;  //1600



    @Override
    public void start(Stage primaryStage) throws Exception {

        /*Graph<URI, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        URI google = new URI("http://www.google.com/%22");
                URI wikipedia = new URI("http://www.wikipedia.org/%22");
                URI jgrapht = new URI("http://www.jgrapht.org/%22");

                // add the vertices
                g.addVertex(google);
        g.addVertex(wikipedia);
        g.addVertex(jgrapht);

        // add edges to create linking structure
        g.addEdge(jgrapht, wikipedia);
        g.addEdge(google, jgrapht);
        g.addEdge(google, wikipedia);
        g.addEdge(wikipedia, google);*/



        primaryStage.setTitle("Graph Editor");
        primaryStage.setScene(new Scene(new AppForm(primaryStage).getVBox()));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

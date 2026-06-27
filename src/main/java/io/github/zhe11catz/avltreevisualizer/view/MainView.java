package io.github.zhe11catz.avltreevisualizer.view;

import io.github.zhe11catz.avltreevisualizer.controller.AVLController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Loads the main FXML layout and exposes the root node.
 */
public class MainView {

    private final Parent root;
    private final AVLController controller;

    public MainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_view.fxml"));
            this.root = loader.load();
            this.controller = loader.getController();
            this.controller.initializeView();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load main_view.fxml", ex);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public AVLController getController() {
        return controller;
    }
}

package com.message.ihm;

import com.message.core.DataManager;
import com.message.core.session.Session;
import com.message.datamodel.User;
import com.message.ihm.controllers.MessageAppController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;


public class MessageApp extends Application {

    private static final boolean TEST_MODE = true;
    private static final String PROD_EXCHANGE_DIR = "\\\\10.66.66.1\\messageAppEchanges";

    private static DataManager sDataManager;
    private MessageAppController mController;

    public static void setDataManager(DataManager dataManager) {
        sDataManager = dataManager;
    }

    @Override
    public void start(Stage primaryStage) {
        if (sDataManager == null) {
            System.err.println("DataManager non initialisé !");
            System.exit(1);
        }

        // Choix du répertoire d'échange
        File exchangeDir;
        if (TEST_MODE) {
            exchangeDir = chooseExchangeDirectory(primaryStage);
        } else {
            exchangeDir = getProdExchangeDirectory();
        }

        if (exchangeDir == null) System.exit(0);
        sDataManager.setExchangeDirectory(exchangeDir.getAbsolutePath());
        // Conteneur principal
        StackPane mainContainer = new StackPane();

        Session session = new Session(sDataManager);
        mController = new MessageAppController(sDataManager, session);
        mController.initView(mainContainer);

        // Création de la scène et affichage
        Scene scene = new Scene(mainContainer, 1200, 800);
        primaryStage.setTitle("MessageAppFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private File chooseExchangeDirectory(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Sélectionnez le répertoire d'échange");
        File dir = chooser.showDialog(stage);
        if (dir != null && dir.isDirectory() && dir.canRead() && dir.canWrite()) {
            return dir;
        }
        // Si répertoire invalide
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Répertoire invalide ou non sélectionné");
        alert.showAndWait();
        return null;
    }

    /**
     * Mode TEST : résout le dossier "echanges" depuis les resources du classpath.
     */
    private File getTestExchangeDirectory() {
        File dir = new File("src/main/resources/echanges");
        if (dir.isDirectory() && dir.canRead() && dir.canWrite()) {
            System.out.println("[TEST] Répertoire d'échange : " + dir.getAbsolutePath());
            return dir;
        }
        showDirectoryError("Impossible de trouver le répertoire de test (src/main/resources/echanges).");
        return null;
    }

    /**
     * Mode PROD : utilise le chemin réseau défini dans PROD_EXCHANGE_DIR.
     */
    private File getProdExchangeDirectory() {
        File dir = new File(PROD_EXCHANGE_DIR);
        if (dir.isDirectory() && dir.canRead() && dir.canWrite()) {
            System.out.println("[PROD] Répertoire d'échange : " + dir.getAbsolutePath());
            return dir;
        }
        showDirectoryError("Impossible d'accéder au serveur : " + PROD_EXCHANGE_DIR);
        return null;
    }

    private void showDirectoryError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
package com.message.ihm;

import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.*;
import com.message.ihm.views.HomeView;
import com.message.ihm.views.LoginView;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Classe principale de la vue JavaFX de l'application.
 * Gère la navigation entre LoginViewFX et HomeViewFX.
 */
public class MessageAppMainView {

    private Stage primaryStage;
    private StackPane mainContainer;

    // Vues enfants
    private LoginView loginView;
    private HomeView homeView;

    // Controllers
    private ILoginController loginController;
    private ISessionController sessionController;
    private IChannelController channelController;
    private IChatController chatController;
    private IProfileController profileController;

    public void init(Stage primaryStage,
                     ILoginController loginController,
                     ISessionController sessionController,
                     IChannelController channelController,
                     IChatController chatController,
                     IProfileController profileController) {

        this.primaryStage = primaryStage;
        this.loginController = loginController;
        this.sessionController = sessionController;
        this.channelController = channelController;
        this.chatController = chatController;
        this.profileController = profileController;

        initGui();
    }

    private void initGui() {
        // Container principal
        mainContainer = new StackPane();

        // Initialisation des vues
        loginView = new LoginView(loginController);
        homeView = new HomeView(sessionController, channelController, chatController, profileController);

        // Ajouter les vues au container
        mainContainer.getChildren().addAll(homeView, loginView); // LoginView au-dessus par défaut
        showLoggedOut(); // Affiche loginView par défaut

        // Création de la scène
        Scene scene = new Scene(mainContainer, 1200, 800);

        // Chargement du CSS global
        String cssPath = "/com/message/ihm/views/styles.css";
        URL cssUrl = getClass().getResource(cssPath);

        if (cssUrl == null) {
            System.err.println("ERREUR : Impossible de trouver le fichier CSS via getClass().getResource('" + cssPath + "')");
            
            // Essai via le ClassLoader du thread courant
            cssUrl = Thread.currentThread().getContextClassLoader().getResource("com/message/ihm/views/styles.css");
            if (cssUrl == null) {
                 System.err.println("ERREUR : Impossible de trouver le fichier CSS via Thread.currentThread().getContextClassLoader().getResource('com/message/ihm/views/styles.css')");
            } else {
                System.out.println("SUCCÈS : CSS trouvé via Thread Context ClassLoader : " + cssUrl.toExternalForm());
            }
        } else {
            System.out.println("SUCCÈS : CSS trouvé via getClass().getResource() : " + cssUrl.toExternalForm());
        }

        if (cssUrl != null) {
            try {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } catch (Exception e) {
                System.err.println("EXCEPTION lors de l'ajout de la feuille de style : " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Icone de l'application
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo_20.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Icone introuvable !");
        }

        primaryStage.setTitle("MessageAppFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Affiche la vue après connexion.
     */
    public void showLoggedIn(User user) {
        homeView.setUser(user);
        loginView.setVisible(false);
        homeView.setVisible(true);
    }

    /**
     * Affiche la vue de login.
     */
    public void showLoggedOut() {
        homeView.setVisible(false);
        loginView.setVisible(true);
    }
}
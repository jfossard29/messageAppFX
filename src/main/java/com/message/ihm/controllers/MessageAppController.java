package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.session.ISession;
import com.message.core.session.ISessionObserver;
import com.message.datamodel.User;
import com.message.ihm.views.HomeView;
import com.message.ihm.views.LoginView;
import javafx.scene.layout.StackPane;

/**
 * Controller principal de l'application JavaFX.
 * Coordonne la navigation entre Login et Home.
 */
public class MessageAppController implements ISessionObserver {

    private final DataManager mDataManager;
    private final ISession mSession;

    private StackPane mainContainer;

    // Sous-contrôleurs
    private final LoginController mLoginController;
    private final SessionController mSessionController;
    private final ChannelController mChannelController;
    private final ChatController mChatController;
    private final ProfileController mProfileController;

    // Vues JavaFX
    private LoginView loginView;
    private HomeView homeView;

    public MessageAppController(DataManager dataManager, ISession session) {
        this.mDataManager = dataManager;
        this.mSession = session;
        this.mSession.addObserver(this);

        // Initialisation des sous-contrôleurs
        this.mLoginController = new LoginController(mDataManager, this);
        this.mSessionController = new SessionController(mDataManager, this);
        this.mChannelController = new ChannelController(mDataManager, session);
        this.mChatController = new ChatController(mDataManager, session);
        this.mProfileController = new ProfileController(mDataManager, session);
    }

    /**
     * Initialise la vue principale JavaFX avec le StackPane fourni.
     */
    public void initView(StackPane container) {
        this.mainContainer = container;

        // Création des vues
        this.loginView = new LoginView(mLoginController);
        this.homeView = new HomeView(mSessionController, mChannelController, mChatController, mProfileController);

        // Affichage par défaut de la vue de login
        showLoginView();
    }

    /**
     * Affiche la vue de login.
     */
    public void showLoginView() {
        mainContainer.getChildren().setAll(loginView);
    }

    /**
     * Affiche la vue d'accueil avec l'utilisateur connecté.
     */
    public void showHomeView(User user) {
        homeView.setUser(user);
        mainContainer.getChildren().setAll(homeView);
    }

    /**
     * Appelé par LoginController quand une connexion réussit.
     */
    public void loginSuccess(User user) {
        mSession.connect(user);
    }

    /**
     * Appelé par SessionController pour se déconnecter.
     */
    public void logout() {
        mSession.disconnect();
    }

    public User getCurrentUser() {
        return mSession.getConnectedUser();
    }

    @Override
    public void notifyLogin(User connectedUser) {
        showHomeView(connectedUser);
    }

    @Override
    public void notifyLogout() {
        showLoginView();
    }
}
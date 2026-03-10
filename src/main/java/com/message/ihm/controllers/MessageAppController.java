package com.message.ihm.controllers;

import com.message.core.DataManager;
import com.message.core.database.IDatabaseObserver;
import com.message.core.session.ISession;
import com.message.core.session.ISessionObserver;
import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.views.HomeView;
import com.message.ihm.views.LoginView;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller principal de l'application JavaFX.
 * Coordonne la navigation entre Login et Home.
 */
public class MessageAppController implements ISessionObserver, IDatabaseObserver {

    private final DataManager mDataManager;
    private final ISession mSession;
    private Stage primaryStage;

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
        this.mDataManager.addObserver(this);

        // Initialisation des sous-contrôleurs
        this.mLoginController = new LoginController(mDataManager, this);
        this.mSessionController = new SessionController(mDataManager, this);
        this.mChannelController = new ChannelController(mDataManager, session);
        this.mChatController = new ChatController(mDataManager, session);
        this.mProfileController = new ProfileController(mDataManager, session);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        User currentUser = mSession.getConnectedUser();
        if (currentUser == null || mainContainer == null || mainContainer.getScene() == null || mainContainer.getScene().getWindow() == null) {
            return;
        }

        if (addedMessage.getSender().getUuid().equals(currentUser.getUuid())) {
            return;
        }

        UUID recipientUuid = addedMessage.getRecipient();
        Optional<Channel> channelOpt = mDataManager.getChannels().stream()
                .filter(c -> c.getUuid().equals(recipientUuid))
                .findFirst();

        String sourceName = "Nouveau message";
        Channel channelToSelect = null;

        if (channelOpt.isPresent()) {
            Channel channel = channelOpt.get();
            sourceName = channel.getName();
            boolean isRelevant = false;
            if (channel.isPrivate()) {
                if (channel.getUsers().stream().anyMatch(u -> u.getUuid().equals(currentUser.getUuid()))) {
                    isRelevant = true;
                }
            } else {
                isRelevant = true;
            }
            if (isRelevant) {
                channelToSelect = channel;
            }
        } else if (recipientUuid.equals(currentUser.getUuid())) {
            sourceName = addedMessage.getSender().getName();
            User otherUser = addedMessage.getSender();
            
            Optional<Channel> dmChannelOpt = mDataManager.getChannels().stream()
                .filter(c -> c.isDirectMessage() &&
                             c.getUsers().size() == 2 &&
                             c.getUsers().stream().anyMatch(u -> u.getUuid().equals(currentUser.getUuid())) &&
                             c.getUsers().stream().anyMatch(u -> u.getUuid().equals(otherUser.getUuid())))
                .findFirst();

            if (dmChannelOpt.isPresent()) {
                channelToSelect = dmChannelOpt.get();
            } else {
                channelToSelect = new Channel(currentUser, otherUser.getName(), List.of(currentUser, otherUser));
                channelToSelect.setDirectMessage(true);
            }
        }

        if (channelToSelect != null) {
            showNotification(sourceName, addedMessage.getText(), channelToSelect);
        }
    }

    private void showNotification(String title, String content, Channel channel) {
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoHide(true);

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(15));
            layout.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20, 0.9), new CornerRadii(5), Insets.EMPTY)));
            layout.setEffect(new DropShadow());
            layout.setPrefWidth(300);
            layout.setMaxWidth(300);
            layout.setOpacity(0); // For fade in

            layout.setOnMouseClicked(e -> {
                if (primaryStage != null) {
                    if (primaryStage.isIconified()) {
                        primaryStage.setIconified(false);
                    }
                    primaryStage.toFront();
                }
                if (channel != null) {
                    mSessionController.selectChannel(channel);
                }
                popup.hide();
            });

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleLabel.setTextFill(Color.WHITE);

            String displayContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
            Label contentLabel = new Label(displayContent);
            contentLabel.setFont(Font.font("System", 12));
            contentLabel.setTextFill(Color.WHITE);
            contentLabel.setWrapText(true);

            layout.getChildren().addAll(titleLabel, contentLabel);
            popup.getContent().add(layout);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            popup.show(mainContainer.getScene().getWindow(),
                    screenBounds.getMaxX() - layout.getPrefWidth() - 20,
                    screenBounds.getMaxY() - 150);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), layout);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), layout);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(event -> popup.hide());
                fadeOut.play();
            });
            delay.play();
        });
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {}
    @Override
    public void notifyMessageModified(Message modifiedMessage) {}
    @Override
    public void notifyUserAdded(User addedUser) {}
    @Override
    public void notifyUserDeleted(User deletedUser) {}
    @Override
    public void notifyUserModified(User modifiedUser) {}
    @Override
    public void notifyChannelAdded(Channel addedChannel) {}
    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {}
    @Override
    public void notifyChannelModified(Channel modifiedChannel) {}
}

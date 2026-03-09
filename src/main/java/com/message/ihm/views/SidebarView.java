package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChannelController;
import com.message.ihm.controllers.IProfileController;
import com.message.ihm.controllers.ISessionController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SidebarView extends BorderPane implements ISessionController.ISessionControllerObserver, IChannelController.IChannelControllerObserver, IProfileController.IProfileControllerObserver {

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IProfileController mProfileController;

    private User mCurrentUser;

    private Set<User> listAllUsers = new HashSet<>();
    private Set<Channel> listAllChannels = new HashSet<>();
    private Channel mSelectedChannel;

    // Gestion des alertes
    private Set<UUID> unreadChannels = new HashSet<>();
    private Set<UUID> unreadUsers = new HashSet<>();

    private VBox mainListBox;
    private TextField searchField;
    private Button userProfileBtn;

    public SidebarView(ISessionController sessionController,
                       IChannelController channelController,
                       IProfileController profileController) {

        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mProfileController = profileController;

        this.mSessionController.addObserver(this);
        this.mChannelController.addObserver(this);
        this.mProfileController.addObserver(this);
        initGui();
    }

    private void initGui() {

        this.setPrefWidth(240);
        this.setStyle("-fx-background-color: rgb(32,34,37);");

        /* ================= HEADER ================= */

        Label appName = new Label("MessageApp");
        appName.setTextFill(Color.WHITE);
        appName.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        appName.setPadding(new Insets(15));

        Button addChannelBtn = new Button("+");

        String btnNormal = """
                -fx-background-color: rgb(88,101,242);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 16px;
                -fx-background-radius: 4;
                -fx-min-width: 28px;
                -fx-min-height: 28px;
                -fx-max-width: 28px;
                -fx-max-height: 28px;
                -fx-padding: 0;
                -fx-cursor: hand;
                """;
        String btnHover = """
                -fx-background-color: rgb(71,82,196);
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 16px;
                -fx-background-radius: 4;
                -fx-min-width: 28px;
                -fx-min-height: 28px;
                -fx-max-width: 28px;
                -fx-max-height: 28px;
                -fx-padding: 0;
                -fx-cursor: hand;
                """;

        addChannelBtn.setStyle(btnNormal);
        addChannelBtn.setOnMouseEntered(e -> addChannelBtn.setStyle(btnHover));
        addChannelBtn.setOnMouseExited(e -> addChannelBtn.setStyle(btnNormal));

        addChannelBtn.setOnAction(e -> {
            AddChannelDialog dialog =
                    new AddChannelDialog((Stage) getScene().getWindow(),
                            mChannelController,
                            listAllUsers,
                            mCurrentUser);
            dialog.showAndWait();
        });

        HBox header = new HBox(appName, new Region(), addChannelBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 15, 0, 0));

        this.setTop(header);

        /* ================= CENTER ================= */

        mainListBox = new VBox();
        mainListBox.setSpacing(2);
        mainListBox.setPadding(new Insets(5));

        ScrollPane scrollPane = new ScrollPane(mainListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: rgb(32,34,37);");

        this.setCenter(scrollPane);

        /* ================= SEARCH ================= */

        searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: white;
                """);

        searchField.textProperty().addListener((obs, oldV, newV) ->
                filterLists(newV));

        /* ================= USER PANEL ================= */

        userProfileBtn = new Button("Utilisateur");
        userProfileBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight:bold;");

        Label settingsIcon = new Label("\u2699");
        settingsIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        userProfileBtn.setGraphic(settingsIcon);
        userProfileBtn.setContentDisplay(ContentDisplay.RIGHT);
        userProfileBtn.setGraphicTextGap(8);

        userProfileBtn.setOnMouseEntered(e -> userProfileBtn.setStyle("-fx-background-color: rgb(60,62,66); -fx-text-fill: white; -fx-font-weight:bold; -fx-background-radius: 4;"));
        userProfileBtn.setOnMouseExited(e -> userProfileBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight:bold;"));

        userProfileBtn.setOnAction(e -> {
            ProfileDialog dialog =
                    new ProfileDialog((Stage) getScene().getWindow(),
                            mProfileController,
                            mCurrentUser);
            dialog.showAndWait();
        });

        Button logoutBtn = new Button("Déconnexion");
        String logoutNormal = "-fx-background-color: rgb(237,66,69); -fx-text-fill: white;";
        String logoutHover = "-fx-background-color: rgb(200,50,50); -fx-text-fill: white;";

        logoutBtn.setStyle(logoutNormal);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutHover));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(logoutNormal));
        logoutBtn.setOnAction(e -> mSessionController.logout());

        HBox userPanel = new HBox(10, userProfileBtn, new Region(), logoutBtn);
        HBox.setHgrow(userPanel.getChildren().get(1), Priority.ALWAYS);
        userPanel.setPadding(new Insets(10));
        userPanel.setStyle("-fx-background-color: rgb(41,43,47);");
        userPanel.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(searchField, userPanel);

        this.setBottom(bottomBox);
    }

    /* ================= PUBLIC METHODS ================= */

    public void setUser(User user) {
        this.mCurrentUser = user;
        if (user != null) {
            userProfileBtn.setText(user.getName());
        }
        refreshLists();
    }

    public void refreshLists() {
        listAllUsers = mSessionController.getAllUsers();
        listAllChannels = mChannelController.getAllChannels();
        filterLists(searchField.getText());
    }

    /* ================= FILTER ================= */

    private void filterLists(String query) {

        if (query == null) query = "";

        String lower = query.toLowerCase();

        // Filter channels first
        Set<Channel> channelsToDisplay = listAllChannels.stream()
                .filter(c -> {
                    if (c.isPrivate()) {
                        // If private, only show if current user is a member
                        return mCurrentUser != null && c.getUsers().stream().anyMatch(u -> u.getUuid().equals(mCurrentUser.getUuid()));
                    }
                    return true; // Public channels are always visible
                })
                .collect(Collectors.toSet());

        Set<Channel> channelsFiltered = channelsToDisplay.stream()
                .filter(c -> c.getName().toLowerCase().contains(lower))
                .collect(Collectors.toSet());


        // Filter users based on selected channel
        Set<User> usersToDisplay;
        
        // On filtre les utilisateurs SEULEMENT si le canal sélectionné est privé ET n'est PAS un DM
        if (mSelectedChannel != null && mSelectedChannel.isPrivate() && !mSelectedChannel.isDirectMessage()) {
            List<User> allowedUsers = mSelectedChannel.getUsers();
            Set<UUID> allowedUserIds = allowedUsers.stream()
                    .map(User::getUuid)
                    .collect(Collectors.toSet());

            usersToDisplay = listAllUsers.stream()
                    .filter(u -> allowedUserIds.contains(u.getUuid()))
                    .collect(Collectors.toSet());
        } else {
            // Si c'est un canal public, un DM, ou aucun canal sélectionné, on affiche tout le monde
            usersToDisplay = new HashSet<>(listAllUsers);
        }

        Set<User> usersFiltered = usersToDisplay.stream()
                .filter(u -> u.getUserTag().toLowerCase().contains(lower)
                        || (u.getName() != null &&
                        u.getName().toLowerCase().contains(lower)))
                .collect(Collectors.toSet());

        updateDisplay(channelsFiltered, usersFiltered);
    }

    /* ================= DISPLAY ================= */

    private void updateDisplay(Set<Channel> channels,
                               Set<User> users) {

        Platform.runLater(() -> {

            mainListBox.getChildren().clear();

            if (!channels.isEmpty()) {

                Label channelTitle = sectionLabel("CANAUX");
                mainListBox.getChildren().add(channelTitle);

                for (Channel c : channels) {
                    Button btn = createChannelButton(c);
                    mainListBox.getChildren().add(btn);
                }
            }

            if (!users.isEmpty()) {

                Label userTitle = sectionLabel("UTILISATEURS");
                userTitle.setPadding(new Insets(10, 0, 0, 0));
                mainListBox.getChildren().add(userTitle);

                for (User u : users) {
                    if (mCurrentUser != null &&
                            u.getUserTag().equals(mCurrentUser.getUserTag()))
                        continue;

                    HBox userRow = createUserRow(u);
                    mainListBox.getChildren().add(userRow);
                }
            }
        });
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.rgb(142,146,151));
        label.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");
        label.setPadding(new Insets(5, 5, 5, 5));
        return label;
    }

    private Button createChannelButton(Channel c) {
        Button btn = new Button("# " + c.getName());
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);

        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: rgb(142,146,151);";
        String hoverStyle = "-fx-background-color: rgb(50,53,59); -fx-text-fill: white;";
        
        // Style pour les canaux avec messages non lus (pastille blanche)
        if (unreadChannels.contains(c.getUuid())) {
            btn.setGraphic(new Circle(4, Color.WHITE));
            btn.setGraphicTextGap(8);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        // Si c'est le canal sélectionné, on le met en surbrillance
        if (mSelectedChannel != null && mSelectedChannel.getUuid().equals(c.getUuid())) {
             btn.setStyle("-fx-background-color: rgb(57,60,67); -fx-text-fill: white;");
        }

        btn.setOnMouseEntered(e -> {
            if (mSelectedChannel == null || !mSelectedChannel.getUuid().equals(c.getUuid())) {
                btn.setStyle(hoverStyle);
            }
        });
        btn.setOnMouseExited(e -> {
            if (mSelectedChannel != null && mSelectedChannel.getUuid().equals(c.getUuid())) {
                btn.setStyle("-fx-background-color: rgb(57,60,67); -fx-text-fill: white;");
            } else if (unreadChannels.contains(c.getUuid())) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                btn.setStyle(normalStyle);
            }
        });

        btn.setOnAction(e -> mSessionController.selectChannel(c));

        return btn;
    }

    private HBox createUserRow(User u) {

        Circle status = new Circle(4);
        status.setFill(u.isOnline()
                ? Color.rgb(59,165,93)
                : Color.rgb(116,127,141));

        Label name = new Label(u.getName());
        name.setTextFill(Color.rgb(142,146,151));
        
        // Gestion de l'alerte DM (pastille rouge)
        Circle alertBadge = null;
        if (unreadUsers.contains(u.getUuid())) {
            alertBadge = new Circle(4, Color.RED);
            name.setTextFill(Color.WHITE);
            name.setStyle("-fx-font-weight: bold;");
        }

        HBox box;
        if (alertBadge != null) {
            box = new HBox(8, status, name, new Region(), alertBadge);
            HBox.setHgrow(box.getChildren().get(2), Priority.ALWAYS);
        } else {
            box = new HBox(8, status, name);
        }
        
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 5, 5, 10));

        String normalStyle = "-fx-background-color: transparent;";
        String hoverStyle = "-fx-background-color: rgb(50,53,59); -fx-cursor: hand; -fx-background-radius: 4;";
        
        // Si c'est l'utilisateur sélectionné (via DM)
        if (mSelectedChannel != null && mSelectedChannel.isDirectMessage()) {
             boolean isSelectedUser = mSelectedChannel.getUsers().stream()
                     .anyMatch(user -> user.getUuid().equals(u.getUuid()));
             if (isSelectedUser) {
                 normalStyle = "-fx-background-color: rgb(57,60,67); -fx-background-radius: 4;";
                 name.setTextFill(Color.WHITE);
             }
        }

        box.setStyle(normalStyle);
        
        final String finalNormalStyle = normalStyle;
        
        box.setOnMouseEntered(e -> {
            box.setStyle(hoverStyle);
            name.setTextFill(Color.WHITE);
        });
        box.setOnMouseExited(e -> {
            box.setStyle(finalNormalStyle);
            if (!unreadUsers.contains(u.getUuid()) && !finalNormalStyle.contains("rgb(57,60,67)")) {
                name.setTextFill(Color.rgb(142,146,151));
            }
        });

        // Ajout de l'action au clic
        box.setOnMouseClicked(e -> {
            if (mCurrentUser != null && !mCurrentUser.getUuid().equals(u.getUuid())) {
                // Création d'un canal "fantôme" pour le DM
                List<User> participants = new ArrayList<>();
                participants.add(mCurrentUser);
                participants.add(u);

                // Le nom est celui de l'autre utilisateur, pour l'affichage dans le header du chat
                Channel phantomChannel = new Channel(mCurrentUser, u.getName(), participants);
                phantomChannel.setDirectMessage(true);

                mSessionController.selectChannel(phantomChannel);
            }
        });

        return box;
    }

    /* ================= OBSERVER ================= */

    @Override
    public void onChannelSelected(Channel channel) {
        this.mSelectedChannel = channel;
        
        // Suppression des alertes si on sélectionne le canal concerné
        if (channel != null) {
            if (channel.isDirectMessage()) {
                // Si c'est un DM, on enlève l'alerte pour l'autre utilisateur
                if (mCurrentUser != null) {
                    channel.getUsers().stream()
                            .filter(u -> !u.getUuid().equals(mCurrentUser.getUuid()))
                            .findFirst()
                            .ifPresent(u -> unreadUsers.remove(u.getUuid()));
                }
            } else {
                // Si c'est un canal normal, on enlève l'alerte pour ce canal
                unreadChannels.remove(channel.getUuid());
            }
        }
        
        filterLists(searchField.getText());
    }

    @Override
    public void onUsersUpdated() {
        refreshLists();
    }

    @Override
    public void onChannelListUpdated() {
        refreshLists();
    }

    @Override
    public void onProfileUpdated() {
        if (mCurrentUser != null) {
            userProfileBtn.setText(mCurrentUser.getName());
        }
        refreshLists();
    }

    @Override
    public void onMessageReceived(Message message) {
        if (mCurrentUser == null) return;

        // Ignorer mes propres messages
        if (message.getSender().getUuid().equals(mCurrentUser.getUuid())) {
            return;
        }

        UUID recipientId = message.getRecipient();
        boolean isDM = false;
        
        // Vérifier si c'est un DM (le destinataire est moi)
        if (recipientId.equals(mCurrentUser.getUuid())) {
            isDM = true;
        }

        boolean needRefresh = false;

        if (isDM) {
            // C'est un DM pour moi
            UUID senderId = message.getSender().getUuid();
            
            // Si je suis déjà en train de regarder ce DM, pas d'alerte
            boolean isViewingThisDM = false;
            if (mSelectedChannel != null && mSelectedChannel.isDirectMessage()) {
                 isViewingThisDM = mSelectedChannel.getUsers().stream()
                         .anyMatch(u -> u.getUuid().equals(senderId));
            }

            if (!isViewingThisDM) {
                unreadUsers.add(senderId);
                needRefresh = true;
            }
        } else {
            // C'est un message de canal
            // Si je suis déjà sur ce canal, pas d'alerte
            if (mSelectedChannel == null || !mSelectedChannel.getUuid().equals(recipientId)) {
                unreadChannels.add(recipientId);
                needRefresh = true;
            }
        }

        if (needRefresh) {
            refreshLists();
        }
    }
}

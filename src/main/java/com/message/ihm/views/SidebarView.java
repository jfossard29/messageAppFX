package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.interfaces.IChannelController;
import com.message.ihm.controllers.interfaces.IProfileController;
import com.message.ihm.controllers.interfaces.ISessionController;
import com.message.ihm.controllers.SidebarController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SidebarView extends BorderPane implements ISessionController.ISessionControllerObserver, IChannelController.IChannelControllerObserver, IProfileController.IProfileControllerObserver {

    private final SidebarController mController;

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

    public SidebarView(SidebarController controller) {
        this.mController = controller;

        this.mController.getSessionController().addObserver(this);
        this.mController.getChannelController().addObserver(this);
        this.mController.getProfileController().addObserver(this);
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
                -fx-background-color: rgb(88,101,242); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;
                -fx-background-radius: 4; -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px;
                -fx-padding: 0; -fx-cursor: hand;
                """;
        String btnHover = """
                -fx-background-color: rgb(71,82,196); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;
                -fx-background-radius: 4; -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px;
                -fx-padding: 0; -fx-cursor: hand;
                """;

        addChannelBtn.setStyle(btnNormal);
        addChannelBtn.setOnMouseEntered(e -> addChannelBtn.setStyle(btnHover));
        addChannelBtn.setOnMouseExited(e -> addChannelBtn.setStyle(btnNormal));

        addChannelBtn.setOnAction(e -> {
            AddChannelDialog dialog =
                    new AddChannelDialog((Stage) getScene().getWindow(),
                            mController.getChannelController(),
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
        searchField.setStyle("-fx-background-color: rgb(64,68,75); -fx-text-fill: white;");
        searchField.textProperty().addListener((obs, oldV, newV) -> filterLists(newV));

        /* ================= USER PANEL ================= */

        userProfileBtn = new Button("Utilisateur");
        userProfileBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight:bold;");

        Label settingsIcon = new Label("⚙");
        settingsIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        userProfileBtn.setGraphic(settingsIcon);
        userProfileBtn.setContentDisplay(ContentDisplay.RIGHT);
        userProfileBtn.setGraphicTextGap(8);

        userProfileBtn.setOnMouseEntered(e -> userProfileBtn.setStyle("-fx-background-color: rgb(60,62,66); -fx-text-fill: white; -fx-font-weight:bold; -fx-background-radius: 4;"));
        userProfileBtn.setOnMouseExited(e -> userProfileBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight:bold;"));

        userProfileBtn.setOnAction(e -> {
            ProfileDialog dialog =
                    new ProfileDialog((Stage) getScene().getWindow(),
                            mController.getProfileController(),
                            mCurrentUser);
            dialog.showAndWait();
        });

        Button logoutBtn = new Button("Déconnexion");
        String logoutNormal = "-fx-background-color: rgb(237,66,69); -fx-text-fill: white;";
        String logoutHover = "-fx-background-color: rgb(200,50,50); -fx-text-fill: white;";

        logoutBtn.setStyle(logoutNormal);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutHover));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(logoutNormal));
        logoutBtn.setOnAction(e -> mController.logout());

        HBox userPanel = new HBox(10, userProfileBtn, new Region(), logoutBtn);
        HBox.setHgrow(userPanel.getChildren().get(1), Priority.ALWAYS);
        userPanel.setPadding(new Insets(10));
        userPanel.setStyle("-fx-background-color: rgb(41,43,47);");
        userPanel.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(searchField, userPanel);

        this.setBottom(bottomBox);
    }

    public void setUser(User user) {
        this.mCurrentUser = user;
        if (user != null) {
            userProfileBtn.setText(user.getName());
            userProfileBtn.setGraphic(createAvatar(user, 16, false));
            userProfileBtn.setContentDisplay(ContentDisplay.LEFT);
        }
        refreshLists();
    }

    public void refreshLists() {
        listAllUsers = mController.getUsers();
        listAllChannels = mController.getChannels();
        filterLists(searchField.getText());
    }

    private void filterLists(String query) {
        if (query == null) query = "";
        String lower = query.toLowerCase();

        Set<Channel> channelsToDisplay = listAllChannels.stream()
                .filter(c -> !c.isPrivate() || (mCurrentUser != null && c.getUsers().stream().anyMatch(u -> u.getUuid().equals(mCurrentUser.getUuid()))))
                .collect(Collectors.toSet());

        Set<Channel> channelsFiltered = channelsToDisplay.stream()
                .filter(c -> c.getName().toLowerCase().contains(lower))
                .collect(Collectors.toSet());

        Set<User> usersToDisplay;
        if (mSelectedChannel != null && mSelectedChannel.isPrivate() && !mSelectedChannel.isDirectMessage()) {
            Set<UUID> allowedUserIds = mSelectedChannel.getUsers().stream().map(User::getUuid).collect(Collectors.toSet());
            usersToDisplay = listAllUsers.stream().filter(u -> allowedUserIds.contains(u.getUuid())).collect(Collectors.toSet());
        } else {
            usersToDisplay = new HashSet<>(listAllUsers);
        }

        Set<User> usersFiltered = usersToDisplay.stream()
                .filter(u -> u.getUserTag().toLowerCase().contains(lower) || (u.getName() != null && u.getName().toLowerCase().contains(lower)))
                .collect(Collectors.toSet());

        updateDisplay(channelsFiltered, usersFiltered);
    }

    private void updateDisplay(Set<Channel> channels, Set<User> users) {
        Platform.runLater(() -> {
            mainListBox.getChildren().clear();

            if (!channels.isEmpty()) {
                mainListBox.getChildren().add(sectionLabel("CANAUX"));
                channels.forEach(c -> mainListBox.getChildren().add(createChannelButton(c)));
            }

            if (!users.isEmpty()) {
                mainListBox.getChildren().add(sectionLabel("UTILISATEURS"));
                users.stream()
                     .filter(u -> mCurrentUser == null || !u.getUserTag().equals(mCurrentUser.getUserTag()))
                     .forEach(u -> mainListBox.getChildren().add(createUserRow(u)));
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
        String selectedStyle = "-fx-background-color: rgb(57,60,67); -fx-text-fill: white;";
        String unreadStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;";

        btn.setStyle(normalStyle);

        if (unreadChannels.contains(c.getUuid())) {
            btn.setGraphic(new Circle(4, Color.WHITE));
            btn.setGraphicTextGap(8);
            btn.setStyle(unreadStyle);
        } else {
            btn.setGraphic(null);
        }

        if (mSelectedChannel != null && !mSelectedChannel.isDirectMessage() && mSelectedChannel.getUuid().equals(c.getUuid())) {
             btn.setStyle(selectedStyle);
        }

        btn.setOnMouseEntered(e -> {
            if (mSelectedChannel == null || mSelectedChannel.isDirectMessage() || !mSelectedChannel.getUuid().equals(c.getUuid())) {
                btn.setStyle(hoverStyle);
            }
        });
        btn.setOnMouseExited(e -> {
            if (mSelectedChannel != null && !mSelectedChannel.isDirectMessage() && mSelectedChannel.getUuid().equals(c.getUuid())) {
                btn.setStyle(selectedStyle);
            } else if (unreadChannels.contains(c.getUuid())) {
                btn.setStyle(unreadStyle);
            } else {
                btn.setStyle(normalStyle);
            }
        });

        btn.setOnAction(e -> mController.selectChannel(c));
        return btn;
    }

    private StackPane createAvatar(User user, double radius, boolean withStatus) {
        StackPane rootStack = new StackPane();
        StackPane avatarContainer = new StackPane();
        Circle avatarCircle = new Circle(radius);

        String picturePath = user.getPicturePath();
        if (picturePath != null && !picturePath.trim().isEmpty()) {
            Image image = new Image(picturePath, true);
            
            image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0 && !image.isError()) {
                    avatarCircle.setFill(new ImagePattern(image));
                    avatarContainer.getChildren().setAll(avatarCircle);
                }
            });
            
            image.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    showUserInitial(avatarContainer, avatarCircle, user, radius);
                }
            });

            if (image.getProgress() == 1.0 && !image.isError()) {
                avatarCircle.setFill(new ImagePattern(image));
                avatarContainer.getChildren().setAll(avatarCircle);
            } else {
                showUserInitial(avatarContainer, avatarCircle, user, radius);
            }
        } else {
            showUserInitial(avatarContainer, avatarCircle, user, radius);
        }

        rootStack.getChildren().add(avatarContainer);

        if (withStatus) {
            Circle status = new Circle(radius * 0.4, user.isOnline() ? Color.rgb(59, 165, 93) : Color.rgb(116, 127, 141));
            status.setStroke(Color.rgb(32, 34, 37));
            status.setStrokeWidth(radius * 0.15);
            rootStack.getChildren().add(status);
            StackPane.setAlignment(status, Pos.BOTTOM_RIGHT);
        }

        return rootStack;
    }

    private void showUserInitial(StackPane container, Circle circle, User user, double radius) {
        circle.setFill(Color.web("#5865F2"));
        Label initial = new Label(user.getName().substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Segoe UI", FontWeight.BOLD, radius));
        container.getChildren().setAll(circle, initial);
    }

    private HBox createUserRow(User u) {
        StackPane avatarStack = createAvatar(u, 16, true);

        Label name = new Label(u.getName());
        name.setTextFill(Color.rgb(142,146,151));
        
        Circle alertBadge = null;
        if (unreadUsers.contains(u.getUuid())) {
            alertBadge = new Circle(4, Color.RED);
            name.setTextFill(Color.WHITE);
            name.setStyle("-fx-font-weight: bold;");
        }

        HBox box;
        if (alertBadge != null) {
            box = new HBox(8, avatarStack, name, new Region(), alertBadge);
            HBox.setHgrow(box.getChildren().get(2), Priority.ALWAYS);
        } else {
            box = new HBox(8, avatarStack, name);
        }
        
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 5, 5, 10));

        String normalStyle = "-fx-background-color: transparent;";
        String hoverStyle = "-fx-background-color: rgb(50,53,59); -fx-cursor: hand; -fx-background-radius: 4;";
        
        if (mSelectedChannel != null && mSelectedChannel.isDirectMessage() && mSelectedChannel.getUsers().stream().anyMatch(user -> user.getUuid().equals(u.getUuid()))) {
             normalStyle = "-fx-background-color: rgb(57,60,67); -fx-background-radius: 4;";
             name.setTextFill(Color.WHITE);
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

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #18191c; -fx-background-radius: 6; -fx-padding: 5;");
        CustomMenuItem profileItem = new CustomMenuItem(new Label("Voir le profil"));
        profileItem.setOnAction(e -> showProfileDialog(u));
        contextMenu.getItems().add(profileItem);

        box.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mController.selectDmChannel(u);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(box, e.getScreenX(), e.getScreenY());
            }
        });

        return box;
    }
    
    private void showProfileDialog(User user) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(this.getScene().getWindow());
        dialogStage.setTitle("Profil de " + user.getName());
        Scene scene = new Scene(new ProfileView(user), 350, 400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    @Override
    public void onChannelSelected(Channel channel) {
        this.mSelectedChannel = channel;
        if (channel != null) {
            if (channel.isDirectMessage()) {
                if (mCurrentUser != null) {
                    channel.getUsers().stream()
                            .filter(u -> !u.getUuid().equals(mCurrentUser.getUuid()))
                            .findFirst()
                            .ifPresent(u -> unreadUsers.remove(u.getUuid()));
                }
            } else {
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
        Platform.runLater(() -> {
            if (mCurrentUser != null) {
                userProfileBtn.setText(mCurrentUser.getName());
                userProfileBtn.setGraphic(createAvatar(mCurrentUser, 16, false));
            }
            refreshLists();
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        if (mCurrentUser == null || message.getSender().getUuid().equals(mCurrentUser.getUuid())) return;

        UUID recipientId = message.getRecipient();
        boolean isDM = recipientId.equals(mCurrentUser.getUuid());
        boolean needRefresh = false;

        if (isDM) {
            UUID senderId = message.getSender().getUuid();
            boolean isViewingThisDM = mSelectedChannel != null && mSelectedChannel.isDirectMessage() && mSelectedChannel.getUsers().stream().anyMatch(u -> u.getUuid().equals(senderId));
            if (!isViewingThisDM) {
                unreadUsers.add(senderId);
                needRefresh = true;
            }
        } else {
            if (mSelectedChannel == null || !mSelectedChannel.getUuid().equals(recipientId)) {
                unreadChannels.add(recipientId);
                needRefresh = true;
            }
        }

        if (needRefresh) {
            refreshLists();
        }
    }

    public List<Node> getChannelCells() {
        List<Node> channels = new ArrayList<>();
        for (Node node : mainListBox.getChildren()) {
            if (node instanceof Button) {
                channels.add(node);
            }
        }
        return channels;
    }

    public List<Node> getUserCells() {
        List<Node> users = new ArrayList<>();
        for (Node node : mainListBox.getChildren()) {
            if (node instanceof HBox) {
                users.add(node);
            }
        }
        return users;
    }
}

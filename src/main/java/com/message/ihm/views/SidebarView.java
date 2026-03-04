package com.message.ihm.views;

import com.message.datamodel.Channel;
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SidebarView extends BorderPane implements ISessionController.ISessionControllerObserver {

    private final ISessionController mSessionController;
    private final IChannelController mChannelController;
    private final IProfileController mProfileController;

    private User mCurrentUser;

    private Set<User> listAllUsers = new HashSet<>();
    private Set<Channel> listAllChannels = new HashSet<>();

    private VBox mainListBox;
    private TextField searchField;
    private Label userLabel;

    public SidebarView(ISessionController sessionController,
                       IChannelController channelController,
                       IProfileController profileController) {

        this.mSessionController = sessionController;
        this.mChannelController = channelController;
        this.mProfileController = profileController;

        this.mSessionController.addObserver(this);
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
        addChannelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        addChannelBtn.setOnAction(e -> {
            AddChannelDialog dialog =
                    new AddChannelDialog((Stage) getScene().getWindow(),
                            mChannelController,
                            listAllUsers);
            dialog.showAndWait();
            refreshLists();
        });

        HBox header = new HBox(appName, new Region(), addChannelBtn);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

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

        userLabel = new Label("Utilisateur");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setStyle("-fx-font-weight:bold;");
        userLabel.setOnMouseClicked(e -> {
            ProfileDialog dialog =
                    new ProfileDialog((Stage) getScene().getWindow(),
                            mProfileController,
                            mCurrentUser);
            dialog.showAndWait();
        });

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("""
                -fx-background-color: rgb(237,66,69);
                -fx-text-fill: white;
                """);
        logoutBtn.setOnAction(e -> mSessionController.logout());

        HBox userPanel = new HBox(userLabel, new Region(), logoutBtn);
        HBox.setHgrow(userPanel.getChildren().get(1), Priority.ALWAYS);
        userPanel.setPadding(new Insets(10));
        userPanel.setStyle("-fx-background-color: rgb(41,43,47);");

        VBox bottomBox = new VBox(searchField, userPanel);

        this.setBottom(bottomBox);
    }

    /* ================= PUBLIC METHODS ================= */

    public void setUser(User user) {
        this.mCurrentUser = user;
        if (user != null) {
            userLabel.setText(user.getUserTag());
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

        Set<User> usersFiltered = listAllUsers.stream()
                .filter(u -> u.getUserTag().toLowerCase().contains(lower)
                        || (u.getName() != null &&
                        u.getName().toLowerCase().contains(lower)))
                .collect(Collectors.toSet());

        Set<Channel> channelsFiltered = listAllChannels.stream()
                .filter(c -> c.getName().toLowerCase().contains(lower))
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
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgb(142,146,151);");

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

        HBox box = new HBox(8, status, name);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5, 5, 5, 10));

        return box;
    }

    /* ================= OBSERVER ================= */

    @Override
    public void onChannelSelected(Channel channel) {
        // Géré par ChatView
    }

    @Override
    public void onUsersUpdated() {
        refreshLists();
    }
}
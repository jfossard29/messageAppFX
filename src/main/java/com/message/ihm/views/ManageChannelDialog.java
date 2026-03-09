package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChannelController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ManageChannelDialog extends Stage {

    private final IChannelController mChannelController;
    private final Channel mChannel;
    private final User mCurrentUser;
    private VBox mUsersBox;

    public ManageChannelDialog(Stage owner, IChannelController channelController, Channel channel, User currentUser) {
        this.mChannelController = channelController;
        this.mChannel = channel;
        this.mCurrentUser = currentUser;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Gérer le canal : " + channel.getName());
        initGui();
    }

    private void initGui() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgb(54, 57, 63);");
        root.setPrefSize(400, 500);

        // Titre
        Label titleLabel = new Label("Membres du canal");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Liste des utilisateurs
        mUsersBox = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(mUsersBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: rgb(47, 49, 54); -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(10));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refreshUserList();

        // Bouton Fermer
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: rgb(79, 84, 92); -fx-text-fill: white;");
        closeButton.setOnAction(e -> close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, scrollPane, buttonBox);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private void refreshUserList() {
        mUsersBox.getChildren().clear();
        List<User> users = mChannel.getUsers();

        if (users.isEmpty()) {
            Label emptyLabel = new Label("Aucun membre.");
            emptyLabel.setTextFill(Color.GRAY);
            mUsersBox.getChildren().add(emptyLabel);
            return;
        }

        for (User user : users) {
            // On ne s'affiche pas soi-même (le créateur) dans la liste des gens à expulser
            if (user.getUuid().equals(mCurrentUser.getUuid())) {
                continue;
            }

            HBox row = createUserRow(user);
            mUsersBox.getChildren().add(row);
        }
        
        if (mUsersBox.getChildren().isEmpty()) {
             Label emptyLabel = new Label("Vous êtes le seul membre.");
             emptyLabel.setTextFill(Color.GRAY);
             mUsersBox.getChildren().add(emptyLabel);
        }
    }

    private HBox createUserRow(User user) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: rgb(47, 49, 54); -fx-background-radius: 5;");

        // Indicateur en ligne (optionnel, on met gris par défaut ou on utilise isOnline si dispo)
        Circle statusCircle = new Circle(5, user.isOnline() ? Color.rgb(59,165,93) : Color.GRAY);

        Label nameLabel = new Label(user.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button kickButton = new Button("Expulser");
        kickButton.setStyle("-fx-background-color: rgb(237, 66, 69); -fx-text-fill: white; -fx-font-size: 12px;");
        
        kickButton.setOnAction(e -> {
            boolean success = mChannelController.leaveChannel(mChannel, user);
            if (success) {
                refreshUserList();
            }
        });

        row.getChildren().addAll(statusCircle, nameLabel, spacer, kickButton);
        return row;
    }
}

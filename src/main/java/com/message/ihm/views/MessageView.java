package com.message.ihm.views;

import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChatController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageView extends HBox {

    private static final String COLOR_TEXT = "#DCDDDE";
    private static final String COLOR_TEXT_MUTED = "#8E9297";
    private static final String COLOR_BACKGROUND = "#36393F";
    private static final String COLOR_BACKGROUND_SELF = "#40444B";
    private static final String COLOR_ACCENT = "#5865F2";
    private static final String FONT_FAMILY = "Segoe UI";

    private final IChatController mChatController;

    public MessageView(Message message, boolean isCurrentUser, IChatController chatController) {
        this.mChatController = chatController;
        initUI(message, isCurrentUser);
    }

    public MessageView(Message message) {
        this(message, false, null);
    }

    private void initUI(Message message, boolean isCurrentUser) {

        String bgColor = isCurrentUser ? COLOR_BACKGROUND_SELF : COLOR_BACKGROUND;

        this.setSpacing(10);
        this.setPadding(new Insets(8));
        this.setAlignment(Pos.TOP_LEFT);
        this.setStyle("-fx-background-color: " + bgColor + ";");

        // Avatar
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(40, 40);
        Circle circle = new Circle(20);
        
        String picturePath = message.getSender().getPicturePath();

        if (picturePath != null && !picturePath.trim().isEmpty()) {
            Image image = new Image(picturePath, true); // background loading
            
            // Wait for loading to complete before setting ImagePattern
            image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                if (newProgress.doubleValue() == 1.0 && !image.isError()) {
                    circle.setFill(new ImagePattern(image));
                    // Remove initials if present
                    if (avatarPane.getChildren().size() > 1) {
                        avatarPane.getChildren().remove(1); 
                    }
                }
            });
            
            image.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    Platform.runLater(() -> showInitials(avatarPane, circle, message.getSender()));
                }
            });
            
            // Initially show initials or placeholder while loading
            showInitials(avatarPane, circle, message.getSender());

        } else {
            showInitials(avatarPane, circle, message.getSender());
        }

        // Conteneur message (droite)
        VBox messageBox = new VBox(3);
        messageBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(messageBox, Priority.ALWAYS);

        // Header (nom + date)
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(message.getSender().getName());
        nameLabel.setTextFill(Color.web(COLOR_TEXT));
        nameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(sdf.format(new Date(message.getEmissionDate())));
        dateLabel.setTextFill(Color.web(COLOR_TEXT_MUTED));
        dateLabel.setFont(Font.font(FONT_FAMILY, 10));

        header.getChildren().addAll(nameLabel, dateLabel);

        // Contenu du message
        Label messageContent = new Label(message.getText());
        messageContent.setWrapText(true);
        messageContent.setTextFill(Color.web(COLOR_TEXT));
        messageContent.setFont(Font.font(FONT_FAMILY, 14));
        
        if ("Message supprimé.".equals(message.getText())) {
             messageContent.setFont(Font.font(FONT_FAMILY, javafx.scene.text.FontPosture.ITALIC, 14));
             messageContent.setTextFill(Color.web(COLOR_TEXT_MUTED));
        }

        messageBox.getChildren().addAll(header, messageContent);

        // Bouton de suppression
        Button deleteButton = new Button("🗑");
        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ED4245; -fx-font-size: 14px; -fx-cursor: hand;");
        deleteButton.setVisible(false);
        
        deleteButton.setOnAction(e -> {
            if (mChatController != null) {
                mChatController.deleteMessage(message);
            }
        });

        this.setOnMouseEntered(e -> {
            if (isCurrentUser && !"Message supprimé.".equals(message.getText())) {
                deleteButton.setVisible(true);
                this.setStyle("-fx-background-color: #32353B;");
            }
        });

        this.setOnMouseExited(e -> {
            deleteButton.setVisible(false);
            this.setStyle("-fx-background-color: " + bgColor + ";");
        });

        this.getChildren().addAll(avatarPane, messageBox, deleteButton);
    }

    private void showInitials(StackPane avatarPane, Circle circle, User user) {
        circle.setFill(Color.web(COLOR_ACCENT));
        Label initial = new Label(user.getName().substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        avatarPane.getChildren().setAll(circle, initial);
    }
}

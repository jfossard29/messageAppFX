package com.message.ihm.views;

import com.message.datamodel.Message;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageView extends HBox {

    private final String COLOR_TEXT = "#DCDDDE";
    private final String COLOR_TEXT_MUTED = "#8E9297";
    private final String COLOR_BACKGROUND = "#36393F";
    private final String COLOR_BACKGROUND_SELF = "#40444B";
    private final String COLOR_ACCENT = "#5865F2";

    public MessageView(Message message, boolean isCurrentUser) {
        initUI(message, isCurrentUser);
    }

    public MessageView(Message message) {
        this(message, false);
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
        circle.setFill(Color.web(COLOR_ACCENT));

        Label initial = new Label(
                message.getSender().getName().substring(0, 1).toUpperCase()
        );
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        avatarPane.getChildren().addAll(circle, initial);

        // Conteneur message (droite)
        VBox messageBox = new VBox(3);
        messageBox.setAlignment(Pos.TOP_LEFT);

        // Header (nom + date)
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(message.getSender().getName());
        nameLabel.setTextFill(Color.web(COLOR_TEXT));
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(
                sdf.format(new Date(message.getEmissionDate()))
        );
        dateLabel.setTextFill(Color.web(COLOR_TEXT_MUTED));
        dateLabel.setFont(Font.font("Segoe UI", 10));

        header.getChildren().addAll(nameLabel, dateLabel);

        // Contenu du message
        Label messageContent = new Label(message.getText());
        messageContent.setWrapText(true);
        messageContent.setTextFill(Color.web(COLOR_TEXT));
        messageContent.setFont(Font.font("Segoe UI", 14));

        messageBox.getChildren().addAll(header, messageContent);

        this.getChildren().addAll(avatarPane, messageBox);
    }
}
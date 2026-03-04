package com.message.ihm.views;

import com.message.datamodel.Channel;
import com.message.datamodel.Message;
import com.message.datamodel.User;
import com.message.ihm.controllers.IChatController;
import com.message.ihm.controllers.ISessionController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatView extends BorderPane implements ISessionController.ISessionControllerObserver {

    private final ISessionController mSessionController;
    private final IChatController mChatController;

    private Label mChannelTitle;
    private VBox mMessagesBox;
    private ScrollPane mScrollPane;

    public ChatView(ISessionController sessionController, IChatController chatController) {
        this.mSessionController = sessionController;
        this.mChatController = chatController;
        this.mSessionController.addObserver(this);
        initGui();
    }

    private void initGui() {

        // Couleurs
        Color COLOR_MAIN = Color.rgb(54, 57, 63);
        Color COLOR_HEADER = Color.rgb(47, 49, 54);
        Color COLOR_TEXT = Color.rgb(220, 221, 222);

        this.setStyle("-fx-background-color: rgb(54,57,63);");

        /* ================= HEADER ================= */

        mChannelTitle = new Label("Sélectionnez un canal");
        mChannelTitle.setTextFill(COLOR_TEXT);
        mChannelTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        mChannelTitle.setPadding(new Insets(15));

        HBox header = new HBox(mChannelTitle);
        header.setStyle("-fx-background-color: rgb(47,49,54);");
        header.setAlignment(Pos.CENTER_LEFT);

        this.setTop(header);

        /* ================= MESSAGES ================= */

        mMessagesBox = new VBox(10);
        mMessagesBox.setPadding(new Insets(10));
        mMessagesBox.setFillWidth(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        mMessagesBox.getChildren().add(spacer);

        mScrollPane = new ScrollPane(mMessagesBox);
        mScrollPane.setFitToWidth(true);
        mScrollPane.setStyle("-fx-background: rgb(54,57,63);");

        this.setCenter(mScrollPane);

        /* ================= INPUT ================= */

        TextField inputField = new TextField();
        inputField.setPromptText("Écrire un message...");
        inputField.setStyle("""
                -fx-background-color: rgb(64,68,75);
                -fx-text-fill: rgb(220,221,222);
                -fx-prompt-text-fill: gray;
                """);

        inputField.setOnAction(e -> {
            String text = inputField.getText();
            if (!text.isEmpty()) {
                mChatController.sendMessage(text, mSessionController.getSelectedChannel());
                inputField.clear();
            }
        });

        HBox inputBox = new HBox(inputField);
        inputBox.setPadding(new Insets(15));
        HBox.setHgrow(inputField, Priority.ALWAYS);

        this.setBottom(inputBox);
    }

    @Override
    public void onChannelSelected(Channel channel) {

        mMessagesBox.getChildren().clear();

        if (channel == null) {
            mChannelTitle.setText("Sélectionnez un canal");
            return;
        }

        mChannelTitle.setText("# " + channel.getName());

        Set<Message> messagesSet = mChatController.getMessagesForChannel(channel);
        User currentUser = mSessionController.getCurrentUser();

        List<Message> sortedMessages = messagesSet.stream()
                .sorted(Comparator.comparingLong(Message::getEmissionDate))
                .collect(Collectors.toList());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        mMessagesBox.getChildren().add(spacer);

        for (Message msg : sortedMessages) {

            boolean isCurrentUser = currentUser != null &&
                    msg.getSender().getUserTag().equals(currentUser.getUserTag());

            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5));

            Label messageLabel = new Label(msg.getText());
            messageLabel.setWrapText(true);
            messageLabel.setStyle("""
                    -fx-background-color: rgb(64,68,75);
                    -fx-text-fill: white;
                    -fx-padding: 10;
                    -fx-background-radius: 8;
                    """);

            if (isCurrentUser) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
            } else {
                messageContainer.setAlignment(Pos.CENTER_LEFT);
            }

            messageContainer.getChildren().add(messageLabel);
            mMessagesBox.getChildren().add(messageContainer);
        }

        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> mScrollPane.setVvalue(1.0));
    }

    @Override
    public void onUsersUpdated() {
        // Géré ailleurs
    }
}
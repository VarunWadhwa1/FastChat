package com.client.chatwindow;

import com.client.login.MainLauncher;
import com.client.util.VoicePlayback;
import com.client.util.VoiceRecorder;
import com.client.util.VoiceUtil;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;
import com.messages.bubble.BubbleSpec;
import com.messages.bubble.BubbledLabel;
import com.traynotifications.animations.AnimationType;
import com.traynotifications.notification.TrayNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML private TextArea messageBox;
    @FXML private Label usernameLabel;
    @FXML private Label onlineCountLabel;
    @FXML private ListView<User> userList;
    @FXML private ImageView userImageView;
//    @FXML private Button recordBtn;
    @FXML ListView<HBox> chatPane;
//    @FXML ListView<HBox> statusList;
    @FXML BorderPane borderPane;
    @FXML ComboBox<String> statusComboBox;
    @FXML ImageView microphoneImageView;
    private double xOffset;
    private double yOffset;
    Logger logger = LoggerFactory.getLogger(ChatController.class);

    Image microphoneActiveImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/microphone-active.png")).toString());
    Image microphoneInactiveImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/microphone.png")).toString());

    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            Listener.send(msg);
            messageBox.clear();
        }
    }
    public void recordVoiceMessage() {
        if (VoiceUtil.isRecording()) {
            Platform.runLater(() -> microphoneImageView.setImage(microphoneInactiveImage));
            VoiceUtil.setRecording(false);
        } else {
            Platform.runLater(() -> microphoneImageView.setImage(microphoneActiveImage));
            VoiceRecorder.captureAudio();
        }
    }
    public synchronized void addToChat(Message msg) {
        Task<HBox> othersMessages = new Task<HBox>() {
            @Override
            public HBox call() {
                Image image = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/" + msg.getPicture().toLowerCase() + ".png")).toString());
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                BubbledLabel bl6 = new BubbledLabel();
                if (msg.getType() == MessageType.VOICE){
                    ImageView imageview = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/sound.png")).toString()));
                    bl6.setGraphic(imageview);
                    bl6.setText("Sent a voice message!");
                    VoicePlayback.playAudio(msg.getVoiceMsg());
                }else {
                    bl6.setText(msg.getName() + ": " + msg.getMsg());
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
                x.getChildren().addAll(profileImage, bl6);
                logger.debug("ONLINE USERS: " + msg.getUserlist().size());
                setOnlineLabel(Integer.toString(msg.getOnlineCount()));
                return x;
            }
        };

        othersMessages.setOnSucceeded(event -> chatPane.getItems().add(othersMessages.getValue()));

        Task<HBox> yourMessages = new Task<HBox>() {
            @Override
            public HBox call() {
                Image image = userImageView.getImage();
                ImageView profileImage = new ImageView(image);
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);

                BubbledLabel bl6 = new BubbledLabel();
                if (msg.getType() == MessageType.VOICE){
                    bl6.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/sound.png")).toString())));
                    bl6.setText("Sent a voice message!");
                    VoicePlayback.playAudio(msg.getVoiceMsg());
                }else {
                    bl6.setText(msg.getMsg());
                }
                bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, null, null)));
                HBox x = new HBox();
                x.setMaxWidth(chatPane.getWidth() - 20);
                x.setAlignment(Pos.TOP_RIGHT);
                bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
                x.getChildren().addAll(bl6, profileImage);

                setOnlineLabel(Integer.toString(msg.getOnlineCount()));
                return x;
            }
        };
        yourMessages.setOnSucceeded(event -> chatPane.getItems().add(yourMessages.getValue()));

        if (msg.getName().equals(usernameLabel.getText())) {
            Thread t2 = new Thread(yourMessages);
            t2.setDaemon(true);
            t2.start();
        } else {
            Thread t = new Thread(othersMessages);
            t.setDaemon(true);
            t.start();
        }
    }
    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    public void setImageLabel() throws IOException {
        this.userImageView.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/male.png")).toString()));
    }

    public void setOnlineLabel(String userCount) {
        Platform.runLater(() -> onlineCountLabel.setText(userCount));
    }

    public void setUserList(Message msg) {
        logger.info("setUserList() method Enter");
        Platform.runLater(() -> {
            ObservableList<User> users = FXCollections.observableList(msg.getUsers());
            userList.setItems(users);
            userList.setCellFactory(new CellRenderer());
            setOnlineLabel(String.valueOf(msg.getUserlist().size()));
        });
        logger.info("setUserList() method Exit");
    }
    public void newUserNotification(Message msg) {
        Platform.runLater(() -> {
            Image profileImg = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/" + msg.getPicture().toLowerCase() + ".png")).toString(),50,50,false,false);
            TrayNotification tray = new TrayNotification();
            tray.setTitle("A new user has joined!");
            tray.setMessage(msg.getName() + " has joined the JavaFX Chatroom!");
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.setImage(profileImg);
            tray.showAndDismiss(Duration.seconds(5));
            try {
                Media hit = new Media(Objects.requireNonNull(getClass().getClassLoader().getResource("sounds/notification.wav")).toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }

    @FXML
    public void closeApplication() {
        Platform.exit();
        System.exit(0);
    }

    /* Method to display server messages */
    public synchronized void addAsServer(Message msg) {
        Task<HBox> task = new Task<HBox>() {
            @Override
            public HBox call() {
                BubbledLabel bl6 = new BubbledLabel();
                bl6.setText(msg.getMsg());
                bl6.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE,
                        null, null)));
                HBox x = new HBox();
                bl6.setBubbleSpec(BubbleSpec.FACE_BOTTOM);
                x.setAlignment(Pos.CENTER);
                x.getChildren().addAll(bl6);
                return x;
            }
        };
        task.setOnSucceeded(event -> chatPane.getItems().add(task.getValue()));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setImageLabel();
        } catch (IOException e) {
            e.printStackTrace();
        }
                /* Drag and Drop */
        borderPane.setOnMousePressed(event -> {
            xOffset = MainLauncher.getPrimaryStage().getX() - event.getScreenX();
            yOffset = MainLauncher.getPrimaryStage().getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });

        borderPane.setOnMouseDragged(event -> {
            MainLauncher.getPrimaryStage().setX(event.getScreenX() + xOffset);
            MainLauncher.getPrimaryStage().setY(event.getScreenY() + yOffset);

        });

        borderPane.setOnMouseReleased(event -> borderPane.setCursor(Cursor.DEFAULT));

        statusComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Listener.sendStatusUpdate(Status.valueOf(newValue.toUpperCase()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        /* Added to prevent to enter from adding a new line to inputMessageBox */
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });

    }

    public void setImageLabel(String selectedPicture) {
        switch (selectedPicture) {
            case "Male":
                this.userImageView.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/male.png")).toString()));
                break;
            case "Female":
                this.userImageView.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/female.png")).toString()));
                break;
            case "Default":
                this.userImageView.setImage(new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/default.png")).toString()));
                break;
        }
    }

    public void logoutScene() {
        Platform.runLater(() -> {
            FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent window = null;
            try {
                window = fmxlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = MainLauncher.getPrimaryStage();
            Scene scene = null;
            if (window != null) {
                scene = new Scene(window);
            }
            stage.setMaxWidth(350);
            stage.setMaxHeight(420);
            stage.setResizable(true);
            stage.setScene(scene);
            stage.centerOnScreen();
        });
    }
}
//Chat Controller
//    @FXML ImageView microphoneImageView;
//Image microphoneActiveImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/microphone-active.png")).toString());
//    Image microphoneInactiveImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResource("images/microphone.png")).toString());
//public void recordVoiceMessage() {
//        if (VoiceUtil.isRecording()) {
//            Platform.runLater(() -> microphoneImageView.setImage(microphoneInactiveImage));
//            VoiceUtil.setRecording(false);
//        } else {
//            Platform.runLater(() -> microphoneImageView.setImage(microphoneActiveImage));
//            VoiceRecorder.captureAudio();
//        }
//    }
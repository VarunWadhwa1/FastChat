package com.client.login;

import com.client.chatwindow.ChatController;
import com.client.chatwindow.Listener;
import com.client.database.UserDAO;
import com.client.util.DBUtil;
import com.client.util.ResizeHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    public Button closeButton;
    public Button registerButton;
    @FXML public Label ServerText;
    @FXML private TextField usernameTextfield;
    @FXML private PasswordField passwordTextfield;
    public static ChatController con;
    @FXML private BorderPane borderPane;
    private double xOffset;
    private double yOffset;
    private Scene scene;
    private static LoginController instance;
    private String picture;

    public LoginController() {
        try {
            DBUtil.dbConnect();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        instance = this;
    }

    public static LoginController getInstance() {
        return instance;
    }

    public void registerButtonAction(){
        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
        try {
            Parent window = fmxlLoader.load();
            getInstance().showRegisterScene();
            this.scene = new Scene(window);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loginButtonAction() throws IOException {
//        192.168.169.109
        String hostname = "192.168.116.109";
        int port = 9001;
        String username = usernameTextfield.getText();
        String password = passwordTextfield.getText();
        picture = UserDAO.validate(username,password);
        if(!picture.equals("")){
            FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/ChatView.fxml"));
            Parent window = fmxlLoader.load();
            con = fmxlLoader.getController();
            Listener listener = new Listener(hostname, port, username, picture, con);
            Thread x = new Thread(listener);
            x.start();
            this.scene = new Scene(window);
        }else{
            picture = "default";
            infoBox("Please enter correct Email and Password", null, "Failed");
        }
    }
    public static void infoBox(String infoMessage, String headerText, String title){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(infoMessage);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }
    public void showScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) usernameTextfield.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(1040);
            stage.setHeight(620);

            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(800);
            stage.setMinHeight(300);
            ResizeHelper.addResizeListener(stage);
            stage.centerOnScreen();
            con.setUsernameLabel(usernameTextfield.getText());
            con.setImageLabel(picture);
        });
    }
    public void showRegisterScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(320);
            stage.setHeight(360);
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(320);
            stage.setMinHeight(220);
            ResizeHelper.addResizeListener(stage);
            stage.centerOnScreen();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        imagePicker.getSelectionModel().selectFirst();
//        selectedPicture.textProperty().bind(imagePicker.getSelectionModel().selectedItemProperty());
//        selectedPicture.setVisible(false);

        ServerText.setText("192.168.169.109 : 9001");

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

//        imagePicker.getSelectionModel().selectedItemProperty().addListener((selected, oldPicture, newPicture) -> {
//            if (oldPicture != null) {
//                switch (oldPicture) {
//                    case "Default":
//                        DefaultView.setVisible(false);
//                        break;
//                    case "Male":
//                        MaleView.setVisible(false);
//                        break;
//                    case "Female":
//                        FemaleView.setVisible(false);
//                        break;
//                }
//            }
//            if (newPicture != null) {
//                switch (newPicture) {
//                    case "Default":
//                        DefaultView.setVisible(true);
//                        break;
//                    case "Male":
//                        MaleView.setVisible(true);
//                        break;
//                    case "Female":
//                        FemaleView.setVisible(true);
//                        break;
//                }
//            }
//        });
        int numberOfSquares = 30;
        while (numberOfSquares > 0){
            generateAnimation();
            numberOfSquares--;
        }
    }

    public void generateAnimation(){
        Random rand = new Random();
        int sizeOfSquare = rand.nextInt(50) + 1;
        int speedOfSquare = rand.nextInt(10) + 5;
        int startXPoint = rand.nextInt(420);
        int startYPoint = rand.nextInt(350);
        int direction = rand.nextInt(5) + 1;

        KeyValue moveXAxis = null;
        KeyValue moveYAxis = null;
        Rectangle r1 = null;

        switch (direction){
            case 1 :
                // MOVE LEFT TO RIGHT
                r1 = new Rectangle(0,startYPoint,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSquare);
                break;
            case 2 :
                // MOVE TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSquare,sizeOfSquare);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSquare);
                break;
            case 3 :
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = new Rectangle(startXPoint,0,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 350 -  sizeOfSquare);
                moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSquare);
                break;
            case 4 :
                // MOVE BOTTOM TO TOP
                r1 = new Rectangle(startXPoint,420-sizeOfSquare ,sizeOfSquare,sizeOfSquare);
                moveYAxis = new KeyValue(r1.xProperty(), 0);
                break;
            case 5 :
                // MOVE RIGHT TO LEFT
                r1 = new Rectangle(420-sizeOfSquare,startYPoint,sizeOfSquare,sizeOfSquare);
                moveXAxis = new KeyValue(r1.xProperty(), 0);
                break;

            default:
                System.out.println("default");
        }

        r1.setFill(Color.web("#274472"));
        r1.setOpacity(0.1);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSquare * 1000), moveXAxis, moveYAxis);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        borderPane.getChildren().add(borderPane.getChildren().size()-1,r1);
    }

    public void closeSystem(){
        Platform.exit();
        System.exit(0);
    }
    public void minimizeWindow(){
        MainLauncher.getPrimaryStage().setIconified(true);
    }

    public void showErrorDialog(String message) {
        Platform.runLater(()-> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText(message);
            alert.setContentText("Please check for firewall issues and check if the server is running.");
            alert.showAndWait();
        });

    }
}

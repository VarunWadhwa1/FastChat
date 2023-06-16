package com.client.login;

import com.client.database.UserDAO;
import org.mindrot.jbcrypt.BCrypt;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import static com.client.login.LoginController.infoBox;

public class RegisterController implements Initializable {
    public Button closeButton;
    public Button loginButton;
    @FXML
    private ImageView DefaultView;
    @FXML private ImageView FemaleView;
    @FXML private ImageView MaleView;
    @FXML private TextField usernameTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private ChoiceBox<String> imagePicker;
    @FXML private Label selectedPicture;
    @FXML private BorderPane borderPane;
    private double xOffset;
    private double yOffset;
    private Scene scene;
    private static RegisterController instance;

    public RegisterController(){
        instance = this;
    }
    public static RegisterController getInstance() {
        return instance;
    }
    public void registerButtonAction(){
        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
        try {
            String username = usernameTextfield.getText();
            String password = passwordTextfield.getText();
            password = BCrypt.hashpw(password, BCrypt.gensalt());
            String picture = selectedPicture.getText();
            Parent window = fmxlLoader.load();
            getInstance().showLoginScene();
            if(UserDAO.insertUser(username,password,picture)){
                this.scene = new Scene(window);
            }else{
                infoBox("Some error occurred while Registering", null, "Failed");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void showLoginScene() {
        Platform.runLater(() -> {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setResizable(true);
            stage.setWidth(350);
            stage.setHeight(420);
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setMinWidth(350);
            stage.setMinHeight(420);
            ResizeHelper.addResizeListener(stage);
            stage.centerOnScreen();
        });
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imagePicker.getSelectionModel().selectFirst();
        selectedPicture.textProperty().bind(imagePicker.getSelectionModel().selectedItemProperty());
        selectedPicture.setVisible(false);

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

        imagePicker.getSelectionModel().selectedItemProperty().addListener((selected, oldPicture, newPicture) -> {
            if (oldPicture != null) {
                switch (oldPicture) {
                    case "Default":
                        DefaultView.setVisible(false);
                        break;
                    case "Male":
                        MaleView.setVisible(false);
                        break;
                    case "Female":
                        FemaleView.setVisible(false);
                        break;
                }
            }
            if (newPicture != null) {
                switch (newPicture) {
                    case "Default":
                        DefaultView.setVisible(true);
                        break;
                    case "Male":
                        MaleView.setVisible(true);
                        break;
                    case "Female":
                        FemaleView.setVisible(true);
                        break;
                }
            }
        });
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

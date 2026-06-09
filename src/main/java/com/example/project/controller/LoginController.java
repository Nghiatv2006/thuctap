package com.example.project.controller;

import com.example.project.UserSession;
import com.example.project.entity.User;
import com.example.project.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private final UserService userService;
    private final ApplicationContext applicationContext;

    @Autowired
    public LoginController(UserService userService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin đăng nhập.");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Thực hiện xác thực
        Optional<User> userOpt = userService.login(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("LOCKED".equals(user.getStatus())) {
                showError("Tài khoản của bạn đã bị khóa! Vui lòng liên hệ Admin.");
                loginButton.setDisable(false);
                return;
            }
            UserSession.setCurrentUser(user);
            try {
                // Đổi giao diện sang Dashboard chính
                switchSceneToDashboard(event);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Lỗi hệ thống khi chuyển cảnh: " + e.getMessage());
                loginButton.setDisable(false);
            }
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không chính xác.");
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void switchSceneToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1100, 750); // Màn hình chính rộng rãi
        stage.setScene(scene);
        stage.setTitle("Hệ thống Quản lý Kho hàng - Chào " + UserSession.getCurrentUser().getFullName());
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }
}

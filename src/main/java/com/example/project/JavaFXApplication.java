package com.example.project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Khởi tạo Spring Boot Context trong quá trình khởi tạo JavaFX
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(ProjectApplication.class)
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load giao diện FXML Đăng nhập trước
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        // Sử dụng Spring IoC Container để làm factory tạo Controller
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        primaryStage.setTitle("Đăng nhập - Hệ thống Quản lý Kho");
        primaryStage.setScene(new Scene(root, 480, 620));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Đóng Spring Context khi tắt ứng dụng Desktop
        this.springContext.close();
        Platform.exit();
    }
}

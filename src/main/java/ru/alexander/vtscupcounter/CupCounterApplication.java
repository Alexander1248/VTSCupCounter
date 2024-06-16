package ru.alexander.vtscupcounter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.alexander.api.VTubeStudioAPI;
import ru.alexander.api.listeners.ResponseListener;
import ru.alexander.api.responses.ErrorResponse;
import ru.alexander.api.responses.PermissionResponse;
import ru.alexander.calls.permissions.LoadCustomImagesAsItemsPermissionCall;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class CupCounterApplication extends Application {
    private final static String pluginName = "Cup Counter";
    private final static String developer = "Alexander 1248";
    private FileChooser chooser;
    private Stage stage;
    private VTubeStudioAPI api;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public void start(Stage stage) throws IOException {
        Class<CupCounterApplication> cl = CupCounterApplication.class;
        Locale.setDefault(Locale.US);
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource("app-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.getIcons().add(new Image(cl.getResourceAsStream("icon.png")));
        stage.setTitle(pluginName);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });

        AtomicReference<LoadCustomImagesAsItemsPermissionCall> call = new AtomicReference<>();
        call.set(new LoadCustomImagesAsItemsPermissionCall(new ResponseListener<>() {
            @Override
            public void onSuccess(PermissionResponse permissionResponse) {
                if (permissionResponse.getStatus() == PermissionResponse.PermissionStatus.Granted) {
                    System.out.println("Permission granted!");
                }
                else {
                    System.out.println("Permission denied!");
                    try {
                        Thread.sleep(1000);
                        System.exit(0);
                    } catch (InterruptedException ignored) {}
                }
            }

            @Override
            public void onFailure(ErrorResponse errorResponse) {
                System.err.println(errorResponse);
                api.call(call.get());
            }
        }));
        ResponseListener<LinkedHashMap<String, Object>> auth = new ResponseListener<>() {
            @Override
            public void onSuccess(LinkedHashMap<String, Object> map) {
                System.out.println("Successful auth!");
                api.call(call.get());
            }

            @Override
            public void onFailure(ErrorResponse errorResponse) {
                System.err.println(errorResponse);
                try {
                    Thread.sleep(1000);
                    System.exit(0);
                } catch (InterruptedException ignored) {}
            }
        };
        try {
            api = new VTubeStudioAPI();
        } catch(Exception e) {
            try {
                Thread.sleep(1000);
                System.exit(0);
            } catch (InterruptedException ignored) {}
        }
        File tokenFile = new File("token");
        if (tokenFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(tokenFile);
                api.auth(
                        pluginName,
                        developer,
                        new String(fis.readAllBytes()),
                        auth
                );
                fis.close();
            } catch (IOException e) {
                System.err.println("Token read error! Requesting...");
                requestToken(stage, tokenFile, auth);
            }

        }
        else requestToken(stage, tokenFile, auth);


        chooser = new FileChooser();
        chooser.setTitle("Open Image File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GIF", "*.gif"));

        AppController controller = fxmlLoader.getController();
        controller.application = this;
        File settingsFile = new File("settings");
        if (settingsFile.exists()) {
            FileReader reader = new FileReader(settingsFile);
            controller.settings = gson.fromJson(reader, Settings.class);
            reader.close();

            controller.filesList.clear();
            controller.filesList.addAll(controller.settings.files);
            controller.updateList();
        }
        else controller.settings = new Settings();
        controller.start();
    }

    private void requestToken(Stage stage, File tokenFile, ResponseListener<LinkedHashMap<String, Object>> auth) throws IOException {
        ResponseListener<LinkedHashMap<String, Object>> request = new ResponseListener<>() {
            @Override
            public void onSuccess(LinkedHashMap<String, Object> map) {
                System.out.println("Token requested!");
                String token = (String) map.get("authenticationToken");
                try {
                    FileOutputStream fos = new FileOutputStream(tokenFile);
                    fos.write(token.getBytes());
                    fos.close();
                } catch (IOException e) {
                    System.err.println(e.getLocalizedMessage());

                    try {
                        Thread.sleep(1000);
                        System.exit(0);
                    } catch (InterruptedException ignored) {}
                }
                api.auth(
                        pluginName,
                        developer,
                        token,
                        auth
                );
            }

            @Override
            public void onFailure(ErrorResponse errorResponse) {
                System.err.println(errorResponse);

                try {
                    Thread.sleep(1000);
                    System.exit(0);
                } catch (InterruptedException ignored) {}
            }
        };
        api.requestToken(
                pluginName,
                developer,
                CupCounterApplication.class.getResourceAsStream("icon.png"),
                request
        );
    }

    public File showOpenDialog() {
        return chooser.showOpenDialog(stage);
    }
    public List<File> showOpenMultipleDialog() {
        return chooser.showOpenMultipleDialog(stage);
    }
    public VTubeStudioAPI getAPI() {
        return api;
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) {
        launch();
    }
}
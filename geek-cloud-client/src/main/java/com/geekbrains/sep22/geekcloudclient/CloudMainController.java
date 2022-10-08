package com.geekbrains.sep22.geekcloudclient;

import com.geekbrains.model.FileRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



public class CloudMainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    private String currentDirectory;

    private DataInputStream dis;

    private DataOutputStream dos;

    private Socket socket;

    private static final String SEND_FILE_COMMAND = "file";


    public void sendToServer(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        String filePath = currentDirectory + "/" + fileName;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    dos.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                System.err.println("e = " + e.getMessage());
            }
        }
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ignored) {}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));
        clientView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
                }
            }
        });
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));
    }

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
        // file.txt 125 b
        // dir [DIR]
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
    }
    public void deleteFile(ActionEvent actionEvent) {
        if (clientView.isMouseTransparent()){
            try{
                Files.delete(Path.of(clientView.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                System.err.println("Error on delete file: " + e.getMessage());
            }
        }
        if (serverView.isMouseTransparent()){

        }
    }

    public void renameFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        if (clientView.isMouseTransparent()){
            try{
                Files.delete(Path.of(clientView.getSelectionModel().getSelectedItem()));
            } catch (IOException e) {
                System.err.println("Error on delete file: " + e.getMessage());
            }
        }
        if (serverView.isMouseTransparent()){

        }
    }

    private void renameLocalForm(File file) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("rename-form.fxml"));
        Parent parent = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));

        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        RenameFormController renameFormController = loader.getController();
        if(renameFormController.getModalResult()){
            String newFileName = renameFormController.getNewName();
            File newNameFile  = new File(newFileName);
            if (newNameFile.exists()){
                System.err.println("File with name " + newFileName + " is exist ");
            } else {
                boolean success = file.renameTo(newNameFile);
            }
        }
    }


}

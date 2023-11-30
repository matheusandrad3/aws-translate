package com.amazon.textract.service;


import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S3UploadDocumento extends Application {

    private Stage primaryStage;

    public S3UploadDocumento(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public String constructS3ObjectUrl(String bucketName, String keyName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
    }

//    public void displayFileChooser() {
//        if (primaryStage == null) {
//            throw new IllegalStateException("Primary stage is not initialized");
//        }
//        escolherCaminho(primaryStage);
//    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("S3 Upload Utility");
        escolherCaminho(primaryStage);
    }

    public String escolherCaminho(Stage primaryStage) {
        try {
            String filePath = selectFile(primaryStage);
            if (filePath == null) {
                //showAlert(Alert.AlertType.WARNING, "Warning", "Nenhum arquivo selecionado. Encerrando...");
                return null;
            }
      // showAlert(Alert.AlertType.INFORMATION, "Success", "Arquivo foi carregado com sucesso");
            return filePath;
        } catch (SdkException e) {
           // showAlert(Alert.AlertType.ERROR, "Error", "Houve um erro ao carregar o arquivo: " + e.getMessage());
        }
        return null;
    }

    private String selectFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um arquivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public void uploadFile(String filePath) {

        String bucketName = "ebook978345";

        String keyName = "uploads/" + Paths.get(filePath).getFileName().toString();

        Region region = Region.US_EAST_1;
        S3Client s3Client = S3Client.builder().region(region).build();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));
        String url = constructS3ObjectUrl(bucketName, keyName);
        saveUrlToDynamoDB(url);
    }

    public void saveUrlToDynamoDB(String url) {
        // Configure o cliente do DynamoDB
        DynamoDbClient dynamoDb = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
        String id = UUID.randomUUID().toString();
        // Construa o item que você deseja inserir
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("FileID", AttributeValue.builder().s(id).build());
        item.put("nome", AttributeValue.builder().s("ebook" + id).build());
        item.put("URL", AttributeValue.builder().s(url).build());

        // Crie a solicitação de inserção
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("pdf-traduzido")
                .item(item)
                .build();

        // Insira o item no DynamoDB
        dynamoDb.putItem(putItemRequest);
    }

//    private void showAlert(Alert.AlertType alertType, String title, String content) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setContentText(content);
//        alert.showAndWait();
//    }
}

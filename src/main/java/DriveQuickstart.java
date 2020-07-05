import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/2/drive-java-quickstart.json");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();


        // Print the names and IDs for up to 10 files.
        //readAllFiles(service);
		//create a folder
		//String folderID = createFolder("Invoices", service);

		//create file inside the folder
        //String fileID = createFileInsideFolder("photo.png", "src/main/resources/files/photo.png", "image/png",
          //     "1Q_uS8IIqSoR25oaFj2MpY8ygCSxNaj3c", service);
        //moveToNewFolder("1xzwVAh1okUZPq7_zZ_wbAHj-VwOLSOB7", folderID, service);
        downloadFile("1VPe11oyVPpsyvOtxmvKUie4z-0JJjAeB", service);
    }

    private static String createFolder(String folderName, Drive service ) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = service.files().create(fileMetadata)
                .setFields("id")
                .execute();
        System.out.println("Folder ID: " + file.getId());
        return file.getId();
    }

    private static void readAllFiles(Drive service) throws IOException {
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, properties)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s) - %s\n", file.getName(), file.getId(), file.toPrettyString());
            }
        }
    }

    private static String createFileInsideFolder(String fileName, String fileLocation, String fileType, String folderID, Drive service) throws IOException {
        String folderId = folderID;
        File fileMetadata_img = new File();
        fileMetadata_img.setName(fileName);
        Map<String, String> metadeta = new HashMap();
        metadeta.put("hash","hi");
        metadeta.put("hash2","hi there");

        fileMetadata_img.setAppProperties(metadeta);
        fileMetadata_img.setProperties(metadeta);

        fileMetadata_img.setParents(Collections.singletonList(folderId));
        java.io.File filePath = new java.io.File(fileLocation);
        FileContent mediaContent = new FileContent(fileType, filePath);
        File file_img = service.files().create(fileMetadata_img, mediaContent)
                .set("hash","hi")
                .setFields("id, appProperties, parents, properties")
                .execute();
        System.out.println(file_img.toPrettyString());
        return file_img.getId();

    }

    private static void moveToNewFolder(String fileID, String folderID, Drive service) throws IOException {

// Retrieve the existing parents to remove
        File file = service.files().get(fileID)
                .setFields("parents")
                .execute();
        StringBuilder previousParents = new StringBuilder();
        for (String parent : file.getParents()) {
            previousParents.append(parent);
            previousParents.append(',');
        }
// Move the file to the new folder
        file = service.files().update(fileID, null)
                .setAddParents(folderID)
                .setRemoveParents(previousParents.toString())
                .setFields("id, parents")
                .execute();
        System.out.println("moved file id :" + file.getId());
    }

    private static void downloadFile(String fileID, Drive service) throws IOException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String fileName = service.files().get(fileID).execute().getName();

        if (service.files().get(fileID).setFields("properties").execute().getProperties().get("hash").equals("hi")){
            service.files().get(fileID)
                    .executeMediaAndDownloadTo(outputStream);

            try(OutputStream outputStream1 = new FileOutputStream("src/main/resources/downloaded_img/"+fileName)) {
                ((ByteArrayOutputStream) outputStream).writeTo(outputStream1);
            }catch (Exception e){
                System.out.println(e);
            }
        }else {
            System.out.println("hash value not match");
        }


    }
}
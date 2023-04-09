package account;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AccountFile {

    public static final String FILE_PATH = "Clients";

    public static HashMap<String,Client> clients = new HashMap();

    public static void writeClient(Client client) throws IOException{
        clients.put(client.getUsername(),client);
        PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH + File.separator + "clients.txt",true));
        pw.println(client);
        pw.flush();
        pw.close();
    }
    public static void createDirectory(Client client){
        File directory = new File(FILE_PATH + File.separator + client.getUsername());
        if(!directory.exists())
            directory.mkdir();
    }
    public static void readClients(){
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH + File.separator + "clients.txt"));
            for(var line : lines) {
                String elements[] = line.split(" ");
                clients.put(elements[0], new Client(elements[0], elements[1], elements[2], elements[3], elements[4], elements[5], elements[6], elements[7]));
            }
        }catch (IOException ex){
            System.out.println("Client File error!");
        }
    }
    public static List<String> getDocuments(Client client){
        List<String> documents = new ArrayList<>();
        String clientPath = FILE_PATH + File.separator + client.getUsername();
        File clientFile = new File(clientPath);
        File docList[] = clientFile.listFiles();
        for(var el : docList)
            documents.add(el.getName());
        return documents;
    }
}

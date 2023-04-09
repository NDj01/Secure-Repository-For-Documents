package userInterface;

import account.AccountFile;
import account.Client;
import exceptions.DataCompromisedException;
import login.StartUp;
import repository.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class DesktopView {
    private static Scanner scanner = new Scanner(System.in);
    private static void clearScreen(){
        for(int i=0;i<100;i++)
            System.out.println();
    }
    public static void startUp(){
        AccountFile.readClients();
        String option = "";
        while(!"3".equals(option)){
            System.out.println();
            System.out.println("Register [1]");
            System.out.println("Login [2]");
            System.out.println("End [3]");
            System.out.println();
            System.out.print("Option: ");
            option = scanner.nextLine();

                //Thread.sleep(500);
            if ("1".equals(option)) {
                    //clearScreen();
                try {
                    StartUp.register();
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            } else if ("2".equals(option)) {
                    //clearScreen();
                try {
                    Client client = StartUp.login();
                    //Thread.sleep(2000);
                    //clearScreen();
                    if (client != null) {
                        repository(client);
                    }
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }

        }
    }
    private static void repository(Client client){
        System.out.println("--------------------------------------------------------------------");
        System.out.println("REPOSITORY");
        System.out.println("--------------------------------------------------------------------");
        String option = "";
        while(!"4".equals(option)){
            System.out.println();
            System.out.println("List files [1]");
            System.out.println("Upload file [2]");
            System.out.println("Download file [3]");
            System.out.println("Logout [4]");
            System.out.println();
            System.out.print("Option: ");
            option = scanner.nextLine();

                //Thread.sleep(500);

            if("1".equals(option)){
                    //clearScreen();
                listFiles(client);
            }else if("2".equals(option)){
                    //clearScreen();
                try {
                    uploadFile(client);
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }else if("3".equals(option)){
                    //clearScreen();
                try {
                    downloadFile(client);
                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }
            }

        }
    }
    private static void listFiles(Client client){
        System.out.println("My files:\n");
        int i=1;
        for(var file : client.getRepositoryFiles()){
            System.out.println(i++ + ". " + file);
        }
        System.out.println("\n");
    }
    private static void uploadFile(Client client) throws IOException{
        System.out.print("Enter file path: ");
        String filePath = scanner.nextLine();
        File file = new File(filePath);
        if(!file.exists())
            throw new IllegalArgumentException("Wrong Path!");
        System.out.print("Enter file name for repository: ");
        String fileName = scanner.nextLine();
        Repository.upload(filePath,client,fileName);
    }
    private static void downloadFile(Client client) throws IOException, DataCompromisedException {
        System.out.print("Enter file path for storage: ");
        String filePath = scanner.nextLine();
        System.out.print("Enter file name from repository: ");
        String fileName = scanner.nextLine();
        Repository.download(filePath,client,fileName);
    }
}

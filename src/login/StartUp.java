package login;

import account.AccountFile;
import account.Client;
import openssl.OpenSSLCommand;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class StartUp {

    public static final int DEFAULT_TIMES = 3;
    private static Scanner scanner = new Scanner(System.in);
    public static void register(){
        System.out.println("--------------------------------------------------------------------");
        System.out.println("REGISTER");
        System.out.println("--------------------------------------------------------------------");
        String username,password,countryName,stateOrProvinceName,localityName,organizationName,organizationalUnitName,emailAddress;
        while(true){
            System.out.println();
            System.out.print("username: ");
            username = scanner.nextLine();
            if(AccountFile.clients.containsKey(username)){
                System.out.println("Try different username!");
            }else
                break;
        }
        while(true) {
            System.out.print("password: ");
            password = scanner.nextLine();
            if (password.length() < 8) {
                System.out.println("Weak password!");
            } else
                break;
        }
        System.out.print("Country Name: ");
        countryName = scanner.nextLine();
        System.out.print("State or Province Name: ");
        stateOrProvinceName = scanner.nextLine();
        System.out.print("Locality Name: ");
        localityName = scanner.nextLine();
        System.out.print("Organization Name: ");
        organizationName = scanner.nextLine();
        System.out.print("Organizational Unit Name: ");
        organizationalUnitName = scanner.nextLine();
        System.out.print("E-mail Address: ");
        emailAddress = scanner.nextLine();
        String keyPath="";
        while(true){
            System.out.print("Key Path: ");
            keyPath = scanner.nextLine();
            File keyFile = new File(keyPath);
            if(!keyFile.exists())
                System.out.println("Invalid Key Path!");
            else
                break;
        }
        System.out.print("Where do you want to store certificate: ");
        String certificatePath = scanner.nextLine();
        Client client = new Client(username,OpenSSLCommand.hash(password),countryName,stateOrProvinceName,localityName,organizationName,organizationalUnitName,emailAddress);
        client.setCertificatePath(certificatePath);
        client.setKeyPath(keyPath);
        try {
            AccountFile.writeClient(client);
            AccountFile.createDirectory(client);
            OpenSSLCommand.createCertificate(client);
            AccountFile.clients.put(client.getUsername(),client);
        }catch (Exception ex){
            System.out.println("Invalid registration!");
            return;
        }
    }
    public static Client login(){
        System.out.println("--------------------------------------------------------------------");
        System.out.println("LOGIN");
        System.out.println("--------------------------------------------------------------------");
        System.out.print("Contribute your certificate: ");
        String certificatePath = scanner.nextLine();
        if(!OpenSSLCommand.validateCertificate(certificatePath) && !OpenSSLCommand.containedInCerts(certificatePath)){
            System.out.println("Invalid Certificate! Access denied!");
            return null;
        }
        if(OpenSSLCommand.revokedCert(certificatePath)){
            System.out.println("Revoked Certificate! Access denied!");
            return null;
        }
        int i = 1;
        while(i <= DEFAULT_TIMES){
            System.out.println();
            System.out.print("username: ");
            String username = scanner.nextLine();
            System.out.print("password: ");
            String password = scanner.nextLine();
            if(!OpenSSLCommand.checkCN(username,certificatePath)){
                System.out.println("Invalid Certificate for given username");
                i++;
                continue;
            }
            Client client = AccountFile.clients.get(username);
            if(client!=null){
                String passHash = OpenSSLCommand.hash(password);
                if(passHash.equals(client.getPassword())){ // successful
                    System.out.println("\nLogin successful!\n");
                    String keyPath = "";
                    while(true) {
                        System.out.print("Contribute your pair of keys: ");
                        keyPath = scanner.nextLine();
                        File keyFile = new File(keyPath);
                        if(!keyFile.exists())
                            System.out.println("Invalid Key Path!");
                        else
                            break;
                    }
                    client.setKeyPath(keyPath);
                    client.setCertificatePath(certificatePath);
                    client.setRepositoryFiles(AccountFile.getDocuments(client));
                    return client;
                }else {
                    System.out.println("\nInvalid password!");
                }
            }
            i++;
        }
        if(i==DEFAULT_TIMES+1){
            System.out.println("\nCertificate suspended!!!");
            OpenSSLCommand.suspendCert(certificatePath);
            String option = "";
            while (true){
                System.out.println();
                System.out.println("Reactivate Certificate[1]");
                System.out.println("Register[2]");
                System.out.print("\nOption: ");
                option = scanner.nextLine();
                if("1".equals(option)){
                    System.out.println();
                    System.out.print("username: ");
                    String username = scanner.nextLine();
                    System.out.print("password: ");
                    String password = scanner.nextLine();
                    if(!OpenSSLCommand.checkCN(username,certificatePath)){
                        System.out.println("Invalid Certificate for given username! Invalid Reactivation!");
                        return null;
                    }
                    Client client = AccountFile.clients.get(username);
                    if(client!=null) {
                        String passHash = OpenSSLCommand.hash(password);
                        if (passHash.equals(client.getPassword())) { // successful
                            try {
                                OpenSSLCommand.reactivateCert(certificatePath);
                                System.out.println("Reactivation successful!");
                            }catch (IOException ex){
                                System.out.println(ex.getMessage());
                            }
                            // only reactivates the certificate, does not log in
                        } else {
                            System.out.println("Invalid password! Invalid Reactivation!");
                        }
                    }
                    break;
                }else if("2".equals(option)){
                    register();
                    break;
                }
            }
        }
        return null;
    }
}

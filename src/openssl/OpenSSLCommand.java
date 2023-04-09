package openssl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import account.Client;
import exceptions.MyLogger;

public class OpenSSLCommand {

    private static String LOG_PATH = "logFile.txt";
    private static MyLogger logger = new MyLogger(LOG_PATH);
    public static final String CA_PATH = "CA";
    public static final String CERTS_PATH = "certs";
    public static final String REQ_PATH = "requests";
    public static final String CONFIG = "openssl.cnf";
    public static final String DEFAULT_KEY = "sigurnost";
    public static final String SALT = "Hmc9Ve8sjXPjFJSW";

    public static final String CA_CERT = "rootca.pem";
    public static final String CRL_PATH = "crl";
    public static final String CRL_LIST = "cRLlist.crl";
    public static final String INDEX = "index.txt";

    public static final String ALGORITHM = "-aes-256-ofb";
    public static final String HASH_FUNCTION= "-sha384";

    public static String execute(String command, File workingDir){
        StringBuilder builder = new StringBuilder();
        var processBuilder = new ProcessBuilder(command.split(" "));
        if (workingDir != null)
            processBuilder.directory(workingDir);
        try {
            var process = processBuilder.start();
            process.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
            }
            BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = err.readLine()) != null) {
                //System.out.println(line);
                logger.logger.log(Level.WARNING,line);
            }
        } catch (InterruptedException | IOException ex) {
            System.out.println(ex.getMessage());
        }
        return builder.toString();
    }
    public static String hash(String password){
        String result = execute( "openssl passwd -5 -salt " + SALT + " " + password, null); // not necessary to be positioned  in some folder
        return result;
    }
    public static void createCertificate(Client client) throws IOException{
        // navodnike stavljati kod putanja ili komandi koje imaju razmake
        String request = REQ_PATH + File.separator + client.getUsername() + ".csr";
        String certificate = CERTS_PATH + File.separator + client.getUsername() + ".crt";
        String certReqCommand ="openssl req -new -key \"" + client.getKeyPath() + "\" -out " + request + " -config " + CONFIG
                + " -subj \"/C=" + client.getCountryName() + "/ST=" + client.getStateOrProvinceName() + "/O=" + client.getOrganizationName()
                + "/OU=" + client.getOrganizationalUnitName() + "/CN=" + client.getUsername() + "/emailAddress=" + client.getEmailAddress() + "\"";
        String certSignCommand = "openssl ca -in " + request + " -out " + certificate + " -config " + CONFIG +" -passin pass:" + DEFAULT_KEY + " -batch";
        execute(certReqCommand,new File(CA_PATH));
        execute(certSignCommand,new File(CA_PATH));
        Files.copy(Paths.get(CA_PATH,certificate), Paths.get(client.getCertificatePath(),client.getUsername() + ".crt"));
    }
    public static boolean validateCertificate(String certificatePath){
        String validateCommand = "openssl verify -CAfile " + CA_CERT + " -verbose " + certificatePath;
        String result = execute(validateCommand,new File(CA_PATH));
        if(result.contains("OK"))
            return true;
        else return false;
    }
    public static boolean containedInCerts(String certificatePath){
        File certsFile = new File(CA_PATH + File.separator + CERTS_PATH);
        File files[] = certsFile.listFiles();
        String certContext = execute("openssl x509 -in " + certificatePath + " -noout -text",null);
        int status = 0;
        for(var el : files){
            String elPath = CERTS_PATH + File.separator + el.getName();
            String elContext = execute("openssl x509 -in " + elPath + " -noout -text",new File(CA_PATH));
            if(certContext.equals(elContext))
                status = 1;
        }
        return status==1;
    }
    public static boolean revokedCert(String certificatePath){
        String crlPath = CRL_PATH + File.separator + CRL_LIST;
        String certificateSerialNumber = execute("openssl x509 -in " + certificatePath + " -noout -serial",null);
        String crlContex = execute("openssl crl -in " + crlPath + " -noout -text",new File(CA_PATH));
        String elements[] = certificateSerialNumber.split("=");
        if(crlContex.contains("Serial Number: " + elements[1]))
            return true;
        else return false;
    }
    public static boolean checkCN(String username, String certificatePath){
        String certContext = execute("openssl x509 -in " + certificatePath + " -noout -text",null);
        if(certContext.contains("CN = " + username + ","))
            return true;
        else return false;
    }
    public static void suspendCert(String certificatePath){
        execute("openssl ca -revoke " + certificatePath + " -crl_reason certificateHold -config openssl.cnf -passin pass:" + DEFAULT_KEY,new File(CA_PATH));
        genCRL();
    }
    public static void reactivateCert(String certificatePath) throws IOException{
        File certFile = new File(certificatePath);
        String certName = certFile.getName();
        certName = certName.substring(0,certName.length()-4);
        List<String> indexLines = Files.readAllLines(Paths.get(CA_PATH,INDEX));
        PrintWriter pw = new PrintWriter(new FileWriter(Paths.get(CA_PATH,INDEX).toString())); // we overwrite the contents of the index file
        for (var line : indexLines) {
            String indexElements[] = line.split("\t");
            if(indexElements[0].equals("R")){
                if(indexElements[5].contains("CN="+certName)){
                    pw.println("V" + "\t" + indexElements[1] + "\t\t" + indexElements[3] + "\t" + indexElements[4] + "\t" + indexElements[5]);
                }
            }else
                pw.println(line);
        }
        pw.close();
        genCRL();
    }
    public static void genCRL(){
        String crlPath = CRL_PATH + File.separator + CRL_LIST;
        execute("openssl ca -gencrl -out " + crlPath + " -config " + CONFIG + " -passin pass:" + DEFAULT_KEY,new File(CA_PATH));
    }
    public static void encrypt(String inputPath, String outputPath, String key){
        execute("openssl enc -" + ALGORITHM + " -in " + inputPath + " -out " + outputPath + " -k " + key,null);
    }
    public static void decrypt(String inputPath, String outputPath, String key){
        execute("openssl enc -" + ALGORITHM + " -d -in " + inputPath + " -out " + outputPath + " -k " + key,null);
    }
    public static void digitalSignature(String inputFile, String signature, String keyPath){
        execute("openssl dgst -" + HASH_FUNCTION + " -sign " + keyPath + " -out " + signature + " " + inputFile,null);
    }
    public static boolean verifyDigitalSignature(String inputFile, String signature, String keyPath){
        String result = execute("openssl dgst -" + HASH_FUNCTION + " -prverify " + keyPath + " -signature " + signature + " " + inputFile,null);
        if(result.contains("OK"))
            return true;
        else return false;
    }
    public static void encryptRSA(String inputFile,String outputFile, String keyPath){
        execute("openssl rsautl -encrypt -in " + inputFile + " -out " + outputFile + " -inkey " + keyPath,null);
    }
    public static void decryptRSA(String inputFile,String outputFile, String keyPath){
        execute("openssl rsautl -decrypt -in " + inputFile + " -inkey " + keyPath + " -out " + outputFile,null);
    }
}

package repository;

import account.AccountFile;
import account.Client;
import exceptions.DataCompromisedException;
import openssl.OpenSSLCommand;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class Repository {

    private static final int SEGMENT_SIZE = 10;
    private static final int MIN_SEGMENT_SIZE = 4;
    private static final String SEGMENT_NAME = "segment.pln";
    private static final String SEGMENT_NAME_DEC = "segment.dec";
    private static final String DIGITAL_SIGNATURE = "digSign.dgt";
    private static final String DOCUMENT = "document.txt";
    private static final String DOCUMENT_ENC = "document.enc";
    private static final String KEYDOC_ENC = "key.enc";
    private static final String KEYDOC = "key.txt";
    private static Random random = new Random();

    public static void upload(String filePath, Client client, String fileName) throws IOException {
        File fileDirectory = new File(AccountFile.FILE_PATH + File.separator + client.getUsername() + File.separator + fileName);
        if(!fileDirectory.exists())
            fileDirectory.mkdir();
        else
            throw new IllegalArgumentException("File already exists!");
        // generisanje broja segmenata
        File document = new File(fileDirectory.toString() + File.separator + DOCUMENT);
        PrintWriter pw = new PrintWriter(new FileWriter(document));

        int segmentNumber = random.nextInt(SEGMENT_SIZE - MIN_SEGMENT_SIZE - 1) + MIN_SEGMENT_SIZE;
        long fileLength = new File(filePath).length();
        int segmentSize = (int) Math.ceil(1.0 * fileLength / segmentNumber);
        FileInputStream input = new FileInputStream(filePath);
        byte buffer[] = new byte[segmentSize];
        int count = 0;
        int read = 0;
        while ((read = input.read(buffer)) != -1) {
            count++;
            String segmentName = generateKey(15);
            String segmentDirPath = fileDirectory.toString() + File.separator + segmentName;
            File segmentDirFile = new File(segmentDirPath);
            segmentDirFile.mkdir();
            File segmentPath = new File(segmentDirPath + File.separator + SEGMENT_NAME);

            FileOutputStream output = new FileOutputStream(segmentPath);
            output.write(buffer,0,read);
            output.close();
            String key = generateKey(10);
            OpenSSLCommand.encrypt(segmentPath.toString(), segmentDirPath + File.separator + SEGMENT_NAME_DEC, key);
            OpenSSLCommand.digitalSignature(segmentPath.toString(),segmentDirPath + File.separator + DIGITAL_SIGNATURE, client.getKeyPath());

            pw.println(segmentName + " " + key); // we document which segment is encrypted with which key
            segmentPath.delete();

            if(count == segmentNumber)
                break;
        }
        input.close();
        pw.close();
        String key = generateKey(15);
        OpenSSLCommand.encrypt(document.toString(), fileDirectory.toString() + File.separator + DOCUMENT_ENC,key);
        document.delete();
        File keyFile = new File(fileDirectory.toString() + File.separator + KEYDOC);
        PrintWriter keyFileInput = new PrintWriter(keyFile);
        keyFileInput.println(key);
        keyFileInput.close();
        OpenSSLCommand.encryptRSA(keyFile.toString(),fileDirectory.toString() + File.separator + KEYDOC_ENC,client.getKeyPath());
        keyFile.delete();
        client.getRepositoryFiles().add(fileName);
    }
    private static String generateKey(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public static void download(String fileStoragePath,Client client, String fileName) throws IOException,DataCompromisedException{
        File fileDirectory = new File(AccountFile.FILE_PATH + File.separator + client.getUsername() + File.separator + fileName);
        Path keyFilePath = Paths.get(fileDirectory.toString(),KEYDOC_ENC);
        // uzimanje kljuca
        File keyDocFile = new File(fileDirectory.toString() + File.separator + KEYDOC);
        OpenSSLCommand.decryptRSA(keyFilePath.toString(),keyDocFile.toString(),client.getKeyPath());
        List<String> keyLines = Files.readAllLines(Paths.get(keyDocFile.toString()));
        keyDocFile.delete();

        // dekripcija fajla sa podacima o segmentu
        File docFile = new File(fileDirectory.toString() + File.separator + DOCUMENT);
        OpenSSLCommand.decrypt(fileDirectory.toString() + File.separator + DOCUMENT_ENC, docFile.toString(),keyLines.get(0));
        List<String> docLines = Files.readAllLines(Paths.get(docFile.toString()));
        docFile.delete();
        // kroz segmente
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(fileStoragePath));

        for(var line : docLines){
            String elements[] = line.split(" ");
            Path segmentPath = Paths.get(fileDirectory + File.separator +  elements[0] + File.separator + SEGMENT_NAME_DEC);
            String key = elements[1];
            File segmentTextFile = new File(fileDirectory + File.separator +  elements[0] + File.separator + SEGMENT_NAME);
            Path digSignPath = Paths.get(fileDirectory + File.separator + elements[0] + File.separator + DIGITAL_SIGNATURE);
            OpenSSLCommand.decrypt(segmentPath.toString(),segmentTextFile.toString(),key);

            if(!OpenSSLCommand.verifyDigitalSignature(segmentTextFile.toString(),digSignPath.toString(),client.getKeyPath())) {
                segmentTextFile.delete();
                throw new DataCompromisedException("Data have been compromised!");
            }

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(segmentTextFile));
            int b;
            while ((b = input.read()) != -1) {
                output.write(b);
            }
            input.close();
            segmentTextFile.delete();
        }
        output.close();
    }
}

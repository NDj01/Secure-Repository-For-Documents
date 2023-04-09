package exceptions;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
    public Logger logger;
    private FileHandler fileHandler;

    public MyLogger(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();

            fileHandler = new FileHandler(fileName, true);
            logger = Logger.getLogger("log");
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.setUseParentHandlers(false);
        }catch (IOException | SecurityException ex){
            System.out.println(ex.getMessage());
        }
    }
}

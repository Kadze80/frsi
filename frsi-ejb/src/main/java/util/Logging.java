package util;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.*;

public class Logging implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Logger LOGGER = Logger.getLogger("kz.bsbnb.FRSI_EJB");

    public static Handler fileHandler;
    public static Formatter simpleFormatter;

    static {
        try {
            simpleFormatter = new SimpleFormatter();
            fileHandler = new FileHandler("../logs/frsi-ejb.log");
            fileHandler.setFormatter(simpleFormatter);
            fileHandler.setLevel(Level.ALL);

            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

            LOGGER.info("LOGGER initialized");

        } catch (IOException e) {
            LOGGER.severe("LOGGER not initialized");
            e.printStackTrace();
        }
    }

    public Logging() {
    }
}

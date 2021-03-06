/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.status.StatusLogger;
import org.semux.config.Config;
import org.semux.config.Constants;
import org.semux.util.SystemUtil;
import org.xml.sax.SAXException;

/**
 * The configurator will try to load log4j2.xml from config directory at
 * runtime. If log4j2.xml doesn't exist in user's config directory, the factory
 * default of src/main/resources/log4j2.xml will be used.
 */
public class LoggerConfigurator {

    public static final String CONFIG_XML = "log4j2.xml";
    public static final String DEBUG_LOG = "debug.log";

    private static FileHandler logHandler;

    private LoggerConfigurator() {
    }

    public static void configure(Config config) {

//        Logger rootLogger = Logger.getLogger("");
//        try {
//        logHandler=new FileHandler("C:\\zzv\\zzv-fxml\\log\\log.txt",true);
//        logHandler.setFormatter(new SimpleFormatter());
//        logHandler.setLevel(Level.FINE);
//        rootLogger.removeHandler(rootLogger.getHandlers()[0]);
//        rootLogger.setLevel(Level.FINE);
//        rootLogger.addHandler(logHandler);
//        }
//        catch (  SecurityException e) {
//            System.err.println("Security exception while initialising logger : " + e.getMessage());
//        }
//        catch (  IOException e) {
//            System.err.println("IO exception while initialising logger : " + e.getMessage());
//        }

        File file = new File(config.configDir(), CONFIG_XML);

        if (file.exists()) {
            File logFile = new File(config.logDir(), DEBUG_LOG);
            System.out.println("*************************************************");
            System.out.println("log.file - " + logFile.getAbsolutePath());
            System.setProperty("log.file",logFile.getAbsolutePath());
            Logger.getGlobal().setLevel(Level.ALL);
            System.out.println("*************************************************");            // register configuration error listener
        }
    }

//    /**
//     * Error listener to configuration error. The listener exits the process when it
//     * receives a configuration error from
//     * {@link org.apache.logging.log4j.core.config.xml.XmlConfiguration}.
//     */
//    private static class ConfigurationErrorStatusListener implements StatusListener {
//
//        @Override
//        public void log(StatusData data) {
//            Throwable throwable = data.getThrowable();
//            if (throwable instanceof SAXException
//                    || throwable instanceof IOException
//                    || throwable instanceof ParserConfigurationException) {
//                SystemUtil.exit(SystemUtil.Code.FAILED_TO_LOAD_CONFIG);
//            }
//        }
//
//        @Override
//        public Level getStatusLevel() {
//            return Level.ERROR;
//        }
//
//        @Override
//        public void close() throws IOException {
//            // do nothing
//        }
//    }
}

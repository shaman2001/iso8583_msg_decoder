package com.epam.isomsg_decoder.helper;

import com.epam.isomsg_decoder.ISOMsgWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jpos.iso.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Helper {

    private static final Logger LOG = LogManager.getLogger(Helper.class);

    public static void logISOMsg(ISOMsg msg, Map<Integer, String> pickedupPkgrs, Logger log) {
        log.debug("Start logging the message...");
        log.debug("BEGIN MESSAGE-----------------------------------");

        if (isRequest(msg)) {
            log.debug("REQUEST MESSAGE");
        } else {
            log.debug("RESPONSE MESSAGE");
        }
        log.debug("MTI: " + msg.getString(0));
        int maxField = msg.getMaxField();
        for(int i=1; i<=maxField; i++) {
            if (msg.hasField(i)) {
                log.debug("Field - " + i + ": " + msg.getString(i) + "\t\t\t\t packager: " + pickedupPkgrs.get(i));
            }
        }
        log.debug("END MESSAGE-----------------------------------");
        log.debug("Finish logging the message...");
    }

    public static boolean isRequest(ISOMsg msg) {
        return Character.getNumericValue(msg.getString(0).charAt (2))%2 == 0;
    }

    public static List<ISOMsgWrapper> parseSourceFile(String path) {
        List<ISOMsgWrapper> result = new ArrayList<>();
        ISOMsgWrapper msgWrapper = null;
        String line;
        try (BufferedReader buff = new BufferedReader (new FileReader(new File(path)))) {
            while ((line = buff.readLine()) != null) {
                //if line is empty
                if (line.matches("[\\s\\t]*")) {

                    if (msgWrapper!=null) {
                        result.add(msgWrapper);
                        msgWrapper = null;
                        continue;
                    } else {
                        continue;
                    }
                }
                if (msgWrapper==null) {
                    msgWrapper = new ISOMsgWrapper();
                }
                //if line is kind of "H2H 0800 TransactionSwitch"
                if (line.matches("^[A-Za-z0-9]{3,}\\s+\\d{4}\\s+\\w+")) {
                    String[] splittedTitle = line.split("\\s+");
                    msgWrapper.setProtocol(splittedTitle[0]);
                    msgWrapper.setKnownMTI(splittedTitle[1]);
                    msgWrapper.setSourceSystem(splittedTitle[2]);
                    //if line contain hex message body
                } else if (line.matches("([A-Z0-9]{1,4}\\s{0,2}){30,}")) {
                    line = line.replaceAll("\\s+", "");
                    msgWrapper.setMsgHexString(line);
                    //if line contain fieldNum:value pair
                } else if (line.matches("^\\s*[A-Za-z0-9.#]{3,15}:[\\w .]*")) {
                    String[] splittedTitle = line.split(":\\s{0,5}");
                    msgWrapper.addMsgDataEntry(splittedTitle[0].trim(),
                                                    splittedTitle[1].trim());
                }
            }
            result.add(msgWrapper);
        } catch (FileNotFoundException e) {
            LOG.error("Can't find source file on path: " + path);
        } catch (IOException e) {
            LOG.error("Can't read source file on path: " + path);
        }
        return result;
    }






}

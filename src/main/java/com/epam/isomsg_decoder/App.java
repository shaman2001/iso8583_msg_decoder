package com.epam.isomsg_decoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.epam.isomsg_decoder.helper.Helper.logISOMsg;
import static com.epam.isomsg_decoder.helper.Helper.parseSourceFile;

public class App {

    private static final Logger LOG = LogManager.getLogger(App.class);
    private static final String PACKAGER_WAY4_H2H_XML = "data/way4_h2h.xml";
    private static final String PACKAGER_WAY4_JCB_XML = "data/way4_jcb.xml";
    private static final String MSG_DATA_FILE_PATH = "data/message_samples.txt";
    public static List<ISOMsgWrapper> wrapMsgs;

    public static void main(String[] args) {
        wrapMsgs = parseSourceFile(MSG_DATA_FILE_PATH);
        /*try {
            String name = ISOMsg.class.getSimpleName() + ".class";
            URI u = ISOMsg.class.getResource(name).toURI();
            //URI uri = "jar:packager/iso87ascii.xml";
            FileSystem f;
            String s;
            if ("jar".equalsIgnoreCase(u.getScheme())) {
                f = FileSystems.newFileSystem(u, Collections.emptyMap());
                s = "/";
            } else {
                //Класс не в jar значит берем ФС системы пишем в префикс путь к папке в которую IDE кладет .class
                f = Paths.get(u).getFileSystem();
                s = u.getPath().substring(3).substring(0, u.getPath().lastIndexOf("MyClassDir") - 3);
            }
            Files.list(f.getPath(s + "org/jpos/iso")).forEach(LOG::info);//список фалов директории MyDir
            f.close();
        } catch (IOException | URISyntaxException e) {
            LOG.error(e.getMessage());
        }*/
        ISOMsgWrapper messageWrToDecode = wrapMsgs.get(1);
        Map<Integer, String> packagersMap = new LinkedHashMap<>();
        try {
            GenericPackagerMod decoder = new GenericPackagerMod(PACKAGER_WAY4_H2H_XML);
            ISOMsg h2hMessage = new ISOMsg();
            byte[] h2h_bytes = ISOUtil.hex2byte(messageWrToDecode.getMsgHexString());
            decoder.unpack(h2hMessage, h2h_bytes);
            //decoder.decode(h2hMessage, h2h_bytes, messageWrToDecode.getMsgData(), packagersMap);
            logISOMsg(h2hMessage, packagersMap, LOG);
        } catch (ISOException e) {
            LOG.error("Message decoding error. " + e.getMessage() + e.getStackTrace());
        }


    }


    public static void testH2HPackager() {
        String test800_H2H = "303830308220000000000000040000000000000006091210540266570301";
        String test800_JCB =  "F0F8F0F0822000008000000004000000100000000921180513002323088871500000010888791000";
        String test0120_H2H = "30313230623C44818AE2A000164321140046707251920000060812595901734904595706081908601109010208887910000888715000373135393031303137333439303053314154494243523030303030303053314154494243522020202020202020202020202020202020202020202020202020202020202020202020202020202000223930343031363530333032394546413042334335433407840784";
        ISOMsg h2hMessage = new ISOMsg();
        ISOMsg jcbMessage = new ISOMsg();
        try {
            ISOPackager h2hPackager = new GenericPackager(PACKAGER_WAY4_H2H_XML);
            ISOPackager jcbPackager = new GenericPackager(PACKAGER_WAY4_JCB_XML);
            h2hMessage.setPackager(h2hPackager);
            jcbMessage.setPackager(jcbPackager);
            byte[] h2h_bytes = ISOUtil.hex2byte(test0120_H2H);
            byte[] jcb_bytes = ISOUtil.hex2byte(test800_JCB);
            h2hMessage.unpack(h2h_bytes);
            jcbMessage.unpack(jcb_bytes);
        } catch (ISOException e) {
            LOG.error("Message packing/unpacking error" + e.getMessage());
        }
        //logISOMsg(h2hMessage, LOG);
        //logISOMsg(jcbMessage, LOG);
        System.exit(0);

    }




}

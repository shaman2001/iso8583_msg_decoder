package com.epam.isomsg_decoder;
import org.jpos.iso.ISOMsg;

import java.util.LinkedHashMap;
import java.util.Map;

public class ISOMsgWrapper {

    private String protocol;
    private String knownMTI;
    private String sourceSystem;

    private String msgHexString;

    private Map<String, String> msgData = new LinkedHashMap<>();

    private ISOMsg decodedMsg;

    public ISOMsgWrapper( ) { }

    public ISOMsgWrapper(String protocol, String knownMTI, String sourceSystem) {
        this.protocol = protocol;
        this.knownMTI = knownMTI;
        this.sourceSystem = sourceSystem;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getKnownMTI() {
        return knownMTI;
    }

    public void setKnownMTI(String knownMTI) {
        this.knownMTI = knownMTI;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getMsgHexString() {
        return msgHexString;
    }

    public void setMsgHexString(String msgHexString) {
        this.msgHexString = msgHexString;
    }

    public ISOMsg getDecodedMsg() {
        return decodedMsg;
    }

    public void setDecodedMsg(ISOMsg decodedMsg) {
        this.decodedMsg = decodedMsg;
    }

    public Map<String, String> getMsgData() {
        return msgData;
    }

    public void setMsgData(Map<String, String> msgData) {
        this.msgData = msgData;
    }

    public void addMsgDataEntry(String key, String value) {
        this.msgData.put(key, value);
    }


}

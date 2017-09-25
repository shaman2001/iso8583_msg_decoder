package com.epam.isomsg_decoder;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.packager.GenericPackager;

import java.io.InputStream;

public class ISOFieldPackagerCollector extends GenericPackager {
    public ISOFieldPackagerCollector() throws ISOException {
    }

    public ISOFieldPackagerCollector(String filename) throws ISOException {
        super(filename);
    }

    public ISOFieldPackagerCollector(InputStream input) throws ISOException {
        super(input);
    }

    public ISOFieldPackager[] getFieldPackagers() {
        return this.fld;
    }


}

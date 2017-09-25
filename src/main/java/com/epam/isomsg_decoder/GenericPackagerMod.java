package com.epam.isomsg_decoder;

import com.epam.isomsg_decoder.helper.Helper;
import org.apache.logging.log4j.LogManager;
import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.InputStream;
import java.util.BitSet;
import java.util.Map;

public class GenericPackagerMod extends GenericPackager {

    private ISOFieldPackagerCollector pkgrCollector;
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(GenericPackagerMod.class);


    public GenericPackagerMod() throws ISOException {
        super();
        pkgrCollector = new ISOFieldPackagerCollector("data/all_component_packager_list.xml");
    }

    public GenericPackagerMod(String filename) throws ISOException {
        super(filename);
        pkgrCollector = new ISOFieldPackagerCollector("data/all_component_packager_list.xml");
    }

    public GenericPackagerMod(InputStream input) throws ISOException {
        super(input);
        pkgrCollector = new ISOFieldPackagerCollector("data/all_component_packager_list.xml");
    }


    public int decode (ISOComponent m, byte[] b, Map<String, String> dataToCompare,
                                        Map<Integer, String> resultPkgrMap) throws ISOException {
        LogEvent evt = logger != null ? new LogEvent (this, "unpack") : null;
        LOG.info("Start decode procedure");
        int consumed = 0;

        try {
            if (m.getComposite() != m)
                throw new ISOException ("Can't call packager on non Composite");
            if (evt != null)  // save a few CPU cycle if no logger available
                evt.addMessage (ISOUtil.hexString (b));


            // if ISOMsg and headerLength defined
            if (m instanceof ISOMsg && headerLength>0)
            {
                byte[] h = new byte[headerLength];
                System.arraycopy(b, 0, h, 0, headerLength);
                ((ISOMsg) m).setHeader(h);
                consumed += headerLength;
            }
            LOG.info("Try to decode MTI bytes");
            if (!(fld[0] == null) && !(fld[0] instanceof ISOBitMapPackager)) {
                int tmpConsumed = 0;
                String valueToCompare = null;
                String decodedMTIValue = null;
                String packagerSimpleName = null;
                ISOComponent mti = null;
                boolean isMTIDecoded = false;
                int j = 0;
                for (ISOFieldPackager pkgr: pkgrCollector.getFieldPackagers()) {
                    LOG.info("Packager №" + j);
                    if (pkgr!=null) {
                        try {
                            packagerSimpleName = pkgr.getClass().getSimpleName();
                            LOG.info("Use packager: " + packagerSimpleName);
                            pkgr.setLength(fld[0].getLength());
                            mti = pkgr.createComponent(0);
                            tmpConsumed = pkgr.unpack(mti, b, consumed);
                            decodedMTIValue = (String) mti.getValue();
                            LOG.info("Decoded value: " + decodedMTIValue);
                            valueToCompare = dataToCompare.get("Type");
                            LOG.info("Expected value: " + valueToCompare);
                            if (valueToCompare.equals(decodedMTIValue)) {
                                LOG.info("Decoded value is VALID");
                                isMTIDecoded = true;
                                break;
                            } else if (decodedMTIValue.matches("^[01][1-8][0-4]0$")) {
                                LOG.info("Decoded value is likely INVALID!!!");
                                isMTIDecoded = true;
                                break;
                            } else {
                                LOG.info("Decoded value is INVALID!!! Continue decoding MTI");
                            }
                        } catch (IllegalArgumentException e) {
                                LOG.warn("Runtime exception rose during MTI decoding. Continue decoding field");
                        } catch (Exception e) {
                            LOG.warn("Exception rose during MTI decoding. Continue decoding field");
                        }
                        j++;
                    } else {
                        LOG.info("Packager №" + j + " is equal NULL. Skip it...");
                    }
                }
                if (isMTIDecoded) {
                    consumed += tmpConsumed;
                    m.set(mti);
                    resultPkgrMap.put(0, packagerSimpleName);
                    LOG.info("MTI decoded");
                } else {
                    LOG.info("We can't pick up MTI value. Exit decode procedure");
                    resultPkgrMap.put(0, "NOT PICKED UP (UNKNOWN)");
                }
            }
            LOG.info("Try to decode 1-st bitmap bytes");
            BitSet bmap = null;
            int bmapBytes= 0;                                   // bitmap length in bytes (usually 8, 16, 24)
            int maxField= fld.length - 1;                       // array length counts position 0!

            if (emitBitMap()) {
                ISOBitMap bitmap = new ISOBitMap (-1);
                consumed += getBitMapfieldPackager().unpack(bitmap,b,consumed);
                bmap = (BitSet) bitmap.getValue();
                bmapBytes= (bmap.length()-1 + 63) >> 6 << 3;
                if (evt != null)
                    evt.addMessage ("<bitmap>"+bmap.toString()+"</bitmap>");
                m.set (bitmap);
                LOG.info("Bitmap is decoded");
                LOG.info("Expected fields:" + bmap.toString());
                maxField = Math.min(maxField, bmap.length()-1); // bmap.length behaves similarly to fld.length
            }
            String fieldName;
            for (int i= getFirstField(); i <= maxField; i++) {
                int tmpConsumed = 0;
                String valueToCompare = null;
                String decodedFieldValue = null;
                String packagerSimpleName = null;
                ISOComponent c = null;
                fieldName = ISOUtil.padleft(String.valueOf(i), 3, '0');
                boolean isFieldDecoded = false;
                try {
                    if (bmap == null && fld[i] == null)
                        continue;
                    if (maxField > 128 && i==65)
                        continue;   // ignore extended bitmap
                    if (bmap == null || bmap.get(i)) {
                        if (fld[i] == null)
                            throw new ISOException ("field packager " + i + " is null");
                        LOG.info("Try to decode field F" + fieldName + " bytes");
                        for (ISOFieldPackager pkgr: pkgrCollector.getFieldPackagers()) {
                            try {
                                packagerSimpleName = pkgr.getClass().getSimpleName();
                                LOG.info("Use packager: " + packagerSimpleName);
                                pkgr.setLength(fld[i].getLength());
                                c = pkgr.createComponent(i);
                                tmpConsumed = pkgr.unpack(c, b, consumed);
                                decodedFieldValue = (String) c.getValue();
                                LOG.info("Decoded value: " + decodedFieldValue);
                                valueToCompare = dataToCompare.get(fieldName);
                                LOG.info("Expected value: " + valueToCompare);
                                if (valueToCompare.equals(decodedFieldValue)) {
                                    LOG.info("Decoded value is VALID");
                                    isFieldDecoded = true;
                                    break;
                                } else if (decodedFieldValue.matches("^[A-Za-z0-9=<> ]+")) {
                                    LOG.info("Decoded value is likely INVALID!!!");
                                    isFieldDecoded = false;
                                } else {
                                    LOG.info("Decoded value is INVALID!!! Continue decoding field F" + fieldName);
                                }
                            } catch (IllegalArgumentException e) {
                                LOG.warn("Runtime exception rose during F" + fieldName + " decoding. Continue decoding field");
                            } catch (Exception e) {
                                LOG.warn("Exception rose during F" + fieldName + " decoding. Continue decoding field");
                            }
                        }
                        if (isFieldDecoded) {
                            consumed += tmpConsumed;
                            m.set(c);
                            resultPkgrMap.put(i, packagerSimpleName);
                            LOG.info("Field " + fieldName + " decoded");
                        } else {
                            LOG.info("We can't pick up field value. Trying to continue decoding");
                            resultPkgrMap.put(i, "NOT PICKED UP (UNKNOWN)");
                        }

                        if (i == thirdBitmapField && fld.length > 129 &&          // fld[128] is at pos 129
                                bmapBytes == 16 &&
                                fld[thirdBitmapField] instanceof ISOBitMapPackager)
                        {
                            BitSet bs3rd= (BitSet)((ISOComponent)m.getChildren().get(thirdBitmapField)).getValue();
                            maxField= 128 + (bs3rd.length() - 1);                 // update loop end condition
                            for (int bit= 1; bit <= 64; bit++)
                                bmap.set(bit+128, bs3rd.get(bit));                // extend bmap with new bits above 128
                        }
                    }
                } catch (ISOException e) {
                    if (evt != null) {
                        evt.addMessage("error unpacking field " + i + " consumed=" + consumed);
                        evt.addMessage(e);
                    }
                    // jPOS-3
                    if (e.getNested() == null) {
                        e = new ISOException(
                                String.format("%s unpacking field=%d, consumed=%d",
                                        e.getMessage(), i, consumed)
                        );
                    } else {
                        e = new ISOException(
                                String.format("%s (%s) unpacking field=%d, consumed=%d",
                                        e.getMessage(), e.getNested().toString(), i, consumed)
                        );
                    }
                    throw e;
                }
            } // for each field
            if (evt != null && b.length != consumed) {
                evt.addMessage ("WARNING: unpack len=" +b.length +" consumed=" +consumed);
            }
            LOG.info("Decoding finished!");
            return consumed;
        } catch (ISOException e) {
            if (evt != null)
                evt.addMessage (e);
            throw e;
        } catch (Exception e) {
            if (evt != null)
                evt.addMessage (e);
            throw new ISOException (e.getMessage() + " consumed=" + consumed);
        } finally {
            if (evt != null)
                Logger.log (evt);
        }
    }




}

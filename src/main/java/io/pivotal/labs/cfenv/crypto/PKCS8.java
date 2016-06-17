package io.pivotal.labs.cfenv.crypto;

import java.util.List;

public class PKCS8 {

    private static final int PKCS8_VERSION = 0;

    public static byte[] wrap(List<byte[]> algorithmOids, byte[] keyBytes) {
        int versionLength = DEROutputStream.measureInteger(PKCS8_VERSION);
        int[] oidLengths = algorithmOids.stream().mapToInt(DEROutputStream::measureObjectID).toArray();
        int oidsLength = DEROutputStream.measureSequence(oidLengths);
        int keyLength = DEROutputStream.measureOctetString(keyBytes);

        return DEROutputStream.toBytes(out -> {
            out.writeSequenceStart(versionLength, oidsLength, keyLength);
            out.writeInteger(PKCS8_VERSION);
            out.writeSequenceStart(oidLengths);
            for (byte[] oid : algorithmOids) {
                out.writeObjectID(oid);
            }
            out.writeOctetString(keyBytes);
        });
    }

}

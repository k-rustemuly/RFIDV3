package rfid;

public class Utils {
    public Utils() {
    }

    public static void getCRC(byte[] data, int Len) {
        try {
            int current_crc_value = 65535;

            int i;
            for(i = 0; i < Len; ++i) {
                current_crc_value ^= data[i] & 255;

                for(int j = 0; j < 8; ++j) {
                    if ((current_crc_value & 1) != 0) {
                        current_crc_value = current_crc_value >> 1 ^ '萈';
                    } else {
                        current_crc_value >>= 1;
                    }
                }
            }

            data[i++] = (byte)(current_crc_value & 255);
            data[i] = (byte)(current_crc_value >> 8 & 255);
        } catch (Exception var5) {
        }

    }

    public static boolean CheckCRC(byte[] data, int len) {
        try {
            byte[] daw = new byte[256];
            System.arraycopy(data, 0, daw, 0, len);
            getCRC(daw, len);
            return daw[len + 1] == 0 && daw[len] == 0;
        } catch (Exception var3) {
            return false;
        }
    }

    public static String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");

        try {
            if (src != null && src.length > 0) {
                for(int i = offset; i < offset + length; ++i) {
                    int v = src[i] & 255;
                    String hv = Integer.toHexString(v);
                    if (hv.length() == 1) {
                        stringBuilder.append(0);
                    }

                    stringBuilder.append(hv);
                }

                return stringBuilder.toString().toUpperCase();
            } else {
                return null;
            }
        } catch (Exception var7) {
            return null;
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        try {
            if (hexString != null && !hexString.equals("")) {
                hexString = hexString.toUpperCase();
                int length = hexString.length() / 2;
                char[] hexChars = hexString.toCharArray();
                byte[] d = new byte[length];

                for(int i = 0; i < length; ++i) {
                    int pos = i * 2;
                    d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
                }

                return d;
            } else {
                return null;
            }
        } catch (Exception var6) {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }
}

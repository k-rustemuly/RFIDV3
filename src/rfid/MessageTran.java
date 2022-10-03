package rfid;

import java.io.*;
import java.net.Socket;

public class MessageTran {
    Socket tcpSocket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    String ipAddr;
    int port = 27011;
    boolean isConnect = false;

    public MessageTran() {
    }

    public int open(String ipaddr, int Port) {
        try {
            if (this.isConnect) {
                return 53;
            } else {
                this.ipAddr = ipaddr;
                this.port = Port;
                if (this.tcpSocket == null) {
                    this.tcpSocket = new Socket(this.ipAddr, this.port);
                    this.tcpSocket.setSoTimeout(10000);
                    this.outputStream = this.tcpSocket.getOutputStream();
                    this.inputStream = this.tcpSocket.getInputStream();
                    this.isConnect = true;
                }

                return 0;
            }
        } catch (Exception var4) {
            return 48;
        }
    }

    public synchronized int sendBytes(byte[] message, int length) {
        try {
            if (!this.isConnect) {
                return 48;
            } else {
                this.outputStream.write(message, 0, length);
                return 0;
            }
        } catch (Exception var4) {
            return 48;
        }
    }

    public synchronized byte[] receiveBytes() {
        try {
            if (!this.isConnect) {
                return null;
            } else {
                byte[] resultBuff = new byte[0];
                byte[] buffer = new byte[8192];
                DataInputStream in = new DataInputStream(new BufferedInputStream(this.tcpSocket.getInputStream()));
//                in.read(buffer);
//                InputStream stream = socket.getInputStream();
//                byte[] data = new byte[100];
                int Count = 0;
                try {
                    while ((Count = in.read(buffer)) > 0) {
                        System.out.println(Count);
                    }
                } catch ( Exception e )
                {
                    System.err.println(e);
                }
                byte[] result = new byte[Count];
                System.arraycopy(buffer, 0, result, 0, Count);
                return result;
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public void flush() {
        try {
            if (this.outputStream != null) {
                this.outputStream.flush();
            }
        } catch (Exception var2) {
        }

    }

    public int close() {
        try {
            if (this.tcpSocket != null) {
                this.tcpSocket.close();
                this.tcpSocket = null;
                if (this.outputStream != null) {
                    this.outputStream.close();
                    this.outputStream = null;
                }

                if (this.inputStream != null) {
                    this.inputStream.close();
                    this.inputStream = null;
                }
            }
        } catch (IOException var2) {
        }

        this.isConnect = false;
        return 0;
    }
}

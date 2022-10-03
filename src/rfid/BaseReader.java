package rfid;

public class BaseReader {
    private MessageTran msg = new MessageTran();
    private long maxScanTime = 2000L;
    private int[] recvLength = new int[1];
    private byte[] recvBuff = new byte[20000];
    private int logswitch = 0;
    private TagCallback callback;
    private String ReadIP;
    private int MaxAntennaNum = 4;
    private int lastPacket = 0;
    private String strEPC = "";

    public BaseReader(String ipAddr, int MaxAntennaNum) {
        this.ReadIP = ipAddr;
        this.MaxAntennaNum = MaxAntennaNum;
    }

    public void SetCallBack(TagCallback callback) {
        this.callback = callback;
    }

    public int Connect(String ipAddr, int Port, int logswitch) {
        this.logswitch = logswitch;
        return this.msg.open(ipAddr, Port);
    }

    public int DisConnect() {
        int result = this.msg.close();
        return result;
    }

    public int SendCMD(byte[] CMD) {
        if (this.logswitch == 1) {
            System.out.printf("Send:" + Utils.bytesToHexString(CMD, 0, (CMD[0] & 255) + 1) + "\n");
        }

        return this.msg.sendBytes(CMD, (CMD[0] & 255) + 1);
    }

    private int GetCMDData(byte[] data, int[] Nlen, int cmd, int endTime) {
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        try {
            while(System.currentTimeMillis() - beginTime < (long)endTime) {
                byte[] buffer = this.msg.receiveBytes();
                if (buffer == null) {
                    Thread.sleep(5L);
                } else {
                    Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        while(daw.length - index > 4) {
                            if ((daw[index] & 255) >= 4 && (daw[index + 2] & 255) == cmd) {
                                int len = daw[index] & 255;
                                if (daw.length < index + len + 1) {
                                    break;
                                }

                                byte[] epcArr = new byte[len + 1];
                                System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                if (Utils.CheckCRC(epcArr, epcArr.length)) {
                                    if (this.logswitch == 1) {
                                        System.out.printf("Recv:" + Utils.bytesToHexString(epcArr, 0, epcArr.length) + "\n");
                                    }

                                    System.arraycopy(epcArr, 0, data, 0, epcArr.length);
                                    Nlen[0] = epcArr.length + 1;
                                    return 0;
                                }

                                ++index;
                            } else {
                                ++index;
                            }
                        }

                        if (daw.length > index) {
                            btLength = daw.length - index;
                            System.arraycopy(daw, index, btArray, 0, btLength);
                        } else {
                            btLength = 0;
                        }
                    }
                }
            }
        } catch (Exception var15) {
            var15.toString();
        }

        return 48;
    }

    private int GetInventoryData(byte ComAddr, int cmd, byte[] epcdata, int[] epcNum, int[] dlen) {
        epcNum[0] = 0;
        dlen[0] = 0;
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        while(true) {
            try {
                byte[] buffer = this.msg.receiveBytes();
                if (buffer == null) {
                    Thread.sleep(5L);
                } else {
                     Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        while(daw.length - index > 5) {
                            if ((ComAddr & 255) == 255) {
                                ComAddr = 0;
                            }

                            if ((daw[index] & 255) >= 5 && daw[index + 1] == ComAddr && (daw[index + 2] & 255) == cmd) {
                                int len = daw[index] & 255;
                                if (daw.length >= index + len + 1) {
                                    byte[] epcArr = new byte[len + 1];
                                    System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                    if (!Utils.CheckCRC(epcArr, epcArr.length)) {
                                        if (this.logswitch == 1) {
                                            System.out.printf("crcERROR");
                                        }

                                        ++index;
                                        continue;
                                    } else {
                                        if (this.logswitch == 1) {
                                            System.out.printf("Recv:" + Utils.bytesToHexString(epcArr, 0, epcArr.length) + "\n");
                                        }

                                        int nLen = (epcArr[0] & 255) + 1;
                                        index += nLen;
                                        int status = epcArr[3] & 255;
                                        if (status != 1 && status != 2 && status != 3 && status != 4) {
                                            return status;
                                        }

                                        int num = epcArr[5] & 255;
                                        if (num > 0) {
                                            int m = 6;

                                            for(int nm = 0; nm < num; ++nm) {
                                                int epclen = epcArr[m] & 255;
                                                System.arraycopy(epcArr, m, epcdata, dlen[0], epclen + 2);
                                                int var10002 = epcNum[0]++;
                                                dlen[0] += epclen + 2;
                                                if (this.callback != null) {
                                                    ReadTag tag = new ReadTag();
                                                    int curant = epcArr[4] & 255;
                                                    if (this.MaxAntennaNum < 9) {
                                                        switch (curant) {
                                                            case 1:
                                                                tag.antId = 1;
                                                                break;
                                                            case 2:
                                                                tag.antId = 2;
                                                                break;
                                                            case 4:
                                                                tag.antId = 3;
                                                                break;
                                                            case 8:
                                                                tag.antId = 4;
                                                                break;
                                                            case 16:
                                                                tag.antId = 5;
                                                                break;
                                                            case 32:
                                                                tag.antId = 6;
                                                                break;
                                                            case 64:
                                                                tag.antId = 7;
                                                                break;
                                                            case 128:
                                                                tag.antId = 8;
                                                        }
                                                    } else {
                                                        tag.antId = curant + 1;
                                                    }

                                                    epclen = epcArr[m] & 255;
                                                    byte[] btArr = new byte[epclen];
                                                    System.arraycopy(epcArr, m + 1, btArr, 0, btArr.length);
                                                    tag.epcId = Utils.bytesToHexString(btArr, 0, btArr.length);
                                                    tag.rssi = epcArr[m + 1 + epclen] & 255;
                                                    tag.ipAddr = this.ReadIP;
                                                    this.callback.tagCallback(tag);
                                                }

                                                m = m + 2 + epclen;
                                            }
                                        }

                                        if (status != 1 && status != 2) {
                                            continue;
                                        }

                                        if (epcNum[0] > 0) {
                                            return 0;
                                        }

                                        return 1;
                                    }
                                }
                                break;
                            } else {
                                if (this.logswitch == 1) {
                                    System.out.printf("HeadERROR");
                                }

                                ++index;
                            }
                        }

                        if (daw.length > index) {
                            btLength = daw.length - index;
                            System.arraycopy(daw, index, btArray, 0, btLength);
                        } else {
                            btLength = 0;
                        }
                    }
                }

                if (System.currentTimeMillis() - beginTime < this.maxScanTime + 3000L) {
                    continue;
                }
            } catch (Exception var25) {
                var25.toString();
            }

            return 48;
        }
    }

    public int GetReaderInformation(byte[] ComAddr, byte[] TVersionInfo, byte[] ReaderType, byte[] TrType, byte[] band, byte[] dmaxfre, byte[] dminfre, byte[] powerdBm, byte[] ScanTime, byte[] Ant, byte[] BeepEn, byte[] OutputRep, byte[] CheckAnt) {
        byte[] buffer = new byte[]{4, ComAddr[0], 33, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 33, 1000);
        if (result == 0) {
            ComAddr[0] = this.recvBuff[1];
            TVersionInfo[0] = this.recvBuff[4];
            TVersionInfo[1] = this.recvBuff[5];
            ReaderType[0] = this.recvBuff[6];
            TrType[0] = this.recvBuff[7];
            dmaxfre[0] = (byte)(this.recvBuff[8] & 63);
            dminfre[0] = (byte)(this.recvBuff[9] & 63);
            band[0] = (byte)((this.recvBuff[8] & 192) >> 4 | (this.recvBuff[9] & 192) >> 6);
            powerdBm[0] = this.recvBuff[10];
            ScanTime[0] = this.recvBuff[11];
            this.maxScanTime = (long)((ScanTime[0] & 255) * 100);
            Ant[0] = this.recvBuff[12];
            BeepEn[0] = this.recvBuff[13];
            OutputRep[0] = this.recvBuff[14];
            CheckAnt[0] = this.recvBuff[15];
            return 0;
        } else {
            return 48;
        }
    }

    public int Inventory_G2(byte ComAddr, byte QValue, byte Session, byte AdrTID, byte LenTID, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen) {
        byte[] buffer = null;
        if (LenTID == 0) {
            buffer = new byte[]{9, ComAddr, 1, QValue, Session, Target, Ant, Scantime, 0, 0};
        } else {
            buffer = new byte[]{11, ComAddr, 1, QValue, Session, AdrTID, LenTID, Target, Ant, Scantime, 0, 0};
        }

        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        return this.GetInventoryData(ComAddr, 1, pOUcharIDList, pOUcharTagNum, pListLen);
    }

    private int GetInventoryMixData(byte ComAddr, int cmd, byte[] epcdata, int[] epcNum, int[] dlen) {
        epcNum[0] = 0;
        dlen[0] = 0;
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        try {
            do {
                byte[] buffer = this.msg.receiveBytes();
                if (buffer != null) {
                    Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        label108:
                        while(true) {
                            while(true) {
                                if (daw.length - index <= 5) {
                                    break label108;
                                }

                                if ((daw[index] & 255) >= 5 && (daw[index + 2] & 255) == cmd) {
                                    int len = daw[index] & 255;
                                    if (daw.length < index + len + 1) {
                                        break label108;
                                    }

                                    byte[] epcArr = new byte[len + 1];
                                    System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                    if (Utils.CheckCRC(epcArr, epcArr.length)) {
                                        if (this.logswitch == 1) {
                                            System.out.printf("Recv:" + Utils.bytesToHexString(epcArr, 0, epcArr.length) + "\n");
                                        }

                                        int nLen = (epcArr[0] & 255) + 1;
                                        index += nLen;
                                        int status = epcArr[3] & 255;
                                        if (status != 1 && status != 2 && status != 3 && status != 4) {
                                            return status;
                                        }

                                        int num = epcArr[5] & 255;
                                        if (num > 0) {
                                            int m = 6;
                                            int PacketParam = epcArr[m] & 255;
                                            int epcfullen = epcArr[m + 1] & 255;
                                            int epclen = epcArr[m + 1] & 255 & 127;
                                            byte[] uid = new byte[epclen];
                                            System.arraycopy(epcArr, m + 2, uid, 0, epclen);
                                            String strTid = "";
                                            if (PacketParam < 128) {
                                                this.strEPC = Utils.bytesToHexString(uid, 0, uid.length);
                                                this.lastPacket = PacketParam;
                                            } else if (this.lastPacket == (PacketParam & 127) - 1 || this.lastPacket == 127 && (PacketParam & 127) == 0) {
                                                int antenna = epcArr[4] & 255;
                                                strTid = Utils.bytesToHexString(uid, 0, uid.length);
                                                if (this.callback != null) {
                                                    ReadTag tag = new ReadTag();
                                                    int curant = epcArr[4] & 255;
                                                    if (this.MaxAntennaNum < 9) {
                                                        switch (curant) {
                                                            case 1:
                                                                tag.antId = 1;
                                                                break;
                                                            case 2:
                                                                tag.antId = 2;
                                                                break;
                                                            case 4:
                                                                tag.antId = 3;
                                                                break;
                                                            case 8:
                                                                tag.antId = 4;
                                                                break;
                                                            case 16:
                                                                tag.antId = 5;
                                                                break;
                                                            case 32:
                                                                tag.antId = 6;
                                                                break;
                                                            case 64:
                                                                tag.antId = 7;
                                                                break;
                                                            case 128:
                                                                tag.antId = 8;
                                                        }
                                                    } else {
                                                        tag.antId = curant + 1;
                                                    }

                                                    tag.epcId = this.strEPC;
                                                    tag.memId = strTid;
                                                    tag.rssi = epcArr[8 + epclen] & 255;
                                                    tag.ipAddr = this.ReadIP;
                                                    this.callback.tagCallback(tag);
                                                }

                                                this.strEPC = "";
                                            }
                                        }

                                        if (status == 1 || status == 2) {
                                            if (epcNum[0] > 0) {
                                                return 0;
                                            }

                                            return 1;
                                        }
                                    } else {
                                        ++index;
                                    }
                                } else {
                                    ++index;
                                }
                            }
                        }

                        if (daw.length > index) {
                            btLength = daw.length - index;
                            System.arraycopy(daw, index, btArray, 0, btLength);
                        } else {
                            btLength = 0;
                        }
                    }
                } else {
                    Thread.sleep(5L);
                }
            } while(System.currentTimeMillis() - beginTime < this.maxScanTime + 5000L);
        } catch (Exception var28) {
            var28.toString();
        }

        return 48;
    }

    public int Inventory_Mix(byte ComAddr, byte QValue, byte Session, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte MaskFlag, byte ReadMem, byte[] ReadAdr, byte ReadLen, byte[] Pwd, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen) {
        byte[] buffer = new byte[18];
        if (MaskFlag == 0) {
            buffer = new byte[]{17, ComAddr, 25, QValue, Session, ReadMem, ReadAdr[0], ReadAdr[1], ReadLen, Pwd[0], Pwd[1], Pwd[2], Pwd[3], Target, Ant, Scantime, 0, 0};
        } else {
            int len = (MaskLen + 7) / 8;
            buffer = new byte[22 + len];
            buffer[0] = (byte)(21 + len);
            buffer[1] = ComAddr;
            buffer[2] = 25;
            buffer[3] = QValue;
            buffer[4] = Session;
            buffer[5] = MaskMem;
            buffer[6] = MaskAdr[0];
            buffer[7] = MaskAdr[1];
            buffer[8] = MaskLen;
            if (len > 0) {
                System.arraycopy(MaskData, 0, buffer, 9, len);
            }

            buffer[9 + len] = ReadMem;
            buffer[10 + len] = ReadAdr[0];
            buffer[11 + len] = ReadAdr[1];
            buffer[12 + len] = ReadLen;
            buffer[13 + len] = Pwd[0];
            buffer[14 + len] = Pwd[1];
            buffer[15 + len] = Pwd[2];
            buffer[16 + len] = Pwd[3];
            buffer[17 + len] = Target;
            buffer[18 + len] = Ant;
            buffer[19 + len] = Scantime;
        }

        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        return this.GetInventoryMixData(ComAddr, 25, pOUcharIDList, pOUcharTagNum, pListLen);
    }

    public int SetRfPower(byte ComAddr, byte power) {
        byte[] buffer = new byte[]{5, ComAddr, 47, power, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 47, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetRfPowerByAnt(byte ComAddr, byte[] Power) {
        if (Power.length != this.MaxAntennaNum) {
            return 255;
        } else {
            byte[] buffer = new byte[5 + Power.length];
            buffer[0] = (byte)(4 + Power.length);
            buffer[1] = ComAddr;
            buffer[2] = 47;
            System.arraycopy(Power, 0, buffer, 3, Power.length);
            Utils.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            int result = this.GetCMDData(this.recvBuff, this.recvLength, 47, 1000);
            return result == 0 ? this.recvBuff[3] & 255 : 48;
        }
    }

    public int GetRfPowerByAnt(byte ComAddr, byte[] Power) {
        if (Power.length < this.MaxAntennaNum) {
            return 255;
        } else {
            byte[] buffer = new byte[]{4, ComAddr, -108, 0, 0};
            Utils.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            int result = this.GetCMDData(this.recvBuff, this.recvLength, 148, 1000);
            if (result == 0) {
                if (this.recvBuff[3] == 0) {
                    System.arraycopy(this.recvBuff, 4, Power, 0, this.recvBuff[0] - 5);
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        }
    }

    public int SetAddress(byte ComAddr, byte newAddr) {
        byte[] buffer = new byte[]{5, ComAddr, 36, newAddr, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 36, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetRegion(byte ComAddr, int band, int maxfre, int minfre) {
        byte[] buffer = new byte[]{6, ComAddr, 34, (byte)((band & 12) << 4 | maxfre & 63), (byte)((band & 3) << 6 | minfre & 63), 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 34, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetAntennaMultiplexing(byte ComAddr, byte AntCfg) {
        byte[] buffer = new byte[]{5, ComAddr, 63, AntCfg, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 63, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetAntennaMultiplexing(byte ComAddr, byte SetOnce, byte AntCfg1, byte AntCfg2) {
        byte[] buffer = new byte[]{7, ComAddr, 63, SetOnce, AntCfg1, AntCfg2, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 63, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int ConfigDRM(byte ComAddr, byte[] DRM) {
        byte[] buffer = new byte[]{5, ComAddr, -112, DRM[0], 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 144, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                DRM[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetRelay(byte ComAddr, byte RelayTime) {
        byte[] buffer = new byte[]{5, ComAddr, 69, RelayTime, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 69, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetGPIO(byte ComAddr, byte OutputPin) {
        byte[] buffer = new byte[]{5, ComAddr, 70, OutputPin, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 70, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetGPIOStatus(byte ComAddr, byte[] OutputPin) {
        byte[] buffer = new byte[]{4, ComAddr, 71, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 71, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                OutputPin[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int GetSerialNo(byte ComAddr, byte[] SeriaNo) {
        byte[] buffer = new byte[]{4, ComAddr, 76, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 76, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                System.arraycopy(this.recvBuff, 4, SeriaNo, 0, 4);
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetBeepNotification(byte ComAddr, byte BeepEn) {
        byte[] buffer = new byte[]{5, ComAddr, 64, BeepEn, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 64, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetCheckAnt(byte ComAddr, byte CheckAnt) {
        byte[] buffer = new byte[]{5, ComAddr, 102, CheckAnt, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 102, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int ReadData_G2(byte ComAddr, byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte Num, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte[] Data, byte[] Errorcode) {
        int mLen;
        if (ENum > 0 && ENum < 16) {
            byte[] buffer = new byte[13 + ENum * 2];
            buffer[0] = (byte)(12 + ENum * 2);
            buffer[1] = ComAddr;
            buffer[2] = 2;
            buffer[3] = ENum;
            System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
            buffer[ENum * 2 + 4] = Mem;
            buffer[ENum * 2 + 5] = WordPtr;
            buffer[ENum * 2 + 6] = Num;
            System.arraycopy(Password, 0, buffer, ENum * 2 + 7, 4);
            Utils.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            mLen = this.GetCMDData(this.recvBuff, this.recvLength, 2, 3000);
            if (mLen == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                    System.arraycopy(this.recvBuff, 4, Data, 0, Num * 2);
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        } else if ((ENum & 255) == 255) {
            if (MaskLen == 0) {
                return 255;
            } else {
                int maskbyte = 0;
                mLen = MaskLen & 255;
                if (mLen % 8 == 0) {
                    maskbyte = mLen / 8;
                } else {
                    maskbyte = mLen / 8 + 1;
                }

                byte[] buffer = new byte[17 + maskbyte];
                buffer[0] = (byte)(16 + maskbyte);
                buffer[1] = ComAddr;
                buffer[2] = 2;
                buffer[3] = ENum;
                buffer[4] = Mem;
                buffer[5] = WordPtr;
                buffer[6] = Num;
                System.arraycopy(Password, 0, buffer, 7, 4);
                buffer[11] = MaskMem;
                buffer[12] = MaskAdr[0];
                buffer[13] = MaskAdr[1];
                buffer[14] = MaskLen;
                System.arraycopy(MaskData, 0, buffer, 15, maskbyte);
                Utils.getCRC(buffer, buffer[0] - 1);
                this.SendCMD(buffer);
                int result = this.GetCMDData(this.recvBuff, this.recvLength, 2, 3000);
                if (result == 0) {
                    if (this.recvBuff[3] == 0) {
                        Errorcode[0] = 0;
                        System.arraycopy(this.recvBuff, 4, Data, 0, Num * 2);
                    } else if ((this.recvBuff[3] & 255) == 252) {
                        Errorcode[0] = this.recvBuff[4];
                    }

                    return this.recvBuff[3] & 255;
                } else {
                    return 48;
                }
            }
        } else {
            return 255;
        }
    }

    public int WriteData_G2(byte ComAddr, byte WNum, byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte[] Writedata, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte[] Errorcode) {
        int mLen;
        if (ENum > 0 && ENum < 16) {
            byte[] buffer = new byte[13 + (ENum + WNum) * 2];
            buffer[0] = (byte)(12 + (ENum + WNum) * 2);
            buffer[1] = ComAddr;
            buffer[2] = 3;
            buffer[3] = WNum;
            buffer[4] = ENum;
            if (ENum > 0) {
                System.arraycopy(EPC, 0, buffer, 5, ENum * 2);
            }

            buffer[ENum * 2 + 5] = Mem;
            buffer[ENum * 2 + 6] = WordPtr;
            System.arraycopy(Writedata, 0, buffer, ENum * 2 + 7, WNum * 2);
            System.arraycopy(Password, 0, buffer, ENum * 2 + WNum * 2 + 7, 4);
            Utils.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            mLen = this.GetCMDData(this.recvBuff, this.recvLength, 3, 3000);
            if (mLen == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        } else if ((ENum & 255) == 255) {
            if (MaskLen == 0) {
                return 255;
            } else {
                int maskbyte = 0;
                mLen = MaskLen & 255;
                if (mLen % 8 == 0) {
                    maskbyte = mLen / 8;
                } else {
                    maskbyte = mLen / 8 + 1;
                }

                byte[] buffer = new byte[17 + WNum * 2 + maskbyte];
                buffer[0] = (byte)(16 + WNum * 2 + maskbyte);
                buffer[1] = ComAddr;
                buffer[2] = 3;
                buffer[3] = WNum;
                buffer[4] = ENum;
                buffer[5] = Mem;
                buffer[6] = WordPtr;
                System.arraycopy(Writedata, 0, buffer, 7, WNum * 2);
                System.arraycopy(Password, 0, buffer, WNum * 2 + 7, 4);
                buffer[WNum * 2 + 11] = MaskMem;
                buffer[WNum * 2 + 12] = MaskAdr[0];
                buffer[WNum * 2 + 13] = MaskAdr[1];
                buffer[WNum * 2 + 14] = MaskLen;
                System.arraycopy(MaskData, 0, buffer, WNum * 2 + 15, maskbyte);
                Utils.getCRC(buffer, buffer[0] - 1);
                this.SendCMD(buffer);
                int result = this.GetCMDData(this.recvBuff, this.recvLength, 3, 3000);
                if (result == 0) {
                    if (this.recvBuff[3] == 0) {
                        Errorcode[0] = 0;
                    } else if ((this.recvBuff[3] & 255) == 252) {
                        Errorcode[0] = this.recvBuff[4];
                    }

                    return this.recvBuff[3] & 255;
                } else {
                    return 48;
                }
            }
        } else {
            return 255;
        }
    }

    public int WriteEPC_G2(byte ComAddr, byte ENum, byte[] Password, byte[] WriteEPC, byte[] Errorcode) {
        byte[] buffer = new byte[10 + ENum * 2];
        buffer[0] = (byte)(9 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 4;
        buffer[3] = ENum;
        if (ENum > 0) {
            System.arraycopy(Password, 0, buffer, 4, 4);
        }

        System.arraycopy(WriteEPC, 0, buffer, 8, ENum * 2);
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 4, 2000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int Lock_G2(byte ComAddr, byte ENum, byte[] EPC, byte select, byte setprotect, byte[] Password, byte[] Errorcode) {
        byte[] buffer = new byte[12 + ENum * 2];
        buffer[0] = (byte)(11 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 6;
        buffer[3] = ENum;
        if (ENum > 0) {
            System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
        }

        buffer[ENum * 2 + 4] = select;
        buffer[ENum * 2 + 5] = setprotect;
        System.arraycopy(Password, 0, buffer, ENum * 2 + 6, 4);
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 6, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int Kill_G2(byte ComAddr, byte ENum, byte[] EPC, byte[] Password, byte[] Errorcode) {
        byte[] buffer = new byte[10 + ENum * 2];
        buffer[0] = (byte)(9 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 5;
        buffer[3] = ENum;
        if (ENum > 0) {
            System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
        }

        System.arraycopy(Password, 0, buffer, ENum * 2 + 4, 4);
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 5, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int MeasureReturnLoss(byte ComAddr, byte[] TestFreq, byte Ant, byte[] ReturnLoss) {
        byte[] buffer = new byte[10];
        buffer[0] = 9;
        buffer[1] = ComAddr;
        buffer[2] = -111;
        System.arraycopy(TestFreq, 0, buffer, 3, 4);
        buffer[7] = Ant;
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 145, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                ReturnLoss[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetWritePower(byte ComAddr, byte WritePower) {
        byte[] buffer = new byte[]{5, ComAddr, 121, WritePower, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 121, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetWritePower(byte ComAddr, byte[] WritePower) {
        byte[] buffer = new byte[]{4, ComAddr, 122, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 122, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                WritePower[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    private int SetReadParameter(byte ComAddr, byte[] Parameter) {
        byte[] buffer = new byte[]{9, ComAddr, 117, Parameter[0], Parameter[1], Parameter[2], Parameter[3], Parameter[4], 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 117, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    private int GetReadParameter(byte ComAddr, byte[] Parameter) {
        byte[] buffer = new byte[]{4, ComAddr, 119, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 119, 1000);
        if (result == 0) {
            System.arraycopy(this.recvBuff, 4, Parameter, 0, 6);
            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    private int SetWorkMode(byte ComAddr, byte ReadMode) {
        byte[] buffer = new byte[]{5, ComAddr, 118, ReadMode, 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 118, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SwitchProtocol(byte ComAddr, byte[] protocol) {
        byte[] buffer = new byte[]{5, ComAddr, -50, protocol[0], 0, 0};
        Utils.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 206, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                protocol[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }
}

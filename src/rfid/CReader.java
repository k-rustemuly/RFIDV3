package rfid;

public class CReader {
    private BaseReader reader = null;
    private ReaderParameter param = new ReaderParameter();
    private volatile boolean mWorking = true;
    private volatile Thread mThread = null;
    private byte[] pOUcharIDList = new byte[25600];
    private volatile int NoCardCOunt = 0;
    private String ipAddr = "192.168.0.250";
    private int Port = 27011;
    private int logswitch = 0;
    private int MaxAntennaNum = 4;
    private TagCallback callback;
    public boolean isConnect = false;

    public CReader(String ipAddr, int Port, int MaxAntennaNum, int logswitch) {
        this.ipAddr = ipAddr;
        this.Port = Port;
        this.logswitch = logswitch;
        this.param.SetAddress((byte)-1);
        this.param.SetScanTime(10);
        this.param.SetSession(1);
        this.param.SetQValue(4);
        this.param.SetTidPtr(0);
        this.param.SetTidLen(6);
        this.param.SetAntenna(1);
        this.param.SetReadType(0);
        this.param.SetReadMem(3);
        this.param.SetReadPtr(0);
        this.param.SetReadLength(6);
        this.param.SetPassword("00000000");
        this.MaxAntennaNum = MaxAntennaNum;
        this.reader = new BaseReader(ipAddr, MaxAntennaNum);
        this.isConnect = false;
    }

    public int Connect() {
        if (this.isConnect) {
            return 48;
        } else {
            int result = this.reader.Connect(this.ipAddr, this.Port, this.logswitch);
            if (result == 0) {
                this.isConnect = true;
            }

            return result;
        }
    }

    public void DisConnect() {
        if (this.isConnect) {
            this.mWorking = false;
            this.reader.DisConnect();
            this.isConnect = false;
        }

    }

    public void SetInventoryParameter(ReaderParameter param) {
        if (this.isConnect) {
            byte[] Pro = new byte[1];
            if (param.GetReadType() == 3) {
                Pro[0] = -127;
            } else {
                Pro[0] = -128;
            }

            this.reader.SwitchProtocol(param.GetAddress(), Pro);
        }

        this.param = param;
    }

    public ReaderParameter GetInventoryParameter() {
        if (this.isConnect) {
            byte[] Pro = new byte[]{0};
            int result1 = this.reader.SwitchProtocol(this.param.GetAddress(), Pro);
            if (result1 == 0 && Pro[0] == 1) {
                this.param.SetReadType(3);
            }
        }

        return this.param;
    }

    public int GetUHFInformation(byte[] Version, byte[] Power, byte[] band, byte[] MaxFre, byte[] MinFre, byte[] BeepEn, int[] Ant) {
        byte[] ReaderType = new byte[1];
        byte[] TrType = new byte[1];
        byte[] ScanTime = new byte[1];
        byte[] OutputRep = new byte[1];
        byte[] CheckAnt = new byte[1];
        byte[] ComAddr = new byte[]{-1};
        byte[] AntCfg0 = new byte[1];
        byte[] AntCfg1 = new byte[1];
        int result = this.reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, AntCfg0, BeepEn, AntCfg1, CheckAnt);
        if (result == 0) {
            Ant[0] = ((AntCfg1[0] & 255) << 8) + (AntCfg0[0] & 255);
            this.param.SetAddress(ComAddr[0]);
            this.param.SetAntenna(Ant[0] & 255);
            byte[] Pro = new byte[]{0};
            int result1 = this.reader.SwitchProtocol(this.param.GetAddress(), Pro);
            if (result1 == 0 && Pro[0] == 1) {
                this.param.SetReadType(3);
            }
        }

        return result;
    }

    public int SetRfPower(int Power) {
        return this.reader.SetRfPower(this.param.GetAddress(), (byte)Power);
    }

    public int SetRegion(int band, int maxfre, int minfre) {
        return this.reader.SetRegion(this.param.GetAddress(), band, maxfre, minfre);
    }

    public int SetAntenna(int SetOnce, int AntCfg) {
        int result = 0;
        if (this.MaxAntennaNum > 4) {
            byte AntCfg1 = (byte)(AntCfg >> 8);
            byte AntCfg2 = (byte)(AntCfg & 255);
            result = this.reader.SetAntennaMultiplexing(this.param.GetAddress(), (byte)SetOnce, AntCfg1, AntCfg2);
            if (result == 0) {
                this.param.SetAntenna(AntCfg);
            }
        } else {
            if (SetOnce == 1) {
                AntCfg |= 128;
            }

            result = this.reader.SetAntennaMultiplexing(this.param.GetAddress(), (byte)AntCfg);
            if (result == 0) {
                this.param.SetAntenna(AntCfg);
            }
        }

        return result;
    }

    public int SetBeepNotification(int BeepEn) {
        return this.reader.SetBeepNotification(this.param.GetAddress(), (byte)BeepEn);
    }

    public int SetRfPowerByAnt(byte[] Power) {
        return Power.length != this.MaxAntennaNum ? 255 : this.reader.SetRfPowerByAnt(this.param.GetAddress(), Power);
    }

    public int GetRfPowerByAnt(byte[] Power) {
        return Power.length != this.MaxAntennaNum ? 255 : this.reader.GetRfPowerByAnt(this.param.GetAddress(), Power);
    }

    public int ConfigDRM(byte[] DRM) {
        return this.reader.ConfigDRM(this.param.GetAddress(), DRM);
    }

    public int SetRelay(int RelayTime) {
        return this.reader.SetRelay(this.param.GetAddress(), (byte)RelayTime);
    }

    public int SetGPIO(int GPIO) {
        return this.reader.SetGPIO(this.param.GetAddress(), (byte)GPIO);
    }

    public int GetGPIOStatus(byte[] OutputPin) {
        return this.reader.GetGPIOStatus(this.param.GetAddress(), OutputPin);
    }

    public String GetSerialNo() {
        byte[] btArr = new byte[4];
        int result = this.reader.GetSerialNo(this.param.GetAddress(), btArr);
        if (result == 0) {
            String temp = Utils.bytesToHexString(btArr, 0, btArr.length);
            return temp;
        } else {
            return null;
        }
    }

    public int MeasureReturnLoss(byte[] TestFreq, byte Ant, byte[] ReturnLoss) {
        return this.reader.MeasureReturnLoss(this.param.GetAddress(), TestFreq, Ant, ReturnLoss);
    }

    public int SetWritePower(byte WritePower) {
        return this.reader.SetWritePower(this.param.GetAddress(), WritePower);
    }

    public int GetWritePower(byte[] WritePower) {
        return this.reader.GetWritePower(this.param.GetAddress(), WritePower);
    }

    public int SetCheckAnt(byte CheckAnt) {
        return this.reader.SetCheckAnt(this.param.GetAddress(), CheckAnt);
    }

    public String ReadDataByEPC(String EPCStr, byte Mem, byte WordPtr, byte Num, String PasswordStr) {
        if (EPCStr != null && EPCStr.length() % 4 != 0) {
            return null;
        } else if (PasswordStr != null && PasswordStr.length() == 8) {
            byte[] Password = Utils.hexStringToBytes(PasswordStr);
            byte ENum = 0;
            if (EPCStr != null) {
                ENum = (byte)(EPCStr.length() / 4);
            }

            byte[] EPC = Utils.hexStringToBytes(EPCStr);
            byte MaskMem = 0;
            byte[] MaskAdr = new byte[2];
            byte MaskLen = 0;
            byte[] MaskData = new byte[12];
            byte[] Data = new byte[Num * 2];
            byte[] Errorcode = new byte[1];
            int result = this.reader.ReadData_G2(this.param.GetAddress(), ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, Data, Errorcode);
            return result == 0 ? Utils.bytesToHexString(Data, 0, Data.length) : null;
        } else {
            return null;
        }
    }

    public String ReadDataByTID(String TIDStr, byte Mem, byte WordPtr, byte Num, String PasswordStr) {
        if (TIDStr != null && TIDStr.length() % 4 == 0) {
            if (PasswordStr != null && PasswordStr.length() == 8) {
                byte[] Password = Utils.hexStringToBytes(PasswordStr);
                byte ENum = -1;
                byte[] EPC = new byte[12];
                byte[] TID = Utils.hexStringToBytes(TIDStr);
                byte MaskMem = 2;
                byte[] MaskAdr = new byte[2];
                MaskAdr[0] = MaskAdr[1] = 0;
                byte MaskLen = (byte)(TIDStr.length() * 4);
                byte[] MaskData = new byte[TIDStr.length()];
                System.arraycopy(TID, 0, MaskData, 0, TID.length);
                byte[] Data = new byte[Num * 2];
                byte[] Errorcode = new byte[1];
                int result = this.reader.ReadData_G2(this.param.GetAddress(), ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, Data, Errorcode);
                return result == 0 ? Utils.bytesToHexString(Data, 0, Data.length) : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public int WriteDataByEPC(String EPCStr, byte Mem, byte WordPtr, String PasswordStr, String wdata) {
        if (EPCStr != null && EPCStr.length() % 4 != 0) {
            return 255;
        } else if (wdata != null && wdata.length() % 4 == 0) {
            if (PasswordStr != null && PasswordStr.length() == 8) {
                byte[] Password = Utils.hexStringToBytes(PasswordStr);
                byte ENum = 0;
                if (EPCStr != null) {
                    ENum = (byte)(EPCStr.length() / 4);
                }

                byte WNum = (byte)(wdata.length() / 4);
                byte[] EPC = Utils.hexStringToBytes(EPCStr);
                byte[] data = Utils.hexStringToBytes(wdata);
                byte MaskMem = 0;
                byte[] MaskAdr = new byte[2];
                byte MaskLen = 0;
                byte[] MaskData = new byte[12];
                byte[] Errorcode = new byte[1];
                return this.reader.WriteData_G2(this.param.GetAddress(), WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, Errorcode);
            } else {
                return 255;
            }
        } else {
            return 255;
        }
    }

    public int WriteDataByTID(String TIDStr, byte Mem, byte WordPtr, String PasswordStr, String wdata) {
        if (TIDStr != null && TIDStr.length() % 4 == 0) {
            if (wdata != null && wdata.length() % 4 == 0) {
                if (PasswordStr != null && PasswordStr.length() == 8) {
                    byte[] Password = Utils.hexStringToBytes(PasswordStr);
                    byte ENum = -1;
                    byte WNum = (byte)(wdata.length() / 4);
                    byte[] EPC = new byte[12];
                    byte[] data = Utils.hexStringToBytes(wdata);
                    byte[] TID = Utils.hexStringToBytes(TIDStr);
                    byte MaskMem = 2;
                    byte[] MaskAdr = new byte[2];
                    MaskAdr[0] = MaskAdr[1] = 0;
                    byte MaskLen = (byte)(TIDStr.length() * 4);
                    byte[] MaskData = new byte[TIDStr.length()];
                    System.arraycopy(TID, 0, MaskData, 0, TID.length);
                    byte[] Errorcode = new byte[1];
                    return this.reader.WriteData_G2(this.param.GetAddress(), WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, Errorcode);
                } else {
                    return 255;
                }
            } else {
                return 255;
            }
        } else {
            return 255;
        }
    }

    public int WriteEPC(String EPCStr, String PasswordStr) {
        if (EPCStr != null && EPCStr.length() % 4 == 0) {
            if (PasswordStr != null && PasswordStr.length() == 8) {
                byte[] Password = Utils.hexStringToBytes(PasswordStr);
                byte WNum = (byte)(EPCStr.length() / 4);
                byte[] Errorcode = new byte[1];
                byte[] data = Utils.hexStringToBytes(EPCStr);
                return this.reader.WriteEPC_G2(this.param.GetAddress(), WNum, Password, data, Errorcode);
            } else {
                return 255;
            }
        } else {
            return 255;
        }
    }

    public int WriteEPCByTID(String TIDStr, String EPCStr, String PasswordStr) {
        if (TIDStr != null && TIDStr.length() % 4 == 0) {
            if (EPCStr != null && EPCStr.length() % 4 == 0) {
                if (PasswordStr != null && PasswordStr.length() == 8) {
                    byte[] Password = Utils.hexStringToBytes(PasswordStr);
                    byte ENum = -1;
                    byte WNum = (byte)(EPCStr.length() / 4);
                    byte[] EPC = new byte[12];
                    String PCStr = "";
                    switch (WNum) {
                        case 1:
                            PCStr = "0800";
                            break;
                        case 2:
                            PCStr = "1000";
                            break;
                        case 3:
                            PCStr = "1800";
                            break;
                        case 4:
                            PCStr = "2000";
                            break;
                        case 5:
                            PCStr = "2800";
                            break;
                        case 6:
                            PCStr = "3000";
                            break;
                        case 7:
                            PCStr = "3800";
                            break;
                        case 8:
                            PCStr = "4000";
                            break;
                        case 9:
                            PCStr = "4800";
                            break;
                        case 10:
                            PCStr = "5000";
                            break;
                        case 11:
                            PCStr = "5800";
                            break;
                        case 12:
                            PCStr = "6000";
                            break;
                        case 13:
                            PCStr = "6800";
                            break;
                        case 14:
                            PCStr = "7000";
                            break;
                        case 15:
                            PCStr = "7800";
                            break;
                        case 16:
                            PCStr = "8000";
                    }

                    String wdata = PCStr + EPCStr;
                    ++WNum;
                    byte[] data = Utils.hexStringToBytes(wdata);
                    byte[] TID = Utils.hexStringToBytes(TIDStr);
                    byte MaskMem = 2;
                    byte[] MaskAdr = new byte[2];
                    MaskAdr[0] = MaskAdr[1] = 0;
                    byte MaskLen = (byte)(TIDStr.length() * 4);
                    byte[] MaskData = new byte[TIDStr.length()];
                    System.arraycopy(TID, 0, MaskData, 0, TID.length);
                    byte[] Errorcode = new byte[1];
                    byte Mem = 1;
                    byte WordPtr = 1;
                    return this.reader.WriteData_G2(this.param.GetAddress(), WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, Errorcode);
                } else {
                    return 255;
                }
            } else {
                return 255;
            }
        } else {
            return 255;
        }
    }

    public int Lock(String EPCStr, byte select, byte setprotect, String PasswordStr) {
        if (EPCStr != null && EPCStr.length() % 4 != 0) {
            return 255;
        } else if (PasswordStr != null && PasswordStr.length() == 8) {
            byte ENum = 0;
            if (EPCStr != null) {
                ENum = (byte)(EPCStr.length() / 4);
            }

            byte[] EPC = Utils.hexStringToBytes(EPCStr);
            byte[] Password = Utils.hexStringToBytes(PasswordStr);
            byte[] Errorcode = new byte[1];
            return this.reader.Lock_G2(this.param.GetAddress(), ENum, EPC, select, setprotect, Password, Errorcode);
        } else {
            return 255;
        }
    }

    public int Kill(String EPCStr, String PasswordStr) {
        if (EPCStr != null && EPCStr.length() % 4 != 0) {
            return 255;
        } else if (PasswordStr != null && PasswordStr.length() == 8) {
            byte ENum = 0;
            if (EPCStr != null) {
                ENum = (byte)(EPCStr.length() / 4);
            }

            byte[] EPC = Utils.hexStringToBytes(EPCStr);
            byte[] Password = Utils.hexStringToBytes(PasswordStr);
            byte[] Errorcode = new byte[1];
            return this.reader.Kill_G2(this.param.GetAddress(), ENum, EPC, Password, Errorcode);
        } else {
            return 255;
        }
    }

    public int ReadData_G2(byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte Num, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte[] Data, byte[] Errorcode) {
        return this.reader.ReadData_G2(this.param.GetAddress(), ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, Data, Errorcode);
    }

    public int WriteData_G2(byte WNum, byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte[] Writedata, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte[] Errorcode) {
        return this.reader.WriteData_G2(this.param.GetAddress(), WNum, ENum, EPC, Mem, WordPtr, Writedata, Password, MaskMem, MaskAdr, MaskLen, MaskData, Errorcode);
    }

    public void SetCallBack(TagCallback mycallback) {
        this.callback = mycallback;
        this.reader.SetCallBack(mycallback);
    }

    public int StartRead() {
        if (this.mThread == null) {
            this.mWorking = true;
            this.mThread = new Thread(() -> {
                byte Target = 0;

                for(int index = 0; CReader.this.mWorking; index %= CReader.this.MaxAntennaNum) {
                    int antenna = 1 << index;
                    if ((CReader.this.param.GetAntenna() & antenna) == antenna) {
                        byte Ant = (byte)(index | 128);
                        int[] pOUcharTagNum = new int[1];
                        int[] pListLen = new int[1];
                        pOUcharTagNum[0] = pListLen[0] = 0;
                        if (CReader.this.param.GetSession() == 0 || CReader.this.param.GetSession() == 1) {
                            Target = 0;
                            CReader.this.NoCardCOunt = 0;
                        }
                        int reTryTime;
                        if (CReader.this.param.GetReadType() == 0) {
                            reTryTime = 0;
                            CReader.this.reader.Inventory_G2(CReader.this.param.GetAddress(), (byte)CReader.this.param.GetQValue(), (byte)CReader.this.param.GetSession(), (byte)CReader.this.param.GetTidPtr(), (byte)reTryTime, Target, Ant, (byte)CReader.this.param.GetScanTime(), CReader.this.pOUcharIDList, pOUcharTagNum, pListLen);
                        } else if (CReader.this.param.GetReadType() == 1) {
                            byte TIDlen = (byte)CReader.this.param.GetTidLen();
                            if (TIDlen == 0) {
                                TIDlen = 6;
                            }

                            CReader.this.reader.Inventory_G2(CReader.this.param.GetAddress(), (byte)CReader.this.param.GetQValue(), (byte)CReader.this.param.GetSession(), (byte)CReader.this.param.GetTidPtr(), TIDlen, Target, Ant, (byte)CReader.this.param.GetScanTime(), CReader.this.pOUcharIDList, pOUcharTagNum, pListLen);
                        } else if (CReader.this.param.GetReadType() == 2) {
                            reTryTime = 0;
                            byte[] MaskAdr = new byte[2];
                            byte MaskLen = 0;
                            byte[] MaskData = new byte[96];
                            byte MaskFlag = 0;
                            byte[] ReadAddr = new byte[]{(byte)(CReader.this.param.GetReadPtr() >> 8), (byte)(CReader.this.param.GetReadPtr() & 255)};
                            byte[] Password = Utils.hexStringToBytes(CReader.this.param.GetPassword());
                            CReader.this.reader.Inventory_Mix(CReader.this.param.GetAddress(), (byte)CReader.this.param.GetQValue(), (byte)CReader.this.param.GetSession(), (byte)reTryTime, MaskAdr, MaskLen, MaskData, MaskFlag, (byte)CReader.this.param.GetReadMem(), ReadAddr, (byte)CReader.this.param.GetReadLength(), Password, Target, Ant, (byte)CReader.this.param.GetScanTime(), CReader.this.pOUcharIDList, pOUcharTagNum, pListLen);
                        }

                        if (pOUcharTagNum[0] == 0) {
                            if (CReader.this.param.GetSession() > 1) {
                                CReader var10000 = CReader.this;
                                var10000.NoCardCOunt = var10000.NoCardCOunt + 1;
                                reTryTime = CReader.this.MaxAntennaNum;
                                if (CReader.this.NoCardCOunt > reTryTime) {
                                    Target = (byte)(1 - Target);
                                    CReader.this.NoCardCOunt = 0;
                                }
                            }
                        } else {
                            CReader.this.NoCardCOunt = 0;
                        }

                        try {
                            Thread.sleep(5L);
                        } catch (InterruptedException var15) {
                            var15.printStackTrace();
                        }
                    }

                    ++index;
                }

                CReader.this.mThread = null;
                if (CReader.this.callback != null) {
                    CReader.this.callback.StopReadCallback();
                }

            });
            this.mThread.start();
            return 0;
        } else {
            return 255;
        }
    }

    public void StopRead() {
        if (this.mThread != null) {
            this.mWorking = false;
        }

    }

    public int Inventory_G2(byte QValue, byte Session, byte AdrTID, byte LenTID, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen) {
        return this.reader.Inventory_G2(this.param.GetAddress(), QValue, Session, AdrTID, LenTID, Target, Ant, Scantime, pOUcharIDList, pOUcharTagNum, pListLen);
    }

    public int Inventory_Mix(byte QValue, byte Session, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte MaskFlag, byte ReadMem, byte[] ReadAdr, byte ReadLen, byte[] Pwd, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen) {
        return this.reader.Inventory_Mix(this.param.GetAddress(), QValue, Session, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, ReadMem, ReadAdr, ReadLen, Pwd, Target, Ant, Scantime, pOUcharIDList, pOUcharTagNum, pListLen);
    }
}

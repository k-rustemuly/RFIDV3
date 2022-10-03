package rfid;

public class ReaderParameter {
    private byte ComAddr = -1;
    private int TidPtr = 0;
    private int TidLen = 6;
    private int Session = 1;
    private int QValue = 4;
    private int ScanTime = 20;
    private int Antenna = 1;
    private int ReadType = 0;
    private int ReadMem = 3;
    private int ReadPtr = 0;
    private int ReadLength = 6;
    private String Password = "00000000";

    public ReaderParameter() {
    }

    public void SetAddress(byte ComAddr) {
        this.ComAddr = ComAddr;
    }

    public byte GetAddress() {
        return this.ComAddr;
    }

    public void SetTidPtr(int TidPtr) {
        this.TidPtr = TidPtr;
    }

    public int GetTidPtr() {
        return this.TidPtr;
    }

    public void SetTidLen(int TidLen) {
        this.TidLen = TidLen;
    }

    public int GetTidLen() {
        return this.TidLen;
    }

    public void SetSession(int Session) {
        this.Session = Session;
    }

    public int GetSession() {
        return this.Session;
    }

    public void SetQValue(int QValue) {
        this.QValue = QValue;
    }

    public int GetQValue() {
        return this.QValue;
    }

    public void SetScanTime(int ScanTime) {
        this.ScanTime = ScanTime;
    }

    public int GetScanTime() {
        return this.ScanTime;
    }

    public void SetAntenna(int Antenna) {
        this.Antenna = Antenna;
    }

    public int GetAntenna() {
        return this.Antenna;
    }

    public void SetReadType(int ReadType) {
        this.ReadType = ReadType;
    }

    public int GetReadType() {
        return this.ReadType;
    }

    public void SetReadMem(int ReadMem) {
        this.ReadMem = ReadMem;
    }

    public int GetReadMem() {
        return this.ReadMem;
    }

    public void SetReadPtr(int ReadPtr) {
        this.ReadPtr = ReadPtr;
    }

    public int GetReadPtr() {
        return this.ReadPtr;
    }

    public void SetReadLength(int ReadLength) {
        this.ReadLength = ReadLength;
    }

    public int GetReadLength() {
        return this.ReadLength;
    }

    public void SetPassword(String Password) {
        this.Password = Password;
    }

    public String GetPassword() {
        return this.Password;
    }
}

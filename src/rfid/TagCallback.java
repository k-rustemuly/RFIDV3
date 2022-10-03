package rfid;

public interface TagCallback {
    void tagCallback(ReadTag var1);

    void StopReadCallback();
}

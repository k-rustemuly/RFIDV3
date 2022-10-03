import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rfid.*;
public class Main {
    //static CReader reader = null;
    public static List<ReadTag> lsTagList = new ArrayList<ReadTag>();
    public static Map<String, Integer> dtIndexMap =new LinkedHashMap<String, Integer>();
    public static class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag arg0) {
            String epc = arg0.epcId.toUpperCase();
            String mem = arg0.memId.toUpperCase();
            int antId = arg0.antId;
            System.out.println(arg0.ipAddr+":"+epc+", Mem: "+mem+", Antenna: "+antId + "\n");
//            Integer findIndex = dtIndexMap.get(epc);
//            if (findIndex == null) {
//                dtIndexMap.put(epc,dtIndexMap.size());
//                lsTagList.add(arg0);
//            }
        }

        @Override
        public void StopReadCallback() {
            System.out.printf("Stop Read\n");
        }
    };
    public static void main(String[] args) {
        try
        {
            CReader reader = null;
            reader = new CReader("192.168.31.249",27011,4,1);
            int result = reader.Connect();
            if(result==0)
            {
                byte[]Version=new byte[2];
                byte[]Power=new byte[1];
                byte[]band=new byte[1];
                byte[]MaxFre=new byte[1];
                byte[]MinFre=new byte[1];
                byte[]BeepEn=new byte[1];
                int[]Ant=new int[1];
                result = reader.GetUHFInformation(Version, Power, band, MaxFre, MinFre, BeepEn, Ant);
                byte[]OutputPin=new byte[1];
                if(BeepEn[0]==0)
                    result = reader.SetBeepNotification(1);
                int rPower = 30|128;
                result = reader.SetRfPower(rPower);
                result = reader.SetAntenna(1,0x0F);
                int curband = 2;
                int curmaxfre = 49;
                int curminfre =0;
                result = reader.SetRegion(curband, curmaxfre, curminfre);
                ReaderParameter param = reader.GetInventoryParameter();
                param.SetSession(0);
                param.SetQValue(4);
                param.SetReadType(1);
                reader.SetInventoryParameter(param);
                MsgCallback callback = new MsgCallback();
                reader.SetCallBack(callback);
                reader.StartRead();
                Thread.sleep(5000);
                reader.StopRead();
                reader.DisConnect();
            }
        }
        catch (Exception e) {

        }
    }
}

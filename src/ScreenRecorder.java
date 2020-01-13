import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScreenRecorder {


    private static List<String> ADBProcessIDsBeforeScreenRecording = null;
    private static List<String> ADBProcessIDsAfterScreenRecording = null;


    public static void StartScreenRecording(String CurrentTestMethodName)
            throws IOException {
        ADBProcessIDsBeforeScreenRecording = getProcessIDs("adb.exe");
        Runtime.getRuntime().exec(

                "cmd /c adb shell screenrecord --bit-rate 1000000 //sdcard//"
                        + CurrentTestMethodName + ".mp4");
    }


    public static void StopScreenRecording(String CurrentTestMethodName,
                                           String DirectoryToSaveRecordedScreen,
                                           boolean RemoveRecordedScreenFromDevice) throws IOException,
            InterruptedException {

        ADBProcessIDsAfterScreenRecording = getProcessIDs("adb.exe");

        for (String id : ADBProcessIDsAfterScreenRecording) {
            boolean found = false;
            for (String tgtid : ADBProcessIDsBeforeScreenRecording) {
                if (tgtid.equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Runtime.getRuntime().exec("taskkill /F /PID " + id);
                break;
            }
        }
        // Выделенное время для сохранения ролика в памяти устройства
        Thread.sleep(2000);
        Runtime.getRuntime().exec(
                "cmd /c adb pull //sdcard//" + CurrentTestMethodName + ".mp4 "
                        + DirectoryToSaveRecordedScreen);

        //Передача ролика с устройства на ПК
        Thread.sleep(5000);
        //Процесс удаления ролика из памяти утсройства
        if (RemoveRecordedScreenFromDevice) {
            Runtime.getRuntime().exec(
                    "cmd /c adb shell rm //sdcard//" + CurrentTestMethodName
                            + ".mp4");
        }
    }
    static List<String> getProcessIDs(String processName) {
        List<String> processIDs = new ArrayList<String>();
        try {
            String line;
            Process p = Runtime.getRuntime().exec("tasklist /v /fo csv");
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (!line.trim().equals("")) {
                    String currentProcessName = line.split("\"")[1];
                    String currentPID = line.split("\"")[3];
                    if (currentProcessName.equalsIgnoreCase(processName)) {
                        processIDs.add(currentPID);
                    }
                }
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return processIDs;
    }
}
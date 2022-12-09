import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.time.*;

public class SMPP_PARSER {

    StringBuffer fileReader(String str) throws Exception {
        StringBuffer myBuffer = new StringBuffer();

        // reading data from the file.
        try (BufferedReader Br = new BufferedReader(new FileReader(str))) {
            while (true) {
                String line = Br.readLine();
                if (line == null) {
                    break;
                }
                myBuffer.append(line).append("\n");
            }

        } catch (Exception e) {

        }
        return myBuffer;
    }

    void fileCreator(String str) throws Exception {

        File file = new File("C:/Users/MY PC/Desktop/Assignment/summary.txt");

        // checking if the file exists
        if (file.exists()) {
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.write(str + "\n");
            } catch (Exception e) {

            }

        } else {
            file.createNewFile();
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.write(str + "\n");
            } catch (Exception e) {

            }

        }
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, Exception {

        SMPP_PARSER pp = new SMPP_PARSER();
        String blk = "";
        String blk2 = "";

        Map<String, String> sM, resp;

        Pattern pat1 = Pattern.compile("(deliver_sm\n|submit_sm\n)");
        Pattern pat2 = Pattern.compile("(deliver_sm_resp\n|submit_sm_resp\n)");

        Matcher mat;

        // maps to store date, time,sender, receiver, command_status of each packet.
        HashMap<String, String> myMap = new HashMap<>();
        Map<String, Map<String, String>> smMap = new HashMap<String, Map<String, String>>();
        Map<String, Map<String, String>> respMap = new HashMap<String, Map<String, String>>();

        // Lists to hold the data that has been retrieved
        List<LocalDate> dates = new ArrayList<LocalDate>();
        List<LocalTime> time = new ArrayList<LocalTime>();
        List<String> source = new ArrayList<>();
        List<String> destination = new ArrayList<>();
        List<String> status = new ArrayList<>();

        // reading data from the file.
        String name = "C:/Users/MY PC/Desktop/Assignment/bearerbox_server.log"; // file to be read
        StringBuffer buf = pp.fileReader(name);

        String[] blocks = buf.toString().split(".+(PDU).+"); // splitting the data into constituent PDU blocks

        for (String block : blocks) {

            block = block.trim();

            // System.out.println(block);
            mat = pat1.matcher(block);

            if (mat.find()) {
                // getting blocks that only have submit_sm or deliver_sm operations.
                blk = block.replaceAll("=.+", "");
                Map<String, String> inner = new HashMap<>();
                String[] str = blk.split("\n");

                for (String i : str) {
                    String[] str2 = i.split("DEBUG:", 2);
                    String smData = str2[1].trim();

                    String[] data = smData.split(":");
                    if (data.length == 2) {
                        inner.put(data[0].trim(), data[1]);
                    }

                }
                if (smMap.containsKey(inner.get("sequence_number")) == false) {
                    smMap.put(inner.get("sequence_number"), inner);

                }

            } else {
                mat = pat2.matcher(block);
                if (mat.find()) {
                    // getting blocks that only have responses to the submit_sm and deliver_sm
                    // operations
                    Map<String, String> inner2 = new HashMap<>();
                    blk2 = block.replaceAll("=.+", "");
                    String[] str = blk2.split("\n");

                    for (String i : str) {
                        String[] str2 = i.split("DEBUG:", 2);
                        String smData = str2[1].trim();
                        String DT = str2[0].trim(); // date and time

                        String[] DT1 = DT.split(" ", 4);
                        String date = DT1[0];
                        String tme = DT1[1];

                        // Add date and time to the map
                        if (inner2.containsValue(date) == false) {
                            if (inner2.containsValue(tme) == false) {
                                inner2.put("date", date);
                                inner2.put("time", tme);
                            }

                        }

                        String[] data = smData.split(":");
                        if (data.length == 2) {
                            // System.out.println(data[0] );
                            inner2.put(data[0].trim(), data[1]);
                        }

                    }
                    if (respMap.containsKey(inner2.get("sequence_number")) == false) {
                        respMap.put(inner2.get("sequence_number"), inner2);
                    }
                }
            }

        }

        // getting sequence numbers
        Set<String> keys = new HashSet<>();
        Set<Map.Entry<String, Map<String, String>>> set = smMap.entrySet();
        for (Map.Entry<String, Map<String, String>> me : set) {
            keys.add(me.getKey());
        }

        // adding values to lists.
        for (String i : keys) {
            sM = smMap.get(i);
            resp = respMap.get(i);

            if ((resp != null) && (sM != null)) {
                source.add(sM.get("source_addr"));
                destination.add(sM.get("destination_addr"));
                dates.add(LocalDate.parse(resp.get("date")));
                time.add(LocalTime.parse(resp.get("time")));
                status.add(resp.get("command_status"));
            } else {
                System.out.println(i);
            }

        }

        // writing data to a file

        for (int i = 0; i < dates.size(); i++) {
           // String Data = dates.get(i).toString() + "  " + time.get(i).toString() + "  " + source.get(i) + "  "
             //       + destination.get(i) + "  " + status.get(i);
        
             // choose this format because the data looks better this way.
            String D1 = "Date: " +dates.get(i).toString();// date
            String D2 = "Time: " + time.get(i).toString(); //time
            String D3 = "Source: " + source.get(i);
            String D4 = "Destination: " + destination.get(i);
            String D5 = "Status: " + status.get(i);
            
            pp.fileCreator(D1);
            pp.fileCreator(D2);
            pp.fileCreator(D3);
            pp.fileCreator(D4);
            pp.fileCreator(D5);
            pp.fileCreator("\n");
        }

    }
}

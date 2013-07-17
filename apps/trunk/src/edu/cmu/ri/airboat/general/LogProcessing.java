/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author pscerri
 */
public class LogProcessing {

    static ArrayList<Hashtable<String, ArrayList<Double>>> data = new ArrayList<Hashtable<String, ArrayList<Double>>>();

    /**
     * Looks for files like this:
     * 
     *  1217797 21:04:30,809 POSE: {586707.5962413325, 4480047.232928311, 181.0, Q[0.0,0.0,2.6847525973656334]} @ 17North 
    1217814 21:04:30,826 TE: [s, 67.06, 0.27, 1.20] 
    1217998 21:04:31,10 POSE: {586707.5962413325, 4480047.232928311, 180.0, Q[0.0,0.0,1.6542201414976476]} @ 17North
     * 
     * created running something like "egrep 'POSE|TE' airboat_20120115_034412.txt | grep -v "ACCELE"" on phone log
     * 
     * @param f Name of file
     * @param sensorNames strings to use to look for sensors
     */
    static void processSensorLog(File f, String[] sensorNames) {
        BufferedReader br = new BufferedReader(getFileReader(f));

        double easting = 0.0;
        double northing = 0.0;

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, " ,{}");
                if (line.contains("POSE")) {
                    // System.out.println("Got pose: " + line);
                    tok.nextToken(); // ?
                    String time = tok.nextToken();
                    tok.nextToken(); // ?
                    tok.nextToken(); // POSE
                    easting = Double.parseDouble(tok.nextToken());
                    northing = Double.parseDouble(tok.nextToken());

                    // System.out.println("Pose @ " + time + " " + easting + " " + northing);

                } else {

                    for (String s : sensorNames) {
                        if (line.contains(s)) {
                            // System.out.println("Got sensor: " + s);
                            while (!tok.nextToken().contains("[") && tok.hasMoreTokens()) {
                            }
                            int index = 0;
                            String hash = (int) (easting / 10) + ":" + (int) (northing / 10);
                            if (tok.hasMoreTokens()) {
                                String v = null;
                                System.out.print(/*s + "\t" +*/easting + "," + northing + ",");
                                do {
                                    v = tok.nextToken();
                                    double val = v.contains("]") ? Double.parseDouble(v.substring(0, v.length() - 1)) : Double.parseDouble(v);

                                    // System.out.println("value: " + v + " -> " + val);
                                    System.out.print(val + ",");

                                    Hashtable<String, ArrayList<Double>> h = null;
                                    try {
                                        h = data.get(index);
                                    } catch (IndexOutOfBoundsException e) {
                                        h = new Hashtable<String, ArrayList<Double>>();
                                        data.add(h);
                                    }
                                    ArrayList<Double> l = h.get(hash);
                                    if (l == null) {
                                        l = new ArrayList<Double>();
                                        h.put(hash, l);
                                    }
                                    l.add(val);

                                } while (!v.contains("]"));
                                System.out.println("");
                            } else {
                                System.out.println("Failed for " + line);
                            }
                        }
                    }
                }
            }

            System.out.println("\n\n\nSummary:");
            for (Hashtable<String, ArrayList<Double>> h : data) {
                for (String string : h.keySet()) {
                    System.out.print("Location: " + string);
                    System.out.println("\tData: " + h.get(string));
                }
            }

        } catch (Exception e) {
            System.out.println("Reading failed: " + e);
            e.printStackTrace();
        }
    }

    static FileReader getFileReader(File f) {
        try {
            FileReader fr = new FileReader(f);
            return fr;
        } catch (Exception e) {
            System.out.println("Failed to open file: " + e);
            System.exit(-1);
        }
        return null;
    }

    static public void main(String[] args) {
        //processSensorLog(new File("/Users/pscerri/Documents/Projects/Airboats/Data/Allegheny/boat_logs/day1/15.sum"), new String[]{"TE", "DEPTH", "DO"});
        // processSensorLog(new File("/Users/pscerri/Documents/Projects/Airboats/Data/Allegheny/boat_logs/day1/15.sum"), new String[]{"DEPTH"});
        processSensorLog(new File("//Users/pscerri/Documents/Projects/Airboats/Data/Fish/phone_logs/boat1/20.sum"), new String[]{"DO"});

    }
}

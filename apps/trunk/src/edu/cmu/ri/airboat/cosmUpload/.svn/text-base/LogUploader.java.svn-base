package edu.cmu.ri.airboat.cosmUpload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import edu.cmu.ri.airboat.cosmUpload.cosm.Cosm;
import edu.cmu.ri.airboat.cosmUpload.cosm.CosmException;
import edu.cmu.ri.airboat.cosmUpload.cosm.Data;
import edu.cmu.ri.airboat.cosmUpload.cosm.Feed;


public class LogUploader {
	public static void main(String args[]) throws CosmException, FileNotFoundException
	{
		Cosm p = new Cosm("R5USeoIXogxwJxP3L9wZjJo6imySAKw0VGtpZExrNHJyQT0g");
		Feed currentFeed = null;

		Scanner scan = new Scanner(System.in);

		HashMap<String, String> allFeeds = p.getAllFeeds("crwcmu");
		HashMap<Integer, String> index = new HashMap<Integer, String>();
		int currentIndex = 1;

		System.out.println("The following feeds currently exist on COSM:");

		for(String feedName : allFeeds.keySet())
		{
			System.out.println(currentIndex + ": " + feedName);
			index.put(currentIndex, feedName);
			currentIndex++;
		}

		System.out.print("\nUpdate Existing Feed(Y/N): ");
		String input = scan.nextLine();
		String feedID;
		if(input.equals("Y") || input.equals("y"))
		{
			System.out.print("Which feed would you like to update(Enter index number)? ");
			feedID = allFeeds.get(index.get(scan.nextInt()));
			currentFeed = p.getFeed(feedID);
		}
		else
		{
			String title, description, dataStreamName, dataStreamtags;
			System.out.println("A new feed will be created after you enter the following details:");
			System.out.print("Title: ");
			title = scan.nextLine();
			System.out.print("Description: ");
			description = scan.nextLine();

			Feed offlineCache = new Feed();
			offlineCache.setDescription(description);
			offlineCache.setTitle(title);

			currentFeed = p.createFeed(offlineCache);
			feedID = currentFeed.getId();

			System.out.println("Creating Standard datastreams(X, Y, Temperature, Conductivity).");
			currentFeed.createDatastream(new Data("temperature", "crw, cmu, temperature, ri, airboats", 0.0, 0.0, 0.0));
			currentFeed.createDatastream(new Data("conductivity", "crw, cmu, conductivity, ri, airboats", 0.0, 0.0, 0.0));
			currentFeed.createDatastream(new Data("x", "crw, cmu, location, ri, airboats", 0.0, 0.0, 0.0));
			currentFeed.createDatastream(new Data("y", "crw, cmu, location, ri, airboats", 0.0, 0.0, 0.0));
		}

		int totalReadings = 0;
		ArrayList<String> logFiles = new ArrayList<String>();

		System.out.println("\nTime to upload data from the log files!");
		logFiles.add(scan.nextLine());

		while(!logFiles.get(logFiles.size()-1).equals("END"))
		{
			System.out.print("Enter location of next log file(or END): ");
			logFiles.add(scan.nextLine());
		}

		for(int i=1; i<logFiles.size()-1; i++)
		{
			System.out.println("Reading " + logFiles.get(i) +"...");
			File file = new File(logFiles.get(i));
			Scanner logFile = new Scanner(file);

			double x, y, t, c;
			String tempX, tempY, tempT, tempC;
			String timeStamp;

			ArrayList<Double> xValues = new ArrayList<Double>();
			ArrayList<Double> yValues = new ArrayList<Double>();
			ArrayList<Double> tValues = new ArrayList<Double>();
			ArrayList<Double> cValues = new ArrayList<Double>();
			ArrayList<String> timeStamps = new ArrayList<String>();

			x = y = t = c = 0.0;

			///home/fahim/Desktop/RISS/Logs/MoonPond/day1/speedTestLog/airboat_20110804_051948.txt
			//Assuming the log file is named in the format 'airboat_20110804.txt'
			String date = logFiles.get(i).substring(logFiles.get(i).length()-19, logFiles.get(i).length()-11);
			date = date.substring(0, 4) + "-" + date.substring(4,6) + "-" + date.substring(6) + "T";
			
			int t1, t2, t3;

			while(logFile.hasNextLine())
			{
				String currentLine = logFile.nextLine();

				if(currentLine.contains("POSE:"))
				{
					t1 = currentLine.indexOf('[')+1;
					t2 = currentLine.indexOf(',', t1);
					t3 = currentLine.indexOf(',', t2+1);

					tempX = currentLine.substring(t1, t2);
					x = Double.parseDouble(tempX);
					tempY = currentLine.substring(t2+1, t3);
					y = Double.parseDouble(tempY);
					//System.out.println(x + "," + y);
				}
				else
					if(currentLine.contains("TE:"))
					{
						t1 = currentLine.indexOf('[')+4;
						t2 = currentLine.indexOf(',', t1);
						t3 = currentLine.indexOf(',', t2+1);

						tempC = currentLine.substring(t1, t2);
						c = Double.parseDouble(tempC);

						t2 = currentLine.indexOf(',', t3+1);

						tempT = currentLine.substring(t3+1, t2);
						t = Double.parseDouble(tempT);

						t1 = currentLine.indexOf(' ');
						t2 = currentLine.indexOf(',');

						timeStamp = currentLine.substring(t1+1, t2);
						
						timeStamp = date + timeStamp;

						System.out.println(timeStamp + " " + x + " , " + y + " , " + c + " , " + t);
						xValues.add(x);
						yValues.add(y);
						tValues.add(t);
						cValues.add(c);
						timeStamps.add(timeStamp);
						totalReadings++;

						if(xValues.size() == 500)
						{
							currentFeed.updateDatastream("x", xValues, timeStamps);
							currentFeed.updateDatastream("y", yValues, timeStamps);
							currentFeed.updateDatastream("conductivity", cValues, timeStamps);
							currentFeed.updateDatastream("temperature", tValues, timeStamps);

							xValues.clear();
							yValues.clear();
							cValues.clear();
							tValues.clear();
							timeStamps.clear();
						}

					}
			}

			if(xValues.size() > 0)
			{
				currentFeed.updateDatastream("x", xValues, timeStamps);
				currentFeed.updateDatastream("y", yValues, timeStamps);
				currentFeed.updateDatastream("conductivity", cValues, timeStamps);
				currentFeed.updateDatastream("temperature", tValues, timeStamps);

				xValues.clear();
				yValues.clear();
				cValues.clear();
				tValues.clear();
				timeStamps.clear();
			}
			
			logFile.close();
		}

		System.out.println("Total Number of Readings : " + totalReadings);
		/*Feed f = p.getFeed(Integer.parseInt(currentFeed.getId()));

        Double[] result = f.getDatastreamHistory("x");
        for(int i=0; i<result.length; i++)
        	System.out.print(result[i] + "\n");*/
	}
}

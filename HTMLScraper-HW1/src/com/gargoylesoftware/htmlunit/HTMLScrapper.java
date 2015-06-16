package com.gargoylesoftware.htmlunit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlItalic;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.google.gson.Gson;

class ArtWork
{
	String title = new String("Unknown");
	String imageUrl = new String("Unknown");
	String artist = new String("Unknown");
	String dateOfWork = new String("Unknown");
	String classification = new String("Unknown");
	String medium = new String("Unknown");
	String dimensions = new String("Unknown");
	String owner = new String("Unknown");
	String aquisitionDate = new String("Unknown");
	String creditLine = new String("Unknown");
}
public class HTMLScrapper {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException 
	{
		WebClient webClient = new WebClient();
		
		//Retrieving home page and the Portrait search anchor tag
		HtmlPage homePage = webClient.getPage("http://npgportraits.si.edu/emuseumCAP/code/emuseum.asp");
		HtmlAnchor portraitAnchor = homePage.getAnchorByText("Portrait Search");
		
		//Retrieving the second page
		HtmlPage portraitPage = portraitAnchor.click();
		
		//Retrieving the form, form's check box (a check box which - for image only results)
		HtmlForm imageSearchForm = portraitPage.getFormByName("searchForm");
		HtmlInput searchButton = imageSearchForm.getInputByName("imgquicksearch");
		
		//Checking the "find only with images" checkbox
		HtmlCheckBoxInput imageOnlyCheckBox = imageSearchForm.getInputByName("hasImage");
		imageOnlyCheckBox.setChecked(true);
		
		//Retrieving the result page
		HtmlPage resultPage = searchButton.click();		
		HtmlAnchor firstTitle = resultPage.getAnchorByHref("javascript:singleview('1');");
		
		//Retrieving the page of the first title
		HtmlPage firstImagePage = firstTitle.click();
		
		//Array List of art works
		ArrayList<ArtWork> artWorks = new ArrayList<ArtWork>();
		for(int i=0;i<300;i++)
		{
			//Creating a temp art work object
			ArtWork temp = new ArtWork();
			
			//Extracting the title
			HtmlItalic artTitle = (HtmlItalic) firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/b/i");
			
			//Extracting the imageURL
			String imageUrl = new String();
			//The image in the website may have two xpaths. If and else if for these 2 xpaths
			if((null != firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[3]/center/a[1]/img")))
			{
				HtmlImage image = (HtmlImage)firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[3]/center/a[1]/img");
				imageUrl = image.getSrcAttribute();				
			}
			else if((null != firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[3]/center/img")))
			{
				HtmlImage image = (HtmlImage)firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[3]/center/img");
				imageUrl = image.getSrcAttribute();	
			}
			
			//Extracting the Artist name
			String artist = new String();
			if(null!=firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/a[2]"))
			{
				HtmlAnchor artistAnchor = firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/a[2]");
				artist = artistAnchor.asText();
			}
			
			//The table has 2 Xpaths. If and else condition for both of these xpaths.
			//The table contains various fields like acquisition date, classification, date of work, medium, owner, credit line 
			if(null!=firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/table"))
			{
				HtmlTable detailsTable = (HtmlTable) firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/table");
				java.util.List<HtmlTableRow> tableRows = detailsTable.getRows();
				for(HtmlTableRow tableRow:tableRows)
				{
					//AddDescription function adds all the above fields to the object
					temp = addDescription(tableRow,temp);
				}
			}
			else if(null!=firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/table[2]"))
			{
				HtmlTable detailsTable = (HtmlTable) firstImagePage.getFirstByXPath("/html/body/table/tbody/tr[2]/td[2]/table[2]/tbody/tr/td[2]/table[3]/tbody/tr/td/table[1]/tbody/tr/td[1]/table[2]");
				java.util.List<HtmlTableRow> tableRows = detailsTable.getRows();
				for(HtmlTableRow tableRow:tableRows)
				{
					//AddDescription function adds all the above fields to the object
					temp = addDescription(tableRow,temp);
				}
			}
			
			//Adding title, imageURL and artist already collected above
			temp.title = artTitle.asText();
			temp.imageUrl = imageUrl;
			temp.artist = artist;
			
			
			//Adding the temp object to the arraylist of art works
			artWorks.add(temp);
			HtmlAnchor next = firstImagePage.getAnchorByText("next >");
			firstImagePage = next.click();
		}
		
		//Creating the JSON using GSON
		Gson gson = new Gson();
		String json = gson.toJson(artWorks);
		
		//Writing the JSON to an output file
		File outputFile = new File("HtmlScraperJSON.txt");
		FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(json);
		bw.close();
	}

	private static ArtWork addDescription(HtmlTableRow tableRow, ArtWork temp) 
	{
		//Each row of the table is received here. Split the table row with ":". The first string contains the heading like 'Date of Work', 'title', etc and the content string contains the actual data
		String rowData = tableRow.asText();
		if(rowData.contains(":"))
		{
			String[] rowDataAsArray = rowData.split(":");
			String rowHeading = rowDataAsArray[0];
			String rowContent = rowDataAsArray[1];
			
			//Switch case the heading and add the corresponding content into the respective fields of the object.
			switch(rowHeading)
			{
			case "Date of Work":
				if(rowContent!="")
					temp.dateOfWork = rowContent.trim();
				break;
			case "Classification":
				if(rowContent!="")
					temp.classification = rowContent.trim();
				break;
			case "Medium":
				if(rowContent!="")
					temp.medium = rowContent.trim();
				break;
			case "Dimensions":
				if(rowContent!="")
					temp.dimensions = rowContent.trim();
				break;
			case "Owner":
				if(rowContent!="")
					temp.owner = rowContent.trim();
				break;
			case "Acquisition Date":
				if(rowContent!="")
					temp.aquisitionDate = rowContent.trim();
				break;
			case "Credit Line":
				if(rowContent!="")
					temp.creditLine = rowContent.trim();
				break;
			}
		}
		return temp;
	}

}

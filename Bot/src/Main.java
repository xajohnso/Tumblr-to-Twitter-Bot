import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GenericTumblrToTwitterBot {

	
	protected static ArrayList<String> theme = new ArrayList<String>();
	protected static HashSet<String> account = new HashSet<String>();
	protected static ArrayList<String> query = new ArrayList<String>();
	
	public static void main(String[] args) throws InterruptedException, AWTException {
		// TODO Auto-generated method stub
		
		start();
		
		
	}
	
	
	//The name of the tables accessed through JDBC will be used to start the program
	public static void start() {
		String queryDatabase = " "; // ie. twitter_bot.query (holds the links to photos not yet scheduled on twiiter)
		String accountDatabase = " "; // ie. twitter_bot.accounts (the accounts you'll take photos from on tumblr )
		String postDatabase = " "; // ie. twitter_bot.posts (table of posts already used)
		
		try {
			phaseOne(accountDatabase, queryDatabase, postDatabase);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	public static void phaseOne(String accountDatabase, String queryDatabase, String postDatabase) throws AWTException {
		
		String statement = null;
		//Check if query is empty in database
		int queryAmount = 0;
		
		
		try {
			Connection cn = DriverManager.getConnection("url", "user", "password");
			// Replace queryDatabase with actual name/location of table you've made
			statement = "select count(*) from queryDatabase"; 
			
			Statement st = cn.createStatement();
			ResultSet resultSet = st.executeQuery(statement);
			
			while (resultSet.next()) { queryAmount = resultSet.getInt(1); }
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//If query is not empty the program will finish the links still in the query instead of finding new photos
		if (queryAmount == 0) {
			databasePrep(accountDatabase);
			startTumblrScrap(accountDatabase, queryDatabase, postDatabase);
			addToQueryCollection(queryDatabase);
			sIT(queryDatabase, postDatabase, accountDatabase);
		} else {
			addToQueryCollection(queryDatabase);
			sIT(queryDatabase, postDatabase, accountDatabase);
		}
	}
	
	//Gets a list of random account links based on themes chosen
	public static void databasePrep(String accountDatabase) {
		
		/*
		 * Depending on the number of themes you have in your account database, 
		 * you can randomize which to pick and the number of times to pull a theme from the table
		 * to add to
		*/
		do {
			int themeNumber = (int) (Math.random() * 10);
			switch (themeNumber) {
			
			default:
				theme.add("theme");
			break;
			
			}
		} while (theme.size() < 10 );

		
		//Picks random account assigned to theme 
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			String statement = "select * from twitter_bot.accounts"
					+ " where theme = ?"
					+ " order by RAND()"
					+ " limit 1";
			PreparedStatement ps = cn.prepareStatement(statement);
			
			
			
			for (int x = 0; x < theme.size(); x++) {
				ps.setString(1, theme.get(x));
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					account.add(rs.getString("username"));
				}
				
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	
	public static void startTumblrScrap(String accountDatabase, String queryDatabase, String postDatabase) { 
		account.forEach((v) -> {
			try {
				chooseAccount(v, accountDatabase, queryDatabase, postDatabase);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	public static void chooseAccount(String accountName, String accountDatabase, String queryDatabase, String postDatabase) throws InterruptedException {
		String statement = null;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			statement = "select link from twitter_bot.accounts"
					+ " where username = ?";
	
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, accountName);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				browseAccount(accountName, rs.getString("link"), queryDatabase, postDatabase, accountDatabase);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 
	 * Used to browse tumblr accounts with the selenium webdriver. Errors can occur due to the periodical class name
	 * change and if the language of the blog differs from english. 
	 * 
	 */
	public static void browseAccount(String accountName, String link, String queryDatabase, String postDatabase, String accountDatabase) throws InterruptedException {
		System.setProperty("type of driver/browser", "location of driver");
		WebDriver driver = new FirefoxDriver();
		
		driver.get(link);
		driver.manage().window().maximize();
		int count = 0;
		
		Thread.sleep(2000);
		
		do {
		ArrayList<WebElement> elements = new ArrayList<WebElement>();
		elements.addAll(driver.findElements(By.className("NGc5k")));
		
		int size = elements.size();
			
		for (int x = 0; x < size; x++) {
			
			String notes = elements.get(x).findElement(By.className("AdXnC")).getAttribute("innerText"); 
			String postLink = elements.get(x).findElement(By.className("oKaff")).getAttribute("href");
			String sourceSet = null;
			
			//If there is no image to copy, then the program skips the post
			try { sourceSet = elements.get(x).findElement(By.className("nLowv")).getAttribute("srcset"); } 
			catch (NoSuchElementException e) { System.out.println("No source"); }
			
			if (sourceSet != null) {
			
			
			if (choosingPost(accountName, notes, postLink, queryDatabase, postDatabase, accountDatabase) == true) {
				
				try {
					setQuery(sourceSet, accountName, postLink, queryDatabase, postDatabase); count++; System.out.println("Count :" + count); driver.close();
					break;
				} catch (IOException e1) { e1.printStackTrace(); }	
			}
			
			} else {}
			
			// Scrolls the element into view if a post is not picked. Solution for endless scroll dilemma 
			if (x < (size - 1)) { ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", elements.get(x)); }
		
			Thread.sleep(1000);
		}
		
		
		
		} while (count < 1);
	} 
	
	
	// Determines if the post notes is higher than the limit in database 
	public static Boolean choosingPost(String accountName, String notes, String link, String queryDatabase, String postDatabase, String accountDatabase) {
	String statement = null;
	
	boolean shouldPost = false;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
				statement = "select * from twitter_bot.accounts "
					+ "where username = ?";
			

			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, accountName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int noteLimit = rs.getInt("note_count_limit");	
				
				if (notesCheck(notes, noteLimit, accountName) == true) {
					
					if (postCheck(link, queryDatabase, postDatabase) == true) { shouldPost = true; } 
				} 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		

	
	return shouldPost;
	}
	
	//Isolates the number of likes/reblogs from the String extracted
	public static Boolean notesCheck(String notes, int noteLimit, String accountName) {
		
		String sp = null;
	
		sp = notes.substring(13);
			
		int amountOfNotes = stringToInt(sp);
		
		boolean isMore = amountOfNotes > noteLimit;
		
		return isMore;
	}
	
	
	public static int stringToInt(String noteString) {
		int noteAmount = 0;
		String finString = null;
		String[] newStrings = noteString.split(" ");
		String one = newStrings[0];
		
		if (one.contains(",")) {
			String[] an = one.split(",");
			for (int x = 0; x < an.length; x++) {
				if (finString == null) {
					finString = an[x];
				} else {
				finString = finString.concat(an[x]);
				noteAmount = Integer.parseInt(finString);
				
				}
			}
		}  else { noteAmount = Integer.parseInt(one); }
		
		return noteAmount;
	}	
	
	//Checks if post has already been posted or is already queued to be posted
	public static Boolean postCheck(String link, String queryDatabase, String postDatabase) {
		String linkExist = null;
		Boolean pass = false;
		Boolean check1 = false;
		Boolean check2 = false;
		String statement = null;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			
			 statement = "select * from twitter_bot.posts "
					+ "where post_link = ?";
			
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, link);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				linkExist = rs.getString("post_link");
			}
			
			if (linkExist == null) {
				System.out.println("Post link null"); check1 = true;
			} else {
				System.out.println("Post link not null");
				
			}
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
		
				 statement = "select * from twitter_bot.query "
						+ "where post_link = ?";
			
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, link);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				linkExist = rs.getString("post_link");
			}
			
			if (linkExist == null) {
				System.out.println("QQ null"); check2 = true;
			} else {
				System.out.println("QQ not null");
				
			}
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (check1 == true && check2 == true) {	pass = true; } 
		
		
		return pass;
		
	}
	
	
	public static void setQuery(String sourceSet, String accountName, String dbLink, String queryDatabase, String postDatabase) throws IOException {
		
		String[] sa = sourceSet.split(" ");
		String newLink = null;
		
		int point = sa.length - 2; //18 length
		
		if (sa[point].contains("gifv") == true) {
			newLink = sa[point].substring(0, sa[point].length() - 1);
		} else {
			newLink = sa[point];
		}
		
		addToQuery(newLink, accountName, dbLink, queryDatabase, postDatabase);
		
	}
	
	
	public static void addToQuery(String photoLink, String account, String postLink, String queryDatabase, String postDatabase) {
		
		int qe = queryEmpty(queryDatabase);
		
		//Returns the last date in the database
		LocalDate chosenDate = postBatch(qe, queryDatabase, postDatabase);
		
		// Returns the date and time a post is assigned
		String daAnTi = timePost(chosenDate, queryDatabase, postDatabase);
		
		//Seperates date and time [0] - date [1] - time
		String[] nd = daAnTi.split("--");
		
		Date ue = Date.valueOf(nd[0]);
		
		String statement = null;
		
		try {
			Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tumblr_bot", "root", "Gb72vsKK1fX@a528Tc");
			
			
			statement = "insert into twitter_bot.query "
					+ "values (?, ?, ?, ?, ?)";
		
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, photoLink);
			ps.setString(2, postLink);
			ps.setString(3, account);
			ps.setDate(4, ue);
			ps.setString(5, nd[1]);
			ps.executeUpdate();
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//gifQuery();
	}
	
	public static int queryEmpty(String queryDatabase) {
		int f = 2;
		String statement = null;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			statement = "SELECT EXISTS(SELECT 1 FROM  twitter_bot.query)"; 
			
			PreparedStatement ps = cn.prepareStatement(statement);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				f = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return f;
	}
	
	public static LocalDate postBatch(int qe, String queryDatabase, String postDatabase) {
		
		LocalDate lastDate = LocalDate.now().plusDays(1);
		ArrayList<String> dates = new ArrayList<String>();
		String statement = null;
		
		if (qe == 1) {
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			statement = "select date from twitter_bot.query"; 
		
			
			Statement md = cn.createStatement();
			ResultSet rs = md.executeQuery(statement);
			
			while (rs.next()) {
				dates.add(rs.getString("date"));
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		} else {
			
			try {
				Connection cn = DriverManager.getConnection("url", "username", "password");
				
				statement = "select date from twitter_bot.posts"; 
				
				Statement md = cn.createStatement();
				ResultSet rs = md.executeQuery(statement);
				
				while (rs.next()) {
					dates.add(rs.getString("date"));
				}
				
				cn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		for (int x = 0; x < dates.size(); x++) {
		LocalDate date1 = LocalDate.parse(dates.get(x));
		
		if (date1.isAfter(lastDate) == true) {lastDate = date1;}
		}
		
		
		return lastDate;
	}
	
	public static String timePost(LocalDate date, String queryDatabase, String postDatabase) {
		
		// Key == time, Value == slot taken (0 or 1)
		HashMap<String, Integer> times = new HashMap<String, Integer>();
		
		//Post times 
		String[] time = {"08:00:00", "08:15:00", "08:30:00", "08:45:00", "12:00:00", "12:15:00", "12:30:00", "12:45:00", "15:00:00", "15:15:00", "15:30:00", "15:45:00"};
		
		
		//Used to hold times not yet assigned to post
		LinkedList<LocalTime> lt = new LinkedList<LocalTime>();
		
		
		lt.add(LocalTime.of(20, 0));
		
		//Edge case
		LocalTime nase = LocalTime.of(16, 00);
		
		// Latest date in the database
		Date nde = Date.valueOf(date);
		
		//String that returns the date and time a post is assigned
		String fi = null;
		
		for (String dm : time) {times.put(dm, 0);}
		
		String statement = null;
		
		
		//Gets all times for specified date and changes HashMap(Times) key value from 0 to 1
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			statement = "select * from twitter_bot.posts " + "where date = ?"; 
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setDate(1, nde);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				times.replace(rs.getTime("post_time").toString(), 1);
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			statement = "select * from twitter_bot.query " + "where date = ?"; 
			
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setDate(1, nde);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				times.replace(rs.getTime("post_time").toString(), 1);
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//If values are 0 (time not assigned to a post), then add to LinkedList
		for (String ne : times.keySet()) {
			if (times.get(ne) == 0) {
				lt.add(LocalTime.parse(ne));
			}
		}
		
		//Finds the earliest time not yet assigned to a post
		for (LocalTime je : lt) {
			if (je.isBefore(nase)) {
				nase = je;
			}
		}
		
		//combines the date with the time
		switch(nase.toString()) {
		
		case "16:00":
			fi = date.plusDays(1).toString() + "--" + "08:00";
		break;
		
		default:
			fi = date.toString() + "--" + nase.toString();
		break;
		}
		
		return fi;
	}
	
	
	public static void addToQueryCollection(String queryDatabase) {
		
		String statement = null;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			statement = "select * from twitter_bot.query"; 
			
			Statement st = cn.createStatement();
			ResultSet rs = st.executeQuery(statement);
			while (rs.next()) {
				query.add(rs.getString("photo_link"));
				
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void sIT(String queryDatabase, String postDatabase, String accountDatabase) throws AWTException {
		String username = null;
		String password = null;
		
		
		System.setProperty("browser/driver", "location");
		WebDriver driver = new FirefoxDriver();
		WebDriverWait WebDriverWait = new WebDriverWait(driver, 20);
		
		driver.get("https://twitter.com/login");
		driver.manage().window().maximize();
		WebDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@autocomplete='username']")));
		
		driver.findElement(By.xpath("//*[@autocomplete='username']")).sendKeys(username);
		driver.findElement(By.xpath("/html/body/div/div/div/div[1]/div/div/div/div/div/div/div[2]/div[2]/div/div/div[2]/div[2]/div[1]/div/div[6]/div")).click();
		
		WebDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div/div/div/div[1]/div/div/div/div/div/div/div[2]/div[2]/div/div/div[2]/div[2]/div[1]/div/div[3]/div/label/div/div[2]/div[1]/input")));
		driver.findElement(By.xpath("/html/body/div/div/div/div[1]/div/div/div/div/div/div/div[2]/div[2]/div/div/div[2]/div[2]/div[1]/div/div[3]/div/label/div/div[2]/div[1]/input")).sendKeys(password);
		
		driver.findElement(By.xpath("/html/body/div/div/div/div[1]/div/div/div/div/div/div/div[2]/div[2]/div/div/div[2]/div[2]/div[2]/div/div/div")).click();
		
		
		query.forEach((v) -> {
			try {
				queryPost(v, driver, WebDriverWait, queryDatabase, postDatabase, accountDatabase);
			} catch (AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
	
	public static void queryPost(String link, WebDriver driver, WebDriverWait wdw, String queryDatabase, String postDatabase, String accountDatabase) throws AWTException {
	
		wdw.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div/div/div[2]/main/div/div/div/div[1]/div/div[2]/div/div[2]/div[1]/div/div/div/div[2]/div[3]/div/div/div[1]/div[1]/div")));
		WebElement e = driver.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/main/div/div/div/div[1]/div/div[2]/div/div[2]/div[1]/div/div/div/div[2]/div[3]/div/div/div[1]/div[1]/div"));
		
		e.click();
		

		
		  Robot robot = new Robot();
		  
		  robot.setAutoDelay(2000); StringSelection ss = new StringSelection(link);
		  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		  
		  robot.setAutoDelay(3000);
		  
		  robot.keyPress(KeyEvent.VK_CONTROL); robot.keyPress(KeyEvent.VK_V);
		  
		  robot.keyRelease(KeyEvent.VK_CONTROL); robot.keyRelease(KeyEvent.VK_V);
		  
		  robot.keyPress(KeyEvent.VK_ENTER);
		  robot.keyRelease(KeyEvent.VK_ENTER);
		 
		
		driver.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/main/div/div/div/div[1]/div/div[2]/div/div[2]/div[1]/div/div/div/div[2]/div[3]/div/div/div[1]/div[5]/div")).click();
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		//Getting the time and date needed for scheduled post
		// Navigating scheduled post options
		gettingDnT(driver, link, queryDatabase);
		driver.findElement(By.xpath("/html/body/div[1]/div/div/div[1]/div[2]/div/div/div/div/div/div[2]/div[2]/div/div[1]/div/div[1]/div/div/div/div/div/div[3]/div/div/div/span/span")).click();
		driver.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/main/div/div/div/div[1]/div/div[2]/div/div[2]/div[1]/div/div/div/div[2]/div[4]/div/div/div[2]/div[4]/div/span/span")).click();
		//Post
		String an = getAccountName(link, queryDatabase);
		//Adjust database
		updatePost(link, postDatabase);
		deleteQuery(link, queryDatabase);
		updateAccounts(an, accountDatabase);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		driver.navigate().refresh();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
	
	public static void gettingDnT(WebDriver driver, String link, String queryDatabase) {
		String sD = null;
		String sT = null;
		String statement = null;
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			statement = "select * from twitter_bot.query " + "where photo_link = ?"; 
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, link);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sD = rs.getDate("date").toString();
				sT = rs.getString("post_time");
			}
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pickingDate(driver, sD);
		pickingTime(driver, sT);
	}
	
	
	public static void pickingDate(WebDriver driver, String date) {
		 String[] ie = date.split("-");
		 

		 Select month = new Select(driver.findElement(By.id("SELECTOR_1")));
		 Select day = new Select(driver.findElement(By.id("SELECTOR_2")));
		 //Day
		 if (ie[2].startsWith("0") == true) { day.selectByValue(ie[2].substring(1)); } 
		 else {  day.selectByValue(ie[2]); }
		 
		 //Month
		 if (ie[1].startsWith("0") == true) { month.selectByValue(ie[1].substring(1)); } 
		 else {  month.selectByValue(ie[1]); }
	}
	
	public static void pickingTime(WebDriver driver, String time) {
		String[] he = time.split(":");
		
		Select hour = new Select(driver.findElement(By.id("SELECTOR_4")));
		Select minute = new Select(driver.findElement(By.id("SELECTOR_5")));
		Select ap = new Select(driver.findElement(By.id("SELECTOR_6")));
		
		switch (he[0]) {
		default:
			if (he[0].startsWith("0") == true) { hour.selectByValue(he[0].substring(1)); }
			else {hour.selectByValue(he[0]); }
			ap.selectByValue("am");
		break;
		case "15":
			hour.selectByValue("3");
			ap.selectByValue("pm");
		break;
		}
		
		switch (he[1]) {
			default:
				if (he[1].startsWith("0") == true) { minute.selectByValue(he[1].substring(1)); }
				else {minute.selectByValue(he[1]); }
			break;
		}
	}
	

	
	
	public static void deleteQuery(String link, String queryDatabase) {
		String statement = null;
		
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			statement = "delete from twiter_bot.query " + "where photo_link = ?"; 
			
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, link);
			ps.executeUpdate();
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updatePost(String link, String postDatabase) {

		String statement = null;
		
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			statement = "insert into twitter_bot.posts "
					+ "select * from twitter_bot.query "
					+ "where photo_link = ?";
			
			PreparedStatement ps = cn.prepareStatement(statement);
			ps.setString(1, link);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateAccounts(String accountName, String accountDatabase) {
		String statement = null;
		
		try {
			Connection cn = DriverManager.getConnection("url", "username", "password");
			
			statement = "update twitter_bot.accounts "
					+ "set post_used = post_used + 1 "
					+ "where username = ?";
			PreparedStatement ps = cn.prepareStatement(statement);
			
			ps.setString(1, accountName);
			ps.executeUpdate();
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getAccountName(String link, String queryDatabase) {
		String accountName = null;
		String statement = null;
		
		Connection cn;
		try {
			cn = DriverManager.getConnection("url", "username", "password");
			
			
			statement = "select * from twitter_bot.query "
								+ "where photo_link = ?";

		
			PreparedStatement ps = cn.prepareStatement(statement);
		
			ps.setString(1, link);
		
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) { accountName = rs.getString("account"); }
			
			cn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return accountName;
	}

}

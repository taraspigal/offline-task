package pl.otomo;

import com.itextpdf.text.DocumentException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class Tests {

    public static void main (String args[]) throws IOException, DocumentException {

        // Initialize webdriver
        System.setProperty("webdriver.chrome.driver", "D:\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

          // check whether table in DB exist. If not - create table "results"
        DB.PrepareTable();

        for (int i = 1; i <= 10; i++) {


            if (i == 1) {

                // Go to otomoto.pl
                driver.get("https://www.otomoto.pl");
                driver.manage().window().maximize();

                // Select "BMW" in 1st dropdown list
                Select car = new Select(driver.findElement(By.id("param571")));
                car.selectByValue("bmw");

                // Select "m3" in 2d dropdown list
                Select model = new Select(driver.findElement(By.id("param573")));
                model.selectByValue("m3");

                // Click on "Find" button
                WebElement btnfind = ((ChromeDriver) driver).findElement(By.xpath("//*[@id=\"searchmain_29\"]/button[1]"));
                btnfind.click();
                driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

                // Accept cookies
                WebElement cookies = ((ChromeDriver) driver).findElement(By.xpath("//*[@id=\"cookiesBar\"]/div/div/a"));
                cookies.click();

            }

            // Click on search result header
            WebElement element = ((ChromeDriver) driver).findElement(By.xpath("(//a[contains(@class,'offer-title__link')])[" +i+ "]"));
            element.click();

            // Get price and convert to integer
            WebElement findprices = ((ChromeDriver) driver).findElement(By.xpath("//*[@id=\"siteWrap\"]/main/section/div[3]/div[1]/span[not (@class='offer-price__currency')]"));
            String sprice = findprices.getText();
            sprice = sprice.replaceAll("\\D+", "");
            int iprice = Integer.parseInt(sprice);

            // Get url with result
            String url = driver.getCurrentUrl();

            // Check whether btn "Get phone" exist
            Boolean isPresent = driver.findElements(By.xpath("//span[contains(@data-type,'top')]")).size() > 0;
            int iphoner = 0;
            if (isPresent) {

                // Click on "Get phone" button
                WebElement phone = ((ChromeDriver) driver).findElement(By.xpath("//span[contains(@data-type,'top')]"));
                phone.click();

                // Delay to get full phone number
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Get phone number
                WebElement result = driver.findElement(By.xpath("//*[@id=\"siteWrap\"]/main/section/div[3]/div[2]/span/span/span[2]"));
                String phoner = result.getText();
                phoner = phoner.replaceAll("[^0-9]", "");

                // Convert to int
                try {
                    iphoner = Integer.parseInt(phoner);
                } catch (NumberFormatException e){
                    iphoner = 0;
                }

            } else {
                iphoner = 0;
            }

            // Get image
            WebElement image = ((ChromeDriver) driver).findElement(By.xpath("//*[@id=\"offer-photos\"]/div[1]/div/div/div[1]/div/div/img"));
            String imageSRC = image.getAttribute("src");
            URL imageurl = new URL(imageSRC);

            // Convert image to byte array
            BufferedImage imagge = ImageIO.read(imageurl);;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] ByteArray;

               ImageIO.write(imagge, "png", baos);
               baos.flush();
               ByteArray = baos.toByteArray();
               baos.close();

            // Convert from byte array to image and save
            /*InputStream in = new ByteArrayInputStream(ByteArray);
            BufferedImage bImageFromConvert = ImageIO.read(in);
            ImageIO.write(bImageFromConvert, "jpg", new File("D:\\car"+i+".png"));*/

            // Insert values into DB
            try {
                DB.insertIntoDB(i, url, iprice, iphoner, imageSRC, ByteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Go to previous page
            ((ChromeDriver) driver).executeScript("window.history.go(-1)");
        }

        // Create pdf report
        DB.CreateReport();

        // Close driver
        driver.close();

    }
}


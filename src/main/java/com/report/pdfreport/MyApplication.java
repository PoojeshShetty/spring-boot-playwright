package com.report.pdfreport;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Media;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;

@RestController
@SpringBootApplication
public class MyApplication {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

//    @GetMapping(
//            value = "/generate-report",
//            produces = MediaType.APPLICATION_PDF_VALUE
//    )

    @RequestMapping("/generate-report")
    String homereports() {
        return "Hello World! from generate reports";
    }

    @RequestMapping("/generate-reports")
    String generateReport() throws Exception {
        String name= "test";
        String address = "test-address";
        String uuid = "test";
        System.out.println("generate report trigger");
        String currentDir = System.getProperty("user.dir");
        Path pathHtml = Paths.get(currentDir+"/tmp/" + uuid + ".html");
        Path pathPdf = Paths.get(currentDir + "/tmp/" + uuid + ".pdf");
        System.out.println("Path html " + pathHtml.toAbsolutePath());
        System.out.println("Path pdf " + pathPdf.toAbsolutePath());
        try {
            // read the template and fill the data
            String htmlContent = new Scanner(getClass().getClassLoader().getResourceAsStream("template.html"), "UTF-8")
                    .useDelimiter("\\A")
                    .next();
            htmlContent = htmlContent.replace("$name", name)
                    .replace( "$address", address);

            // write to html
            Files.write(pathHtml.toAbsolutePath(), htmlContent.getBytes());

            String command = "wkhtmltopdf " + pathHtml.toString() + " " + pathPdf.toString();

            System.out.println("Command executed is "+ command);
            // convert html to pdf
            Process generateToPdf = Runtime.getRuntime().exec("wkhtmltopdf --javascript-delay 6000 --no-stop-slow-scripts --enable-javascript " + pathHtml.toString() + " " + pathPdf.toString() );
            generateToPdf.waitFor();

            // deliver pdf
            return pathPdf.toString();

        }
        catch (Exception error) {
            System.out.println(error.getMessage());
            return "Exception occurred";
        }
        finally {
            // delete temp files
//            Files.delete(pathHtml);
//            Files.delete(pathPdf);
        }
    }

    @RequestMapping("/screenshot-website")
    String generateScreenshot() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(50));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            // Emulate the print media type
            page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));


            page.navigate("https://www.wednesday.is");

//            page.click("#container");
//
//            for(var i = 0; i< 10; i++) {
//                page.mouse().wheel(0, 2500);
//            }

            try {
                Thread.sleep(2000); // Sleep for 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            page.pdf(new Page.PdfOptions().setPath(Paths.get("screenshot.pdf")).setWidth("11in"));
            browser.close();
        } catch (Exception error) {
            System.out.println("Error value is " +  error);
            return "error";
        }
        return "done";
    }

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

}
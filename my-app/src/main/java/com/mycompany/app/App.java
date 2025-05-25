package com.mycompany.app;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws Exception {
        Path dataPath = Paths.get("D:\\project\\ST\\me\\ST-8\\data\\data.txt");
        List<String> lines = Files.readAllLines(dataPath);
        String artist = "";
        String album = "";
        List<String> tracks = new ArrayList<>();
        boolean isTrackSection = false;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Artist:")) {
                artist = line.substring("Artist:".length()).trim();
                isTrackSection = false;
            } else if (line.startsWith("Title:")) {
                album = line.substring("Title:".length()).trim();
                isTrackSection = false;
            } else if (line.equalsIgnoreCase("Tracks:")) {
                isTrackSection = true;
            } else if (isTrackSection && !line.isEmpty()) {
                tracks.add(line);
            }
        }
        if (artist.isEmpty() || album.isEmpty() || tracks.isEmpty()) {
            throw new IllegalArgumentException("data.txt не содержит полных данных.");
        }

        System.setProperty("webdriver.chrome.driver",
                "C:\\Users\\bogda\\OneDrive\\Рабочий стол\\chromedriver-win64\\chromedriver.exe");
        String downloadPath = "D:\\project\\ST\\me\\ST-8\\result";
        Path outputDir = Paths.get(downloadPath);
        Files.createDirectories(outputDir);

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", outputDir.toAbsolutePath().toString());
        prefs.put("plugins.always_open_pdf_externally", true);
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--headless", "--disable-gpu");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        try {
            driver.get("https://www.papercdcase.com/index.php");

            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[1]/td[2]/input"))
                    .sendKeys(artist);
            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[2]/td[2]/input"))
                    .sendKeys(album);

            for (int i = 0; i < Math.min(tracks.size(), 16); i++) {
                String xpath;
                if (i < 8) {
                    xpath = "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[3]/td[2]/table"
                            + "/tbody/tr/td[1]/table/tbody/tr[" + (i + 1) + "]/td[2]/input";
                } else {
                    xpath = "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[3]/td[2]/table"
                            + "/tbody/tr/td[2]/table/tbody/tr[" + (i - 7) + "]/td[2]/input";
                }
                driver.findElement(By.xpath(xpath)).sendKeys(tracks.get(i));
            }

            driver.findElement(By.xpath(
                            "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[4]/td[2]/input[2]"))
                    .click();

            driver.findElement(By.xpath(
                            "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[5]/td[2]/input[2]"))
                    .click();

            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/p/input"))
                    .click();

            Thread.sleep(6000);

            File[] pdfs = outputDir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfs != null && pdfs.length > 0) {
                File newest = Arrays.stream(pdfs)
                        .max(Comparator.comparingLong(File::lastModified))
                        .orElse(null);
                if (newest != null) {
                    Path target = outputDir.resolve("cd.pdf");
                    Files.deleteIfExists(target);
                    Files.move(newest.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("PDF сохранён: " + target.toAbsolutePath());
                }
            } else {
                System.err.println("PDF в папке не найден.");
            }

        } finally {
            driver.quit();
        }
    }
}

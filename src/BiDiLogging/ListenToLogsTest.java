package BiDiLogging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.log.ConsoleLogEntry;
import org.openqa.selenium.bidi.log.JavascriptLogEntry;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.Assert;

public class ListenToLogsTest {

	private WebDriver driver;
	private LogInspector logInspector;

	private String webPage = "https://selenium.dev/selenium/web/bidi/logEntryAdded.html";

	@BeforeTest
	public void setup() {
		FirefoxOptions options = new FirefoxOptions();
		options.enableBiDi();
		driver = new FirefoxDriver(options);
		logInspector = new LogInspector(driver);
	}

	@AfterTest
	public void teardown() {
		logInspector.close();
		driver.quit();
	}

	@Test
	public void consoleMessageTest() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<ConsoleLogEntry> future = new CompletableFuture<>();
		logInspector.onConsoleEntry(future::complete);

		driver.get(webPage);
		driver.findElement(By.id("consoleLog")).click();

		ConsoleLogEntry consoleLogEntry = future.get(5, TimeUnit.SECONDS);

		Assert.assertEquals(consoleLogEntry.getText(), "Hello, world!");
		Assert.assertEquals(consoleLogEntry.getType(), "console");
		Assert.assertEquals(consoleLogEntry.getLevel(), LogLevel.INFO);
	}

	@Test
	public void javascriptExceptionTest() throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<JavascriptLogEntry> future = new CompletableFuture<>();
		logInspector.onJavaScriptLog(future::complete);

		driver.get(webPage);
		driver.findElement(By.id("jsException")).click();

		JavascriptLogEntry jsLogEntry = future.get(5, TimeUnit.SECONDS);

		Assert.assertEquals(jsLogEntry.getText(), "Error: Not working");
		Assert.assertEquals(jsLogEntry.getType(), "javascript");
		Assert.assertEquals(jsLogEntry.getLevel(), LogLevel.ERROR);
	}

}
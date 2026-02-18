package dev.right.filez;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@EnableWebSecurity
@SpringBootApplication
public class Application {
	public static Dotenv dotenv;

	public static boolean disableFullAccessAccounts;
	public static String fileStoragePaths;
	public static boolean fileTypesWhitelist;
	public static Set<String> fileWhitelistMimeTypes;
	public static Set<String> fileWhitelistExt;

	private static Set<String> toSet(String set) {
		return Arrays.stream(set.split(","))
				.map(String::trim) // removes spaces
				.collect(Collectors.toSet());
	}

	private static void loadEnvironmentSettings() {
		disableFullAccessAccounts = Boolean.parseBoolean(dotenv.get("disable-full-access-accounts", "true"));
		fileStoragePaths = dotenv.get("files-storage-path", "default");
		fileTypesWhitelist = Boolean.parseBoolean(dotenv.get("files-types-whitelist", "false"));

		if (!fileTypesWhitelist) {
			return;
		}

		fileWhitelistMimeTypes = toSet(dotenv.get("files-whitelist-mimetypes", "image/jpg, image/png"));
		fileWhitelistExt = toSet(dotenv.get("files-whitelist-extensions", ".jpg, .png"));
	}

	public static void main(String[] args) {
		System.out.println("Application boot init.");
		dotenv = Dotenv
				.configure()
				.directory(".")
				.load();

		loadEnvironmentSettings();

		SpringApplication.run(Application.class, args);
	}
}

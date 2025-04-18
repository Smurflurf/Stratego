package strados2.tools;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream; 

import javax.net.ssl.HttpsURLConnection;

/**
 * Scrapes all archived Stratego Games from gravon (ignoring the 'old' directory).
 * @author Gemini 2.5 pro Preview 03-25, modified by Simon Stumpf
 */
public class Scraper {
	    public static final String BASE_URL = "https://www.gravon.de/strados2/files/";
	    public static final String EXTRACT_DIR = "gravon_extracted_games";
	    public static final Pattern ZIP_LINK_PATTERN = Pattern.compile(
	            "<a\\s+[^>]*href\\s*=\\s*\"([^\"]+\\.zip)\"[^>]*>",
	            Pattern.CASE_INSENSITIVE);
	    public static final int BUFFER_SIZE = 8192;

	    public static void main(String[] args) {
	        System.out.println("Starte Gravon Stratego Direct Extractor...");
	        System.out.println("Basis-URL: " + BASE_URL);
	        System.out.println("Extraktionsverzeichnis: " + EXTRACT_DIR);

	        Path extractPath = Paths.get(EXTRACT_DIR);
	        try {
	            Files.createDirectories(extractPath);
	            System.out.println("Extraktionsverzeichnis '" + extractPath.toAbsolutePath() + "' erstellt oder bereits vorhanden.");
	        } catch (IOException e) {
	            System.err.println("Fehler beim Erstellen des Extraktionsverzeichnisses: " + e.getMessage());
	        }

	        List<String> zipFileNames = getZipFileLinks(BASE_URL);
	        if (zipFileNames == null || zipFileNames.isEmpty()) {
	            System.err.println("Konnte keine .zip-Dateien zum Extrahieren finden auf " + BASE_URL);
	            return;
	        }

	        System.out.println("Gefundene .zip-Dateien zum Extrahieren (ohne 'old/'): " + zipFileNames.size());

	        int successCount = 0;
	        int failCount = 0;
	        for (String zipFileName : zipFileNames) {
	            String fileUrl = BASE_URL + zipFileName;

	            System.out.print("Verarbeite: " + zipFileName + " ... ");
	            boolean success = downloadAndExtractZip(fileUrl, extractPath);
	            if (success) {
	                System.out.println("OK");
	                successCount++;
	            } else {
	                System.out.println("FEHLGESCHLAGEN");
	                failCount++;
	            }
	            try {
	                Thread.sleep(50);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	        }

	        System.out.println("\n--- Extraktions Zusammenfassung ---");
	        System.out.println("Archive erfolgreich verarbeitet: " + successCount);
	        System.out.println("Fehler bei Archiven:           " + failCount);
	        System.out.println("Extraktion abgeschlossen. Dateien in: " + extractPath.toAbsolutePath());
	    }

	    private static List<String> getZipFileLinks(String urlString) {
	        List<String> links = new ArrayList<>();
	        StringBuilder htmlContent = new StringBuilder();
	        try {
	            @SuppressWarnings("deprecation")
				URL url = new URL(urlString);
	            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");
	            connection.setConnectTimeout(10000);
	            connection.setReadTimeout(10000);
	            int responseCode = connection.getResponseCode();
	            if (responseCode == HttpsURLConnection.HTTP_OK) {
	                try (InputStream inputStream = connection.getInputStream();
	                     InputStreamReader reader = new InputStreamReader(inputStream);
	                     BufferedReader bufferedReader = new BufferedReader(reader)) {
	                    String line;
	                    while ((line = bufferedReader.readLine()) != null) {
	                        htmlContent.append(line).append("\n");
	                    }
	                }
	            } else {
	                System.err.println("Fehler beim Abrufen der Seite: HTTP-Code " + responseCode);
	                return null;
	            }
	            connection.disconnect();
	        } catch (IOException e) {
	            System.err.println("Netzwerkfehler beim Abrufen der Verzeichnisliste: " + e.getMessage());
	            return null;
	        }
	        Matcher matcher = ZIP_LINK_PATTERN.matcher(htmlContent);
	        while (matcher.find()) {
	            String link = matcher.group(1);
	            if (link != null && !link.trim().isEmpty()) {
	                if (!link.toLowerCase().startsWith("old/") && !link.contains("://")) {
	                    String fileName = Paths.get(link).getFileName().toString();
	                     if (!links.contains(fileName)) {
	                         links.add(fileName);
	                     }
	                }
	            }
	        }
	        return links;
	    }

	    /**
	     * Lädt eine ZIP-Datei von einer URL herunter und extrahiert ihren Inhalt
	     * direkt in das angegebene Verzeichnis, ohne die ZIP-Datei zu speichern.
	     *
	     * @param fileUrlString  Die URL der ZIP-Datei.
	     * @param extractDirPath Der Pfad zum Verzeichnis, in das extrahiert werden soll.
	     * @return true bei Erfolg, false bei Fehlern.
	     */
	    private static boolean downloadAndExtractZip(String fileUrlString, Path extractDirPath) {
	        try {
	            @SuppressWarnings("deprecation")
				URL fileUrl = new URL(fileUrlString);
	            HttpsURLConnection fileConnection = (HttpsURLConnection) fileUrl.openConnection();
	            fileConnection.setRequestMethod("GET");
	            fileConnection.setConnectTimeout(15000);
	            fileConnection.setReadTimeout(60000);

	            int responseCode = fileConnection.getResponseCode();
	            if (responseCode == HttpsURLConnection.HTTP_OK) {
	                try (InputStream inputStream = fileConnection.getInputStream();
	                     ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

	                    ZipEntry entry;
	                    byte[] buffer = new byte[BUFFER_SIZE];
	                    while ((entry = zipInputStream.getNextEntry()) != null) {
	                        Path targetEntryPath = extractDirPath.resolve(entry.getName()).normalize();

	                        if (!targetEntryPath.startsWith(extractDirPath.normalize())) {
	                           System.err.println("\nWARNUNG: Unsicherer Eintrag übersprungen (Path Traversal): " + entry.getName());
	                           continue;
	                        }

	                        if (entry.isDirectory()) {
	                            Files.createDirectories(targetEntryPath);
	                        } else {
	                            Files.createDirectories(targetEntryPath.getParent());

	                            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetEntryPath.toFile()))) {
	                                int len;
	                                while ((len = zipInputStream.read(buffer)) > 0) {
	                                    outputStream.write(buffer, 0, len);
	                                }
	                            }
	                        }
	                        zipInputStream.closeEntry();
	                    }
	                    return true;

	                } catch (IOException e) {
	                    System.err.println("\nFehler beim Extrahieren von '" + fileUrlString + "': " + e.getMessage());
	                    return false;
	                }
	            } else {
	                System.err.println("\nFehler beim Download von '" + fileUrlString + "': HTTP-Code " + responseCode);
	                return false;
	            }
	        } catch (IOException e) {
	            System.err.println("\nNetzwerk- oder IO-Fehler beim Verarbeiten von '" + fileUrlString + "': " + e.getMessage());
	            return false;
	        }
	    }
	}
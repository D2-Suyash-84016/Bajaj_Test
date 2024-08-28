import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Step 1: Parse Command-Line Arguments
        if (args.length != 2) {
            System.out.println("Usage: java -jar <JAR_FILE_NAME> <PRN Number> <Path to JSON file>");
            return;
        }

        String prnNumber = args[0].toLowerCase().replaceAll("\\s", ""); // Ensure PRN is in lowercase and no spaces
        String jsonFilePath = args[1];

        // Step 2: Read and Parse JSON File
        JSONParser parser = new JSONParser();
        String destinationValue = null;

        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            // Step 3: Traverse JSON to find "destination"
            destinationValue = findDestination(jsonObject);
            if (destinationValue == null) {
                System.out.println("No 'destination' key found in JSON file.");
                return;
            }
        } catch (IOException | ParseException e) {
            System.out.println("Error reading or parsing the JSON file: " + e.getMessage());
            return;
        }

        // Step 4: Generate a random alphanumeric string of size 8 characters
        String randomString = generateRandomString(8);

        // Step 5: Generate MD5 hash
        String concatenatedString = prnNumber + destinationValue + randomString;
        String md5Hash = generateMD5Hash(concatenatedString);

        // Step 6: Format the output
        String output = md5Hash + ";" + randomString;
        System.out.println(output);
    }

    // Function to recursively find the first occurrence of "destination" key
    private static String findDestination(JSONObject jsonObject) {
        for (Object key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if ("destination".equals(key)) {
                return value.toString();
            } else if (value instanceof JSONObject) {
                String result = findDestination((JSONObject) value);
                if (result != null)
                    return result;
            } else if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof JSONObject) {
                        String result = findDestination((JSONObject) item);
                        if (result != null)
                            return result;
                    }
                }
            }
        }
        return null;
    }

    // Function to generate a random alphanumeric string
    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Function to generate MD5 hash
    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating MD5 hash", e);
        }
    }
}

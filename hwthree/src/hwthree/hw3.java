package hwthree;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class hw3 {

    public static void main(String[] args) {
        // Create a MongoClient instance
        MongoClient client = MongoClients.create("mongodb://localhost:27017");

        // Access the database and collection
        MongoDatabase database = client.getDatabase("testdb");
        MongoCollection<Document> collection = database.getCollection("testcollection");

        Scanner scanner = new Scanner(System.in);
        boolean keepRunning = true;

        while (keepRunning) {
            System.out.println("Choose an operation: ");
            System.out.println("1. Create a document");
            System.out.println("2. Read documents");
            System.out.println("3. Update a document");
            System.out.println("4. Delete a document");
            System.out.println("5. Exit");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createDocument(collection, scanner);
                    break;
                case "2":
                    readDocuments(collection);
                    break;
                case "3":
                    updateDocument(collection, scanner);
                    break;
                case "4":
                    deleteDocument(collection, scanner);
                    break;
                case "5":
                    keepRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    break;
            }
        }

        scanner.close();
        client.close();
    }

    private static void createDocument(MongoCollection<Document> collection, Scanner scanner) {
        String name = getValidInput(scanner, "Enter name: ", "Name cannot be empty.", hw3::validateName);
        String email = getValidInput(scanner, "Enter email: ", "Invalid email format.", hw3::validateEmail);
        String phone = getValidInput(scanner, "Enter phone (at least 10 digits): ", "Phone must contain at least 10 digits.", hw3::validatePhone);
        String address = getValidInput(scanner, "Enter address: ", "Address cannot be empty.", hw3::validateAddress);

        Document document = new Document("name", name)
                .append("email", email)
                .append("phone", phone)
                .append("address", address)
                .append("created_at", Instant.now());

        collection.insertOne(document);
        System.out.println("Document inserted successfully.");
    }

    private static String getValidInput(Scanner scanner, String prompt, String errorMessage, Predicate<String> validator) {
        String input;
        do {
            System.out.print(prompt);
            input = scanner.nextLine();
            if (!validator.test(input)) {
                System.out.println(errorMessage);
            }
        } while (!validator.test(input));
        return input;
    }

    private static void readDocuments(MongoCollection<Document> collection) {
        List<Document> documents = collection.find().into(new java.util.ArrayList<>());
        if (documents.isEmpty()) {
            System.out.println("No documents found.");
            return;
        }
        System.out.printf("%-20s %-30s %-15s %-30s%n", "Name", "Email", "Phone", "Address");
        System.out.println(new String(new char[100]).replace("\0", "-"));

        for (Document doc : documents) {
            System.out.printf("%-20s %-30s %-15s %-30s%n",
                    doc.getString("name"),
                    doc.getString("email"),
                    doc.getString("phone"),
                    doc.getString("address"));
        }
    }

    private static void updateDocument(MongoCollection<Document> collection, Scanner scanner) {
        String nameToUpdate = getValidInput(scanner, "Enter the name of the document to update: ", "Name cannot be empty.", hw3::validateName);
        Document docToUpdate = collection.find(Filters.eq("name", nameToUpdate)).first();
        if (docToUpdate == null) {
            System.out.println("No document found with the given name.");
            return;
        }

        String newEmail = getValidInput(scanner, "Enter new email: ", "Invalid email format.", hw3::validateEmail);
        String newPhone = getValidInput(scanner, "Enter new phone: ", "Phone must contain at least 10 digits.", hw3::validatePhone);
        String newAddress = getValidInput(scanner, "Enter new address: ", "Address cannot be empty.", hw3::validateAddress);

        if (!newEmail.isEmpty()) {
            collection.updateOne(Filters.eq("name", nameToUpdate), new Document("$set", new Document("email", newEmail)));
        }
        if (!newPhone.isEmpty()) {
            collection.updateOne(Filters.eq("name", nameToUpdate), new Document("$set", new Document("phone", newPhone)));
        }
        if (!newAddress.isEmpty()) {
            collection.updateOne(Filters.eq("name", nameToUpdate), new Document("$set", new Document("address", newAddress)));
        }

        System.out.println("Document updated successfully.");
    }

    private static void deleteDocument(MongoCollection<Document> collection, Scanner scanner) {
        String nameToDelete = getValidInput(scanner, "Enter the name of the document to delete: ", "Name cannot be empty.", hw3::validateName);
        Document docToDelete = collection.find(Filters.eq("name", nameToDelete)).first();
        if (docToDelete == null) {
            System.out.println("No document found with the given name.");
            return;
        }

        collection.deleteOne(Filters.eq("name", nameToDelete));
        System.out.println("Document deleted successfully.");
    }

    // Validator functions
    private static boolean validateName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private static boolean validateEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    private static boolean validatePhone(String phone) {
        return phone != null && phone.matches("\\d{10,}");
    }

    private static boolean validateAddress(String address) {
        return address != null && !address.trim().isEmpty();
    }
}

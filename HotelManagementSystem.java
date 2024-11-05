import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

class Guest {
    String name;
    String phoneNumber;
    String roomType;
    String checkInDate;
    String checkOutDate;
    int roomNumber;
    double billAmount;

    public Guest(String name, String phoneNumber, String roomType, String checkInDate, String checkOutDate, int roomNumber, double billAmount) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomNumber = roomNumber;
        this.billAmount = billAmount;
    }

    @Override
    public String toString() {
        return String.format("Guest Name: %s, Phone: %s, Room Type: %s, Room No: %d, Check-In: %s, Check-Out: %s, Bill: $%.2f",
                name, phoneNumber, roomType, roomNumber, checkInDate, checkOutDate, billAmount);
    }
}

class DatabaseConnection {
    private String url = "jdbc:mysql://localhost:3306/hotel_management"; // Database URL
    private String user = "root"; // Replace with your database username
    private String password = "password"; // Replace with your database password
    private Connection connection;

    // Method to establish a connection to the database
    public Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connection to database established successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Connection to database failed: " + e.getMessage());
        }
        return connection;
    }

    // Method to close the database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

public class HotelManagementSystem extends JFrame implements ActionListener {
    private HashMap<Integer, Boolean> roomAvailability = new HashMap<>();
    private HashMap<String, Guest> guestInformation = new HashMap<>();
    private ArrayList<Guest> billingHistory = new ArrayList<>();
    private DatabaseConnection dbConnection;

    private final int SINGLE_ROOM_PRICE = 2000;
    private final int DOUBLE_ROOM_PRICE = 3500;
    private final int SUITE_PRICE = 5000;

    private JTextField roomInput, guestNameInput, guestPhoneInput, checkInDateInput, checkOutDateInput;
    private JComboBox<String> roomTypeChoice;
    private JTextArea displayArea;

    public HotelManagementSystem() {
        dbConnection = new DatabaseConnection(); // Initialize DatabaseConnection

        // Initialize room availability
        for (int i = 1; i <= 100; i++) {
            roomAvailability.put(i, true);
        }

        // Set up JFrame
        setTitle("Hotel Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(220, 220, 220));

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(new TitledBorder(new LineBorder(Color.BLUE, 2), "Guest Information", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), Color.BLUE));

        roomInput = new JTextField(5);
        guestNameInput = new JTextField(20);
        guestPhoneInput = new JTextField(15);
        checkInDateInput = new JTextField(10);
        checkOutDateInput = new JTextField(10);
        roomTypeChoice = new JComboBox<>(new String[]{"Single", "Double", "Suite"});

        inputPanel.add(new JLabel("Room Number:"));
        inputPanel.add(roomInput);
        inputPanel.add(new JLabel("Guest Name:"));
        inputPanel.add(guestNameInput);
        inputPanel.add(new JLabel("Guest Phone:"));
        inputPanel.add(guestPhoneInput);
        inputPanel.add(new JLabel("Check-In Date (dd/mm/yyyy):"));
        inputPanel.add(checkInDateInput);
        inputPanel.add(new JLabel("Check-Out Date (dd/mm/yyyy):"));
        inputPanel.add(checkOutDateInput);
        inputPanel.add(new JLabel("Room Type:"));
        inputPanel.add(roomTypeChoice);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBorder(new TitledBorder(new LineBorder(Color.BLUE, 2), "Actions", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 16), Color.BLUE));

        String[] buttonLabels = {"Check In", "Check Out", "View Inmates", "Available Rooms", "Billing History", "Customer History", "Close", "Special Offer"};
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setBackground(Color.BLUE);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(this);
            buttonPanel.add(button);
        }

        // Display area (increased size)
        displayArea = new JTextArea(15, 60);
        displayArea.setEditable(false);
        displayArea.setBackground(Color.LIGHT_GRAY);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        try {
            if (command.equals("Check In")) {
                int roomNumber = Integer.parseInt(roomInput.getText());
                String guestName = guestNameInput.getText();
                String guestPhone = guestPhoneInput.getText();
                String checkInDate = checkInDateInput.getText();
                String checkOutDate = checkOutDateInput.getText();
                String roomType = (String) roomTypeChoice.getSelectedItem();

                handleCheckIn(roomNumber, guestName, guestPhone, roomType, checkInDate, checkOutDate);
            } else if (command.equals("Check Out")) {
                String guestPhone = guestPhoneInput.getText();
                handleCheckOut(guestPhone);
            } else if (command.equals("View Inmates")) {
                viewInmates();
            } else if (command.equals("Available Rooms")) {
                viewAvailableRooms();
            } else if (command.equals("Billing History")) {
                viewBillingHistory();
            } else if (command.equals("Customer History")) {
                viewCustomerHistory();
            } else if (command.equals("Close")) {
                dbConnection.closeConnection(); // Close connection on exit
                System.exit(0);
            } else if (command.equals("Special Offer")) {
                handleSpecialOffer();
            }
        } catch (NumberFormatException ex) {
            displayArea.append("Invalid input. Please check your entries.\n");
        }
    }

    private void handleCheckIn(int roomNumber, String guestName, String guestPhone, String roomType, String checkInDate, String checkOutDate) {
        if (roomAvailability.get(roomNumber)) {
            double estimatedBill = calculateBill(roomType, checkInDate, checkOutDate);
            Guest guest = new Guest(guestName, guestPhone, roomType, checkInDate, checkOutDate, roomNumber, estimatedBill);
            guestInformation.put(guestPhone, guest);
            roomAvailability.put(roomNumber, false);

            // Insert guest details into the database
            insertGuestToDatabase(guest);

            displayArea.append("Check-In Successful for Room " + roomNumber + "\n");
        } else {
            displayArea.append("Room " + roomNumber + " is already booked.\n");
        }
    }

    private void insertGuestToDatabase(Guest guest) {
        String sql = "INSERT INTO guests (name, phone_number, room_type, check_in_date, check_out_date, room_number, bill_amount) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guest.name);
            pstmt.setString(2, guest.phoneNumber);
            pstmt.setString(3, guest.roomType);
            pstmt.setString(4, guest.checkInDate + " " + getCurrentTime());
            pstmt.setString(5, guest.checkOutDate + " " + getCurrentTime());
            pstmt.setInt(6, guest.roomNumber);
            pstmt.setDouble(7, guest.billAmount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            displayArea.append("Error saving guest to database: " + e.getMessage() + "\n");
        }
    }

    private void handleCheckOut(String guestPhone) {
        if (guestInformation.containsKey(guestPhone)) {
            Guest guest = guestInformation.get(guestPhone);
            roomAvailability.put(guest.roomNumber, true);
            billingHistory.add(guest);
            guestInformation.remove(guestPhone);

            // Update the checkout date in the database
            updateCheckoutInDatabase(guestPhone);

            displayArea.append("Check-Out Successful for " + guest.name + ". Room " + guest.roomNumber + " is now available.\n");
        } else {
            displayArea.append("No guest found with phone number: " + guestPhone + "\n");
        }
    }

    private void updateCheckoutInDatabase(String guestPhone) {
        String sql = "UPDATE guests SET check_out_date = ? WHERE phone_number = ?";
        try (Connection conn = dbConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, getCurrentTime());
            pstmt.setString(2, guestPhone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            displayArea.append("Error updating checkout in database: " + e.getMessage() + "\n");
        }
    }

    private void viewInmates() {
        displayArea.append("Current Inmates:\n");
        for (Guest guest : guestInformation.values()) {
            displayArea.append(guest.toString() + "\n");
        }
    }

    private void viewAvailableRooms() {
        displayArea.append("Available Rooms:\n");
        for (Map.Entry<Integer, Boolean> entry : roomAvailability.entrySet()) {
            if (entry.getValue()) {
                displayArea.append("Room " + entry.getKey() + " is available.\n");
            }
        }
    }

    private void viewBillingHistory() {
        displayArea.append("Billing History:\n");
        for (Guest guest : billingHistory) {
            displayArea.append(guest.toString() + "\n");
        }
    }

    private void viewCustomerHistory() {
        String guestPhone = guestPhoneInput.getText();
        if (guestInformation.containsKey(guestPhone)) {
            Guest guest = guestInformation.get(guestPhone);
            displayArea.append("Customer History for " + guest.name + ":\n");
            displayCustomerHistory(guestPhone);
        } else {
            displayArea.append("No customer history found for phone number: " + guestPhone + "\n");
        }
    }

    private void displayCustomerHistory(String guestPhone) {
        String sql = "SELECT * FROM guests WHERE phone_number = ?";
        try (Connection conn = dbConnection.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guestPhone);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String roomType = rs.getString("room_type");
                String checkInDate = rs.getString("check_in_date");
                String checkOutDate = rs.getString("check_out_date");
                int roomNumber = rs.getInt("room_number");
                double billAmount = rs.getDouble("bill_amount");
                displayArea.append(String.format("Name: %s, Room Type: %s, Room No: %d, Check-In: %s, Check-Out: %s, Bill: $%.2f\n",
                        name, roomType, roomNumber, checkInDate, checkOutDate, billAmount));
            }
        } catch (SQLException e) {
            displayArea.append("Error retrieving customer history: " + e.getMessage() + "\n");
        }
    }

    private void handleSpecialOffer() {
        String guestPhone = guestPhoneInput.getText();
        if (guestInformation.containsKey(guestPhone)) {
            Guest guest = guestInformation.get(guestPhone);
            if (guestInformation.size() >= 3) { // Check if the guest has stayed at least 3 times
                displayArea.append("Congratulations " + guest.name + "! You are eligible for a special offer!\n");
            } else {
                displayArea.append("You are not eligible for a special offer yet.\n");
            }
        } else {
            displayArea.append("No guest found with phone number: " + guestPhone + "\n");
        }
    }

    private double calculateBill(String roomType, String checkInDate, String checkOutDate) {
        double pricePerNight = 0;
        switch (roomType) {
            case "Single":
                pricePerNight = SINGLE_ROOM_PRICE;
                break;
            case "Double":
                pricePerNight = DOUBLE_ROOM_PRICE;
                break;
            case "Suite":
                pricePerNight = SUITE_PRICE;
                break;
        }
        // For simplicity, let's assume 1 night stay; you can calculate the number of nights based on the input dates.
        return pricePerNight; // Modify to calculate based on the actual number of nights
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) {
        HotelManagementSystem hms = new HotelManagementSystem();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hms.dbConnection.closeConnection(); // Close connection on exit
        }));
    }
}

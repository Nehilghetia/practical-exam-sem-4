package com.practical.demo.q1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Q1FlightBooking {

    // Database credentials
    static final String DB_URL = "jdbc:mysql://localhost:3000/";
    static final String DB_NAME = "airlinedb";
    static final String USER = "root";
    static final String PASS = "Nehil@123";

    public static void main(String[] args) {
        setupDatabase();

        try (Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("=== Flight Ticket Booking ===");
            System.out.print("Enter Flight ID: ");
            int flightId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            System.out.print("Enter Passenger Name: ");
            String passengerName = scanner.nextLine();

            System.out.print("Enter Seats Requested: ");
            int seatsRequested = scanner.nextInt();

            bookFlight(conn, flightId, passengerName, seatsRequested);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            
            // Create flights table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS flights (" +
                    "flight_id INT PRIMARY KEY, " +
                    "flight_name VARCHAR(100), " +
                    "available_seats INT, " +
                    "price_per_seat DOUBLE)");
            
            // Create bookings table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "booking_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "passenger_name VARCHAR(100), " +
                    "flight_id INT, " +
                    "seats_booked INT, " +
                    "total_amount DOUBLE, " +
                    "FOREIGN KEY (flight_id) REFERENCES flights(flight_id))");
            
            // Insert dummy flight if not exists
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM flights WHERE flight_id = 101");
            rs.next();
            if (rs.getInt(1) == 0) {
                stmt.executeUpdate("INSERT INTO flights (flight_id, flight_name, available_seats, price_per_seat) VALUES (101, 'Air Express', 50, 1500.00)");
                System.out.println("Inserted dummy flight 101 for testing.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
        }
    }

    private static void bookFlight(Connection conn, int flightId, String passengerName, int seatsRequested) {
        String checkSeatsSql = "SELECT available_seats, price_per_seat FROM flights WHERE flight_id = ?";
        String updateSeatsSql = "UPDATE flights SET available_seats = available_seats - ? WHERE flight_id = ?";
        String insertBookingSql = "INSERT INTO bookings (passenger_name, flight_id, seats_booked, total_amount) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSeatsSql)) {
                checkStmt.setInt(1, flightId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int availableSeats = rs.getInt("available_seats");
                    double pricePerSeat = rs.getDouble("price_per_seat");

                    if (availableSeats >= seatsRequested) {
                        // Deduct seats
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSeatsSql)) {
                            updateStmt.setInt(1, seatsRequested);
                            updateStmt.setInt(2, flightId);
                            updateStmt.executeUpdate();
                        }

                        // Insert booking
                        double totalAmount = seatsRequested * pricePerSeat;
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingSql)) {
                            insertStmt.setString(1, passengerName);
                            insertStmt.setInt(2, flightId);
                            insertStmt.setInt(3, seatsRequested);
                            insertStmt.setDouble(4, totalAmount);
                            insertStmt.executeUpdate();
                        }

                        conn.commit();
                        System.out.println("Booking Successful!");
                    } else {
                        conn.rollback();
                        System.out.println("Booking Failed: Not enough seats available");
                    }
                } else {
                    conn.rollback();
                    System.out.println("Booking Failed: Flight ID not found.");
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error during booking: " + e.getMessage());
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore default
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

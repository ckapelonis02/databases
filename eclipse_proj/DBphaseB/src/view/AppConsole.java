package view;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Scanner;

import app.DBapplication;

public class AppConsole {

	// class variables:
	static Scanner input;
	static int userChoice;
	static DBapplication app;

	// class methods:

	/**
	 * Method to open a console for user interface and connect to database
	 */
	public static void runApplication() {
		// initialize application

		// take user input
		input = new Scanner(System.in);
		userChoice = 0;

		// initialize application
		System.out.println("Enter ip of database (host):");
		String host = input.nextLine();

		System.out.println("Enter data base name:");
		String dbName = input.nextLine();

		System.out.println("Enter username:");
		String username = input.nextLine();

		System.out.println("Enter password");
		String password = input.nextLine();

		System.out.println("Enter port No. :");
		String port = input.nextLine();

		app = new DBapplication(host, dbName, username, password, port);
		do {
			printMenu();
			String user_choice = input.nextLine();
			if (user_choice == null) {
				continue;
			} else {
				try {
					userChoice = Integer.parseInt(user_choice);
				} catch (NumberFormatException ex) {
					userChoice = 0;
				}
			}

			// according to the user input , invoke the corresponding function
			switch (userChoice) {
			case 1:
				try {
					System.out.println("Enter AM:");
					String am = input.nextLine();
					System.out.println("Enter course code:");
					String code = input.nextLine();

					app.showGrade(am, code);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					System.out.println("Enter AM:");
					String am = input.nextLine();
					System.out.println("Enter course code (use Greek characters):");
					String code = input.nextLine();
					System.out.println("Enter serial number");
					String serial_number = input.nextLine();

					int serialNo = Integer.parseInt(serial_number);

					System.out.println("Enter new grade");
					String grade = input.nextLine();
					BigDecimal grade_bd = new BigDecimal(grade);

					app.changeGrade(am, code, serialNo, grade_bd);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case 3:
				try {
					System.out.println("Enter first letters of surname (use Greek characters):");
					String given_surname = input.nextLine();
					app.findPerson(given_surname);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case 4:
				try {
					System.out.println("Enter am of student:");
					String am = input.nextLine();
					app.showGrades(am);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 5:
				app.dbClose();
				System.out.println("Program exited");
				System.exit(0);

			}
		} while (userChoice != 5 && userChoice != 0);

	}

	/** Method that prints the System Console **/
	public static void printMenu() {
		System.out
				.println("-----------------------------------------[Menu]-------------------------------------------");
		System.out.println("1. Presentation of student's grade based on his/her A.M. and the course code.");
		System.out.println("2. Change student's grade based on his/her A.M., course code and serial number of course.");
		System.out.println("3. Search person based on the initial letters of their surname.");
		System.out
				.println("4. Presentation of a student's detailed score based on the A.M. sorted by standard semester");
		System.out.println("5. Exit\n");
		System.out.println("Enter your choice by typing the corresponding number 1-5");
	}

}

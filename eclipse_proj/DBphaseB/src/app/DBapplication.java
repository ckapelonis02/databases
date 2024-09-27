package app;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DBapplication {

	private Connection conn;

	/**
	 * class constructor.
	 */
	public DBapplication(String host, String dbName, String username, String pass, String port) {
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver Found!");
		} catch (ClassNotFoundException e) {
			System.err.println("Driver not found! Please check the build path.");
		}
		this.dbConnect(host, dbName, username, pass, port);

	}

	/**
	 * @param ip
	 * @param dbName
	 * @param username
	 * @param password
	 * 
	 *                 Function to establish a connection between the application
	 *                 and the database. 1)load Driver 2)determine URL 3)establish
	 *                 connection (Connection object..)
	 */
	public void dbConnect(String ip, String dbName, String username, String password, String port) {
		try {
			this.conn = DriverManager.getConnection("jdbc:postgresql://" + ip + ":"+port+"/" + dbName, username, password);
			System.out.println("Successfull connection:" + conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param AM
	 * @param course_code
	 * @throws SQLException
	 * 
	 *                      Function to show a student's (final) grade on a specific
	 *                      course
	 */
	public void showGrade(String AM, String course_code) throws SQLException {
		String myQUERY = "SELECT r.final_grade FROM \"Register\" r "
				+ "JOIN \"Student\" s ON r.amka = s.amka WHERE s.am = ? AND r.course_code = ?";
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		BigDecimal grade;
		Boolean cond;

		statement = this.conn.prepareStatement(myQUERY);
		// set the "parameters" of the query
		statement.setString(1, AM);
		statement.setString(2, course_code);

		// result set will contain ALL students that are registered in (different)
		// courses...
		resultSet = statement.executeQuery();
		while (true) {
			cond = resultSet.next();
			if (cond) {
				grade = resultSet.getBigDecimal(1);
				System.out.println("Students grade is : " + grade);
				break;
			} else {
				System.err.println("There is no grade in the datbase for student with AM:" + AM + " and course code:"
						+ course_code);
				System.err.println("Either AM or course code or both are incorrect, please try again...");
				break;
			}
		}

	}

	/**
	 * @param AM
	 * @param course_code
	 * @param serial_number
	 * @param new_grade
	 * @throws SQLException
	 * 
	 *                      Function to change a student's final grade on na
	 *                      specific course with a specific serial_number
	 */
	public void changeGrade(String AM, String course_code, int serial_number, BigDecimal new_grade)
			throws SQLException {
		String myQUERY = "UPDATE \"Register\" SET final_grade = ?\r\n"
				+ "WHERE amka=(SELECT amka as st_amka FROM \"Student\" WHERE am = ? LIMIT 1) \r\n"
				+ "AND course_code = ? AND serial_number =?; ";

		PreparedStatement statement = null;

		statement = this.conn.prepareStatement(myQUERY);
		// set the "parameters" of the statement
		statement.setBigDecimal(1, new_grade);
		statement.setString(2, AM);
		statement.setString(3, course_code);
		statement.setInt(4, serial_number);

		int cnt = statement.executeUpdate();

		if (cnt == 0) {
			System.out.println(
					"Update failed possibly because you entered wrong or non existent arguments, please try again.");
		} else {
			System.out.println("Grade succesfully updated.");
		}

	}

	/**
	 * @param AM
	 * @throws SQLException
	 * 
	 *                      Function to show the grades of a student. It prints the
	 *                      course code , the grade , the typical year (1,2,3,4 or
	 *                      5) and the typical season of the course.
	 */
	public void showGrades(String AM) throws SQLException {
		ResultSet result = null;
		Boolean cond;
		String myQUERY = "SELECT c.course_code,r.final_grade, c.typical_year, c.typical_season\r\n"
				+ "FROM \"Student\" s\r\n" + "JOIN \"Register\" r ON r.amka = s.amka \r\n" + "JOIN \"Course\" c\r\n"
				+ "ON c.course_code = r.course_code\r\n" + "WHERE s.am = ? "
				+ "ORDER BY typical_year, typical_season asc;\r\n";
		PreparedStatement statement = null;

		statement = this.conn.prepareStatement(myQUERY);

		statement.setString(1, AM);

		result = statement.executeQuery();

		System.out.println("For student with AM:" + AM + " we retrieve the following grades: ");
		System.out.println("Course code | Final grade | Year         | Season ");
		while (true) {
			cond = result.next();
			if (cond) {
				String code = result.getString(1);
				BigDecimal final_grade = result.getBigDecimal(2);
				int year = result.getInt(3);
				String season = result.getString(4);

				System.out.println(code + "     |		" + final_grade + " |		" + year + "| " + season);
			} else {
				System.out.println("()");
				break;
			}
		}
	}

	/**
	 * @param given_surname
	 * @throws SQLException
	 * 
	 *                      Function to print name,surname and property for a person
	 *                      (Professor, Laboratory staff, Student) based on initial
	 *                      letters of the last name
	 */
	public void findPerson(String given_surname) throws SQLException {
		Boolean cond;
		String name, surname, property;
		int string_len = given_surname.length();
		List<String> names = new ArrayList<String>();
		List<String> surnames = new ArrayList<String>();
		List<String> properties = new ArrayList<String>();

		String myQUERY = " SELECT p.name, p.surname, 'Student' AS property\r\n"
				+ "        FROM \"Person\" p JOIN \"Student\" s ON p.amka = s.amka WHERE LEFT(p.surname, ?) = ? \r\n"
				+ "        UNION\r\n" + "        SELECT p.name, p.surname, 'Professor' AS property\r\n"
				+ "        FROM \"Person\" p JOIN \"Professor\" pr ON p.amka = pr.amka WHERE LEFT(p.surname, ?) = ? \r\n"
				+ "        UNION\r\n" + "        SELECT p.name, p.surname, 'Lab Teacher' AS property\r\n"
				+ "        FROM \"Person\" p JOIN \"LabTeacher\" lt ON p.amka = lt.amka \r\n"
				+ "		WHERE LEFT(p.surname, ?) = ? ; ";
		PreparedStatement statement = null;
		ResultSet resultSet = null;

		// variables used for user inteface and printing on screen:
		@SuppressWarnings("resource")
		Scanner input2 = new Scanner(System.in);
		int person_ctr = 0; // how many tuples have been returned.
		int user_page = 0; // the number of the page the user wants to see.
		int page_ctr = 0; // counter to keep track which one is the current page
		int wanted_num = 0; // the number of people printed per page
		int pages = 0;
		String user_input = null;
		statement = this.conn.prepareStatement(myQUERY);

		// set the "parameters" of the query
		statement.setInt(1, string_len);
		statement.setString(2, given_surname);
		statement.setInt(3, string_len);
		statement.setString(4, given_surname);
		statement.setInt(5, string_len);
		statement.setString(6, given_surname);

		resultSet = statement.executeQuery();

		// while the query gives tuples, fill the corresponding arrays with
		// names,surnames and properties
		while (true) {
			cond = resultSet.next();
			if (cond) {
				name = resultSet.getString(1);
				names.add(name);
				surname = resultSet.getString(2);
				surnames.add(surname);
				property = resultSet.getString(3);
				properties.add(property);

				person_ctr = person_ctr + 1;
			} else {
				break;
			}
		}

		/**
		 * 3 BASIC CASES : 1) NO PEOPLE WHERE FOUND (person_ctr==0) 2) PEOPLE WHERE
		 * FOUND BUT THEY ARE LESS THAN 5 3) PEOPLE WHERE FOUND BUT THEY ARE MORE THAN 5
		 * IN THIS CASE THE USER MUST CHOOSE THE NUMBER OF PEOPLE PER PAGE
		 */
		if (person_ctr == 0) {
			System.out.println("There are no people with the given surname in the database.");
		}

		if (person_ctr <= 5) {
			// print contents of the arrays
			for (int i = 0; i < person_ctr; i++) {
				System.out.println(names.get(i) + " " + surnames.get(i) + " " + properties.get(i) + "\n");
			}
		}

		if (person_ctr > 5) {
			System.out.println("There are totally " + person_ctr + " people with this surname in the database.");
			System.out.println("Enter number of people you would like to print per page:");

			String in = input2.nextLine();
			wanted_num = Integer.parseInt(in);

			if (wanted_num > person_ctr) {
				System.out.println("Invalid input...please try again (return to the main menu)");
				return;
			}

			pages = (int) Math.ceil(person_ctr / wanted_num);
			if (pages == 0) {
				pages = 1;
			}
			if (wanted_num * pages < person_ctr) {
				pages = pages + 1;
			}
			System.out.println("The result will need " + pages + " pages in total");

			do {
				System.out.println(
						"->Enter the number of the page you want to visit \n->Enter the letter n (using latin characters), to move to the next page\n->Type \"exit\" , to exit.");

				user_input = input2.nextLine();
				if (user_input.equals("exit")) {
					break;
				} else if (user_input.equals("n")) {
					page_ctr++;
					int index2 = (page_ctr - 1) * wanted_num;
					for (int i = index2; i < (page_ctr) * wanted_num; i++) {
						if (i >= person_ctr) {
							System.out.println("No more people to print.");
							return;
						}
						System.out.println(names.get(i) + " " + surnames.get(i) + " " + properties.get(i) + "\n");

					}

				} else {
					try {
						user_page = Integer.parseInt(user_input);
						if (user_page <= pages) {
							page_ctr = user_page; // so we can go from page 1 to n ...
							int index = (user_page - 1) * wanted_num;
							// start printing:
							for (int i = index; i < (user_page) * wanted_num; i++) {
								if (i >= person_ctr) {
									System.out.println("No more people to print.");
									return;
								}
								System.out
										.println(names.get(i) + " " + surnames.get(i) + " " + properties.get(i) + "\n");

							}

						}

					} catch (NumberFormatException e) {
						System.out.println("Input is not valid, try again (return to the main menu)");
						break;

					}

				}

			} while (!user_input.equals("exit"));
		}

	}

	/**
	 * Method to close the connection that has been established between our app and
	 * the database
	 */
	public void dbClose() {
		try {
			this.conn.close();
			System.out.println("Closed successfully");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Main {
	static Scanner scan = new Scanner(System.in);
	static Random rnd = new Random();
	static Connection conn;

	public static void main(String[] args) {
		String url = "jdbc:mysql://localhost:3306/society";
		String user = "Pavlo";
		String password = "123456";
		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		createTableHuman();

		while (true) {

			System.out.println("Choose action: "
					+ "\n1 - add human, "
					+ "\n2 - delete human, "
					+ "\n3 - show list of all humans, "
					+ "\n4 - show information about human, "
					+ "\n5 - fill table by random data, "
					+ "\n6 - update information about human, "
					+ "\nq to quit");
			if (scan.hasNextInt()) {
				switch (scan.nextInt()) {
				case 1:
					System.out.print("first name: ");
					String fName = scan.next();
					System.out.print("last name: ");
					String lName = scan.next();
					int age;
					while (true) {
						System.out.print("age: ");
						if (!scan.hasNextInt()) {
							System.out.println("Wrong input!");
							String wrong = scan.next();
							continue;
						} else {
							age = scan.nextInt();
							if (age < 0) {
								System.out.println("Age can't be negative!");
								continue;
							}
							break;
						}
					}
					System.out.print("city: ");
					String city = scan.next();
					addHuman(fName, lName, age, city);
					break;

				case 2:
					System.out.print("Enter ID: ");
					if (scan.hasNextInt()) {
						int humanId = scan.nextInt();
						if (validId(humanId))
							deleteHumanById(humanId);
					} else {
						String wrong = scan.next();
						System.out.println("Wrong input!");
					}
					break;

				case 3:
					showAllHumans();
					break;

				case 4:
					System.out.print("Enter ID: ");
					if (scan.hasNextInt()) {
						int humanId = scan.nextInt();
						if (validId(humanId))
							showHumanInfoById(humanId);
					} else {
						String wrong = scan.next();
						System.out.println("Wrong input!");
					}
					break;

				case 5:
					fillRandomData();
					break;

				case 6:
					System.out.print("Enter ID: ");
					if (scan.hasNextInt()) {
						int humanId = scan.nextInt();
						if (validId(humanId))
							updateHumanInfoById(humanId);
					} else {
						String wrong = scan.next();
						System.out.println("Wrong input!");
					}
					break;

				default:
					System.out.println("Wrong input!");
					break;
				}
			} else {
				if (scan.next().equalsIgnoreCase("q"))
					break;
				System.out.println("Wrong input!");
			}
		}
		System.out.println("Bye!");
	}

	static void createTableHuman() {
		String dropQuery = "DROP TABLE IF EXISTS human;";
		String query = "CREATE TABLE human("
				+ "id INT PRIMARY KEY AUTO_INCREMENT,"
				+ "first_name VARCHAR(20),"
				+ "last_name VARCHAR(20),"
				+ "age INT,"
				+ "city VARCHAR(30)"
				+ ");";
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute(dropQuery);
			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public void addHuman(String first_name, String last_name, int age, String city) {
		System.out.println("addHuman");
		String query = "INSERT INTO human(first_name, last_name, age, city) "
				+ "VALUES(?, ?, ?, ?);";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, first_name);
			pstmt.setString(2, last_name);
			pstmt.setInt(3, age);
			pstmt.setString(4, city);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public void deleteHumanById(int id) {
		System.out.println("deleteHumanById");
		String query = "DELETE FROM human WHERE id=?;";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public void showAllHumans() {
		System.out.println("showAllHumans");
		String query = "SELECT * FROM human;";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();

			List<String> humans = new ArrayList<>();

			while (rs.next())
				humans.add("Id: " + rs.getInt("id") + "\t|"
						+ "Full name: " + rs.getString("first_name") + " " + rs.getString("last_name") + "\t|"
						+ "Age: " + rs.getInt("age") + "\t|"
						+ "City: " + rs.getString("city"));
			humans.forEach(System.out::println);
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public void showHumanInfoById(int id) {
		System.out.println("showHumanInfoById");
		String query = "SELECT * FROM human WHERE id=?;";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			System.out.println("Id: " + rs.getInt("id") + "\t|"
					+ "Full name: " + rs.getString("first_name") + " " + rs.getString("last_name") + "\t|"
					+ "Age: " + rs.getInt("age") + "\t|"
					+ "City: " + rs.getString("city"));
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public void fillRandomData() {
		System.out.println("fillRandomData");
		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(new FileReader("Random_data.txt"));

			String query = "INSERT INTO human(first_name, last_name, age, city) VALUES";

			String nameOrCity;
			List<String> firstNameList = new LinkedList<>();
			List<String> lastNameList = new LinkedList<>();
			List<String> cityList = new LinkedList<>();
			boolean names = true;
			if (inputStream.readLine().contains("Random user name"))
				while ((nameOrCity = inputStream.readLine()) != null) {
					if (nameOrCity.contains("Random fancy cities")) {
						names = false;
						continue;
					}
					if (names) {
						query += " (?, ?, ?, ?),";
						String[] parts = nameOrCity.split(" ", 2);
						firstNameList.add(parts[0]);
						lastNameList.add(parts[1]);
					} else
						cityList.add(nameOrCity);
				}
			query = query.substring(0, query.length() - 1);
			query += ";";
			inputStream.close();

			Iterator<String> iterFirstNameList = firstNameList.iterator();
			Iterator<String> iterLastNameList = lastNameList.iterator();
			PreparedStatement pstmt = conn.prepareStatement(query);
			int count = 1;
			while (iterFirstNameList.hasNext() && iterLastNameList.hasNext()) {
				String firstName = iterFirstNameList.next();
				String lastName = iterLastNameList.next();

				pstmt.setString(count++, firstName);
				pstmt.setString(count++, lastName);
				pstmt.setInt(count++, rnd.nextInt(100) + 1);
				pstmt.setString(count++, cityList.get(rnd.nextInt(cityList.size())));
			}
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void updateHumanInfoById(int id) {
		System.out.println("updateHumanInfoById");
		System.out.println("What you want to change (first_name, last_name, age, city)?");
		scan.nextLine();
		String infoUpdate = scan.nextLine();

		String querySelect = "SELECT * FROM human WHERE id=?";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(querySelect);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			String firstName = rs.getString("first_name");
			String lastName = rs.getString("last_name");
			int age = rs.getInt("age");
			String city = rs.getString("city");

			if (infoUpdate.contains("first_name")) {
				System.out.print("New first name: ");
				firstName = scan.next();
			}
			if (infoUpdate.contains("last_name")) {
				System.out.print("New last name: ");
				lastName = scan.next();
			}
			if (infoUpdate.contains("age")) {
				while (true) {
					System.out.print("New age: ");
					if (!scan.hasNextInt()) {
						String wrong = scan.next();
						System.out.println("Wrong input!");
						continue;
					}
					int newAge = scan.nextInt();
					if (newAge < 0) {
						System.out.println("Age can't be negative!");
						continue;
					}
					age = newAge;
					break;
				}
			}
			if (infoUpdate.contains("city")) {
				System.out.print("New city: ");
				city = scan.next();
			}
			String queryUpdate = "UPDATE human SET first_name=?, last_name=?, age=?, city=? WHERE id=?;";
			pstmt = conn.prepareStatement(queryUpdate);
			pstmt.setString(1, firstName);
			pstmt.setString(2, lastName);
			pstmt.setInt(3, age);
			pstmt.setString(4, city);
			pstmt.setInt(5, id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static public boolean validId(int id) {
		String query = "SELECT * FROM human;";
		PreparedStatement pstmt;
		try {
			pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				if (rs.getInt("id") == id) {
					pstmt.close();
					return true;
				}
			System.out.println("Wrong ID!");
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
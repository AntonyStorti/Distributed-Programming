package com.employees.ws;

import database.Database;

/**
 * This service allows employees to manage and update the content of the
 * employee database.
 *
 *
 */
public class EmployeeService extends BusinessInterfaceApplet implements EmployeeManagementService{

	private static Database database = new Database();
	
	/**
	 * Method to insert a employee.
	 * @param username
	 * @param name
	 * @param surname
	 * @param email
	 * @param department
	 * @param phoneNumber
	 * @return String
	 */
	public String insertEmployee(String username, String name, String surname, String email, String department,
			String phoneNumber) {
		try {
			database.insertEmployee(username, name, surname, email, department, phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Employee " + username + " correctly inserted";
	}
	
	/**
	 * Method to remove a employee given the username.
	 * @param username
	 * @return
	 */
	public String removeEmployeeByUsername(String username) {
		try {
			database.removeEmployeeByUsername(username);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Employee " + username + " correctly deleted";
	}
	
	/**
	 * Method to remove a employee given the id.
	 * @param id
	 * @return
	 */
	public String removeEmployeeById(String id) {
		try {
			database.removeEmployeeById(id);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Employee " + id + " correctly deleted";
	}
	
	/**
	 * Method to update a employee given the username.
	 * @param username
	 * @param fieldToUpdate
	 * @param newValue
	 * @return
	 */
	public String updateEmployeeByUsername(String username, String fieldToUpdate, String newValue) {
		try {
			database.updateEmployeeByUsername(username, fieldToUpdate, newValue);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Employee " + username + " correctly updated";
	}
	
	/**
	 * Method to update a employee given the id.
	 * @param id
	 * @param fieldToUpdate
	 * @param newValue
	 * @return
	 */
	public String updateEmployeeById(String id, String fieldToUpdate, String newValue) {
		try {
			database.updateEmployeeById(id, fieldToUpdate, newValue);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Employee " + id + " correctly updated";
	}
	
	/**
	 * Method to insert the phone number of a employee given the id.
	 * @param id
	 * @param phoneNumber
	 * @return
	 */
	public String insertPhoneNumberById(String id, String phoneNumber) {
		try {
			database.insertPhoneNumberById(id, phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number " + phoneNumber + " correctly inserted for the employee " + id;
	}
	
	/**
	 * Method to insert the phone number of a employee given the username.
	 * @param username
	 * @param phoneNumber
	 * @return
	 */
	public String insertPhoneNumberByUsername(String username, String phoneNumber) {
		try {
			database.insertPhoneNumberByUsername(username, phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number " + phoneNumber + " correctly inserted for the employee " + username;
	}
	
	/**
	 * Method to update the phone number of a employee given the id.
	 * @param id
	 * @param oldPhoneNumber
	 * @param newPhoneNumber
	 * @return
	 */
	public String updatePhoneNumberById(String id, String oldPhoneNumber, String newPhoneNumber) {
		try {
			database.updatePhoneNumberById(id, oldPhoneNumber, newPhoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number correctly updated for the employee " + id;
	}
	
	/**
	 * Method to update the phone number of a employee given the username.
	 * @param username
	 * @param oldPhoneNumber
	 * @param newPhoneNumber
	 * @return
	 */
	public String updatePhoneNumberByUsername(String username, String oldPhoneNumber, String newPhoneNumber) {
		try {
			database.updatePhoneNumberByUsername(username, oldPhoneNumber, newPhoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number correctly updated for the employee " + username;
	}
	
	/**
	 * Method to remove the phone number of a employee given the id.
	 * @param id
	 * @param phoneNumber
	 * @return
	 */
	public String removePhoneNumberById(String id, String phoneNumber) {
		try {
			database.removePhoneNumberById(id, phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number " + phoneNumber + " correctly removed for the employee " + id;
	}
	
	/**
	 * Method to remove the phone number of a employee given the username.
	 * @param username
	 * @param phoneNumber
	 * @return
	 */
	public String removePhoneNumberByUsername(String username, String phoneNumber) {
		try {
			database.removePhoneNumberByUsername(username, phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return "Phone number " + phoneNumber + " correctly removed for the employee " + username;
	}

}

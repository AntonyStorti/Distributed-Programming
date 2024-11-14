package com.employees.ws;

import java.util.HashSet;

import com.employees.Employee;

import database.Database;

/**
 * Allows users to search for employees based on various criteria.
 *
 *
 */
public class BusinessInterfaceApplet implements EmployeeSearchService {

	private static Database database = new Database();

	/**
	 * Method to search all the employees.
	 * @return
	 */
	public String searchAllEmployees() {
		return database.searchAllEmployees().toString();
	}

	/**
	 * Method to search a employee given the id.
	 * @param id
	 * @return
	 */
	public String searchEmployeeById(String id) {
		Employee empl;
		try {
			empl = database.searchEmployeeById(id);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search all the employees with the same name.
	 * @param name
	 * @return
	 */
	public String searchEmployeesByName(String name) {
		HashSet<Employee> empl;
		try {
			empl = database.searchEmployeesByName(name);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search all the employees with the same surname.
	 * @param surname
	 * @return
	 */
	public String searchEmployeesBySurname(String surname) {
		HashSet<Employee> empl;
		try {
			empl = database.searchEmployeesBySurname(surname);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search a employee given the username.
	 * @param username
	 * @return
	 */
	public String searchEmployeeByUsername(String username) {
		Employee empl;
		try {
			empl = database.searchEmployeeByUsername(username);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search all the employees with the same email.
	 * @param email
	 * @return
	 */
	public String searchEmployeesByEmail(String email) {
		HashSet<Employee> empl;
		try {
			empl = database.searchEmployeesByEmail(email);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search all the employees with the same department.
	 * @param detpartment
	 * @return
	 */
	public String searchEmployeesByDepartment(String detpartment) {
		HashSet<Employee> empl;
		try {
			empl = database.searchEmployeesByDepartment(detpartment);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

	/**
	 * Method to search all the employees with the same phone number.
	 * @param phoneNumber
	 * @return
	 */
	public String searchEmployeesByPhoneNumber(String phoneNumber) {
		HashSet<Employee> empl;
		try {
			empl = database.searchEmployeesByPhoneNumber(phoneNumber);
		} catch (Exception e) {
			return e.getMessage();
		}
		return empl.toString();
	}

}

package com.employees;

import java.io.Serializable;
import java.util.HashSet;
import java.util.UUID;

/**
 * Representation of a employee.
 *
 */
public class Employee implements Serializable {

	private static final long serialVersionUID = 1L;
	private UUID id;
	private String name, surname, username, email, department;
	private HashSet<String> phoneNumbers;

	/**
	 * Creation of a employee.
	 * @param name
	 * @param surname
	 * @param username
	 * @param email
	 * @param department
	 * @param phoneNumber
	 */
	public Employee(String name, String surname, String username, String email, String department, String phoneNumber) {
		id = UUID.randomUUID();
		this.name = name;
		this.surname = surname;
		this.username = username;
		this.email = email;
		this.department = department;
		this.phoneNumbers = new HashSet<>();
		this.phoneNumbers.add(phoneNumber);
	}

	/**
	 * Method to insert a phone number.
	 * @param phoneNumber
	 * @return
	 * @throws Exception
	 */
	public boolean insertPhoneNumber(String phoneNumber) throws Exception {
		if (phoneNumbers.contains(phoneNumber))
			throw new Exception("Phone number already present");
		phoneNumbers.add(phoneNumber);
		return true;
	}

	/**
	 * Method to update a phone number.
	 * @param oldPhoneNumber
	 * @param newPhoneNumber
	 * @return
	 * @throws Exception
	 */
	public boolean updatePhoneNumber(String oldPhoneNumber, String newPhoneNumber) throws Exception {
		if (!phoneNumbers.contains(oldPhoneNumber))
			throw new Exception("Old phone number not present");
		if (phoneNumbers.contains(newPhoneNumber))
			throw new Exception("New phone number already present");
		phoneNumbers.remove(oldPhoneNumber);
		phoneNumbers.add(newPhoneNumber);
		return true;
	}

	/**
	 * Method to remove a phone number.
	 * @param phoneNumber
	 * @return
	 * @throws Exception
	 */
	public boolean removePhoneNumber(String phoneNumber) throws Exception {
		if (!phoneNumbers.contains(phoneNumber))
			throw new Exception("Phone number not present");
		phoneNumbers.remove(phoneNumber);
		return true;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public HashSet<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(HashSet<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	@Override
	public String toString() {
		StringBuilder phoneNumbersStr = new StringBuilder();
		for (String phoneNumber : phoneNumbers) {
			phoneNumbersStr.append(phoneNumber).append(", ");
		}
		if (phoneNumbersStr.length() > 0) {
			phoneNumbersStr.setLength(phoneNumbersStr.length() - 2); // Remove the extra comma and space
		}

		return "Employee {" + "id=" + id + ", name='" + name + '\'' + ", surname='" + surname + '\'' + ", username='"
				+ username + '\'' + ", email='" + email + '\'' + ", department='" + department + '\''
				+ ", phoneNumbers=" + (phoneNumbers.isEmpty() ? "[]" : "[" + phoneNumbersStr + "]") + '}';
	}
	
	

}

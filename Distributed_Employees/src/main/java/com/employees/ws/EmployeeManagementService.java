package com.employees.ws;

public interface EmployeeManagementService {

	public String insertEmployee(String username, String name, String surname, String email, String department,
			String phoneNumber);

	public String removeEmployeeByUsername(String username);

	public String removeEmployeeById(String id);

	public String updateEmployeeByUsername(String username, String fieldToUpdate, String newValue);

	public String updateEmployeeById(String id, String fieldToUpdate, String newValue);

	public String insertPhoneNumberById(String id, String phoneNumber);

	public String insertPhoneNumberByUsername(String username, String phoneNumber);

	public String updatePhoneNumberById(String id, String oldPhoneNumber, String newPhoneNumber);

	public String updatePhoneNumberByUsername(String username, String oldPhoneNumber, String newPhoneNumber);

	public String removePhoneNumberById(String id, String phoneNumber);

	public String removePhoneNumberByUsername(String username, String phoneNumber);

}

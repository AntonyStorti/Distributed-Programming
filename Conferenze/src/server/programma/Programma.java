package server.programma;

public class Programma {

	private String[][][] programs;
	private int numDays, numSessions, maxSpeakers;
	private int[][] slots;


	public Programma(int numDays, int numSessions, int maxSpeakers) {

		programs = new String[numDays][numSessions][maxSpeakers];
		slots = new int[numDays][numSessions];

		int i, j, k;

		for (i = 0; i < numDays; i++)
			for (j = 0; j < numSessions; j++)
				for (k = 0; k < maxSpeakers; k++)
					programs[i][j][k] = "-";

		for (i = 0; i < numDays; i++)
			for (j = 0; j < numSessions; j++)
				slots[i][j] = 0;

		this.numDays = numDays;
		this.numSessions = numSessions;
		this.maxSpeakers = maxSpeakers;

	}

	/**
	 * Returns the programs of all of the days of the congress.
	 * 
	 * @return
	 */
	public String[][][] getPrograms() {
		return programs;
	}

	/**
	 * Returns the number of days of the congress.
	 * 
	 * @return
	 */
	public int getNumDays() {
		return numDays;
	}

	/**
	 * Returns the number of sessions of the congress.
	 * 
	 * @return
	 */
	public int getNumSessions() {
		return numSessions;
	}

	/**
	 * Returns the number of speakers of the congress.
	 * 
	 * @return
	 */
	public int getMaxSpeakers() {
		return maxSpeakers;
	}

	/**
	 * Allows, if all the fields are valid, to register for an intervention of a
	 * given session in a given day of the congress.
	 * 
	 * @param name
	 * @param day
	 * @param session
	 * @return
	 */
	public boolean insertSpeaker(String name, int day, int session) {
		if (invalidDay(day) || invalidSession(session))
			return false;
		if (isBusy(slots[day - 1][session - 1]))
			return false;
		int intervention = searchAvailableSlot(day, session);
		if (intervention == -1)
			return false;
		programs[day - 1][session - 1][intervention] = name;
		slots[day - 1][session - 1] += 1;
		printPrograms();
		return true;
	}

	/**
	 * Checks if a session is full.
	 * @param i
	 * @return
	 */
	private boolean isBusy(int i) {
		return i >= maxSpeakers;
	}

	/**
	 * Checks if an intervention is invalid.
	 * @param intervention
	 * @return
	 */
	private boolean invalidIntervention(int intervention) {
		if (intervention < 1 || intervention > maxSpeakers)
			return true;
		return false;
	}

	/**
	 * Checks if a session is invalid.
	 * @param session
	 * @return
	 */
	private boolean invalidSession(int session) {
		if (session < 1 || session > numSessions)
			return true;
		return false;
	}

	/**
	 * Checks if a day is invald.
	 * @param day
	 * @return
	 */
	private boolean invalidDay(int day) {
		if (day < 1 || day > numDays)
			return true;
		return false;
	}

	/**
	 * Checks if there's an available slot.
	 * @param day
	 * @param session
	 * @return
	 */
	private int searchAvailableSlot(int day, int session) {
		String[] sessionArray = programs[day - 1][session - 1];
		for (int i = 0; i < maxSpeakers; i++)
			if (sessionArray[i] == "-")
				return i;
		return -1;
	}

	/**
	 * Utility method to print the programs.
	 */
	private void printPrograms() {

		int i, j, k;

		for (i = 0; i < numDays; i++) {
			System.out.println("Day " + i);
			for (j = 0; j < numSessions; j++) {
				System.out.print("S" + (j + 1) + " ");
				for (k = 0; k < maxSpeakers; k++)
					System.out.print(programs[i][j][k] + " ");
				System.out.print("\n");
			}

		}

	}

}

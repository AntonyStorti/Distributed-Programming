package server;

import java.io.Serial;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import server.programma.Programma;


/**
 * Class representing the logic of the server. The contained methods are the
 * ones of the interface, the documentation is reported there.
 */
public class ConferenceRegistrationImpl extends UnicastRemoteObject implements common.ConferenceRegistration {

	//serve per garantire che una versione di una classe serializzabile sia compatibile con le versioni precedenti,
	// evitando errori nella deserializzazione quando si trasferiscono o archiviano oggetti.
	@Serial
	private static final long serialVersionUID = 1L;

	private final Programma programma;

	/**
	 * Creates an instance of the server.
	 * @param numDays
	 * @param numSessions
	 * @param maxSpeakers
	 * @throws RemoteException
	 */
	public ConferenceRegistrationImpl(int numDays, int numSessions, int maxSpeakers) throws RemoteException {
		super();
		this.programma = new Programma(numDays, numSessions, maxSpeakers);
	}

	@Override
	public boolean register(String name, int day, int session) throws RemoteException {
		return programma.insertSpeaker(name.strip(), day, session);
	}

	@Override
	public String[][][] getInformation() throws RemoteException {
		return programma.getPrograms();
	}

	@Override
	public String[][] getInformationByDay(int day) throws RemoteException {
		return programma.getPrograms()[day - 1];
	}

	@Override
	public int getDays() throws RemoteException {
		return programma.getNumDays();
	}

	@Override
	public int getSessions() throws RemoteException {
		return programma.getNumSessions();
	}

	@Override
	public int getMaxSpeakers() throws RemoteException {
		return programma.getMaxSpeakers();
	}

	/**
	 * Creates a registry and binds a remote object.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.createRegistry(1099);
			ConferenceRegistrationImpl cr = new ConferenceRegistrationImpl(3, 12, 5);
			reg.rebind("rmi://localhost/CRServer", cr);

		} catch (RemoteException e) {
			System.out.println("Error creating the server!");
		}
	}

}
